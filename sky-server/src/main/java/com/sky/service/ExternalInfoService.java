package com.sky.service;

/**
 * 外部信息服务接口
 * 负责整合各种外部API，为AI助手提供实时信息
 */
public interface ExternalInfoService {
    
    /**
     * 获取天气信息
     * @param cityName 城市名称
     * @return 天气信息描述
     */
    String getWeatherInfo(String cityName);
    
    /**
     * 获取汇率信息
     * @param fromCurrency 源货币
     * @param toCurrency 目标货币
     * @return 汇率信息描述
     */
    String getExchangeRate(String fromCurrency, String toCurrency);
    
    /**
     * 获取旅游相关新闻
     * @param keyword 搜索关键词
     * @return 新闻信息描述
     */
    String getTravelNews(String keyword);
    
    /**
     * 获取交通信息
     * @param location 位置信息
     * @return 交通信息描述
     */
    String getTrafficInfo(String location);
    
    /**
     * 智能搜索信息
     * @param query 搜索查询
     * @param context 上下文信息
     * @return 搜索结果描述
     */
    String smartSearch(String query, String context);
    
    /**
     * 获取旅游攻略信息
     * @param destination 目的地
     * @return 攻略信息描述
     */
    String getTravelGuide(String destination);
} 