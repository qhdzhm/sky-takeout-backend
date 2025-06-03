package com.sky.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 导游分配VO
 */
@Data
public class GuideAssignmentVO {
    
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
     * 导游信息
     */
    private GuideInfo guide;
    
    /**
     * 车辆信息
     */
    private VehicleInfo vehicle;
    
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
     * 订单列表
     */
    private List<GuideAssignmentOrderVO> orders;
    
    /**
     * 行程标题列表
     */
    private List<String> tourTitles;
    
    /**
     * 订单号列表
     */
    private List<String> orderNumbers;
    
    /**
     * 导游信息内部类
     */
    @Data
    public static class GuideInfo {
        private Long guideId;
        private String guideName;
        private String languages;
        private Integer experienceYears;
        private String phone;
        private String email;
    }
    
    /**
     * 车辆信息内部类
     */
    @Data
    public static class VehicleInfo {
        private Long vehicleId;
        private String vehicleType;
        private String licensePlate;
        private Integer seatCount;
        private String location;
        private String notes;
    }
} 