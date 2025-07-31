package com.sky.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private Date bookingDate; // 预订日期
    private String flightNumber; // 航班号
    private Date arrivalLandingTime; // 到达时间
    private String returnFlightNumber; // 返程航班号
    private Date departureDepartureTime; // 起飞时间
    private Date tourStartDate; // 旅行开始日期
    private Date tourEndDate; // 旅行结束日期
    private Date pickupDate; // 接机日期
    private Date dropoffDate; // 送机日期
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
    private String roomType; // 房间类型
    private Integer hotelRoomCount; // 酒店房间数量
    private String roomDetails; // 酒店房间详情
    private String specialRequests; // 特殊要求
    private String itineraryDetails; // 行程详情
    private String status; // 订单状态
    private String paymentStatus; // 支付状态
    private BigDecimal totalPrice; // 总价格
    private Date createdAt; // 创建时间
    private Date updatedAt; // 更新时间
    
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