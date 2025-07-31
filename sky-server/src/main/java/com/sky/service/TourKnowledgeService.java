package com.sky.service;

import com.sky.dto.RegionDTO;
import com.sky.dto.TourDTO;
import com.sky.vo.TourRecommendationResponse;

import java.util.List;
import java.util.Map;

/**
 * 旅游知识服务接口
 */
public interface TourKnowledgeService {

    /**
     * 获取产品推荐
     * @param userMessage 用户消息
     * @param agentId 代理商ID
     * @return 推荐响应
     */
    TourRecommendationResponse getProductRecommendations(String userMessage, Long agentId);

    /**
     * 根据关键词搜索旅游产品
     * @param keywords 关键词
     * @param tourType 产品类型
     * @param agentId 代理商ID
     * @return 产品列表
     */
    List<TourDTO> searchToursByKeywords(String keywords, String tourType, Long agentId);

    /**
     * 获取热门产品
     * @param limit 限制数量
     * @param agentId 代理商ID
     * @return 产品列表
     */
    List<TourDTO> getHotTours(Integer limit, Long agentId);

    /**
     * 根据地区获取产品
     * @param regionName 地区名称
     * @param tourType 产品类型
     * @param agentId 代理商ID
     * @return 产品列表
     */
    List<TourDTO> getToursByRegion(String regionName, String tourType, Long agentId);

    /**
     * 根据行程天数获取产品
     * @param days 天数
     * @param agentId 代理商ID
     * @return 产品列表
     */
    List<TourDTO> getToursByDuration(Integer days, Long agentId);

    /**
     * 根据主题获取产品
     * @param theme 主题
     * @param agentId 代理商ID
     * @return 产品列表
     */
    List<TourDTO> getToursByTheme(String theme, Long agentId);

    /**
     * 获取所有地区
     * @return 地区列表
     */
    List<RegionDTO> getAllRegions();

    /**
     * 获取所有主题
     * @return 主题列表
     */
    List<Map<String, Object>> getAllThemes();

    /**
     * 生成AI系统提示词
     * @return 提示词
     */
    String generateAISystemPrompt();

    /**
     * 根据关键词搜索旅游知识
     * @param keyword 关键词
     * @return 搜索结果
     */
    String searchByKeyword(String keyword);
} 