package com.sky.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class GuideAvailabilityDTO implements Serializable {

    private Integer guideId;
    
    private LocalDate date;
    
    private LocalTime availableStartTime;
    
    private LocalTime availableEndTime;
    
    private String status; // available, busy, off, sick (匹配数据库enum类型)
    
    private Integer maxGroups;
    
    private String notes;
    
    // 批量设置相关字段
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Boolean excludeWeekends;
} 