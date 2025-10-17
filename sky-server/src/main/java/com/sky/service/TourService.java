package com.sky.service;

import com.sky.dto.TourDTO;
import com.sky.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 通用旅游服务接口
 */
public interface TourService {

    /**
     * 获取所有旅游产品
     * @param params 查询参数
     * @return 分页结果
     */
    PageResult getAllTours(Map<String, Object> params);

    /**
     * 根据ID获取旅游产品
     * @param id 旅游产品ID
     * @return 旅游产品信息
     */
    TourDTO getTourById(Integer id);

    /**
     * 根据ID和类型获取旅游产品
     * @param id 旅游产品ID
     * @param tourType 旅游类型（day或group）
     * @return 旅游产品信息
     */
    TourDTO getTourById(Integer id, String tourType);

    /**
     * 搜索旅游产品
     * @param params 查询参数
     * @return 旅游产品列表
     */
    List<TourDTO> searchTours(Map<String, Object> params);

    /**
     * 获取热门旅游产品
     * @param limit 限制数量
     * @return 热门旅游产品列表
     */
    List<TourDTO> getHotTours(Integer limit);

    /**
     * 获取推荐旅游产品
     * @param limit 限制数量
     * @return 推荐旅游产品列表
     */
    List<TourDTO> getRecommendedTours(Integer limit);

    /**
     * 获取热门一日游
     * @param limit 限制数量
     * @return 热门一日游列表
     */
    List<TourDTO> getHotDayTours(Integer limit);

    /**
     * 获取热门跟团游
     * @param limit 限制数量
     * @return 热门跟团游列表
     */
    List<TourDTO> getHotGroupTours(Integer limit);

    /**
     * 获取推荐一日游
     * @param limit 限制数量
     * @return 推荐一日游列表
     */
    List<TourDTO> getRecommendedDayTours(Integer limit);

    /**
     * 获取推荐跟团游
     * @param limit 限制数量
     * @return 推荐跟团游列表
     */
    List<TourDTO> getRecommendedGroupTours(Integer limit);

    /**
     * 获取适合人群选项
     * @return 适合人群选项列表
     */
    List<Map<String, Object>> getSuitableForOptions();

    /**
     * 根据旅游ID和类型获取适合人群
     * @param tourId 旅游ID
     * @param tourType 旅游类型 (day_tour/group_tour)
     * @return 适合人群列表
     */
    List<Map<String, Object>> getSuitableForByTourId(Integer tourId, String tourType);

    /**
     * 获取基于订单统计的热门产品
     * @param days 统计天数
     * @param limit 限制数量
     * @return 热门产品列表（包含一日游和多日游）
     */
    List<Map<String, Object>> getPopularToursByOrders(Integer days, Integer limit);
}