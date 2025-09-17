package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 导游车辆分配表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourGuideVehicleAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 分配日期
     */
    private LocalDate assignmentDate;

    /**
     * 分配开始时间
     */
    private LocalTime startTime;

    /**
     * 分配结束时间
     */
    private LocalTime endTime;

    /**
     * 目的地/行程地点
     */
    private String destination;

    /**
     * 导游ID
     */
    private Long guideId;

    /**
     * 导游姓名（冗余字段，便于查询显示）
     */
    private String guideName;

    /**
     * 车辆ID
     */
    private Long vehicleId;

    /**
     * 车牌号（冗余字段，便于查询显示）
     */
    private String licensePlate;

    /**
     * 车辆类型（冗余字段）
     */
    private String vehicleType;

    /**
     * 车辆座位数（冗余字段）
     */
    private Integer seatCount;

    /**
     * 总人数
     */
    private Integer totalPeople;

    /**
     * 成人数量
     */
    private Integer adultCount;

    /**
     * 儿童数量
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
     * 接送方式：hotel_pickup-酒店接送, self_pickup-自行前往
     */
    private String pickupMethod;

    /**
     * 接送地点
     */
    private String pickupLocation;

    /**
     * 返回地点
     */
    private String dropoffLocation;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 下一站信息
     */
    private String nextDestination;

    /**
     * 分配状态：confirmed-已确认, in_progress-进行中, completed-已完成, cancelled-已取消
     */
    private String status;

    /**
     * 关联的订单ID列表（JSON格式）
     */
    private String bookingIds;

    /**
     * 关联的预订订单ID列表（JSON格式）- 对应tour_schedule_order表中的booking_id
     */
    private String tourScheduleOrderIds;

    /**
     * 乘客详细信息（JSON格式）
     */
    private String passengerDetails;

    /**
     * 特殊要求
     */
    private String specialRequirements;

    /**
     * 饮食限制
     */
    private String dietaryRestrictions;

    /**
     * 行李信息
     */
    private String luggageInfo;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 语言偏好
     */
    private String languagePreference;

    /**
     * 优先级：1-高, 2-中, 3-低
     */
    private Integer priority;

    // ====== 团型管理字段 ======
    /**
     * 团型类型（standard：普通团，small_12：12人团，small_14：14人团，luxury：精品团）
     */
    private String groupType;
    
    /**
     * 团型人数限制
     */
    private Integer groupSizeLimit;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 创建用户ID
     */
    private Long createdBy;

    /**
     * 更新用户ID
     */
    private Long updatedBy;
} 