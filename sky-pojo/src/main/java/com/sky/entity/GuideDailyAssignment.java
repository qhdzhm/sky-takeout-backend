package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 导游每日分配实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideDailyAssignment {
    
    private Long id;
    
    /**
     * 分配日期
     */
    private LocalDate assignmentDate;
    
    /**
     * 地点/目的地
     */
    private String location;
    
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
     * 开始时间
     */
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    private LocalTime endTime;
    
    /**
     * 预计时长(分钟)
     */
    private Integer estimatedDuration;
    
    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;
    
    /**
     * 实际结束时间
     */
    private LocalDateTime actualEndTime;
    
    /**
     * 分配状态
     */
    private String assignmentStatus;
    
    /**
     * 优先级(1-5)
     */
    private Integer priority;
    
    /**
     * 天气条件
     */
    private String weatherConditions;
    
    /**
     * 路线备注
     */
    private String routeNotes;
    
    /**
     * 紧急联系人
     */
    private String emergencyContact;
    
    /**
     * 成本预估
     */
    private Double costEstimate;
    
    /**
     * 备注
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
    
    /**
     * 关联的行程排序ID列表(JSON格式)
     */
    private String tourScheduleOrderIds;
} 