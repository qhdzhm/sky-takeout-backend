package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单-乘客关联实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPassengerRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    // 关联ID
    private Integer id;
    
    // 订单ID
    private Integer bookingId;
    
    // 乘客ID
    private Integer passengerId;
    
    // 是否为主要乘客
    private Boolean isPrimary;
    
    // 机票号
    private String ticketNumber;
    
    // 座位号
    private String seatNumber;
    
    // 行李标签
    private String luggageTags;
    
    // 登记状态（未登记/已登记）
    private String checkInStatus;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
} 