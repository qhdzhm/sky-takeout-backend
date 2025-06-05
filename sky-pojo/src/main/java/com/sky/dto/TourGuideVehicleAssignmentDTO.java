package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 导游车辆游客分配DTO
 */
@Data
public class TourGuideVehicleAssignmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分配日期
     */
    private LocalDate assignmentDate;

    /**
     * 目的地/地点
     */
    private String destination;

    /**
     * 导游ID
     */
    private Long guideId;

    /**
     * 车辆ID
     */
    private Long vehicleId;

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
     * 联系方式
     */
    private String contactPhone;

    /**
     * 联系人姓名
     */
    private String contactPerson;

    /**
     * 接送方式
     */
    private String pickupMethod;

    /**
     * 接送地点
     */
    private String pickupLocation;

    /**
     * 送达地点
     */
    private String dropoffLocation;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 下一站信息
     */
    private String nextDestination;

    /**
     * 关联的订单ID列表
     */
    private List<Long> bookingIds;

    /**
     * 关联的行程排序ID列表
     */
    private List<Long> tourScheduleOrderIds;

    /**
     * 游客详细信息列表
     */
    private List<PassengerInfo> passengerDetails;

    /**
     * 特殊要求汇总
     */
    private String specialRequirements;

    /**
     * 饮食限制汇总
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
     * 游客信息内部类
     */
    @Data
    public static class PassengerInfo {
        private String name;
        private Integer age;
        private String requirements;
        private String phoneNumber;
        private String specialNeeds;
    }
} 