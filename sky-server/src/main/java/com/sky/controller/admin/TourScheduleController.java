package com.sky.controller.admin;

import com.sky.dto.TourScheduleBatchSaveDTO;
import com.sky.dto.TourScheduleOrderDTO;
import com.sky.result.Result;
import com.sky.service.TourScheduleOrderService;
import com.sky.vo.TourScheduleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 行程排序接口
 */
@RestController
@RequestMapping("/admin/tour/schedule")
@Api(tags = "行程排序相关接口")
@Slf4j
public class TourScheduleController {

    @Autowired
    private TourScheduleOrderService tourScheduleOrderService;

    /**
     * 通过订单ID获取行程排序
     */
    @GetMapping("/booking/{bookingId}")
    @ApiOperation("通过订单ID获取行程排序")
    public Result<List<TourScheduleVO>> getSchedulesByBookingId(@PathVariable Integer bookingId) {
        log.info("通过订单ID获取行程排序: {}", bookingId);
        List<TourScheduleVO> schedules = tourScheduleOrderService.getSchedulesByBookingId(bookingId);
        return Result.success(schedules);
    }

    /**
     * 通过日期范围获取行程排序
     */
    @GetMapping("/date")
    @ApiOperation("通过日期范围获取行程排序")
    public Result<List<TourScheduleVO>> getSchedulesByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("通过日期范围获取行程排序: {} - {}", startDate, endDate);
        List<TourScheduleVO> schedules = tourScheduleOrderService.getSchedulesByDateRange(startDate, endDate);
        return Result.success(schedules);
    }

    /**
     * 保存单个行程排序
     */
    @PostMapping
    @ApiOperation("保存单个行程排序")
    public Result<Boolean> saveSchedule(@RequestBody TourScheduleOrderDTO tourScheduleOrderDTO) {
        log.info("保存单个行程排序: {}", tourScheduleOrderDTO);
        boolean result = tourScheduleOrderService.saveSchedule(tourScheduleOrderDTO);
        return Result.success(result);
    }

    /**
     * 批量保存行程排序
     */
    @PostMapping("/batch")
    @ApiOperation("批量保存行程排序")
    public Result<Boolean> saveBatchSchedules(@RequestBody TourScheduleBatchSaveDTO batchSaveDTO) {
        log.info("批量保存行程排序: {}", batchSaveDTO);
        boolean result = tourScheduleOrderService.saveBatchSchedules(batchSaveDTO);
        return Result.success(result);
    }

    /**
     * 初始化订单的行程排序
     */
    @PostMapping("/init/{bookingId}")
    @ApiOperation("初始化订单的行程排序")
    public Result<Boolean> initOrderSchedules(@PathVariable Integer bookingId) {
        log.info("初始化订单的行程排序: {}", bookingId);
        boolean result = tourScheduleOrderService.initOrderSchedules(bookingId);
        return Result.success(result);
    }
} 