package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 酒店供应商实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelSupplier implements Serializable {

    private static final long serialVersionUID = 1L;

    // 供应商ID
    private Integer id;
    
    // 供应商名称
    private String supplierName;
    
    // 联系人
    private String contactPerson;
    
    // 联系电话
    private String contactPhone;
    
    // 联系邮箱
    private String contactEmail;
    
    // 地址
    private String address;
    
    // 佣金比例(%)
    private BigDecimal commissionRate;
    
    // 付款条款
    private String paymentTerms;
    
    // 状态（active：活跃，inactive：停用）
    private String status;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
} 