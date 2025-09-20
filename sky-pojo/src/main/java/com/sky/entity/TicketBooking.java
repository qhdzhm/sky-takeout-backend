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
 * 票务预订实体类 - 基于酒店预订实体类架构设计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketBooking implements Serializable {

    private static final long serialVersionUID = 1L;

    // 预订ID
    private Long id;
    
    // 预订参考号（自动生成：TB+日期+序号）
    private String bookingReference;
    
    // 确认号（景点回复的确认号）
    private String confirmationNumber;
    
    // 关联的排团记录ID（可选）
    private Long scheduleOrderId;
    
    // 关联的旅游订单ID（可选）
    private Long tourBookingId;
    
    // 关联的导游车辆分配ID（可选）
    private Long assignmentId;
    
    // 景点ID
    private Long attractionId;
    
    // 票务类型ID
    private Long ticketTypeId;
    
    // 游客姓名
    private String guestName;
    
    // 游客电话
    private String guestPhone;
    
    // 游客邮箱
    private String guestEmail;
    
    // 游览日期
    private LocalDate visitDate;
    
    // 预订日期
    private LocalDate bookingDate;
    
    // 成人数量
    private Integer adultCount;
    
    // 儿童数量
    private Integer childCount;
    
    // 游客总数（自动计算）
    private Integer totalGuests;
    
    // 门票单价
    private BigDecimal ticketPrice;
    
    // 总金额
    private BigDecimal totalAmount;
    
    // 货币类型（AUD：澳元，USD：美元，CNY：人民币）
    private String currency;
    
    // 预订方式（email：邮件预订，website：官网预订）
    private String bookingMethod;
    
    // 预订状态（pending：待预订，email_sent：已发邮件，confirmed：已确认，visited：已游览，cancelled：已取消）
    private String bookingStatus;
    
    // 支付状态（unpaid：未支付，paid：已支付，refunded：已退款）
    private String paymentStatus;
    
    // 特殊要求
    private String specialRequirements;
    
    // 预订来源（agent：代理商，direct：直接预订，system：系统预订）
    private String bookingSource;
    
    // 预订人ID
    private Long bookedBy;
    
    // 票务专员（负责此预订的员工用户名）
    private String ticketSpecialist;
    
    // 内部备注
    private String notes;
    
    // 邮件发送时间
    private LocalDateTime emailSentTime;
    
    // 确认时间
    private LocalDateTime confirmedTime;
    
    // 游览时间
    private LocalDateTime visitedTime;
    
    // 取消时间
    private LocalDateTime cancelledTime;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
}

