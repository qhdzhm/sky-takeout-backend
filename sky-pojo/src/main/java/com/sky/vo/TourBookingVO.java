package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 旅游订单视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "旅游订单视图对象")
public class TourBookingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("订单ID")
    private Integer bookingId;
    
    @ApiModelProperty("订单号")
    private String orderNumber;
    
    @ApiModelProperty("旅游项目ID")
    private Integer tourId;
    
    @ApiModelProperty("旅游项目名称")
    private String tourName;
    
    @ApiModelProperty("旅游类型(day_tour:一日游,group_tour:团队游)")
    private String tourType;
    
    @ApiModelProperty("用户ID")
    private Integer userId;
    
    @ApiModelProperty("用户姓名")
    private String userName;
    
    @ApiModelProperty("代理商ID")
    private Integer agentId;
    
    @ApiModelProperty("代理商名称")
    private String agentName;
    
    @ApiModelProperty("预订日期")
    private LocalDateTime bookingDate;
    
    @ApiModelProperty("航班号")
    private String flightNumber;
    
    @ApiModelProperty("返程航班号")
    private String returnFlightNumber;
    
    @ApiModelProperty("行程开始日期")
    private LocalDate tourStartDate;
    
    @ApiModelProperty("行程结束日期")
    private LocalDate tourEndDate;
    
    @ApiModelProperty("接客日期")
    private LocalDate pickupDate;
    
    @ApiModelProperty("送客日期")
    private LocalDate dropoffDate;
    
    @ApiModelProperty("接客地点")
    private String pickupLocation;
    
    @ApiModelProperty("送客地点")
    private String dropoffLocation;
    
    @ApiModelProperty("服务类型")
    private String serviceType;
    
    @ApiModelProperty("团队规模")
    private Integer groupSize;
    
    @ApiModelProperty("行李数量")
    private Integer luggageCount;
    
    @ApiModelProperty("乘客联系方式")
    private String passengerContact;
    
    @ApiModelProperty("联系人")
    private String contactPerson;
    
    @ApiModelProperty("联系电话")
    private String contactPhone;
    
    @ApiModelProperty("酒店等级")
    private String hotelLevel;
    
    @ApiModelProperty("房间类型")
    private String roomType;
    
    @ApiModelProperty("酒店间房数量")
    private Integer hotelRoomCount;
    
    @ApiModelProperty("酒店入住日期")
    private LocalDate hotelCheckInDate;
    
    @ApiModelProperty("酒店退房日期")
    private LocalDate hotelCheckOutDate;
    
    @ApiModelProperty("每个房间的样式")
    private String roomDetails;
    
    @ApiModelProperty("特殊请求")
    private String specialRequests;
    
    @ApiModelProperty("行程详情")
    private String itineraryDetails;
    
    @ApiModelProperty("订单状态")
    private String status;
    
    @ApiModelProperty("支付状态")
    private String paymentStatus;
    
    @ApiModelProperty("总价")
    private BigDecimal totalPrice;
    
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
    
    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
    
    @ApiModelProperty("乘客列表")
    private List<PassengerVO> passengers;
} 