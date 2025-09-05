package com.sky.service;

import com.sky.vo.DashboardOverviewVO;
import com.sky.vo.ProductStatisticsVO;

import java.time.LocalDate;
import java.util.Map;

/**
 * 仪表盘数据服务接口
 */
public interface DashboardService {

    /**
     * 获取仪表盘概览数据
     * @return 概览数据
     */
    DashboardOverviewVO getOverviewData();

    /**
     * 获取产品统计数据
     * @return 产品统计数据
     */
    ProductStatisticsVO getProductStatistics();

    /**
     * 获取指定日期范围的营业数据
     * @param begin 开始日期
     * @param end 结束日期
     * @return 营业数据
     */
    Map<String, Object> getBusinessDataByDateRange(LocalDate begin, LocalDate end);

    /**
     * 获取最近7天的趋势数据
     * @return 趋势数据（用于图表）
     */
    Map<String, Object> getSevenDaysTrendData();

    /**
     * 获取订单状态分布数据
     * @return 订单状态统计
     */
    Map<String, Integer> getOrderStatusDistribution();

    /**
     * 获取用户类型分布数据
     * @return 用户类型统计
     */
    Map<String, Integer> getUserTypeDistribution();

    /**
     * 获取热门目的地统计
     * @return 热门目的地数据
     */
    Map<String, Object> getPopularDestinations();

    /**
     * 获取代理商表现数据
     * @return 代理商表现统计
     */
    Map<String, Object> getAgentPerformanceData();
}