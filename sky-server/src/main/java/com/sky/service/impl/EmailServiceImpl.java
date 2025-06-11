package com.sky.service.impl;

import com.sky.dto.EmailConfirmationDTO;
import com.sky.dto.EmailInvoiceDTO;
import com.sky.entity.TourBooking;
import com.sky.mapper.AgentMapper;
import com.sky.mapper.AgentOperatorMapper;
import com.sky.mapper.TourBookingMapper;
import com.sky.mapper.TourItineraryMapper;
import com.sky.service.EmailService;
import com.sky.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 邮件服务实现类
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "sky.mail.enabled", havingValue = "true")
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AgentOperatorMapper agentOperatorMapper;

    @Autowired
    private TourBookingMapper tourBookingMapper;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private TourItineraryMapper tourItineraryMapper;

    @Value("${sky.mail.from}")
    private String fromEmail;
    
    @Value("${sky.mail.username}")
    private String mailUsername;

    @Override
    public void sendConfirmationEmail(EmailConfirmationDTO emailConfirmationDTO) {
        log.info("开始发送确认单邮件: orderId={}, recipientType={}, agentId={}, operatorId={}", 
                emailConfirmationDTO.getOrderId(), emailConfirmationDTO.getRecipientType(),
                emailConfirmationDTO.getAgentId(), emailConfirmationDTO.getOperatorId());

        try {
            // 确定收件人列表
            List<String> recipients = new ArrayList<>();
            
            if ("agent".equals(emailConfirmationDTO.getRecipientType())) {
                // 中介主号下单：只发送给中介主号
                String agentEmail = getAgentEmail(emailConfirmationDTO.getAgentId());
                if (agentEmail != null && !agentEmail.isEmpty()) {
                    recipients.add(agentEmail);
                    log.info("中介主号下单，发送确认单给主号: {}", agentEmail);
                }
            } else if ("operator".equals(emailConfirmationDTO.getRecipientType())) {
                // 操作员下单：发送给中介主号和操作员
                String agentEmail = getAgentEmail(emailConfirmationDTO.getAgentId());
                if (agentEmail != null && !agentEmail.isEmpty()) {
                    recipients.add(agentEmail);
                    log.info("操作员下单，发送确认单给主号: {}", agentEmail);
                }
                
                String operatorEmail = getOperatorEmail(emailConfirmationDTO.getOperatorId());
                if (operatorEmail != null && !operatorEmail.isEmpty() && !operatorEmail.equals(agentEmail)) {
                    recipients.add(operatorEmail);
                    log.info("操作员下单，发送确认单给操作员: {}", operatorEmail);
                }
            }

            if (recipients.isEmpty()) {
                log.warn("无法获取任何有效的收件人邮箱地址，跳过发送确认单邮件");
                return;
            }

            // 像发票邮件一样，从数据库获取完整的订单数据
            Long orderId = emailConfirmationDTO.getOrderId();
            TourBooking orderData = tourBookingMapper.getById(orderId.intValue());
            if (orderData == null) {
                log.warn("订单不存在: orderId={}", orderId);
                return;
            }

            // 从数据库数据构建完整的订单详情
            EmailConfirmationDTO.OrderDetails orderDetails = new EmailConfirmationDTO.OrderDetails();
            orderDetails.setTourName("塔斯马尼亚旅游"); // TourBooking中没有tourName，使用默认值或通过tourId查询
            orderDetails.setTourType(orderData.getTourType());
            orderDetails.setStartDate(orderData.getTourStartDate() != null ? orderData.getTourStartDate().toString() : null);
            orderDetails.setEndDate(orderData.getTourEndDate() != null ? orderData.getTourEndDate().toString() : null);
            orderDetails.setAdultCount(orderData.getAdultCount() != null ? orderData.getAdultCount() : orderData.getGroupSize()); // 优先使用adultCount
            orderDetails.setChildCount(orderData.getChildCount() != null ? orderData.getChildCount() : 0);
            // 不设置价格信息，确认单不显示价格
            // orderDetails.setTotalPrice(orderData.getTotalPrice() != null ? orderData.getTotalPrice().toString() : "0");
            orderDetails.setContactPerson(orderData.getContactPerson());
            orderDetails.setContactPhone(orderData.getContactPhone());
            orderDetails.setPickupLocation(orderData.getPickupLocation());
            orderDetails.setDropoffLocation(orderData.getDropoffLocation());
            orderDetails.setHotelLevel(orderData.getHotelLevel());
            orderDetails.setSpecialRequests(orderData.getSpecialRequests());
            // 设置代理商和操作员名称  
            orderDetails.setAgentName("代理商"); // TourBooking中没有agentName，使用默认值或通过agentId查询
            orderDetails.setOperatorName("操作员"); // 可以从数据库获取，暂时设置默认值
            
            // 获取产品的默认行程信息
            if (orderData.getTourId() != null && orderData.getTourType() != null) {
                try {
                    List<Map<String, Object>> itinerary = tourItineraryMapper.getItineraryByTourId(orderData.getTourId(), orderData.getTourType());
                    orderDetails.setItinerary(itinerary);
                    log.info("成功获取产品行程信息: tourId={}, tourType={}, items={}", 
                            orderData.getTourId(), orderData.getTourType(), itinerary != null ? itinerary.size() : 0);
                } catch (Exception e) {
                    log.error("获取产品行程信息失败: tourId={}, tourType={}", orderData.getTourId(), orderData.getTourType(), e);
                    orderDetails.setItinerary(new ArrayList<>());
                }
            } else {
                orderDetails.setItinerary(new ArrayList<>());
            }

            log.info("构建完整订单详情: tourName={}, startDate={}, passengers={}", 
                    orderDetails.getTourName(), orderDetails.getStartDate(), 
                    orderDetails.getAdultCount() + orderDetails.getChildCount());

            // 准备邮件内容
            Context context = new Context();
            context.setVariable("tourDetails", orderDetails);
            context.setVariable("orderId", emailConfirmationDTO.getOrderId());
            context.setVariable("orderNumber", String.valueOf(emailConfirmationDTO.getOrderId()));
            context.setVariable("confirmationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)));
            context.setVariable("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)));
            context.setVariable("issueDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)));

            // 渲染确认单HTML模板
            String confirmationHtml = templateEngine.process("confirmation-letter", context);
            
            // 生成确认单PDF
            byte[] confirmationPdf = pdfService.generatePdfFromHtml(confirmationHtml);

            // 准备邮件内容
            String emailSubject = "订单确认单 - Happy Tassie Holiday (Order: " + emailConfirmationDTO.getOrderId() + ")";
            // 创建临时DTO用于邮件正文构建
            EmailConfirmationDTO tempDTO = new EmailConfirmationDTO();
            tempDTO.setOrderId(emailConfirmationDTO.getOrderId());
            tempDTO.setOrderDetails(orderDetails);
            String emailBody = buildConfirmationEmailBody(tempDTO);

            // 批量发送邮件
            for (String recipient : recipients) {
                try {
                    sendEmailWithAttachment(recipient, emailSubject, emailBody, 
                    confirmationPdf, "Order_Confirmation_" + emailConfirmationDTO.getOrderId() + ".pdf");
                    log.info("确认单邮件发送成功: orderId={}, recipient={}", 
                            emailConfirmationDTO.getOrderId(), recipient);
                } catch (Exception e) {
                    log.error("发送确认单邮件失败: orderId={}, recipient={}", 
                            emailConfirmationDTO.getOrderId(), recipient, e);
                    // 继续尝试发送给其他收件人
                }
            }

            log.info("确认单邮件发送完成: orderId={}, 成功发送给 {} 个收件人", 
                    emailConfirmationDTO.getOrderId(), recipients.size());

        } catch (Exception e) {
            log.error("发送确认单邮件处理失败: orderId={}", emailConfirmationDTO.getOrderId(), e);
            throw new RuntimeException("发送确认单邮件失败", e);
        }
    }

    @Override
    public void sendInvoiceEmail(EmailInvoiceDTO emailInvoiceDTO) {
        log.info("开始发送发票邮件: orderId={}, agentId={}", 
                emailInvoiceDTO.getOrderId(), emailInvoiceDTO.getAgentId());

        try {
            // 获取代理商主号邮箱
            String agentEmail = getAgentEmail(emailInvoiceDTO.getAgentId());
            if (agentEmail == null || agentEmail.isEmpty()) {
                log.warn("无法获取代理商邮箱地址，跳过发送发票邮件");
                return;
            }

            // 从数据库获取实际的订单数据和价格
            Long orderId = emailInvoiceDTO.getOrderId();
            TourBooking orderData = tourBookingMapper.getById(orderId.intValue());
            if (orderData == null) {
                log.warn("订单不存在: orderId={}", orderId);
                return;
            }

            // 使用数据库中的实际价格，而不是前端传递的数据
            EmailInvoiceDTO.InvoiceDetails actualInvoiceDetails = emailInvoiceDTO.getInvoiceDetails();
            if (orderData.getTotalPrice() != null) {
                actualInvoiceDetails.setTotalPrice(orderData.getTotalPrice().doubleValue());
                log.info("使用数据库中的实际价格: ${}", orderData.getTotalPrice());
            } else {
                log.warn("订单价格为空，使用前端传递的价格: {}", actualInvoiceDetails.getTotalPrice());
            }

            // 确保其他必要字段也从数据库获取
            if (actualInvoiceDetails.getTourName() == null || actualInvoiceDetails.getTourName().isEmpty()) {
                // 可以从tourData中获取tourName，这里暂时保持原逻辑
            }
            if (actualInvoiceDetails.getAdultCount() == null) {
                actualInvoiceDetails.setAdultCount(orderData.getAdultCount() != null ? orderData.getAdultCount() : 0);
            }
            if (actualInvoiceDetails.getChildCount() == null) {
                actualInvoiceDetails.setChildCount(orderData.getChildCount() != null ? orderData.getChildCount() : 0);
            }

            // 准备邮件内容
            Context context = new Context();
            context.setVariable("invoiceDetails", actualInvoiceDetails);
            context.setVariable("orderId", orderId);
            context.setVariable("orderNumber", orderData.getOrderNumber() != null ? orderData.getOrderNumber() : String.valueOf(orderId));
            context.setVariable("invoiceNumber", "INV-" + orderId);
            context.setVariable("invoiceDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)));
            context.setVariable("dueDate", LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)));

            // 渲染Invoice HTML模板
            String invoiceHtml = templateEngine.process("tax-invoice", context);
            
            // 生成Invoice PDF
            byte[] invoicePdf = pdfService.generatePdfFromHtml(invoiceHtml);

            // 准备邮件内容
            String emailSubject = "Tax Invoice - Happy Tassie Holiday (Invoice: INV-" + orderId + ")";
            String emailBody = buildInvoiceEmailBody(emailInvoiceDTO, actualInvoiceDetails);

            // 发送带附件的邮件
            sendEmailWithAttachment(agentEmail, emailSubject, emailBody, 
                    invoicePdf, "Tax_Invoice_INV-" + orderId + ".pdf");

            log.info("发票邮件发送成功: orderId={}, agentEmail={}, actualPrice=${}", 
                    orderId, agentEmail, actualInvoiceDetails.getTotalPrice());

        } catch (Exception e) {
            log.error("发送发票邮件失败: orderId={}", emailInvoiceDTO.getOrderId(), e);
            throw new RuntimeException("发送发票邮件失败", e);
        }
    }

    /**
     * 发送HTML邮件
     */
    private void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }



    /**
     * 获取代理商邮箱 - 从agent表获取
     */
    private String getAgentEmail(Long agentId) {
        log.info("获取代理商邮箱: agentId={}", agentId);
        try {
            String email = agentMapper.getEmailById(agentId);
            if (email == null || email.trim().isEmpty()) {
                log.warn("代理商邮箱为空: agentId={}, 使用测试邮箱", agentId);
                // 开发环境使用测试邮箱
                return "agent" + agentId + "@test.com";
            }
            log.info("获取到代理商邮箱: agentId={}, email={}", agentId, email);
            return email.trim();
        } catch (Exception e) {
            log.error("获取代理商邮箱失败: agentId={}, 使用测试邮箱", agentId, e);
            return "agent" + agentId + "@test.com";
        }
    }

    /**
     * 获取操作员邮箱
     */
    private String getOperatorEmail(Long operatorId) {
        log.info("获取操作员邮箱: operatorId={}", operatorId);
        try {
            String email = agentOperatorMapper.getEmailById(operatorId);
            if (email == null || email.trim().isEmpty()) {
                log.warn("操作员邮箱为空: operatorId={}, 使用测试邮箱", operatorId);
                // 开发环境使用测试邮箱
                return "operator" + operatorId + "@test.com";
            }
            log.info("获取到操作员邮箱: operatorId={}, email={}", operatorId, email);
            return email.trim();
        } catch (Exception e) {
            log.error("获取操作员邮箱失败: operatorId={}, 使用测试邮箱", operatorId, e);
            return "operator" + operatorId + "@test.com";
        }
    }

    /**
     * 构建确认单邮件正文
     */
    private String buildConfirmationEmailBody(EmailConfirmationDTO dto) {
        StringBuilder body = new StringBuilder();
        body.append("Dear Customer,\n\n");
        body.append("Thank you for booking with Happy Tassie Holiday!\n\n");
        body.append("We are pleased to confirm your booking with the following details:\n\n");
        body.append("Order Number: ").append(dto.getOrderId()).append("\n");
        body.append("Tour: ").append(dto.getOrderDetails().getTourName()).append("\n");
        body.append("Date: ").append(dto.getOrderDetails().getStartDate()).append("\n");
        body.append("Guests: ").append(dto.getOrderDetails().getAdultCount()).append(" Adult(s)");
        if (dto.getOrderDetails().getChildCount() > 0) {
            body.append(", ").append(dto.getOrderDetails().getChildCount()).append(" Child(ren)");
        }
        body.append("\n\n");
        body.append("Please find attached your detailed order confirmation.\n\n");
        body.append("If you have any questions, please don't hesitate to contact us.\n\n");
        body.append("Best regards,\n");
        body.append("Happy Tassie Holiday Team\n");
        body.append("Email: booking@htas.com.au\n");
        body.append("Phone: 04 3342 4877");
        return body.toString();
    }

    /**
     * 构建Invoice邮件正文
     */
    private String buildInvoiceEmailBody(EmailInvoiceDTO dto, EmailInvoiceDTO.InvoiceDetails details) {
        StringBuilder body = new StringBuilder();
        body.append("Dear Agent,\n\n");
        body.append("Please find attached the tax invoice for the following booking:\n\n");
        body.append("Order Number: ").append(dto.getOrderId()).append("\n");
        body.append("Tour: ").append(details.getTourName()).append("\n");
        body.append("Agent: ").append(details.getAgentName()).append("\n");
        body.append("Total Amount: $").append(String.format("%.2f", details.getTotalPrice())).append("\n");
        body.append("Invoice Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))).append("\n");
        body.append("Due Date: ").append(LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))).append("\n\n");
        body.append("Payment can be made to our bank account details provided in the attached invoice.\n\n");
        body.append("Thank you for your business.\n\n");
        body.append("Best regards,\n");
        body.append("Happy Tassie Holiday Team\n");
        body.append("Email: booking@htas.com.au\n");
        body.append("Phone: 04 3342 4877");
        return body.toString();
    }

    /**
     * 发送带附件的邮件
     */
    private void sendEmailWithAttachment(String to, String subject, String body, 
                                       byte[] attachment, String attachmentName) throws MessagingException {
        log.info("准备发送邮件: 收件人={}, 主题={}, 附件大小={} bytes", to, subject, attachment.length);
        
        // 检查是否为开发环境的占位符配置
        if ("your-app-password".equals(mailUsername) || mailUsername.contains("your-email")) {
            log.warn("⚠️ 检测到开发环境占位符配置，模拟邮件发送");
            log.info("📧 [模拟邮件发送] 收件人: {}", to);
            log.info("📧 [模拟邮件发送] 主题: {}", subject);
            log.info("📧 [模拟邮件发送] 附件: {} ({} bytes)", attachmentName, attachment.length);
            log.info("📧 [模拟邮件发送] 正文预览: {}", body.length() > 100 ? body.substring(0, 100) + "..." : body);
            log.info("✅ [模拟邮件发送] 邮件发送成功（开发环境模拟）");
            return;
        }
        
        try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, false); // 使用纯文本邮件正文

        // 添加PDF附件
        helper.addAttachment(attachmentName, new ByteArrayResource(attachment));

            log.info("开始发送邮件: 从={}, 到={}", fromEmail, to);
        mailSender.send(message);
            log.info("✅ 邮件发送成功: 收件人={}, 主题={}", to, subject);
            
        } catch (Exception e) {
            log.error("❌ 邮件发送失败: 收件人={}, 主题={}, 错误信息={}", to, subject, e.getMessage(), e);
            throw e;
        }
    }
} 