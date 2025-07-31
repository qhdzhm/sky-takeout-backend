package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 酒店预订视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "酒店预订详细信息")
public class HotelBookingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("预订ID")
    private Integer id;

    @ApiModelProperty("预订参考号")
    private String bookingReference;

    @ApiModelProperty("关联的旅游预订ID")
    private Integer tourBookingId;

    @ApiModelProperty("旅游订单号")
    private String orderNumber;

    @ApiModelProperty("排团记录标题")
    private String scheduleTitle;

    @ApiModelProperty("排团日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    @ApiModelProperty("酒店名称")
    private String hotelName;

    @ApiModelProperty("酒店等级")
    private String hotelLevel;

    @ApiModelProperty("酒店邮箱")
    private String hotelEmail;

    @ApiModelProperty("酒店联系人")
    private String contactPerson;

    @ApiModelProperty("酒店地址")
    private String hotelAddress;

    @ApiModelProperty("酒店电话")
    private String hotelPhone;

    @ApiModelProperty("房型名称")
    private String roomType;

    @ApiModelProperty("房型描述")
    private String roomDescription;

    @ApiModelProperty("客人姓名")
    private String guestName;

    @ApiModelProperty("客人电话")
    private String guestPhone;

    @ApiModelProperty("客人邮箱")
    private String guestEmail;

    @ApiModelProperty("入住日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @ApiModelProperty("退房日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    @ApiModelProperty("住宿天数")
    private Integer nights;

    @ApiModelProperty("房间数量")
    private Integer roomCount;

    @ApiModelProperty("成人数量")
    private Integer adultCount;

    @ApiModelProperty("儿童数量")
    private Integer childCount;

    @ApiModelProperty("总客人数")
    private Integer totalGuests;

    @ApiModelProperty("房间单价")
    private BigDecimal roomRate;

    @ApiModelProperty("总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("预订状态")
    private String bookingStatus;

    @ApiModelProperty("支付状态")
    private String paymentStatus;

    @ApiModelProperty("特殊要求")
    private String specialRequests;

    @ApiModelProperty("代理商邮箱")
    private String agentEmail;

    @ApiModelProperty("导游姓名")
    private String guideName;

    @ApiModelProperty("车牌号")
    private String licensePlate;

    @ApiModelProperty("目的地")
    private String destination;

    @ApiModelProperty("分配日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate assignmentDate;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
} 