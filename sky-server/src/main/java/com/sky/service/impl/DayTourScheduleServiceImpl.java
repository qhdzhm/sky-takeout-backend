package com.sky.service.impl;

import com.sky.dto.DayTourScheduleDTO;
import com.sky.entity.DayTourSchedule;
import com.sky.exception.BusinessException;
import com.sky.mapper.DayTourScheduleMapper;
import com.sky.service.DayTourScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 一日游日程安排服务实现类
 */
@Service
@Slf4j
public class DayTourScheduleServiceImpl implements DayTourScheduleService {

    @Autowired
    private DayTourScheduleMapper dayTourScheduleMapper;

    /**
     * 根据一日游ID获取所有日程安排
     *
     * @param dayTourId 一日游ID
     * @return 日程安排列表
     */
    @Override
    public List<DayTourSchedule> getByDayTourId(Integer dayTourId) {
        return dayTourScheduleMapper.getByDayTourId(dayTourId);
    }

    /**
     * 保存日程安排
     *
     * @param dayTourScheduleDTO 日程安排DTO
     */
    @Override
    @Transactional
    public void save(DayTourScheduleDTO dayTourScheduleDTO) {
        // 检查是否是日期范围
        if (dayTourScheduleDTO.getStartDate() != null && dayTourScheduleDTO.getEndDate() != null) {
            // 处理日期范围
            LocalDate startDate = dayTourScheduleDTO.getStartDate();
            LocalDate endDate = dayTourScheduleDTO.getEndDate();
            
            if (endDate.isBefore(startDate)) {
                throw new BusinessException("结束日期不能早于开始日期");
            }
            
            // 计算日期差
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            
            // 对日期范围内的每一天创建日程安排
            for (int i = 0; i <= daysBetween; i++) {
                LocalDate currentDate = startDate.plusDays(i);
                DayTourSchedule schedule = createScheduleFromDTO(dayTourScheduleDTO);
                schedule.setScheduleDate(currentDate);
                dayTourScheduleMapper.insert(schedule);
            }
        } else if (dayTourScheduleDTO.getDates() != null && !dayTourScheduleDTO.getDates().isEmpty()) {
            // 处理多个日期的列表
            for (LocalDate date : dayTourScheduleDTO.getDates()) {
                DayTourSchedule schedule = createScheduleFromDTO(dayTourScheduleDTO);
                schedule.setScheduleDate(date);
                dayTourScheduleMapper.insert(schedule);
            }
        } else if (dayTourScheduleDTO.getScheduleDate() != null) {
            // 处理单个日期
            DayTourSchedule schedule = createScheduleFromDTO(dayTourScheduleDTO);
            schedule.setScheduleDate(dayTourScheduleDTO.getScheduleDate());
            dayTourScheduleMapper.insert(schedule);
        } else {
            throw new BusinessException("必须提供有效的日期信息");
        }
    }

    /**
     * 从DTO创建日程安排实体
     *
     * @param dto 日程安排DTO
     * @return 日程安排实体
     */
    private DayTourSchedule createScheduleFromDTO(DayTourScheduleDTO dto) {
        DayTourSchedule schedule = new DayTourSchedule();
        schedule.setDayTourId(dto.getDayTourId());
        schedule.setAvailableSeats(dto.getAvailableSeats());
        schedule.setStatus(dto.getStatus() != null ? dto.getStatus() : 1); // 默认为开放预订状态
        schedule.setRemarks(dto.getRemarks());
        return schedule;
    }

    /**
     * 根据ID删除日程安排
     *
     * @param scheduleId 日程安排ID
     */
    @Override
    public void deleteById(Integer scheduleId) {
        dayTourScheduleMapper.deleteById(scheduleId);
    }

    /**
     * 根据一日游ID删除所有日程安排
     *
     * @param dayTourId 一日游ID
     */
    @Override
    public void deleteByDayTourId(Integer dayTourId) {
        dayTourScheduleMapper.deleteByDayTourId(dayTourId);
    }

    /**
     * 获取指定日期范围内的日程安排
     *
     * @param dayTourId 一日游ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日程安排列表
     */
    @Override
    public List<DayTourSchedule> getSchedulesInDateRange(Integer dayTourId, LocalDate startDate, LocalDate endDate) {
        return dayTourScheduleMapper.getSchedulesInDateRange(dayTourId, startDate, endDate);
    }

    /**
     * 根据ID获取日程安排
     *
     * @param scheduleId 日程安排ID
     * @return 日程安排实体
     */
    @Override
    public DayTourSchedule getById(Integer scheduleId) {
        return dayTourScheduleMapper.getById(scheduleId);
    }
} 