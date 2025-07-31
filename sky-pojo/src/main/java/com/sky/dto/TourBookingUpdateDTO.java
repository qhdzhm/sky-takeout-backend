package com.sky.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 旅游订单更新数据传输对象
 */
@Data
@ApiModel(description = "旅游订单更新数据传输对象")
public class TourBookingUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("订单ID")
    private Integer bookingId;
    
    @ApiModelProperty("航班号")
    private String flightNumber;
    
    @ApiModelProperty("到达航班起飞时间")
    private LocalDateTime arrivalDepartureTime;
    
    @ApiModelProperty("到达航班降落时间")
    private LocalDateTime arrivalLandingTime;
    
    @ApiModelProperty("返程航班号")
    private String returnFlightNumber;
    
    @ApiModelProperty("返程航班起飞时间")
    private LocalDateTime departureDepartureTime;
    
    @ApiModelProperty("返程航班降落时间")
    private LocalDateTime departureLandingTime;
    
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
    
    @ApiModelProperty("成人数量")
    private Integer adultCount;
    
    @ApiModelProperty("儿童数量")
    private Integer childCount;
    
    @ApiModelProperty("行李数量")
    private Integer luggageCount;
    
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
    
    @ApiModelProperty("乘客列表")
    private List<PassengerDTO> passengers;
} 