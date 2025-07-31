package com.sky.service.impl;

import com.sky.dto.GuideAvailabilityDTO;
import com.sky.mapper.GuideAvailabilityMapper;
import com.sky.service.GuideAvailabilityService;
import com.sky.vo.GuideAvailabilityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GuideAvailabilityServiceImpl implements GuideAvailabilityService {

    @Autowired
    private GuideAvailabilityMapper guideAvailabilityMapper;

    /**
     * 获取导游可用性列表
     */
    @Override
    public List<GuideAvailabilityVO> getGuideAvailability(Integer guideId, LocalDate startDate, LocalDate endDate) {
        // 如果没有提供日期范围，默认查询当前月份
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1); // 当前月份第一天
        }
        if (endDate == null) {
            endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()); // 当前月份最后一天
        }
        
        return guideAvailabilityMapper.getGuideAvailability(guideId, startDate, endDate);
    }

    /**
     * 设置导游可用性
     */
    @Override
    public void setGuideAvailability(GuideAvailabilityDTO guideAvailabilityDTO) {
        // 为必需字段提供默认值
        if (guideAvailabilityDTO.getAvailableStartTime() == null) {
            guideAvailabilityDTO.setAvailableStartTime(java.time.LocalTime.of(8, 0)); // 默认8:00开始
        }
        if (guideAvailabilityDTO.getAvailableEndTime() == null) {
            guideAvailabilityDTO.setAvailableEndTime(java.time.LocalTime.of(18, 0)); // 默认18:00结束
        }
        if (guideAvailabilityDTO.getMaxGroups() == null) {
            guideAvailabilityDTO.setMaxGroups(1); // 默认最大接团数为1
        }
        
        // 检查是否已存在该日期的设置
        GuideAvailabilityVO existing = guideAvailabilityMapper.getGuideAvailabilityByDate(
                guideAvailabilityDTO.getGuideId(), guideAvailabilityDTO.getDate());
        
        if (existing != null) {
            // 更新现有记录
            guideAvailabilityMapper.updateGuideAvailability(guideAvailabilityDTO);
        } else {
            // 插入新记录
            guideAvailabilityMapper.insertGuideAvailability(guideAvailabilityDTO);
        }
    }

    /**
     * 批量设置导游可用性
     */
    @Override
    public void batchSetGuideAvailability(GuideAvailabilityDTO guideAvailabilityDTO) {
        LocalDate startDate = guideAvailabilityDTO.getStartDate();
        LocalDate endDate = guideAvailabilityDTO.getEndDate();
        Boolean excludeWeekends = guideAvailabilityDTO.getExcludeWeekends();
        
        List<GuideAvailabilityDTO> batchList = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 如果排除周末，跳过周六和周日
            if (excludeWeekends != null && excludeWeekends) {
                if (currentDate.getDayOfWeek().getValue() == 6 || currentDate.getDayOfWeek().getValue() == 7) {
                    currentDate = currentDate.plusDays(1);
                    continue;
                }
            }
            
            GuideAvailabilityDTO dto = new GuideAvailabilityDTO();
            BeanUtils.copyProperties(guideAvailabilityDTO, dto);
            dto.setDate(currentDate);
            
            // 为必需字段提供默认值
            if (dto.getAvailableStartTime() == null) {
                dto.setAvailableStartTime(java.time.LocalTime.of(8, 0)); // 默认8:00开始
            }
            if (dto.getAvailableEndTime() == null) {
                dto.setAvailableEndTime(java.time.LocalTime.of(18, 0)); // 默认18:00结束
            }
            if (dto.getMaxGroups() == null) {
                dto.setMaxGroups(1); // 默认最大接团数为1
            }
            
            batchList.add(dto);
            
            currentDate = currentDate.plusDays(1);
        }
        
        // 批量插入或更新
        for (GuideAvailabilityDTO dto : batchList) {
            setGuideAvailability(dto);
        }
    }

    /**
     * 删除导游可用性设置
     */
    @Override
    public void deleteGuideAvailability(Integer guideId, LocalDate date) {
        guideAvailabilityMapper.deleteGuideAvailability(guideId, date);
    }
} 