package com.sky.mapper;

import com.sky.dto.GuideAvailabilityDTO;
import com.sky.vo.GuideAvailabilityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface GuideAvailabilityMapper {

    /**
     * 获取导游可用性列表
     */
    List<GuideAvailabilityVO> getGuideAvailability(@Param("guideId") Integer guideId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 根据日期获取导游可用性
     */
    GuideAvailabilityVO getGuideAvailabilityByDate(@Param("guideId") Integer guideId, 
                                                   @Param("date") LocalDate date);

    /**
     * 插入导游可用性
     */
    void insertGuideAvailability(GuideAvailabilityDTO guideAvailabilityDTO);

    /**
     * 更新导游可用性
     */
    void updateGuideAvailability(GuideAvailabilityDTO guideAvailabilityDTO);

    /**
     * 删除导游可用性
     */
    void deleteGuideAvailability(@Param("guideId") Integer guideId, @Param("date") LocalDate date);
} 