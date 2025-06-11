package com.sky.controller.common;

import com.sky.dto.EmailConfirmationDTO;
import com.sky.dto.EmailInvoiceDTO;
import com.sky.result.Result;
import com.sky.service.EmailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮件发送控制器
 */
@RestController
@RequestMapping("/email")
@Api(tags = "邮件发送相关接口")
@Slf4j
@ConditionalOnProperty(name = "sky.mail.enabled", havingValue = "true")
public class EmailController {

    @Autowired
    private EmailService emailService;

    /**
     * 发送确认单邮件
     *
     * @param emailConfirmationDTO 确认单邮件数据
     * @return 结果
     */
    @PostMapping("/send-confirmation")
    @ApiOperation("发送确认单邮件")
    public Result<String> sendConfirmation(@RequestBody EmailConfirmationDTO emailConfirmationDTO) {
        log.warn("⚠️ EmailController已禁用，邮件现在由订单创建自动发送！前端调用了: {}", emailConfirmationDTO);
        return Result.error("EmailController已禁用，邮件现在由订单创建自动发送");
    }

    /**
     * 发送发票邮件
     *
     * @param emailInvoiceDTO 发票邮件数据
     * @return 结果
     */
    @PostMapping("/send-invoice")
    @ApiOperation("发送发票邮件") 
    public Result<String> sendInvoice(@RequestBody EmailInvoiceDTO emailInvoiceDTO) {
        log.warn("⚠️ EmailController已禁用，邮件现在由订单创建自动发送！前端调用了: {}", emailInvoiceDTO);
        return Result.error("EmailController已禁用，邮件现在由订单创建自动发送");
    }

    
} 