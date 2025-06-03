package com.sky.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 导游分配订单VO
 */
@Data
public class GuideAssignmentOrderVO {
    
    private Long id;
    
    /**
     * 分配ID
     */
    private Long assignmentId;
    
    /**
     * 订单ID
     */
    private Long bookingId;
    
    /**
     * 行程排序ID
     */
    private Integer tourScheduleOrderId;
    
    /**
     * 订单号
     */
    private String orderNumber;
    
    /**
     * 客户姓名
     */
    private String customerNames;
    
    /**
     * 人数
     */
    private Integer peopleCount;
    
    /**
     * 联系方式
     */
    private String contactPhone;
    
    /**
     * 接客时间
     */
    private LocalTime pickupTime;
    
    /**
     * 送客时间
     */
    private LocalTime dropoffTime;
    
    /**
     * 接客信息
     */
    private String pickupInfo;
    
    /**
     * 送客信息
     */
    private String dropoffInfo;
    
    /**
     * 特殊要求
     */
    private String specialRequirements;
    
    /**
     * 行李详情
     */
    private String luggageDetails;
    
    /**
     * 饮食限制
     */
    private String dietaryRestrictions;
    
    /**
     * 是否需要行动辅助
     */
    private Boolean mobilityAssistance;
    
    /**
     * 语言偏好
     */
    private String languagePreference;
    
    /**
     * 订单状态
     */
    private String orderStatus;
    
    /**
     * 下一站目的地
     */
    private String nextDestination;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
} 