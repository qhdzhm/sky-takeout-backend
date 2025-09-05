package com.sky.service.impl;

import com.sky.mapper.*;
import com.sky.service.DashboardService;
import com.sky.vo.DashboardOverviewVO;
import com.sky.vo.ProductStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘数据服务实现类
 */
@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {
    
    /**
     * 安全转换数值类型为Integer，处理MySQL数值函数返回不同类型的问题
     * 支持：Long(COUNT)、BigDecimal(SUM)、Integer等类型
     */
    private Integer safeToInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    @Autowired
    private TourBookingMapper tourBookingMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private DayTourMapper dayTourMapper;

    @Autowired
    private GroupTourMapper groupTourMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 获取仪表盘概览数据
     */
    @Override
    public DashboardOverviewVO getOverviewData() {
        log.info("获取仪表盘概览数据");

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        LocalDate lastMonthEnd = monthStart.minusDays(1);

        // 今日数据
        Map<String, Object> todayData = getTodayData(today);
        
        // 本月数据
        Map<String, Object> monthData = getMonthData(monthStart, today);
        
        // 上月数据（用于计算增长率）
        Map<String, Object> lastMonthData = getMonthData(lastMonthStart, lastMonthEnd);

        // 订单状态统计
        Map<String, Integer> orderStatus = getOrderStatusDistribution();

        // 产品统计
        Map<String, Object> productStats = getProductStats();

        // 用户和代理商统计
        Map<String, Object> userStats = getUserStats();

        // 资源统计
        Map<String, Object> resourceStats = getResourceStats();

        // 计算增长率
        BigDecimal revenueGrowthRate = calculateGrowthRate(
            (BigDecimal) monthData.get("revenue"),
            (BigDecimal) lastMonthData.get("revenue")
        );
        
        BigDecimal orderGrowthRate = calculateGrowthRate(
            BigDecimal.valueOf(safeToInteger(monthData.get("orders"))),
            BigDecimal.valueOf(safeToInteger(lastMonthData.get("orders")))
        );
        
        BigDecimal userGrowthRate = calculateGrowthRate(
            BigDecimal.valueOf(safeToInteger(monthData.get("newUsers"))),
            BigDecimal.valueOf(safeToInteger(lastMonthData.get("newUsers")))
        );

        // 热门产品信息
        Map<String, Object> popularTour = getMostPopularTour();

        // 7天趋势数据
        Map<String, Object> trendData = getSevenDaysTrendData();

        return DashboardOverviewVO.builder()
                .todayRevenue((BigDecimal) todayData.get("revenue"))
                .todayOrders(safeToInteger(todayData.get("orders")))
                .todayNewUsers(safeToInteger(todayData.get("newUsers")))
                .averageOrderValue((BigDecimal) todayData.get("averageOrderValue"))
                .monthRevenue((BigDecimal) monthData.get("revenue"))
                .monthOrders(safeToInteger(monthData.get("orders")))
                .monthNewUsers(safeToInteger(monthData.get("newUsers")))
                .monthNewAgents(safeToInteger(monthData.get("newAgents")))
                .pendingOrders(orderStatus.getOrDefault("pending", 0))
                .confirmedOrders(orderStatus.getOrDefault("confirmed", 0))
                .completedOrders(orderStatus.getOrDefault("completed", 0))
                .cancelledOrders(orderStatus.getOrDefault("cancelled", 0))
                .totalDayTours(safeToInteger(productStats.get("totalDayTours")))
                .activeDayTours(safeToInteger(productStats.get("activeDayTours")))
                .totalGroupTours(safeToInteger(productStats.get("totalGroupTours")))
                .activeGroupTours(safeToInteger(productStats.get("activeGroupTours")))
                .totalUsers(safeToInteger(userStats.get("totalUsers")))
                .activeUsers(safeToInteger(userStats.get("activeUsers")))
                .totalAgents(safeToInteger(userStats.get("totalAgents")))
                .activeAgents(safeToInteger(userStats.get("activeAgents")))
                .totalVehicles(safeToInteger(resourceStats.get("totalVehicles")))
                .availableVehicles(safeToInteger(resourceStats.get("availableVehicles")))
                .totalEmployees(safeToInteger(resourceStats.get("totalEmployees")))
                .activeEmployees(safeToInteger(resourceStats.get("activeEmployees")))
                .revenueGrowthRate(revenueGrowthRate)
                .orderGrowthRate(orderGrowthRate)
                .userGrowthRate(userGrowthRate)
                .mostPopularTour((String) popularTour.get("name"))
                .mostPopularTourBookings(safeToInteger(popularTour.get("bookings")))
                .revenueChart((String) trendData.get("revenueChart"))
                .orderChart((String) trendData.get("orderChart"))
                .userChart((String) trendData.get("userChart"))
                .build();
    }

    /**
     * 获取产品统计数据
     */
    @Override
    public ProductStatisticsVO getProductStatistics() {
        log.info("获取产品统计数据");

        // 产品数量统计
        Integer dayTourCount = dayTourMapper.count();
        Integer groupTourCount = groupTourMapper.count();
        Integer activeDayTourCount = dayTourMapper.countByStatus(1);
        Integer activeGroupTourCount = groupTourMapper.countByStatus(1);

        // 销售统计
        Map<String, Object> salesData = getProductSalesData();

        // 热门产品
        List<ProductStatisticsVO.PopularTourVO> topDayTours = getTopDayTours(5);
        List<ProductStatisticsVO.PopularTourVO> topGroupTours = getTopGroupTours(5);

        return ProductStatisticsVO.builder()
                .dayTourCount(dayTourCount)
                .groupTourCount(groupTourCount)
                .activeDayTourCount(activeDayTourCount)
                .activeGroupTourCount(activeGroupTourCount)
                .dayTourRevenue((BigDecimal) salesData.get("dayTourRevenue"))
                .groupTourRevenue((BigDecimal) salesData.get("groupTourRevenue"))
                .dayTourBookings(safeToInteger(salesData.get("dayTourBookings")))
                .groupTourBookings(safeToInteger(salesData.get("groupTourBookings")))
                .topDayTours(topDayTours)
                .topGroupTours(topGroupTours)
                .averageDayTourPrice((BigDecimal) salesData.get("averageDayTourPrice"))
                .averageGroupTourPrice((BigDecimal) salesData.get("averageGroupTourPrice"))
                .averageRating((Double) salesData.get("averageRating"))
                .build();
    }

    /**
     * 获取指定日期范围的营业数据
     */
    @Override
    public Map<String, Object> getBusinessDataByDateRange(LocalDate begin, LocalDate end) {
        log.info("获取日期范围营业数据: {} - {}", begin, end);

        Map<String, Object> result = new HashMap<>();
        
        // 查询订单数据
        List<Map<String, Object>> orderData = tourBookingMapper.getBusinessDataByDateRange(begin, end);
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalOrders = 0;
        
        for (Map<String, Object> data : orderData) {
            BigDecimal revenue = (BigDecimal) data.get("revenue");
            // 使用安全转换方法处理MySQL COUNT()返回的Long类型
            Integer orders = safeToInteger(data.get("orders"));
            
            if (revenue != null) totalRevenue = totalRevenue.add(revenue);
            totalOrders += orders;
        }
        
        result.put("totalRevenue", totalRevenue);
        result.put("totalOrders", totalOrders);
        result.put("averageOrderValue", totalOrders > 0 ? 
            totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        result.put("dailyData", orderData);
        
        return result;
    }

    /**
     * 获取最近7天的趋势数据
     */
    @Override
    public Map<String, Object> getSevenDaysTrendData() {
        log.info("获取最近7天趋势数据");

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        
        List<Map<String, Object>> dailyData = tourBookingMapper.getBusinessDataByDateRange(startDate, endDate);
        List<Map<String, Object>> userRegistrationData = userMapper.getUserRegistrationByDateRange(startDate, endDate);
        
        // 构建7天的完整数据
        List<String> dates = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();
        List<Integer> orders = new ArrayList<>();
        List<Integer> newUsers = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            dates.add(date.toString());
            
            // 查找对应日期的数据
            Map<String, Object> dayOrderData = dailyData.stream()
                .filter(data -> date.equals(data.get("date")))
                .findFirst()
                .orElse(new HashMap<>());
                
            Map<String, Object> dayUserData = userRegistrationData.stream()
                .filter(data -> date.equals(data.get("date")))
                .findFirst()
                .orElse(new HashMap<>());
            
            revenues.add((BigDecimal) dayOrderData.getOrDefault("revenue", BigDecimal.ZERO));
            orders.add((Integer) dayOrderData.getOrDefault("orders", 0));
            newUsers.add((Integer) dayUserData.getOrDefault("newUsers", 0));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("revenues", revenues);
        result.put("orders", orders);
        result.put("newUsers", newUsers);
        
        // 转换为JSON字符串用于前端图表
        result.put("revenueChart", revenues.toString());
        result.put("orderChart", orders.toString());
        result.put("userChart", newUsers.toString());
        
        return result;
    }

    /**
     * 获取订单状态分布数据
     */
    @Override
    public Map<String, Integer> getOrderStatusDistribution() {
        log.info("获取订单状态分布数据");
        Map<String, Object> rawData = tourBookingMapper.getOrderStatusDistribution();
        
        // 安全转换所有的数值类型为Integer
        Map<String, Integer> result = new HashMap<>();
        result.put("pending", safeToInteger(rawData.get("pending")));
        result.put("confirmed", safeToInteger(rawData.get("confirmed")));
        result.put("completed", safeToInteger(rawData.get("completed")));
        result.put("cancelled", safeToInteger(rawData.get("cancelled")));
        
        return result;
    }

    /**
     * 获取用户类型分布数据
     */
    @Override
    public Map<String, Integer> getUserTypeDistribution() {
        log.info("获取用户类型分布数据");
        return userMapper.getUserTypeDistribution();
    }

    /**
     * 获取热门目的地统计
     */
    @Override
    public Map<String, Object> getPopularDestinations() {
        log.info("获取热门目的地统计");
        return tourBookingMapper.getPopularDestinations();
    }

    /**
     * 获取代理商表现数据
     */
    @Override
    public Map<String, Object> getAgentPerformanceData() {
        log.info("获取代理商表现数据");
        return agentMapper.getAgentPerformanceData();
    }

    // 私有辅助方法

    private Map<String, Object> getTodayData(LocalDate today) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        Map<String, Object> orderData = tourBookingMapper.getOrderDataByDateRange(startOfDay, endOfDay);
        Integer newUsers = userMapper.getNewUsersByDateRange(startOfDay, endOfDay);
        
        BigDecimal revenue = (BigDecimal) orderData.getOrDefault("totalRevenue", BigDecimal.ZERO);
        // 使用安全转换方法处理MySQL COUNT()返回的Long类型
        Integer orders = safeToInteger(orderData.getOrDefault("totalOrders", 0));
        
        result.put("revenue", revenue);
        result.put("orders", orders);
        result.put("newUsers", newUsers);
        result.put("averageOrderValue", orders > 0 ? 
            revenue.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        
        return result;
    }

    private Map<String, Object> getMonthData(LocalDate start, LocalDate end) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDateTime startOfMonth = start.atStartOfDay();
        LocalDateTime endOfMonth = end.plusDays(1).atStartOfDay();
        
        Map<String, Object> orderData = tourBookingMapper.getOrderDataByDateRange(startOfMonth, endOfMonth);
        Integer newUsers = userMapper.getNewUsersByDateRange(startOfMonth, endOfMonth);
        Integer newAgents = agentMapper.getNewAgentsByDateRange(startOfMonth, endOfMonth);
        
        result.put("revenue", orderData.getOrDefault("totalRevenue", BigDecimal.ZERO));
        result.put("orders", orderData.getOrDefault("totalOrders", 0));
        result.put("newUsers", newUsers);
        result.put("newAgents", newAgents);
        
        return result;
    }

    private BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private Map<String, Object> getProductStats() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("totalDayTours", dayTourMapper.count());
        result.put("activeDayTours", dayTourMapper.countByStatus(1));
        result.put("totalGroupTours", groupTourMapper.count());
        result.put("activeGroupTours", groupTourMapper.countByStatus(1));
        
        return result;
    }

    private Map<String, Object> getUserStats() {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDateTime startOfMonth = monthStart.atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        
        // 用户统计：总用户数和活跃用户数都显示状态正常的用户
        Integer totalUsers = userMapper.count();
        result.put("totalUsers", totalUsers);
        // 对于用户，我们将所有状态正常的用户都视为活跃用户
        result.put("activeUsers", totalUsers);
        
        // 代理商统计：保持原有逻辑
        result.put("totalAgents", agentMapper.count());
        result.put("activeAgents", agentMapper.getActiveAgentsByDateRange(startOfMonth, now));
        
        return result;
    }

    private Map<String, Object> getResourceStats() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("totalVehicles", vehicleMapper.count());
        result.put("availableVehicles", vehicleMapper.countByStatus("1")); // status=1表示可用
        result.put("totalEmployees", employeeMapper.count());
        result.put("activeEmployees", employeeMapper.countByStatus(1));
        
        return result;
    }

    private Map<String, Object> getMostPopularTour() {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> popularTour = tourBookingMapper.getMostPopularTour();
        
        result.put("name", popularTour.getOrDefault("tourName", "暂无数据"));
        result.put("bookings", popularTour.getOrDefault("bookingCount", 0));
        
        return result;
    }

    private Map<String, Object> getProductSalesData() {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> dayTourSales = tourBookingMapper.getSalesByTourType("day_tour");
        Map<String, Object> groupTourSales = tourBookingMapper.getSalesByTourType("group_tour");
        
        result.put("dayTourRevenue", dayTourSales.getOrDefault("revenue", BigDecimal.ZERO));
        result.put("groupTourRevenue", groupTourSales.getOrDefault("revenue", BigDecimal.ZERO));
        result.put("dayTourBookings", dayTourSales.getOrDefault("bookings", 0));
        result.put("groupTourBookings", groupTourSales.getOrDefault("bookings", 0));
        
        // 平均价格
        BigDecimal avgDayTourPrice = dayTourMapper.getAveragePrice();
        BigDecimal avgGroupTourPrice = groupTourMapper.getAveragePrice();
        Double avgRating = tourBookingMapper.getAverageRating();
        
        result.put("averageDayTourPrice", avgDayTourPrice != null ? avgDayTourPrice : BigDecimal.ZERO);
        result.put("averageGroupTourPrice", avgGroupTourPrice != null ? avgGroupTourPrice : BigDecimal.ZERO);
        result.put("averageRating", avgRating != null ? avgRating : 0.0);
        
        return result;
    }

    private List<ProductStatisticsVO.PopularTourVO> getTopDayTours(int limit) {
        List<Map<String, Object>> topTours = tourBookingMapper.getTopToursByType("day_tour", limit);
        
        return topTours.stream()
            .map(tour -> ProductStatisticsVO.PopularTourVO.builder()
                .tourId((Integer) tour.get("tourId"))
                .tourName((String) tour.get("tourName"))
                .bookingCount(safeToInteger(tour.get("bookingCount")))
                .revenue((BigDecimal) tour.get("revenue"))
                .rating((BigDecimal) tour.get("rating"))
                .tourType("day_tour")
                .build())
            .collect(Collectors.toList());
    }

    private List<ProductStatisticsVO.PopularTourVO> getTopGroupTours(int limit) {
        List<Map<String, Object>> topTours = tourBookingMapper.getTopToursByType("group_tour", limit);
        
        return topTours.stream()
            .map(tour -> ProductStatisticsVO.PopularTourVO.builder()
                .tourId((Integer) tour.get("tourId"))
                .tourName((String) tour.get("tourName"))
                .bookingCount(safeToInteger(tour.get("bookingCount")))
                .revenue((BigDecimal) tour.get("revenue"))
                .rating((BigDecimal) tour.get("rating"))
                .tourType("group_tour")
                .build())
            .collect(Collectors.toList());
    }
}