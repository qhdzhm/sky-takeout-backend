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

    /**
     * 渲染确认单HTML
     */
    String renderConfirmationHtml(Long orderId, String logoPreference);

    /**
     * 生成确认单PDF字节
     */
    byte[] renderConfirmationPdf(Long orderId, String logoPreference);

    /**
     * 渲染发票HTML
     */
    String renderInvoiceHtml(Long orderId);

    /**
     * 生成发票PDF字节
     */
    byte[] renderInvoicePdf(Long orderId);
} 