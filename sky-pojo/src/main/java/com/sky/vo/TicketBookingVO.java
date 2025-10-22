package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 票务预订视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "票务预订视图对象")
public class TicketBookingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("预订ID")
    private Long id;
    
    @ApiModelProperty("预订参考号")
    private String bookingReference;
    
    @ApiModelProperty("确认号（景点回复的确认号）")
    private String confirmationNumber;
    
    @ApiModelProperty("关联的排团记录ID")
    private Long scheduleOrderId;
    
    @ApiModelProperty("关联的旅游订单ID")
    private Long tourBookingId;
    
    @ApiModelProperty("景点ID")
    private Long attractionId;
    
    @ApiModelProperty("景点名称")
    private String attractionName;
    
    @ApiModelProperty("景点名称（英文）")
    private String attractionNameEn;
    
    @ApiModelProperty("票务类型ID")
    private Long ticketTypeId;
    
    @ApiModelProperty("票务类型名称")
    private String ticketTypeName;
    
    @ApiModelProperty("游客姓名")
    private String guestName;
    
    @ApiModelProperty("游客电话")
    private String guestPhone;
    
    @ApiModelProperty("游客邮箱")
    private String guestEmail;
    
    @ApiModelProperty("游览日期")
    private LocalDate visitDate;
    
    @ApiModelProperty("预订日期")
    private LocalDate bookingDate;
    
    @ApiModelProperty("成人数量")
    private Integer adultCount;
    
    @ApiModelProperty("儿童数量")
    private Integer childCount;
    
    @ApiModelProperty("游客总数")
    private Integer totalGuests;
    
    @ApiModelProperty("门票单价")
    private BigDecimal ticketPrice;
    
    @ApiModelProperty("总金额")
    private BigDecimal totalAmount;
    
    @ApiModelProperty("货币类型")
    private String currency;
    
    @ApiModelProperty("预订方式：email-邮件预订, website-官网预订")
    private String bookingMethod;
    
    @ApiModelProperty("预订状态：pending-待处理, email_sent-邮件已发送, confirmed-已确认, visited-已游览, cancelled-已取消")
    private String bookingStatus;
    
    @ApiModelProperty("支付状态：unpaid-未支付, paid-已支付, refunded-已退款")
    private String paymentStatus;
    
    @ApiModelProperty("特殊要求")
    private String specialRequirements;
    
    @ApiModelProperty("票务专员")
    private String ticketSpecialist;
    
    @ApiModelProperty("确认时间")
    private LocalDateTime confirmedTime;
    
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
    
    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}




