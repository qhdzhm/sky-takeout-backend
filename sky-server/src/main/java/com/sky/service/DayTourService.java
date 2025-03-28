package com.sky.service;

import com.sky.dto.DayTourDTO;
import com.sky.dto.DayTourPageQueryDTO;
import com.sky.entity.*;
import com.sky.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 一日游服务接口
 */
public interface DayTourService {

    /**
     * 分页查询
     * @param dto
     * @return
     */
    com.sky.result.PageResult pageQuery(DayTourPageQueryDTO dto);

    /**
     * 根据ID查询一日游详情
     * @param id
     * @return
     */
    DayTour getById(Integer id);

    /**
     * 新增一日游
     * @param dayTourDTO
     * @return
     */
    void save(DayTourDTO dayTourDTO);

    /**
     * 修改一日游
     * @param dayTourDTO
     */
    void update(DayTourDTO dayTourDTO);

    /**
     * 删除一日游
     * @param id
     */
    void deleteById(Integer id);

    /**
     * 上下架一日游
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Integer id);

    /**
     * 获取一日游亮点
     * @param dayTourId
     * @return
     */
    List<DayTourHighlight> getHighlightsByDayTourId(Integer dayTourId);

    /**
     * 保存一日游亮点
     * @param dayTourHighlight
     */
    void saveHighlight(DayTourHighlight dayTourHighlight);

    /**
     * 删除一日游亮点
     * @param id
     */
    void deleteHighlight(Integer id);

    /**
     * 获取一日游包含项
     * @param dayTourId
     * @return
     */
    List<DayTourInclusion> getInclusionsByDayTourId(Integer dayTourId);

    /**
     * 保存一日游包含项
     * @param dayTourInclusion
     */
    void saveInclusion(DayTourInclusion dayTourInclusion);

    /**
     * 删除一日游包含项
     * @param id
     */
    void deleteInclusion(Integer id);

    /**
     * 获取一日游不包含项
     * @param dayTourId
     * @return
     */
    List<DayTourExclusion> getExclusionsByDayTourId(Integer dayTourId);

    /**
     * 保存一日游不包含项
     * @param dayTourExclusion
     */
    void saveExclusion(DayTourExclusion dayTourExclusion);

    /**
     * 删除一日游不包含项
     * @param id
     */
    void deleteExclusion(Integer id);

    /**
     * 获取一日游常见问题
     * @param dayTourId
     * @return
     */
    List<DayTourFaq> getFaqsByDayTourId(Integer dayTourId);

    /**
     * 保存一日游常见问题
     * @param dayTourFaq
     */
    void saveFaq(DayTourFaq dayTourFaq);

    /**
     * 删除一日游常见问题
     * @param id
     */
    void deleteFaq(Integer id);

    /**
     * 获取一日游行程
     * @param dayTourId
     * @return
     */
    List<DayTourItinerary> getItinerariesByDayTourId(Integer dayTourId);

    /**
     * 保存一日游行程
     * @param dayTourItinerary
     */
    void saveItinerary(DayTourItinerary dayTourItinerary);

    /**
     * 删除一日游行程
     * @param id
     */
    void deleteItinerary(Integer id);

    /**
     * 获取一日游旅行提示
     * @param dayTourId
     * @return
     */
    List<DayTourTip> getTipsByDayTourId(Integer dayTourId);

    /**
     * 保存一日游旅行提示
     * @param dayTourTip
     */
    void saveTip(DayTourTip dayTourTip);

    /**
     * 删除一日游旅行提示
     * @param id
     */
    void deleteTip(Integer id);

    /**
     * 获取一日游日程安排
     * @param dayTourId
     * @return
     */
    List<DayTourSchedule> getSchedulesByDayTourId(Integer dayTourId);

    /**
     * 保存一日游日程安排
     * @param dayTourSchedule
     */
    void saveSchedule(DayTourSchedule dayTourSchedule);

    /**
     * 删除一日游日程安排
     * @param scheduleId
     */
    void deleteSchedule(Integer scheduleId);

    /**
     * 获取所有一日游
     * @param params 查询参数
     * @return 分页结果
     */
    PageResult getAllDayTours(Map<String, Object> params);

    /**
     * 根据ID获取一日游详情
     * @param id 一日游ID
     * @return 一日游详情
     */
    DayTourDTO getDayTourById(Integer id);

    /**
     * 获取一日游行程安排
     * @param tourId 一日游ID
     * @param params 查询参数
     * @return 行程安排列表
     */
    List<Map<String, Object>> getDayTourSchedules(Integer tourId, Map<String, Object> params);

    /**
     * 获取一日游主题列表
     * @return 主题列表
     */
    List<Map<String, Object>> getDayTourThemes();

    /**
     * 根据一日游ID获取主题列表
     * @param tourId 一日游ID
     * @return 主题列表
     */
    List<Map<String, Object>> getDayTourThemesByTourId(Integer tourId);

    /**
     * 获取一日游图片列表
     * @param dayTourId 一日游ID
     * @return 图片列表
     */
    List<DayTourImage> getImagesByDayTourId(Integer dayTourId);
} 