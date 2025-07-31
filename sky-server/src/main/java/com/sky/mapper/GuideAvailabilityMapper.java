package com.sky.mapper;

import com.sky.dto.GuideAvailabilityDTO;
import com.sky.vo.GuideAvailabilityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 导游可用性Mapper接口
 */
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

    /**
     * 更新导游可用性状态
     */
    void updateAvailability(@Param("guideId") Long guideId,
                           @Param("date") LocalDate date,
                           @Param("startTime") LocalTime startTime,
                           @Param("endTime") LocalTime endTime,
                           @Param("isAvailable") Boolean isAvailable,
                           @Param("currentGroups") Integer currentGroups);

    /**
     * 检查导游在指定时间段是否可用
     */
    Boolean checkAvailability(@Param("guideId") Long guideId,
                             @Param("date") LocalDate date,
                             @Param("startTime") LocalTime startTime,
                             @Param("endTime") LocalTime endTime);

    /**
     * 增加导游当前团数
     */
    void incrementCurrentGroups(@Param("guideId") Long guideId,
                               @Param("date") LocalDate date);

    /**
     * 减少导游当前团数
     */
    void decrementCurrentGroups(@Param("guideId") Long guideId,
                               @Param("date") LocalDate date);

    /**
     * 重置导游可用性状态（取消分配时用）
     */
    void resetAvailability(@Param("guideId") Long guideId,
                          @Param("date") LocalDate date);

    /**
     * 确保导游在指定日期有可用性记录，如果不存在则创建
     */
    void ensureAvailabilityRecord(@Param("guideId") Long guideId,
                                 @Param("date") LocalDate date);

    /**
     * 根据日期和时间获取可用导游
     */
    List<GuideAvailabilityVO> getAvailableGuidesByDateTime(@Param("date") LocalDate date,
                                                         @Param("startTime") LocalTime startTime,
                                                         @Param("endTime") LocalTime endTime);
} 