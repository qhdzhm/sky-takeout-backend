package com.sky.service.impl;

import com.sky.mapper.TourBookingMapper;
import com.sky.mapper.UserMapper;
import com.sky.mapper.VehicleMapper;
import com.sky.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private TourBookingMapper tourBookingMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VehicleMapper vehicleMapper;

    // 删除未使用的注入，避免编译警告（需要时再加回）

    private Integer safeToInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof java.math.BigDecimal) return ((java.math.BigDecimal) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    @Override
    public Map<String, Object> getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<Map<String, Object>> daily = tourBookingMapper.getBusinessDataByDateRange(begin, end);
        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();
        for (Map<String, Object> m : daily) {
            dateList.add(String.valueOf(m.get("date")));
            BigDecimal revenue = (BigDecimal) m.getOrDefault("revenue", BigDecimal.ZERO);
            turnoverList.add(revenue.setScale(2, RoundingMode.HALF_UP).toPlainString());
        }
        Map<String, Object> res = new HashMap<>();
        res.put("dateList", String.join(",", dateList));
        res.put("turnoverList", String.join(",", turnoverList));
        return res;
    }

    @Override
    public Map<String, Object> getUserStatistics(LocalDate begin, LocalDate end) {
        List<Map<String, Object>> regDaily = userMapper.getUserRegistrationByDateRange(begin, end);
        // 组装日期 -> 新增
        Map<String, Integer> dateToNew = new HashMap<>();
        for (Map<String, Object> m : regDaily) {
            dateToNew.put(String.valueOf(m.get("date")), safeToInteger(m.get("newUsers")));
        }
        List<String> dates = new ArrayList<>();
        List<Integer> newUsers = new ArrayList<>();
        List<Integer> totals = new ArrayList<>();
        LocalDate cursor = begin;
        int total = 0;
        while (!cursor.isAfter(end)) {
            String d = cursor.toString();
            dates.add(d);
            int nu = dateToNew.getOrDefault(d, 0);
            newUsers.add(nu);
            total += nu;
            totals.add(total);
            cursor = cursor.plusDays(1);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("dateList", String.join(",", dates));
        res.put("newUserList", newUsers.stream().map(String::valueOf).reduce((a,b)->a+","+b).orElse(""));
        res.put("totalUserList", totals.stream().map(String::valueOf).reduce((a,b)->a+","+b).orElse(""));
        return res;
    }

    @Override
    public Map<String, Object> getOrderStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime start = begin.atStartOfDay();
        LocalDateTime finish = end.plusDays(1).atStartOfDay();
        Map<String, Object> data = tourBookingMapper.getOrderDataByDateRange(start, finish);
        int totalOrders = safeToInteger(data.get("totalOrders"));

        // 逐日数据
        List<Map<String, Object>> daily = tourBookingMapper.getBusinessDataByDateRange(begin, end);
        List<String> dates = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        int validSum = 0;
        for (Map<String, Object> m : daily) {
            dates.add(String.valueOf(m.get("date")));
            int orders = safeToInteger(m.get("orders"));
            orderCountList.add(orders);
            // 使用当日 completed 数量作为有效订单
            int valid = safeToInteger(m.get("completed"));
            validOrderCountList.add(valid);
            validSum += valid;
        }

        String dateListStr = String.join(",", dates);
        String orderListStr = orderCountList.stream().map(String::valueOf).reduce((a,b)->a+","+b).orElse("");
        String validListStr = validOrderCountList.stream().map(String::valueOf).reduce((a,b)->a+","+b).orElse("");
        BigDecimal rate = totalOrders == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(validSum).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        Map<String, Object> res = new HashMap<>();
        res.put("dateList", dateListStr);
        res.put("orderCountList", orderListStr);
        res.put("validOrderCountList", validListStr);
        res.put("totalOrderCount", totalOrders);
        res.put("validOrderCount", validSum);
        res.put("orderCompletionRate", rate);
        return res;
    }

    @Override
    public Map<String, Object> getTop(LocalDate begin, LocalDate end) {
        // 复用热门产品：取前10个日游+团游按销量排序
        List<Map<String, Object>> dayTop = tourBookingMapper.getTopToursByType("day_tour", 10);
        List<Map<String, Object>> groupTop = tourBookingMapper.getTopToursByType("group_tour", 10);
        // 合并后取前10（按bookingCount）
        List<Map<String, Object>> all = new ArrayList<>();
        all.addAll(dayTop);
        all.addAll(groupTop);
        all.sort((a, b) -> safeToInteger(b.get("bookingCount")) - safeToInteger(a.get("bookingCount")));
        if (all.size() > 10) all = all.subList(0, 10);
        List<String> names = new ArrayList<>();
        List<Integer> nums = new ArrayList<>();
        for (Map<String, Object> m : all) {
            names.add(String.valueOf(m.get("tourName")));
            nums.add(safeToInteger(m.get("bookingCount")));
        }
        Map<String, Object> res = new HashMap<>();
        res.put("nameList", String.join(",", names));
        res.put("numberList", nums.stream().map(String::valueOf).reduce((a,b)->a+","+b).orElse(""));
        return res;
    }

    @Override
    public String exportCsv(LocalDate begin, LocalDate end) {
        Map<String, Object> t = getTurnoverStatistics(begin, end);
        Map<String, Object> o = getOrderStatistics(begin, end);
        Map<String, Object> u = getUserStatistics(begin, end);
        StringBuilder sb = new StringBuilder();
        sb.append("日期,营业额,订单数,新增用户\n");
        String[] dates = String.valueOf(t.getOrDefault("dateList", "")).split(",");
        String[] turnover = String.valueOf(t.getOrDefault("turnoverList", "")).split(",");
        String[] orderList = String.valueOf(o.getOrDefault("orderCountList", "")).split(",");
        String[] newUsers = String.valueOf(u.getOrDefault("newUserList", "")).split(",");
        int n = dates.length;
        for (int i = 0; i < n; i++) {
            String d = i < dates.length ? dates[i] : "";
            String rev = i < turnover.length ? turnover[i] : "0";
            String oc = i < orderList.length ? orderList[i] : "0";
            String nu = i < newUsers.length ? newUsers[i] : "0";
            sb.append(d).append(',').append(rev).append(',').append(oc).append(',').append(nu).append('\n');
        }
        return sb.toString();
    }

    @Override
    public Map<String, Object> getKpi(LocalDate begin, LocalDate end, String granularity) {
        // 汇总区间：成交额、完成单、客单价；并按粒度提供序列
        Map<String, Object> res = new HashMap<>();
        // 区间订单数据
        Map<String, Object> range = tourBookingMapper.getOrderDataByDateRange(begin.atStartOfDay(), end.plusDays(1).atStartOfDay());
        int totalOrders = safeToInteger(range.get("totalOrders"));
        int completedOrders = safeToInteger(range.get("completedOrders"));
        java.math.BigDecimal totalRevenue = (java.math.BigDecimal) range.getOrDefault("totalRevenue", java.math.BigDecimal.ZERO);
        java.math.BigDecimal aov = completedOrders == 0 ? java.math.BigDecimal.ZERO : totalRevenue.divide(java.math.BigDecimal.valueOf(completedOrders), 2, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal completionRate = totalOrders == 0 ? java.math.BigDecimal.ZERO : java.math.BigDecimal.valueOf(completedOrders).multiply(java.math.BigDecimal.valueOf(100)).divide(java.math.BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP);
        res.put("totalRevenue", totalRevenue);
        res.put("completedOrders", completedOrders);
        res.put("avgOrderValue", aov);
        res.put("completionRate", completionRate);
        // 简版：序列先用按日数据
        List<Map<String, Object>> daily = tourBookingMapper.getBusinessDataByDateRange(begin, end);
        res.put("dates", daily.stream().map(m -> String.valueOf(m.get("date"))).collect(java.util.stream.Collectors.toList()));
        res.put("revenues", daily.stream().map(m -> (java.math.BigDecimal) m.getOrDefault("revenue", java.math.BigDecimal.ZERO)).collect(java.util.stream.Collectors.toList()));
        res.put("completedSeries", daily.stream().map(m -> safeToInteger(m.get("completed"))).collect(java.util.stream.Collectors.toList()));
        return res;
    }

    @Override
    public Map<String, Integer> getUserTypeDistribution() {
        return userMapper.getUserTypeDistribution();
    }

    @Override
    public Map<String, Integer> getOrderStatusDistribution() {
        Map<String, Object> raw = tourBookingMapper.getOrderStatusDistribution();
        Map<String, Integer> m = new HashMap<>();
        m.put("pending", safeToInteger(raw.get("pending")));
        m.put("confirmed", safeToInteger(raw.get("confirmed")));
        m.put("completed", safeToInteger(raw.get("completed")));
        m.put("cancelled", safeToInteger(raw.get("cancelled")));
        return m;
    }

    @Override
    public Map<String, Object> getFunnel(LocalDate begin, LocalDate end) {
        List<Map<String, Object>> daily = tourBookingMapper.getBusinessDataByDateRange(begin, end);
        List<String> dates = new ArrayList<>();
        List<Integer> placed = new ArrayList<>();
        List<Integer> confirmed = new ArrayList<>();
        List<Integer> completed = new ArrayList<>();
        for (Map<String, Object> m : daily) {
            dates.add(String.valueOf(m.get("date")));
            placed.add(safeToInteger(m.get("orders")));
            // 复用状态分布：再查一次区间内每日confirmed、completed（简化：用整体分布的比例估计，这里先置0，留给后续SQL细化）
            confirmed.add(0);
            completed.add(safeToInteger(m.get("completed")));
        }
        Map<String, Object> res = new HashMap<>();
        res.put("dates", dates);
        res.put("placed", placed);
        res.put("confirmed", confirmed);
        res.put("completed", completed);
        return res;
    }

    @Override
    public Map<String, Object> getVehicleUtilization(LocalDate begin, LocalDate end) {
        // 简版：统计每日分配记录数量 / 车辆总数
        Map<String, Object> res = new HashMap<>();
        Integer totalVehicles = vehicleMapper.count();
        // 直接用 tour_guide_vehicle_assignment 的数量作为占用数（需要新SQL更精确到distinct vehicle_id）。
        // 为不大量改动，这里先从订单日数据生成 0 列表，前端展示结构预留。
        res.put("totalVehicles", totalVehicles);
        res.put("dates", java.util.Collections.emptyList());
        res.put("utilizations", java.util.Collections.emptyList());
        return res;
    }
}

