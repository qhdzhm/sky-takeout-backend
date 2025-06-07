package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 行程排序数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourScheduleOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Integer id;

    /**
     * 订单ID
     */
    private Integer bookingId;

    /**
     * 第几天（固定顺序）
     */
    private Integer dayNumber;

    /**
     * 一日游或团队游ID
     */
    private Integer tourId;

    /**
     * 产品类型
     */
    private String tourType;

    /**
     * 实际行程日期
     */
    private LocalDate tourDate;

    /**
     * 行程标题
     */
    private String title;

    /**
     * 行程描述
     */
    private String description;

    /**
     * 显示顺序
     */
    private Integer displayOrder;
    
    /**
     * 订单号
     */
    private String orderNumber;
    
    /**
     * 成人人数
     */
    private Integer adultCount;
    
    /**
     * 儿童人数
     */
    private Integer childCount;
    
    /**
     * 联系人姓名
     */
    private String contactPerson;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 接送地点
     */
    private String pickupLocation;
    
    /**
     * 送达地点
     */
    private String dropoffLocation;
    
    /**
     * 特殊要求/备注
     */
    private String specialRequests;
    
    /**
     * 行李数量
     */
    private Integer luggageCount;
    
    /**
     * 酒店等级
     */
    private String hotelLevel;
    
    /**
     * 房间类型
     */
    private String roomType;
    
    /**
     * 酒店房间数量
     */
    private Integer hotelRoomCount;
    
    /**
     * 酒店入住日期
     */
    private LocalDate hotelCheckInDate;
    
    /**
     * 酒店退房日期
     */
    private LocalDate hotelCheckOutDate;
    
    /**
     * 房间详情
     */
    private String roomDetails;
    
    /**
     * 到达航班号
     */
    private String flightNumber;
    
    /**
     * 到达航班起飞时间
     */
    private LocalDateTime arrivalDepartureTime;
    
    /**
     * 到达航班降落时间
     */
    private LocalDateTime arrivalLandingTime;
    
    /**
     * 返程航班号
     */
    private String returnFlightNumber;
    
    /**
     * 返程航班起飞时间
     */
    private LocalDateTime departureDepartureTime;
    
    /**
     * 返程航班降落时间
     */
    private LocalDateTime departureLandingTime;
    
    /**
     * 行程开始日期
     */
    private LocalDate tourStartDate;
    
    /**
     * 行程结束日期
     */
    private LocalDate tourEndDate;
    
    /**
     * 接客日期
     */
    private LocalDate pickupDate;
    
    /**
     * 送客日期
     */
    private LocalDate dropoffDate;
    
    /**
     * 乘客联系方式
     */
    private String passengerContact;
    
    /**
     * 行程详情
     */
    private String itineraryDetails;
    
    /**
     * 是否首单
     */
    private Boolean isFirstOrder;
    
    /**
     * 是否来自推荐
     */
    private Boolean fromReferral;
    
    /**
     * 推荐码
     */
    private String referralCode;
    
    /**
     * 服务类型
     */
    private String serviceType;
    
    /**
     * 支付状态
     */
    private String paymentStatus;
    
    /**
     * 订单总价
     */
    private BigDecimal totalPrice;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 代理商ID
     */
    private Integer agentId;
    
    /**
     * 操作员ID
     */
    private Long operatorId;
    
    /**
     * 预订日期
     */
    private LocalDateTime bookingDate;
    
    /**
     * 团队大小
     */
    private Integer groupSize;
    
    /**
     * 订单状态
     */
    private String status;
    
    /**
     * 旅游产品名称
     */
    private String tourName;
    
    /**
     * 旅游目的地
     */
    private String tourLocation;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 