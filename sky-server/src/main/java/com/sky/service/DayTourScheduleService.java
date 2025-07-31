package com.sky.service;

import com.sky.dto.DayTourScheduleDTO;
import com.sky.entity.DayTourSchedule;

import java.time.LocalDate;
import java.util.List;

/**
 * 一日游日程安排服务接口
 */
public interface DayTourScheduleService {

    /**
     * 根据一日游ID获取所有日程安排
     * 
     * @param dayTourId 一日游ID
     * @return 日程安排列表
     */
    List<DayTourSchedule> getByDayTourId(Integer dayTourId);

    /**
     * 保存日程安排
     * 
     * @param dayTourScheduleDTO 日程安排DTO
     */
    void save(DayTourScheduleDTO dayTourScheduleDTO);

    /**
     * 根据ID删除日程安排
     * 
     * @param scheduleId 日程安排ID
     */
    void deleteById(Integer scheduleId);

    /**
     * 根据一日游ID删除所有日程安排
     * 
     * @param dayTourId 一日游ID
     */
    void deleteByDayTourId(Integer dayTourId);

    /**
     * 获取指定日期范围内的日程安排
     * 
     * @param dayTourId 一日游ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日程安排列表
     */
    List<DayTourSchedule> getSchedulesInDateRange(Integer dayTourId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据ID获取日程安排
     * 
     * @param scheduleId 日程安排ID
     * @return 日程安排实体
     */
    DayTourSchedule getById(Integer scheduleId);
} 