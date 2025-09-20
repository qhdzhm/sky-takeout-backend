package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.EmailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件测试控制器 - 演示如何使用员工邮箱发送邮件
 */
@RestController
@RequestMapping("/admin/email-test")
@Api(tags = "邮件发送测试")
@Slf4j
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    /**
     * 测试使用员工邮箱发送邮件
     */
    @PostMapping("/send-with-employee")
    @ApiOperation("测试使用员工邮箱发送邮件")
    public Result testSendWithEmployee(@RequestParam Long employeeId,
                                     @RequestParam String to,
                                     @RequestParam(defaultValue = "测试邮件") String subject,
                                     @RequestParam(defaultValue = "这是一封使用员工个人邮箱发送的测试邮件") String body) {
        log.info("测试使用员工邮箱发送邮件: employeeId={}, to={}, subject={}", employeeId, to, subject);
        
        try {
            boolean success = emailService.sendEmailWithEmployeeAccount(
                    employeeId, to, subject, body, null, null);
            
            if (success) {
                return Result.success("邮件发送成功");
            } else {
                return Result.error("邮件发送失败");
            }
        } catch (Exception e) {
            log.error("测试发送邮件失败", e);
            return Result.error("发送失败: " + e.getMessage());
        }
    }

    /**
     * 测试使用员工邮箱发送带附件的邮件
     */
    @PostMapping("/send-with-attachment")
    @ApiOperation("测试使用员工邮箱发送带附件的邮件")
    public Result testSendWithAttachment(@RequestParam Long employeeId,
                                       @RequestParam String to,
                                       @RequestParam(defaultValue = "测试邮件（含附件）") String subject) {
        log.info("测试使用员工邮箱发送带附件邮件: employeeId={}, to={}", employeeId, to);
        
        try {
            // 创建一个简单的文本附件作为示例
            String attachmentContent = "这是一个测试附件的内容\n\n发送时间: " + 
                    java.time.LocalDateTime.now().toString();
            byte[] attachment = attachmentContent.getBytes("UTF-8");
            
            String body = "<h2>测试邮件（含附件）</h2>" +
                         "<p>这是一封使用员工个人邮箱发送的测试邮件，包含一个文本附件。</p>" +
                         "<p>发送时间: " + java.time.LocalDateTime.now() + "</p>";
            
            boolean success = emailService.sendEmailWithEmployeeAccount(
                    employeeId, to, subject, body, attachment, "test-attachment.txt");
            
            if (success) {
                return Result.success("带附件邮件发送成功");
            } else {
                return Result.error("带附件邮件发送失败");
            }
        } catch (Exception e) {
            log.error("测试发送带附件邮件失败", e);
            return Result.error("发送失败: " + e.getMessage());
        }
    }
}
