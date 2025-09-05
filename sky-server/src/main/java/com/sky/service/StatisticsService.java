package com.sky.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * 统计服务
 */
public interface StatisticsService {
    Map<String, Object> getTurnoverStatistics(LocalDate begin, LocalDate end);
    Map<String, Object> getUserStatistics(LocalDate begin, LocalDate end);
    Map<String, Object> getOrderStatistics(LocalDate begin, LocalDate end);
    Map<String, Object> getTop(LocalDate begin, LocalDate end);
    String exportCsv(LocalDate begin, LocalDate end);
    Map<String, Object> getKpi(LocalDate begin, LocalDate end, String granularity);
    Map<String, Integer> getUserTypeDistribution();
    Map<String, Integer> getOrderStatusDistribution();
    Map<String, Object> getFunnel(LocalDate begin, LocalDate end);
    Map<String, Object> getVehicleUtilization(LocalDate begin, LocalDate end);
}

