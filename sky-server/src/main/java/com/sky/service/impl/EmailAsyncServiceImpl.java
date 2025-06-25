package com.sky.service.impl;

import com.sky.dto.EmailConfirmationDTO;
import com.sky.dto.EmailInvoiceDTO;
import com.sky.entity.TourBooking;
import com.sky.mapper.DayTourMapper;
import com.sky.mapper.GroupTourMapper;
import com.sky.service.EmailAsyncService;
import com.sky.service.EmailService;
import com.sky.dto.GroupTourDTO;
import com.sky.entity.DayTour;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 邮件异步服务实现类
 * 专门处理异步邮件发送任务
 */
@Service
@Slf4j
public class EmailAsyncServiceImpl implements EmailAsyncService {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private GroupTourMapper groupTourMapper;
    
    @Autowired
    private DayTourMapper dayTourMapper;

    /**
     * 支付成功后异步发送确认信和发票邮件（不阻塞主线程）
     * @param orderId 订单ID
     * @param tourBooking 订单信息
     */
    @Override
    @Async("emailTaskExecutor")
    public void sendEmailsAfterPaymentAsync(Long orderId, TourBooking tourBooking) {
        log.info("🚀 异步邮件发送任务开始: orderId={}, 线程={}", orderId, Thread.currentThread().getName());
        try {
            sendEmailsAfterPayment(orderId, tourBooking);
            log.info("✅ 异步邮件发送任务完成: orderId={}", orderId);
        } catch (Exception e) {
            log.error("❌ 异步邮件发送任务失败: orderId={}", orderId, e);
        }
    }

    /**
     * 支付成功后发送确认信和发票邮件（同步方法，供异步调用）
     * @param orderId 订单ID
     * @param tourBooking 订单信息
     */
    private void sendEmailsAfterPayment(Long orderId, TourBooking tourBooking) {
        log.info("支付成功，开始发送确认信和发票邮件: orderId={}", orderId);
        
        try {
            // 从订单信息中获取用户类型信息
            Long agentId = tourBooking.getAgentId() != null ? tourBooking.getAgentId().longValue() : null;
            Long operatorId = tourBooking.getOperatorId() != null ? tourBooking.getOperatorId().longValue() : null;
            Long userId = tourBooking.getUserId() != null ? tourBooking.getUserId().longValue() : null;
            
            // 确定收件人类型和实际的代理商ID、操作员ID
            String recipientType;
            Long actualAgentId;
            Long actualOperatorId = null;
            
            if (agentId != null) {
                if (operatorId != null) {
                    // 操作员下单
                    actualAgentId = agentId;
                    actualOperatorId = operatorId;
                    recipientType = "operator";
                    log.info("✅ 操作员支付成功: 代理商ID={}, 操作员ID={}", actualAgentId, actualOperatorId);
                } else {
                    // 代理商主号下单
                    actualAgentId = agentId;
                    recipientType = "agent";
                    log.info("✅ 代理商主号支付成功: 代理商ID={}", actualAgentId);
                }
            } else {
                // 普通用户，不发送邮件
                log.info("普通用户支付，不发送代理商邮件");
                return;
            }
            
            // 构建订单详情
            EmailConfirmationDTO.OrderDetails orderDetails = buildOrderDetailsFromBooking(tourBooking);
            EmailInvoiceDTO.InvoiceDetails invoiceDetails = buildInvoiceDetailsFromBooking(tourBooking);
            
            // 1. 发送发票邮件给代理商主号
            try {
                EmailInvoiceDTO invoiceDTO = new EmailInvoiceDTO();
                invoiceDTO.setOrderId(orderId);
                invoiceDTO.setAgentId(actualAgentId);
                invoiceDTO.setOperatorId(actualOperatorId);
                invoiceDTO.setInvoiceDetails(invoiceDetails);
                
                emailService.sendInvoiceEmail(invoiceDTO);
                log.info("✅ 支付后发票邮件发送成功: orderId={}, agentId={}", orderId, actualAgentId);
            } catch (Exception e) {
                log.error("❌ 支付后发票邮件发送失败: orderId={}", orderId, e);
            }
            
            // 2. 发送确认单邮件
            try {
                EmailConfirmationDTO confirmationDTO = new EmailConfirmationDTO();
                confirmationDTO.setOrderId(orderId);
                confirmationDTO.setRecipientType(recipientType);
                confirmationDTO.setAgentId(actualAgentId);
                confirmationDTO.setOperatorId(actualOperatorId);
                confirmationDTO.setOrderDetails(orderDetails);
                
                emailService.sendConfirmationEmail(confirmationDTO);
                
                if ("operator".equals(recipientType)) {
                    log.info("✅ 操作员支付后确认单邮件发送成功: orderId={}, 发送给操作员和主号", orderId);
                } else {
                    log.info("✅ 主号支付后确认单邮件发送成功: orderId={}, 发送给主号", orderId);
                }
            } catch (Exception e) {
                log.error("❌ 支付后确认单邮件发送失败: orderId={}, recipientType={}", orderId, recipientType, e);
            }
            
            log.info("支付后邮件发送处理完成: orderId={}, recipientType={}", orderId, recipientType);
            
        } catch (Exception e) {
            log.error("支付后邮件发送处理异常: orderId={}", orderId, e);
        }
    }
    
    /**
     * 从TourBooking构建订单详情（用于确认邮件）
     */
    private EmailConfirmationDTO.OrderDetails buildOrderDetailsFromBooking(TourBooking tourBooking) {
        EmailConfirmationDTO.OrderDetails orderDetails = new EmailConfirmationDTO.OrderDetails();
        
        // 获取真实的产品名称
        String actualTourName = getTourNameByIdAndType(tourBooking.getTourId(), tourBooking.getTourType());
        orderDetails.setTourName(actualTourName != null ? actualTourName : "塔斯马尼亚旅游");
        orderDetails.setTourType(tourBooking.getTourType());
        orderDetails.setStartDate(tourBooking.getTourStartDate() != null ? tourBooking.getTourStartDate().toString() : null);
        orderDetails.setEndDate(tourBooking.getTourEndDate() != null ? tourBooking.getTourEndDate().toString() : null);
        orderDetails.setAdultCount(tourBooking.getAdultCount() != null ? tourBooking.getAdultCount() : 0);
        orderDetails.setChildCount(tourBooking.getChildCount() != null ? tourBooking.getChildCount() : 0);
        orderDetails.setContactPerson(tourBooking.getContactPerson());
        orderDetails.setContactPhone(tourBooking.getContactPhone());
        orderDetails.setPickupLocation(tourBooking.getPickupLocation());
        orderDetails.setDropoffLocation(tourBooking.getDropoffLocation());
        orderDetails.setHotelLevel(tourBooking.getHotelLevel());
        orderDetails.setSpecialRequests(tourBooking.getSpecialRequests());
        return orderDetails;
    }
    
    /**
     * 从TourBooking构建发票详情（用于发票邮件）
     */
    private EmailInvoiceDTO.InvoiceDetails buildInvoiceDetailsFromBooking(TourBooking tourBooking) {
        EmailInvoiceDTO.InvoiceDetails invoiceDetails = new EmailInvoiceDTO.InvoiceDetails();
        
        // 获取真实的产品名称
        String actualTourName = getTourNameByIdAndType(tourBooking.getTourId(), tourBooking.getTourType());
        invoiceDetails.setTourName(actualTourName != null ? actualTourName : "塔斯马尼亚旅游");
        invoiceDetails.setTourType(tourBooking.getTourType());
        invoiceDetails.setStartDate(tourBooking.getTourStartDate() != null ? tourBooking.getTourStartDate().toString() : null);
        invoiceDetails.setEndDate(tourBooking.getTourEndDate() != null ? tourBooking.getTourEndDate().toString() : null);
        invoiceDetails.setAdultCount(tourBooking.getAdultCount() != null ? tourBooking.getAdultCount() : 0);
        invoiceDetails.setChildCount(tourBooking.getChildCount() != null ? tourBooking.getChildCount() : 0);
        invoiceDetails.setTotalPrice(tourBooking.getTotalPrice() != null ? tourBooking.getTotalPrice().doubleValue() : 0.0);
        return invoiceDetails;
    }
    
    /**
     * 根据tourId和tourType获取产品名称
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
} 