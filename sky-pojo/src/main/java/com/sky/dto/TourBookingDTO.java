package com.sky.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 旅游订单数据传输对象
 */
@Data
@ApiModel(description = "旅游订单数据传输对象")
public class TourBookingDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("订单ID")
    private Integer bookingId;
    
    @ApiModelProperty("订单号")
    private String orderNumber;
    
    @ApiModelProperty("旅游项目ID")
    private Integer tourId;
    
    @ApiModelProperty("旅游类型(day_tour:一日游,group_tour:团队游)")
    private String tourType;
    
    @ApiModelProperty("用户ID")
    private Integer userId;
    
    @ApiModelProperty("代理商ID")
    private Integer agentId;
    
    @ApiModelProperty("操作员ID")
    private Long operatorId;
    
    @ApiModelProperty("航班号")
    private String flightNumber;
    
    @ApiModelProperty("到达航班起飞时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime arrivalDepartureTime;
    
    @ApiModelProperty("到达航班降落时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime arrivalLandingTime;
    
    @ApiModelProperty("返程航班号")
    private String returnFlightNumber;
    
    @ApiModelProperty("返程航班起飞时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime departureDepartureTime;
    
    @ApiModelProperty("返程航班降落时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
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
    
    @ApiModelProperty("服务类型")
    private String serviceType;
    
    @ApiModelProperty("团队规模")
    private Integer groupSize;
    
    @ApiModelProperty("成人数量")
    private Integer adultCount;
    
    @ApiModelProperty("儿童数量")
    private Integer childCount;
    
    @ApiModelProperty("行李数量")
    private Integer luggageCount;
    
    @ApiModelProperty("乘客联系方式")
    private String passengerContact;
    
    @ApiModelProperty("联系人")
    private String contactPerson;
    
    @ApiModelProperty("联系电话")
    private String contactPhone;
    
    @ApiModelProperty("是否包含酒店（true: 包含酒店, false: 不包含酒店）")
    private Boolean includeHotel;
    
    @ApiModelProperty("酒店等级")
    private String hotelLevel;
    
    @ApiModelProperty("房间类型")
    private String roomType;
    
    @ApiModelProperty("房间类型数组（前端传递）")
    private List<String> roomTypes;
    
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
    
    @ApiModelProperty("是否小团")
    private Boolean isSmallGroup;
    
    @ApiModelProperty("小团额外费用")
    private BigDecimal smallGroupExtraFee;
    
    @ApiModelProperty("用户选择的可选项目（JSON字符串）")
    private String selectedOptionalTours;
    
    @ApiModelProperty("团型类型（standard：普通团，small_12：12人团，small_14：14人团，luxury：精品团）")
    private String groupType;
    
    @ApiModelProperty("团型人数限制")
    private Integer groupSizeLimit;
    
    @ApiModelProperty("乘客列表")
    private List<PassengerDTO> passengers;
}