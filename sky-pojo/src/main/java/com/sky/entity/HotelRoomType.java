package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 酒店房型实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelRoomType implements Serializable {

    private static final long serialVersionUID = 1L;

    // 房型ID
    private Integer id;
    
    // 酒店ID
    private Integer hotelId;
    
    // 房型名称
    private String roomType;
    
    // 房型代码
    private String roomTypeCode;
    
    // 最大入住人数
    private Integer maxOccupancy;
    
    // 床型
    private String bedType;
    
    // 房间大小
    private String roomSize;
    
    // 基础价格
    private BigDecimal basePrice;
    
    // 房间设施（JSON格式）
    private String amenities;
    
    // 房间描述
    private String description;
    
    // 房间图片（JSON格式）
    private String images;
    
    // 状态（active：可用，inactive：停用）
    private String status;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
} 