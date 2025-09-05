package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * 仪表盘概览数据VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 今日营业概况
    private BigDecimal todayRevenue;        // 今日营业额
    private Integer todayOrders;            // 今日订单数
    private Integer todayNewUsers;          // 今日新用户
    private BigDecimal averageOrderValue;   // 平均订单价值

    // 本月统计
    private BigDecimal monthRevenue;        // 本月营业额
    private Integer monthOrders;            // 本月订单数
    private Integer monthNewUsers;          // 本月新用户
    private Integer monthNewAgents;         // 本月新代理商

    // 订单状态统计
    private Integer pendingOrders;          // 待处理订单
    private Integer confirmedOrders;        // 已确认订单
    private Integer completedOrders;        // 已完成订单
    private Integer cancelledOrders;        // 已取消订单

    // 产品统计
    private Integer totalDayTours;          // 一日游产品总数
    private Integer activeDayTours;         // 活跃一日游产品
    private Integer totalGroupTours;        // 团队游产品总数
    private Integer activeGroupTours;       // 活跃团队游产品

    // 用户和代理商统计
    private Integer totalUsers;             // 用户总数
    private Integer activeUsers;            // 活跃用户数（本月有活动）
    private Integer totalAgents;            // 代理商总数
    private Integer activeAgents;           // 活跃代理商数

    // 资源统计
    private Integer totalVehicles;          // 车辆总数
    private Integer availableVehicles;      // 可用车辆数
    private Integer totalEmployees;         // 员工总数
    private Integer activeEmployees;        // 在职员工数

    // 同比增长率（与上月对比）
    private BigDecimal revenueGrowthRate;   // 营业额增长率
    private BigDecimal orderGrowthRate;     // 订单增长率
    private BigDecimal userGrowthRate;      // 用户增长率

    // 热门产品信息
    private String mostPopularTour;         // 最受欢迎的产品
    private Integer mostPopularTourBookings; // 最受欢迎产品的预订数

    // 最近7天的趋势数据（用于图表）
    private String revenueChart;            // 7天收入趋势（JSON字符串）
    private String orderChart;              // 7天订单趋势（JSON字符串）
    private String userChart;               // 7天新用户趋势（JSON字符串）
}