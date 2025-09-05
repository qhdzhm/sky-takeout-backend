package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.DashboardService;
import com.sky.vo.DashboardOverviewVO;
import com.sky.vo.ProductStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘数据管理
 */
@RestController("adminDashboardController")
@RequestMapping("/admin/dashboard")
@Api(tags = "仪表盘数据管理")
@Slf4j
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表盘概览数据
     */
    @GetMapping("/overview")
    @ApiOperation("获取仪表盘概览数据")
    public Result<DashboardOverviewVO> getOverviewData() {
        log.info("获取仪表盘概览数据");
        DashboardOverviewVO overviewData = dashboardService.getOverviewData();
        return Result.success(overviewData);
    }

    /**
     * 获取产品统计数据
     */
    @GetMapping("/products")
    @ApiOperation("获取产品统计数据")
    public Result<ProductStatisticsVO> getProductStatistics() {
        log.info("获取产品统计数据");
        ProductStatisticsVO productStatistics = dashboardService.getProductStatistics();
        return Result.success(productStatistics);
    }

    /**
     * 获取营业数据（兼容原有接口）
     */
    @GetMapping("/businessData")
    @ApiOperation("获取营业数据")
    public Result<Map<String, Object>> getBusinessData() {
        log.info("获取营业数据");
        
        // 获取今日数据
        LocalDate today = LocalDate.now();
        Map<String, Object> businessData = dashboardService.getBusinessDataByDateRange(today, today);
        
        // 转换为原有格式
        Map<String, Object> result = new HashMap<>();
        result.put("newUsers", businessData.getOrDefault("newUsers", 0));
        result.put("orderCompletionRate", 85.5); // 可以后续从数据库计算
        result.put("newOrders", businessData.getOrDefault("totalOrders", 0));
        result.put("turnover", businessData.getOrDefault("totalRevenue", 0));
        result.put("validOrderCount", businessData.getOrDefault("totalOrders", 0));
        result.put("orderCount", businessData.getOrDefault("totalOrders", 0));
        result.put("unitPrice", businessData.getOrDefault("averageOrderValue", 0));
        
        return Result.success(result);
    }

    /**
     * 获取订单数据（兼容原有接口）
     */
    @GetMapping("/orderData")
    @ApiOperation("获取订单数据")
    public Result<Map<String, Integer>> getOrderData() {
        log.info("获取订单数据");
        
        Map<String, Integer> orderStatus = dashboardService.getOrderStatusDistribution();
        
        // 转换为原有格式
        Map<String, Integer> result = new HashMap<>();
        result.put("waitingOrders", orderStatus.getOrDefault("pending", 0));
        result.put("deliveredOrders", orderStatus.getOrDefault("confirmed", 0));
        result.put("completedOrders", orderStatus.getOrDefault("completed", 0));
        result.put("cancelledOrders", orderStatus.getOrDefault("cancelled", 0));
        result.put("allOrders", orderStatus.values().stream().mapToInt(Integer::intValue).sum());
        
        return Result.success(result);
    }

    /**
     * 获取产品概览数据（兼容原有接口）
     */
    @GetMapping("/overviewDishes")
    @ApiOperation("获取产品概览数据")
    public Result<Map<String, Object>> getOverviewDishes() {
        log.info("获取产品概览数据");
        
        ProductStatisticsVO productStats = dashboardService.getProductStatistics();
        
        // 转换为原有格式
        Map<String, Object> result = new HashMap<>();
        result.put("sold", productStats.getActiveDayTourCount() + productStats.getActiveGroupTourCount());
        result.put("discontinued", (productStats.getDayTourCount() - productStats.getActiveDayTourCount()) + 
                                 (productStats.getGroupTourCount() - productStats.getActiveGroupTourCount()));
        
        return Result.success(result);
    }

    /**
     * 获取套餐统计数据（兼容原有接口）
     */
    @GetMapping("/setMealStatistics")
    @ApiOperation("获取套餐统计数据")
    public Result<Map<String, Object>> getSetMealStatistics() {
        log.info("获取套餐统计数据");
        
        ProductStatisticsVO productStats = dashboardService.getProductStatistics();
        
        // 将团队游作为套餐统计
        Map<String, Object> result = new HashMap<>();
        result.put("sold", productStats.getActiveGroupTourCount());
        result.put("discontinued", productStats.getGroupTourCount() - productStats.getActiveGroupTourCount());
        
        return Result.success(result);
    }

    /**
     * 获取指定日期范围的营业数据
     */
    @GetMapping("/businessData/range")
    @ApiOperation("获取指定日期范围的营业数据")
    public Result<Map<String, Object>> getBusinessDataByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("获取日期范围营业数据: {} - {}", begin, end);
        
        Map<String, Object> businessData = dashboardService.getBusinessDataByDateRange(begin, end);
        return Result.success(businessData);
    }

    /**
     * 获取最近7天的趋势数据
     */
    @GetMapping("/trend/seven-days")
    @ApiOperation("获取最近7天的趋势数据")
    public Result<Map<String, Object>> getSevenDaysTrendData() {
        log.info("获取最近7天趋势数据");
        
        Map<String, Object> trendData = dashboardService.getSevenDaysTrendData();
        return Result.success(trendData);
    }

    /**
     * 获取订单状态分布数据
     */
    @GetMapping("/order-status")
    @ApiOperation("获取订单状态分布数据")
    public Result<Map<String, Integer>> getOrderStatusDistribution() {
        log.info("获取订单状态分布数据");
        
        Map<String, Integer> orderStatus = dashboardService.getOrderStatusDistribution();
        return Result.success(orderStatus);
    }

    /**
     * 获取用户类型分布数据
     */
    @GetMapping("/user-types")
    @ApiOperation("获取用户类型分布数据")
    public Result<Map<String, Integer>> getUserTypeDistribution() {
        log.info("获取用户类型分布数据");
        
        Map<String, Integer> userTypes = dashboardService.getUserTypeDistribution();
        return Result.success(userTypes);
    }

    /**
     * 获取热门目的地统计
     */
    @GetMapping("/popular-destinations")
    @ApiOperation("获取热门目的地统计")
    public Result<Map<String, Object>> getPopularDestinations() {
        log.info("获取热门目的地统计");
        
        Map<String, Object> destinations = dashboardService.getPopularDestinations();
        return Result.success(destinations);
    }

    /**
     * 获取代理商表现数据
     */
    @GetMapping("/agent-performance")
    @ApiOperation("获取代理商表现数据")
    public Result<Map<String, Object>> getAgentPerformanceData() {
        log.info("获取代理商表现数据");
        
        Map<String, Object> agentData = dashboardService.getAgentPerformanceData();
        return Result.success(agentData);
    }
}