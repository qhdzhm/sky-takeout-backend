package com.sky.config;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * Druid数据库连接池配置类
 */
@Configuration
public class DruidConfig {

    /**
     * 解决Druid连接池"discard long time none received connection"错误
     * 通过设置druid.mysql.usePingMethod=false，让验证空闲连接使用SELECT 1而不是MySQL的Ping
     * 这样每次检测连接时都会刷新最后接收时间，避免连接被意外丢弃
     */
    @PostConstruct
    public void setProperties() {
        System.setProperty("druid.mysql.usePingMethod", "false");
    }
} 