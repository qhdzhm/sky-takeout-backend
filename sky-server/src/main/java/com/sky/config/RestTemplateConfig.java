package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * 创建 RestTemplate Bean
     * 用于 HTTP 请求，特别是 Google OAuth 请求
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 设置连接超时时间（10秒）
        factory.setConnectTimeout(10000);
        
        // 设置读取超时时间（10秒）
        factory.setReadTimeout(10000);
        
        return new RestTemplate(factory);
    }
}

