package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 客服登录响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceLoginVO implements Serializable {

    private Long id;

    private String token;

    private String username;

    private String name;

    private String serviceNo;

    private Integer onlineStatus;

    private Integer maxConcurrentCustomers;

    private Integer currentCustomerCount;

    private Integer serviceLevel;
} 