package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 客服信息DTO
 */
@Data
public class CustomerServiceDTO implements Serializable {

    private Long id;
    private String username;
    private String name;
    private String password;
    private String phone;
    private String sex;
    private String serviceNo;
    private Integer serviceLevel;
    private Integer maxConcurrentCustomers;
    private String skillTags;
} 