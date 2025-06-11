package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件配置类
 */
@Configuration
@ConditionalOnProperty(name = "sky.mail.enabled", havingValue = "true")
@Slf4j
public class EmailConfig {

    public EmailConfig() {
        log.info("邮件服务已启用");
    }
} 