package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 一日游日程安排实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayTourSchedule implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Integer id;
    
    /** 一日游ID */
    private Integer dayTourId;
    
    /** 日期 */
    private LocalDate scheduleDate;
    
    /** 可用座位数 */
    private Integer availableSeats;
    
    /** 状态 (0:未开放, 1:开放预订, 2:已满座, 3:已结束) */
    private Integer status;
    
    /** 备注 */
    private String remarks;
} 