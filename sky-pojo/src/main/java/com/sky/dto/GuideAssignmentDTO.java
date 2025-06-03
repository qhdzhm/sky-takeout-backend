package com.sky.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 导游分配DTO
 */
@Data
public class GuideAssignmentDTO {
    
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
     * 关联的行程排序ID列表
     */
    private List<Integer> tourScheduleOrderIds;
    
    /**
     * 订单详情列表
     */
    private List<GuideAssignmentOrderDTO> orders;
} 