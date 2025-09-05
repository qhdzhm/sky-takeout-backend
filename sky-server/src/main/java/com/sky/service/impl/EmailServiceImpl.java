package com.sky.service.impl;

import com.sky.dto.EmailConfirmationDTO;
import com.sky.dto.EmailInvoiceDTO;
import com.sky.dto.GroupTourDTO;
import com.sky.entity.DayTour;
import com.sky.entity.TourBooking;
import com.sky.mapper.AgentMapper;
import com.sky.mapper.AgentOperatorMapper;
import com.sky.mapper.DayTourMapper;
import com.sky.mapper.GroupTourMapper;
import com.sky.mapper.TourBookingMapper;
import com.sky.mapper.TourItineraryMapper;
import com.sky.service.EmailService;
import com.sky.service.PdfService;
import com.sky.service.TourScheduleOrderService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
    
    @Autowired
    private GroupTourMapper groupTourMapper;
    
    @Autowired
    private DayTourMapper dayTourMapper;

    @Autowired
    private TourScheduleOrderService tourScheduleOrderService;

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
            
            // 根据tourId和tourType获取真实的产品名称
            String actualTourName = getTourNameByIdAndType(orderData.getTourId(), orderData.getTourType());
            orderDetails.setTourName(actualTourName != null ? actualTourName : "塔斯马尼亚旅游");
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
            // 设置代理商和操作员名称（真实显示代理商公司名/用户名）  
            try {
                if (orderData.getAgentId() != null) {
                    com.sky.entity.Agent agent = agentMapper.getById(orderData.getAgentId().longValue());
                    String agentDisplayName = null;
                    if (agent != null) {
                        agentDisplayName = (agent.getCompanyName() != null && !agent.getCompanyName().trim().isEmpty())
                                ? agent.getCompanyName()
                                : agent.getUsername();
                    }
                    orderDetails.setAgentName(agentDisplayName != null ? agentDisplayName : "");
                }
            } catch (Exception ignore) {
                orderDetails.setAgentName("");
            }
            orderDetails.setOperatorName("操作员"); // 可以从数据库获取，暂时设置默认值
            
            // 获取“真实行程”：优先使用排团行程（按订单号），否则回退到产品默认行程
            List<Map<String, Object>> finalItinerary = new ArrayList<>();
            Map<Integer, Integer> selectedMapForReplace = parseSelectedOptionalToursSafe(orderData.getSelectedOptionalTours());
            java.time.format.DateTimeFormatter dayFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy年M月d日", java.util.Locale.CHINA);
            try {
                String ordNo = orderData.getOrderNumber();
                if (ordNo != null && !ordNo.trim().isEmpty()) {
                    List<com.sky.vo.TourScheduleVO> schedules = tourScheduleOrderService.getSchedulesByOrderNumber(ordNo);
                    if (schedules != null && !schedules.isEmpty()) {
                        for (com.sky.vo.TourScheduleVO vo : schedules) {
                            Map<String, Object> m = new HashMap<>();
                            if (vo.getDayNumber() != null) m.put("day_number", vo.getDayNumber());
                            if (vo.getDisplayOrder() != null) m.put("display_order", vo.getDisplayOrder());
                            // 若为团队游且该天有可选项目，则用所选一日游名称替换标题
                            String title = vo.getTitle();
                            if ("group_tour".equals(orderData.getTourType()) && vo.getDayNumber() != null && selectedMapForReplace.containsKey(vo.getDayNumber())) {
                                try {
                                    Integer optId = selectedMapForReplace.get(vo.getDayNumber());
                                    DayTour optTour = dayTourMapper.getById(optId);
                                    if (optTour != null) {
                                        title = optTour.getName();
                                    }
                                } catch (Exception ignore) {}
                            }
                            m.put("title", title);
                            // 行程日期（若有排团具体日期则使用，否则按开始日期+偏移计算）
                            java.time.LocalDate d = vo.getTourDate();
                            if (d == null && orderData.getTourStartDate() != null && vo.getDayNumber() != null) {
                                d = orderData.getTourStartDate().plusDays(Math.max(0, vo.getDayNumber() - 1));
                            }
                            if (d != null) m.put("date", d.format(dayFmt));
                            // 为了紧凑，确认信不展示长描述
                            m.put("description", "");
                            finalItinerary.add(m);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("按订单号获取排团行程失败，使用产品默认行程: {}", e.getMessage());
            }

            if (finalItinerary.isEmpty()) {
                // 回退至产品默认行程，并叠加可选项目到每天的标题上
                if (orderData.getTourId() != null && orderData.getTourType() != null) {
                    try {
                        List<Map<String, Object>> itinerary = tourItineraryMapper.getItineraryByTourId(orderData.getTourId(), orderData.getTourType());
                        for (Map<String, Object> item : itinerary) {
                            // 清空描述以避免冗长营销文案
                            item.put("description", "");
                            // 如为团队游且该天有可选项目，用所选一日游名称替换标题
                            if ("group_tour".equals(orderData.getTourType()) && item.get("day_number") != null) {
                                Integer dayNum = null;
                                try { dayNum = Integer.valueOf(String.valueOf(item.get("day_number"))); } catch (Exception ignore) {}
                                if (dayNum != null && selectedMapForReplace.containsKey(dayNum)) {
                                    try {
                                        Integer optId = selectedMapForReplace.get(dayNum);
                                        DayTour optTour = dayTourMapper.getById(optId);
                                        if (optTour != null) {
                                            item.put("title", optTour.getName());
                                        }
                                    } catch (Exception ignore) {}
                                }
                            }
                            // 计算该日日期
                            if (orderData.getTourStartDate() != null && item.get("day_number") != null) {
                                try {
                                    int dayNum2 = Integer.parseInt(String.valueOf(item.get("day_number")));
                                    java.time.LocalDate d = orderData.getTourStartDate().plusDays(Math.max(0, dayNum2 - 1));
                                    item.put("date", d.format(dayFmt));
                                } catch (Exception ignore) {}
                            }
                            finalItinerary.add(item);
                        }
                        log.info("使用产品默认行程: tourId={}, tourType={}, items={}", 
                                orderData.getTourId(), orderData.getTourType(), finalItinerary.size());
                    } catch (Exception e) {
                        log.error("获取产品行程信息失败: tourId={}, tourType={}", orderData.getTourId(), orderData.getTourType(), e);
                    }
                }
            }
            orderDetails.setItinerary(finalItinerary);

            log.info("构建完整订单详情: tourName={}, startDate={}, passengers={}", 
                    orderDetails.getTourName(), orderDetails.getStartDate(), 
                    orderDetails.getAdultCount() + orderDetails.getChildCount());

            // 准备邮件内容
            Context context = new Context();
            context.setVariable("tourDetails", orderDetails);
            context.setVariable("orderId", emailConfirmationDTO.getOrderId());
            context.setVariable("orderNumber", orderData.getOrderNumber() != null ? orderData.getOrderNumber() : String.valueOf(emailConfirmationDTO.getOrderId()));
            context.setVariable("confirmationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)));
            context.setVariable("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)));
            context.setVariable("issueDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)));
            // 注入logo变量：优先代理商头像，其次默认地接社logo
            Long agentIdLong = orderData.getAgentId() == null ? null : orderData.getAgentId().longValue();
            context.setVariable("logoDataUrl", resolveLogoDataUrl(agentIdLong, emailConfirmationDTO.getLogoPreference()));

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
    public String renderConfirmationHtml(Long orderId, String logoPreference) {
        TourBooking orderData = tourBookingMapper.getById(orderId.intValue());
        if (orderData == null) {
            throw new RuntimeException("订单不存在: orderId=" + orderId);
        }
        // 构建确认单详情（包含行程）
        Context context = new Context();
        EmailConfirmationDTO.OrderDetails orderDetails = new EmailConfirmationDTO.OrderDetails();
        String actualTourName = getTourNameByIdAndType(orderData.getTourId(), orderData.getTourType());
        orderDetails.setTourName(actualTourName != null ? actualTourName : "塔斯马尼亚旅游");
        orderDetails.setTourType(orderData.getTourType());
        orderDetails.setStartDate(orderData.getTourStartDate() != null ? orderData.getTourStartDate().toString() : null);
        orderDetails.setEndDate(orderData.getTourEndDate() != null ? orderData.getTourEndDate().toString() : null);
        orderDetails.setAdultCount(orderData.getAdultCount() != null ? orderData.getAdultCount() : orderData.getGroupSize());
        orderDetails.setChildCount(orderData.getChildCount() != null ? orderData.getChildCount() : 0);
        orderDetails.setContactPerson(orderData.getContactPerson());
        orderDetails.setContactPhone(orderData.getContactPhone());
        orderDetails.setPickupLocation(orderData.getPickupLocation());
        orderDetails.setDropoffLocation(orderData.getDropoffLocation());
        orderDetails.setHotelLevel(orderData.getHotelLevel());
        orderDetails.setSpecialRequests(orderData.getSpecialRequests());
        // 代理商显示名（可选）
        try {
            if (orderData.getAgentId() != null) {
                com.sky.entity.Agent agent = agentMapper.getById(orderData.getAgentId().longValue());
                String agentDisplayName = null;
                if (agent != null) {
                    agentDisplayName = (agent.getCompanyName() != null && !agent.getCompanyName().trim().isEmpty())
                            ? agent.getCompanyName()
                            : agent.getUsername();
                }
                orderDetails.setAgentName(agentDisplayName != null ? agentDisplayName : "");
            }
        } catch (Exception ignore) {}

        // 构建行程（与邮件发送时一致）：优先按订单号的排团行程，回退产品默认行程；并按选择的一日游替换标题
        List<Map<String, Object>> finalItinerary = new ArrayList<>();
        Map<Integer, Integer> selectedMapForReplace = parseSelectedOptionalToursSafe(orderData.getSelectedOptionalTours());
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA);
        try {
            String ordNo = orderData.getOrderNumber();
            if (ordNo != null && !ordNo.trim().isEmpty()) {
                List<com.sky.vo.TourScheduleVO> schedules = tourScheduleOrderService.getSchedulesByOrderNumber(ordNo);
                if (schedules != null && !schedules.isEmpty()) {
                    for (com.sky.vo.TourScheduleVO vo : schedules) {
                        Map<String, Object> m = new HashMap<>();
                        if (vo.getDayNumber() != null) m.put("day_number", vo.getDayNumber());
                        if (vo.getDisplayOrder() != null) m.put("display_order", vo.getDisplayOrder());
                        String title = vo.getTitle();
                        if ("group_tour".equals(orderData.getTourType()) && vo.getDayNumber() != null && selectedMapForReplace.containsKey(vo.getDayNumber())) {
                            try {
                                Integer optId = selectedMapForReplace.get(vo.getDayNumber());
                                DayTour optTour = dayTourMapper.getById(optId);
                                if (optTour != null) {
                                    title = optTour.getName();
                                }
                            } catch (Exception ignore) {}
                        }
                        m.put("title", title);
                        java.time.LocalDate d = vo.getTourDate();
                        if (d == null && orderData.getTourStartDate() != null && vo.getDayNumber() != null) {
                            d = orderData.getTourStartDate().plusDays(Math.max(0, vo.getDayNumber() - 1));
                        }
                        if (d != null) m.put("date", d.format(dayFmt));
                        m.put("description", "");
                        finalItinerary.add(m);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("按订单号获取排团行程失败，使用产品默认行程: {}", e.getMessage());
        }

        if (finalItinerary.isEmpty()) {
            if (orderData.getTourId() != null && orderData.getTourType() != null) {
                try {
                    List<Map<String, Object>> itinerary = tourItineraryMapper.getItineraryByTourId(orderData.getTourId(), orderData.getTourType());
                    for (Map<String, Object> item : itinerary) {
                        item.put("description", "");
                        if ("group_tour".equals(orderData.getTourType()) && item.get("day_number") != null) {
                            Integer dayNum = null;
                            try { dayNum = Integer.valueOf(String.valueOf(item.get("day_number"))); } catch (Exception ignore) {}
                            if (dayNum != null && selectedMapForReplace.containsKey(dayNum)) {
                                try {
                                    Integer optId = selectedMapForReplace.get(dayNum);
                                    DayTour optTour = dayTourMapper.getById(optId);
                                    if (optTour != null) {
                                        item.put("title", optTour.getName());
                                    }
                                } catch (Exception ignore) {}
                            }
                        }
                        if (orderData.getTourStartDate() != null && item.get("day_number") != null) {
                            try {
                                int dayNum2 = Integer.parseInt(String.valueOf(item.get("day_number")));
                                java.time.LocalDate d = orderData.getTourStartDate().plusDays(Math.max(0, dayNum2 - 1));
                                item.put("date", d.format(dayFmt));
                            } catch (Exception ignore) {}
                        }
                        finalItinerary.add(item);
                    }
                } catch (Exception e) {
                    log.error("获取产品行程信息失败: tourId={}, tourType={}", orderData.getTourId(), orderData.getTourType(), e);
                }
            }
        }
        orderDetails.setItinerary(finalItinerary);

        context.setVariable("tourDetails", orderDetails);
        context.setVariable("orderId", orderId);
        context.setVariable("orderNumber", orderData.getOrderNumber() != null ? orderData.getOrderNumber() : String.valueOf(orderId));
        context.setVariable("confirmationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)));
        context.setVariable("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)));
        context.setVariable("issueDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)));
        Long agentIdLong = orderData.getAgentId() == null ? null : orderData.getAgentId().longValue();
        context.setVariable("logoDataUrl", resolveLogoDataUrl(agentIdLong, logoPreference));
        return templateEngine.process("confirmation-letter", context);
    }

    @Override
    public byte[] renderConfirmationPdf(Long orderId, String logoPreference) {
        String html = renderConfirmationHtml(orderId, logoPreference);
        return pdfService.generatePdfFromHtml(html);
    }

    @Override
    public String renderInvoiceHtml(Long orderId) {
        TourBooking orderData = tourBookingMapper.getById(orderId.intValue());
        if (orderData == null) {
            throw new RuntimeException("订单不存在: orderId=" + orderId);
        }
        EmailInvoiceDTO.InvoiceDetails invoiceDetails = new EmailInvoiceDTO.InvoiceDetails();
        // 产品名称
        String actualTourName = getTourNameByIdAndType(orderData.getTourId(), orderData.getTourType());
        invoiceDetails.setTourName(actualTourName != null ? actualTourName : "塔斯马尼亚旅游");
        invoiceDetails.setTourType(orderData.getTourType());
        invoiceDetails.setStartDate(orderData.getTourStartDate() != null ? orderData.getTourStartDate().toString() : null);
        invoiceDetails.setEndDate(orderData.getTourEndDate() != null ? orderData.getTourEndDate().toString() : null);
        invoiceDetails.setAdultCount(orderData.getAdultCount() != null ? orderData.getAdultCount() : orderData.getGroupSize());
        invoiceDetails.setChildCount(orderData.getChildCount() != null ? orderData.getChildCount() : 0);
        if (orderData.getTotalPrice() != null) {
            invoiceDetails.setTotalPrice(orderData.getTotalPrice().doubleValue());
        }
        // 代理商名称（若有）
        try {
            if (orderData.getAgentId() != null) {
                com.sky.entity.Agent agent = agentMapper.getById(orderData.getAgentId().longValue());
                if (agent != null) {
                    String agentDisplayName = (agent.getCompanyName() != null && !agent.getCompanyName().trim().isEmpty())
                            ? agent.getCompanyName() : agent.getUsername();
                    invoiceDetails.setAgentName(agentDisplayName);
                }
            }
        } catch (Exception ignore) {}

        Context context = new Context();
        context.setVariable("invoiceDetails", invoiceDetails);
        context.setVariable("orderId", orderId);
        context.setVariable("orderNumber", orderData.getOrderNumber() != null ? orderData.getOrderNumber() : String.valueOf(orderId));
        context.setVariable("invoiceNumber", "INV-" + orderId);
        context.setVariable("invoiceDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)));
        context.setVariable("dueDate", LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)));
        // Invoice 强制使用公司logo
        context.setVariable("logoDataUrl", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
        return templateEngine.process("tax-invoice", context);
    }

    @Override
    public byte[] renderInvoicePdf(Long orderId) {
        String html = renderInvoiceHtml(orderId);
        return pdfService.generatePdfFromHtml(html);
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
                // 从数据库获取真实的产品名称
                String actualTourName = getTourNameByIdAndType(orderData.getTourId(), orderData.getTourType());
                actualInvoiceDetails.setTourName(actualTourName != null ? actualTourName : "塔斯马尼亚旅游");
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
            // 注入logo变量：优先代理商头像，其次默认地接社logo
            // Invoice 强制使用公司logo
            context.setVariable("logoDataUrl", "data:image/png;base64,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");

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
    // private helper retained for potential future HTML emails without attachments



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
     * 解析Logo Data URL：
     * - 若存在代理商且其头像URL有效，使用其头像（以 <img th:src="${logoDataUrl}"> 渲染）
     * - 否则回退到内置的地接社logo（static/images/logo.png，经由PdfServiceImpl在生成PDF时自动替换为base64）
     */
    private String resolveLogoDataUrl(Long agentId, String logoPreference) {
        try {
            String defaultInline = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
            if (agentId == null) {
                return defaultInline; // 让PdfServiceImpl在生成PDF时替换为默认logo
            }
            // 若明确要求使用公司logo
            if (logoPreference != null && logoPreference.equalsIgnoreCase("company")) {
                return defaultInline;
            }
            com.sky.entity.Agent agent = agentMapper.getById(agentId);
            if (agent != null && agent.getAvatar() != null && !agent.getAvatar().trim().isEmpty()) {
                // 如果配置里明确偏好使用头像作为Logo，或者没有传preference但useAvatarAsLogo为true，则用头像
                boolean preferAvatar = (logoPreference != null && logoPreference.equalsIgnoreCase("agent"))
                        || (logoPreference == null && Boolean.TRUE.equals(agent.getUseAvatarAsLogo()));
                if (!preferAvatar) {
                    return defaultInline;
                }
                String avatar = agent.getAvatar().trim();
                // 如果是base64已内联，直接返回；否则当作URL供openhtmltopdf下载（建议使用可公网访问的HTTPS）
                if (avatar.startsWith("data:image")) {
                    return avatar;
                }
                return avatar;
            }
            // 若明确要求使用代理商但没有头像，则也回退公司logo
            return defaultInline;
        } catch (Exception e) {
            log.warn("解析logo失败，使用默认logo: agentId={}", agentId, e);
            return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
        }
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
    
    /**
     * 根据tourId和tourType获取产品名称
     * @param tourId 产品ID
     * @param tourType 产品类型
     * @return 产品名称
     */
    private String getTourNameByIdAndType(Integer tourId, String tourType) {
        if (tourId == null || tourType == null) {
            return null;
        }
        
        try {
            if ("group_tour".equals(tourType)) {
                GroupTourDTO groupTour = groupTourMapper.getById(tourId);
                return groupTour != null ? groupTour.getName() : null;
            } else if ("day_tour".equals(tourType)) {
                DayTour dayTour = dayTourMapper.getById(tourId);
                return dayTour != null ? dayTour.getName() : null;
            }
        } catch (Exception e) {
            log.error("获取产品名称失败: tourId={}, tourType={}", tourId, tourType, e);
        }
        
        return null;
    }

    /**
     * 解析用户选择的可选项目JSON，容错实现，如：{"1":25,"2":26}
     */
    private Map<Integer, Integer> parseSelectedOptionalToursSafe(String selectedOptionalTours) {
        Map<Integer, Integer> result = new HashMap<>();
        if (selectedOptionalTours == null || selectedOptionalTours.trim().isEmpty()) return result;
        try {
            String cleaned = selectedOptionalTours.trim();
            // 去掉花括号
            if (cleaned.startsWith("{") && cleaned.endsWith("}")) cleaned = cleaned.substring(1, cleaned.length() - 1);
            if (cleaned.isEmpty()) return result;
            String[] pairs = cleaned.split(",");
            for (String p : pairs) {
                String[] kv = p.split(":");
                if (kv.length != 2) continue;
                String k = kv[0].replaceAll("[\"'\\s]", "");
                String v = kv[1].replaceAll("[\"'\\s]", "");
                Integer day = Integer.valueOf(k);
                Integer id = Integer.valueOf(v);
                result.put(day, id);
            }
        } catch (Exception ignore) {
        }
        return result;
    }
} 