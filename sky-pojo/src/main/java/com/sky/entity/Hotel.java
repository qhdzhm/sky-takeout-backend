package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 酒店实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hotel implements Serializable {

    private static final long serialVersionUID = 1L;

    // 酒店ID
    private Integer id;
    
    // 酒店名称
    private String hotelName;
    
    // 酒店等级
    private String hotelLevel;
    
    // 地址
    private String address;
    
    // 区域位置
    private String locationArea;
    
    // 电话
    private String phone;
    
    // 入住时间
    private LocalTime checkInTime;
    
    // 退房时间
    private LocalTime checkOutTime;
    
    // 设施列表（JSON格式）
    private String facilities;
    
    // 酒店描述
    private String description;
    
    // 酒店图片（JSON格式）
    private String images;
    
    // 评分
    private BigDecimal rating;
    
    // 状态（active：活跃，inactive：停用）
    private String status;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;

    // 联系人
    private String contactPerson;

    // 联系人电话
    private String contactPhone;

    // 酒店邮箱
    private String contactEmail;
} 