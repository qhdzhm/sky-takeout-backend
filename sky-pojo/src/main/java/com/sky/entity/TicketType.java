package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 票务类型实体类 - 对标酒店房型实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketType implements Serializable {

    private static final long serialVersionUID = 1L;

    // 票务类型ID
    private Long id;
    
    // 景点ID
    private Long attractionId;
    
    // 票务类型名称
    private String ticketType;
    
    // 英文名称
    private String ticketTypeEn;
    
    // 票务代码
    private String ticketCode;
    
    // 基础价格
    private BigDecimal basePrice;
    
    // 年龄限制
    private String ageRestriction;
    
    // 游览时长
    private String duration;
    
    // 最大容量
    private Integer maxCapacity;
    
    // 包含项目
    private String includes;
    
    // 不包含项目
    private String excludes;
    
    // 详细描述
    private String description;
    
    // 票务图片（JSON格式）
    private String images;
    
    // 状态（active：可用，inactive：停用）
    private String status;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
}

