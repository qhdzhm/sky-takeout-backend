package com.sky.service;

import com.sky.dto.EmailConfirmationDTO;
import com.sky.entity.TourBooking;

/**
 * 邮件异步服务接口
 * 专门处理异步邮件发送任务
 */
public interface EmailAsyncService {
    
    /**
     * 支付成功后异步发送确认信和发票邮件
     * @param orderId 订单ID
     * @param tourBooking 订单信息
     */
    void sendEmailsAfterPaymentAsync(Long orderId, TourBooking tourBooking);
    
    /**
     * 订单确认后异步发送确认邮件
     * @param emailConfirmationDTO 确认邮件信息
     */
    void sendConfirmationEmailAsync(EmailConfirmationDTO emailConfirmationDTO);
} 