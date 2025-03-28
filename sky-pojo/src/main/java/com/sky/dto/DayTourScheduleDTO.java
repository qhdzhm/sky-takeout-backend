package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 一日游日程安排数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayTourScheduleDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private Integer dayTourId;
    private LocalDate scheduleDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer availableSeats;
    private Integer status;
    private String remarks;
    private String dayTourName;
    
    // 批量安排的辅助字段
    private List<LocalDate> dates;
} 