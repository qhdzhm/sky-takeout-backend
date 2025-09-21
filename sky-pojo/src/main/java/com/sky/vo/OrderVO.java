package com.sky.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图对象
 */
@Data
public class OrderVO {
    // 订单信息
    private Integer bookingId; // 订单ID
    private String orderNumber; // 订单号
    private Integer tourId; // 旅行ID
    private String tourType; // 旅行类型
    private Integer userId; // 用户ID
    private Integer agentId; // 代理商ID
    private Long operatorId; // 操作员ID
    private LocalDate bookingDate; // 预订日期
    private String flightNumber; // 航班号
    private LocalDateTime arrivalLandingTime; // 到达时间
    private String returnFlightNumber; // 返程航班号
    private LocalDateTime departureDepartureTime; // 起飞时间
    private LocalDate tourStartDate; // 旅行开始日期
    private LocalDate tourEndDate; // 旅行结束日期
    private LocalDate pickupDate; // 接机日期
    private LocalDate dropoffDate; // 送机日期
    private String pickupLocation; // 接机地点
    private String dropoffLocation; // 送机地点
    private String serviceType; // 服务类型
    private Integer groupSize; // 团队人数
    private Integer adultCount; // 成人数量
    private Integer childCount; // 儿童数量
    private Integer luggageCount; // 行李数量
    private String passengerContact; // 乘客联系方式
    private String contactPerson; // 联系人
    private String contactPhone; // 联系电话
    private String hotelLevel; // 酒店级别
    private String roomType; // 房间类型（JSON或单个房型）
    private List<String> roomTypes; // 房间类型数组（解析后的）
    private Integer hotelRoomCount; // 酒店房间数量
    private String roomDetails; // 酒店房间详情
    private String specialRequests; // 特殊要求
    private String itineraryDetails; // 行程详情
    private String status; // 订单状态
    private String paymentStatus; // 支付状态
    private BigDecimal totalPrice; // 总价格
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
    private String selectedOptionalTours; // 选中的可选行程（JSON格式）
    
    // 🆕 团型管理字段
    private String groupType; // 团型类型（standard：普通团，small_12：12人团，small_14：14人团，luxury：精品团）
    private Integer groupSizeLimit; // 团型人数限制
    private LocalDateTime arrivalDepartureTime; // 接机时间
    private LocalDate hotelCheckInDate; // 酒店入住日期
    private LocalDate hotelCheckOutDate; // 酒店退房日期
    
    // 附加信息
    private String tourName; // 旅行名称
    private String tourLocation; // 旅行地点
    private String tourImage; // 旅行图片

    @ApiModelProperty("用户姓名")
    private String userName;
    
    @ApiModelProperty("代理商名称")
    private String agentName;
    
    @ApiModelProperty("操作员名称")
    private String operatorName;
    
    @ApiModelProperty("乘客列表")
    private List<PassengerVO> passengers;
} 