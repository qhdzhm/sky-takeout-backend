package com.sky.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VehicleAvailabilityDTO implements Serializable {

    private Long vehicleId;
    
    private LocalDate availableDate;
    
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    private String status; // available, maintenance, assigned, unavailable
    
    private Integer fuelLevel;
    
    private String currentLocation;
    
    private String notes;
    
    // 批量设置相关字段
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Boolean excludeWeekends;
} 