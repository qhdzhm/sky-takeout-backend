package com.sky.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 酒店预订DTO
 */
@Data
@ApiModel(description = "酒店预订数据传输对象")
public class HotelBookingDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("预订ID")
    private Integer id;

    @ApiModelProperty("关联的旅游订单ID")
    private Integer tourBookingId;

    @ApiModelProperty("关联的排团记录ID")
    private Integer scheduleOrderId;

    @ApiModelProperty("关联的导游车辆分配ID")
    private Integer assignmentId;

    @ApiModelProperty("酒店ID")
    private Integer hotelId;

    @ApiModelProperty("房型ID")
    private Integer roomTypeId;

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

    @ApiModelProperty("房间数量")
    private Integer roomCount;

    @ApiModelProperty("成人数量")
    private Integer adultCount;

    @ApiModelProperty("儿童数量")
    private Integer childCount;

    @ApiModelProperty("房间单价")
    private BigDecimal roomRate;

    @ApiModelProperty("预订状态")
    private String bookingStatus;

    @ApiModelProperty("支付状态")
    private String paymentStatus;

    @ApiModelProperty("特殊要求")
    private String specialRequests;

    @ApiModelProperty("预订来源")
    private String bookingSource;

    @ApiModelProperty("内部备注")
    private String notes;
} 