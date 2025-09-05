package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

/**
 * 统计报表接口（供后台 /statistics 页面使用）
 */
@RestController
@RequestMapping("/admin/statistics")
@Api(tags = "统计报表")
@Slf4j
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/turnover")
    @ApiOperation("营业额统计（按天）")
    public Result<Map<String, Object>> getTurnover(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("查询营业额统计: {} - {}", begin, end);
        return Result.success(statisticsService.getTurnoverStatistics(begin, end));
    }

    @GetMapping("/users")
    @ApiOperation("用户统计（新增/累计）")
    public Result<Map<String, Object>> getUserStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("查询用户统计: {} - {}", begin, end);
        return Result.success(statisticsService.getUserStatistics(begin, end));
    }

    @GetMapping("/orders")
    @ApiOperation("订单统计（按天/完成率）")
    public Result<Map<String, Object>> getOrderStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("查询订单统计: {} - {}", begin, end);
        return Result.success(statisticsService.getOrderStatistics(begin, end));
    }

    @GetMapping("/top")
    @ApiOperation("Top10 热门产品")
    public Result<Map<String, Object>> getTop(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("查询Top10产品: {} - {}", begin, end);
        return Result.success(statisticsService.getTop(begin, end));
    }

    @GetMapping("/export")
    @ApiOperation("导出统计（CSV）")
    public ResponseEntity<byte[]> export(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("导出统计CSV: {} - {}", begin, end);
        String csv = statisticsService.exportCsv(begin, end);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statistics.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @GetMapping("/kpi")
    @ApiOperation("核心KPI（支持granularity=day|week|month）")
    public Result<Map<String, Object>> getKpi(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
            @RequestParam(defaultValue = "day") String granularity) {
        log.info("查询KPI: {} - {}, granularity={}", begin, end, granularity);
        return Result.success(statisticsService.getKpi(begin, end, granularity));
    }

    @GetMapping("/user-type")
    @ApiOperation("用户类型分布")
    public Result<Map<String, Integer>> getUserTypeDistribution() {
        return Result.success(statisticsService.getUserTypeDistribution());
    }

    @GetMapping("/order-status")
    @ApiOperation("订单状态占比")
    public Result<Map<String, Integer>> getOrderStatusDistribution() {
        return Result.success(statisticsService.getOrderStatusDistribution());
    }

    @GetMapping("/funnel")
    @ApiOperation("订单转化漏斗（按日）")
    public Result<Map<String, Object>> getFunnel(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(statisticsService.getFunnel(begin, end));
    }

    @GetMapping("/vehicle-utilization")
    @ApiOperation("车辆使用率（按日）")
    public Result<Map<String, Object>> getVehicleUtilization(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(statisticsService.getVehicleUtilization(begin, end));
    }
}
