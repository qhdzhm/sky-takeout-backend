package com.sky.service;

import com.sky.dto.EmailConfirmationDTO;
import com.sky.dto.EmailInvoiceDTO;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送确认单邮件
     *
     * @param emailConfirmationDTO 确认单邮件数据
     */
    void sendConfirmationEmail(EmailConfirmationDTO emailConfirmationDTO);

    /**
     * 发送发票邮件
     *
     * @param emailInvoiceDTO 发票邮件数据
     */
    void sendInvoiceEmail(EmailInvoiceDTO emailInvoiceDTO);
} 