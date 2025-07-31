package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 酒店预订实体类（基于排团记录的业务模式）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelBooking implements Serializable {

    private static final long serialVersionUID = 1L;

    // 预订ID
    private Integer id;
    
    // 预订参考号（自动生成：HB+日期+序号）
    private String bookingReference;
    
    // 关联的旅游订单ID
    private Integer tourBookingId;
    
    // 关联的排团记录ID（核心关联）
    private Integer scheduleOrderId;
    
    // 关联的导游车辆分配ID（可选）
    private Integer assignmentId;
    
    // 酒店ID
    private Integer hotelId;
    
    // 房型ID
    private Integer roomTypeId;
    
    // 客人姓名
    private String guestName;
    
    // 客人电话
    private String guestPhone;
    
    // 客人邮箱
    private String guestEmail;
    
    // 入住日期
    private LocalDate checkInDate;
    
    // 退房日期
    private LocalDate checkOutDate;
    
    // 住宿天数（自动计算）
    private Integer nights;
    
    // 房间数量
    private Integer roomCount;
    
    // 成人数量
    private Integer adultCount;
    
    // 儿童数量
    private Integer childCount;
    
    // 总客人数（自动计算）
    private Integer totalGuests;
    
    // 房间单价
    private BigDecimal roomRate;
    
    // 总金额
    private BigDecimal totalAmount;
    
    // 货币类型（AUD：澳元，USD：美元，CNY：人民币）
    private String currency;
    
    // 预订状态（pending：待确认，confirmed：已确认，checked_in：已入住，checked_out：已退房，cancelled：已取消）
    private String bookingStatus;
    
    // 支付状态（unpaid：未支付，paid：已支付，refunded：已退款）
    private String paymentStatus;
    
    // 特殊要求
    private String specialRequests;
    
    // 预订来源（agent：代理商，direct：直接预订，system：系统预订）
    private String bookingSource;
    
    // 预订人ID
    private Long bookedBy;
    
    // 内部备注
    private String notes;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
} 