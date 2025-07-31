package com.sky.service;

import com.sky.dto.GuideAvailabilityDTO;
import com.sky.vo.GuideAvailabilityVO;

import java.time.LocalDate;
import java.util.List;

public interface GuideAvailabilityService {

    /**
     * 获取导游可用性列表
     */
    List<GuideAvailabilityVO> getGuideAvailability(Integer guideId, LocalDate startDate, LocalDate endDate);

    /**
     * 设置导游可用性
     */
    void setGuideAvailability(GuideAvailabilityDTO guideAvailabilityDTO);

    /**
     * 批量设置导游可用性
     */
    void batchSetGuideAvailability(GuideAvailabilityDTO guideAvailabilityDTO);

    /**
     * 删除导游可用性设置
     */
    void deleteGuideAvailability(Integer guideId, LocalDate date);
} 