package com.sky.controller.admin;

import com.sky.dto.DayTourScheduleDTO;
import com.sky.entity.DayTourSchedule;
import com.sky.result.Result;
import com.sky.service.DayTourScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 一日游日程安排管理
 */
@RestController
@RequestMapping("/admin/daytour/schedule")
@Api(tags = "一日游日程安排相关接口")
@Slf4j
public class DayTourScheduleController {

    @Autowired
    private DayTourScheduleService dayTourScheduleService;

    /**
     * 根据一日游ID获取所有日程安排
     *
     * @param dayTourId 一日游ID
     * @return 日程安排列表
     */
    @GetMapping("/list/{dayTourId}")
    @ApiOperation("获取一日游日程安排列表")
    public Result<List<DayTourSchedule>> getByDayTourId(@PathVariable Integer dayTourId) {
        log.info("查询一日游日程安排列表: {}", dayTourId);
        List<DayTourSchedule> schedules = dayTourScheduleService.getByDayTourId(dayTourId);
        return Result.success(schedules);
    }

    /**
     * 保存日程安排
     *
     * @param dayTourScheduleDTO 日程安排DTO
     * @return 操作结果
     */
    @PostMapping("/save")
    @ApiOperation("保存一日游日程安排")
    public Result<String> save(@RequestBody DayTourScheduleDTO dayTourScheduleDTO) {
        log.info("保存一日游日程安排: {}", dayTourScheduleDTO);
        dayTourScheduleService.save(dayTourScheduleDTO);
        return Result.success();
    }

    /**
     * 根据ID删除日程安排
     *
     * @param scheduleId 日程安排ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{scheduleId}")
    @ApiOperation("删除一日游日程安排")
    public Result<String> deleteById(@PathVariable Integer scheduleId) {
        log.info("删除一日游日程安排: {}", scheduleId);
        dayTourScheduleService.deleteById(scheduleId);
        return Result.success();
    }

    /**
     * 根据一日游ID删除所有日程安排
     *
     * @param dayTourId 一日游ID
     * @return 操作结果
     */
    @DeleteMapping("/deleteByDayTourId/{dayTourId}")
    @ApiOperation("删除一日游的所有日程安排")
    public Result<String> deleteByDayTourId(@PathVariable Integer dayTourId) {
        log.info("删除一日游的所有日程安排: {}", dayTourId);
        dayTourScheduleService.deleteByDayTourId(dayTourId);
        return Result.success();
    }

    /**
     * 获取指定日期范围内的日程安排
     *
     * @param dayTourId 一日游ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日程安排列表
     */
    @GetMapping("/dateRange/{dayTourId}")
    @ApiOperation("获取指定日期范围内的日程安排")
    public Result<List<DayTourSchedule>> getSchedulesInDateRange(
            @PathVariable Integer dayTourId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("查询指定日期范围内的日程安排: dayTourId={}, startDate={}, endDate={}", dayTourId, startDate, endDate);
        List<DayTourSchedule> schedules = dayTourScheduleService.getSchedulesInDateRange(dayTourId, startDate, endDate);
        return Result.success(schedules);
    }

    /**
     * 根据ID获取日程安排
     *
     * @param scheduleId 日程安排ID
     * @return 日程安排实体
     */
    @GetMapping("/{scheduleId}")
    @ApiOperation("获取日程安排详情")
    public Result<DayTourSchedule> getById(@PathVariable Integer scheduleId) {
        log.info("查询日程安排详情: {}", scheduleId);
        DayTourSchedule schedule = dayTourScheduleService.getById(scheduleId);
        return Result.success(schedule);
    }
} 