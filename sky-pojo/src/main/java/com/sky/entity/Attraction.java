package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景点实体类 - 基于酒店系统架构设计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attraction implements Serializable {

    private static final long serialVersionUID = 1L;

    // 景点ID
    private Long id;
    
    // 景点名称
    private String attractionName;
    
    // 英文名称
    private String attractionNameEn;
    
    // 位置
    private String location;
    
    // 联系电话
    private String contactPhone;
    
    // 联系人
    private String contactPerson;
    
    // 联系邮箱
    private String contactEmail;
    
    // 预订方式（email：邮件预订，website：官网预订）
    private String bookingType;
    
    // 官网预订地址
    private String websiteUrl;
    
    // 邮件预订地址
    private String emailAddress;
    
    // 需提前预订天数
    private Integer advanceDays;
    
    // 景点描述
    private String description;
    
    // 景点图片（JSON格式）
    private String images;
    
    // 评分（1-5分）
    private BigDecimal rating;
    
    // 状态（active：启用，inactive：停用）
    private String status;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
}

