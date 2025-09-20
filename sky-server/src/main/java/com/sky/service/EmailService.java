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

    /**
     * 使用员工邮箱发送邮件（带附件）
     * @param employeeId 员工ID
     * @param to 收件人
     * @param subject 主题
     * @param body 邮件正文
     * @param attachment 附件内容
     * @param attachmentName 附件名称
     * @return 是否发送成功
     */
    boolean sendEmailWithEmployeeAccount(Long employeeId, String to, String subject, 
                                       String body, byte[] attachment, String attachmentName);
} 