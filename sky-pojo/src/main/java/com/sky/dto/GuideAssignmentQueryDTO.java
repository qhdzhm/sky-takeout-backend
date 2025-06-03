package com.sky.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 导游分配查询DTO
 */
@Data
public class GuideAssignmentQueryDTO {
    
    /**
     * 页码
     */
    private int page = 1;
    
    /**
     * 每页记录数
     */
    private int pageSize = 10;
    
    /**
     * 分配日期开始
     */
    private LocalDate startDate;
    
    /**
     * 分配日期结束
     */
    private LocalDate endDate;
    
    /**
     * 地点
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
     * 分配状态
     */
    private String assignmentStatus;
    
    /**
     * 导游姓名
     */
    private String guideName;
    
    /**
     * 车牌号
     */
    private String licensePlate;
} 