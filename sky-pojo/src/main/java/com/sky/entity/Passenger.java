package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 乘客信息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Passenger implements Serializable {

    private static final long serialVersionUID = 1L;

    // 乘客ID
    private Integer passengerId;
    
    // 乘客姓名
    private String fullName;
    
    // 性别
    private String gender;
    
    // 出生日期
    private LocalDate dateOfBirth;
    
    // 是否是儿童
    private Boolean isChild;
    
    // 儿童年龄
    private String childAge;
    
    // 护照号码
    private String passportNumber;
    
    // 护照有效期
    private LocalDate passportExpiry;
    
    // 国籍
    private String nationality;
    
    // 电话号码
    private String phone;
    
    // 微信号
    private String wechatId;
    
    // 电子邮箱
    private String email;
    
    // 紧急联系人姓名
    private String emergencyContactName;
    
    // 紧急联系人电话
    private String emergencyContactPhone;
    
    // 饮食需求
    private String dietaryRequirements;
    
    // 健康状况
    private String medicalConditions;
    
    // 行李数量
    private Integer luggageCount;
    
    // 特殊需求
    private String specialRequests;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
} 