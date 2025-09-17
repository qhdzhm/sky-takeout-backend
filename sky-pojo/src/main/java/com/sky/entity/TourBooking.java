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
 * 旅游订单实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourBooking implements Serializable {

    private static final long serialVersionUID = 1L;

    // 订单ID
    private Integer bookingId;
    
    // 订单号
    private String orderNumber;
    
    // 旅游项目ID
    private Integer tourId;
    
    // 旅游类型（day_tour：一日游，group_tour：团队游）
    private String tourType;
    
    // 用户ID
    private Integer userId;
    
    // 代理商ID
    private Integer agentId;
    
    // 操作员ID（如果是操作员下单）
    private Long operatorId;
    
    // 预订日期
    private LocalDateTime bookingDate;
    
    // 航班号
    private String flightNumber;
    
    // 到达航班起飞时间
    private LocalDateTime arrivalDepartureTime;
    
    // 到达航班降落时间
    private LocalDateTime arrivalLandingTime;
    
    // 返程航班号
    private String returnFlightNumber;
    
    // 返程航班起飞时间
    private LocalDateTime departureDepartureTime;
    
    // 返程航班降落时间
    private LocalDateTime departureLandingTime;
    
    // 行程开始日期
    private LocalDate tourStartDate;
    
    // 行程结束日期
    private LocalDate tourEndDate;
    
    // 接客日期
    private LocalDate pickupDate;
    
    // 送客日期
    private LocalDate dropoffDate;
    
    // 接客地点
    private String pickupLocation;
    
    // 送客地点
    private String dropoffLocation;
    
    // 服务类型
    private String serviceType;
    
    // 团队规模
    private Integer groupSize;
    
    // 成人数量
    private Integer adultCount;
    
    // 儿童数量
    private Integer childCount;
    
    // 行李数量
    private Integer luggageCount;
    
    // 乘客联系方式
    private String passengerContact;
    
    // 联系人
    private String contactPerson;
    
    // 联系电话
    private String contactPhone;
    
    // 酒店等级
    private String hotelLevel;
    
    // 房间类型
    private String roomType;
    
    // 酒店间房数量
    private Integer hotelRoomCount;
    
    // 酒店入住日期
    private LocalDate hotelCheckInDate;
    
    // 酒店退房日期
    private LocalDate hotelCheckOutDate;
    
    // 每个房间的样式
    private String roomDetails;
    
    // 特殊请求
    private String specialRequests;
    
    // 行程详情
    private String itineraryDetails;
    
    // 订单状态（pending：待处理，confirmed：已确认，cancelled：已取消，completed：已完成）
    private String status;
    
    // 支付状态（unpaid：未支付，partial：部分支付，paid：已支付）
    private String paymentStatus;
    
    // 总价
    private BigDecimal totalPrice;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
    
    // 是否为用户首单
    private Integer isFirstOrder;
    
    // 是否来自推荐
    private Integer fromReferral;
    
    // 推荐码
    private String referralCode;
    
    // 用户选择的可选项目（JSON字符串）
    private String selectedOptionalTours;
    
    // 用户端是否隐藏（软删除标记）
    private Boolean userHidden;
    
    // 用户隐藏时间
    private LocalDateTime userHiddenAt;
    
    // ====== 操作员分工管理字段 ======
    
    // 分配给的酒店专员ID
    private Long assignedOperatorId;
    
    // 分配时间
    private LocalDateTime assignedAt;
    
    // 分配人ID（排团主管）
    private Long assignedBy;
    
    // 分配状态（unassigned：未分配，assigned：已分配，transferred：已转移）
    private String assignmentStatus;
    
    // ====== 团型管理字段 ======
    
    // 团型类型（standard：普通团，small_12：12人团，small_14：14人团，luxury：精品团）
    private String groupType;
    
    // 团型人数限制
    private Integer groupSizeLimit;
    
    // 团型价格（如有调整）
    private BigDecimal groupTypePrice;
    
    // ====== 关联查询字段（非数据库字段） ======
    
    // 分配的操作员姓名
    private String assignedOperatorName;
    
    // 分配人姓名
    private String assignedByName;
}