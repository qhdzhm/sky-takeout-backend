package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 导游实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Guide implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer guideId;
    private Integer userId;
    private String bio;
    private Integer experienceYears;
    private String languages;
    private String certification;
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;
    private Boolean isActive;
    private Integer status;
    private Integer maxGroups;
    private String name;
    private String phone;
    private String email;
    private Long employeeId;
} 