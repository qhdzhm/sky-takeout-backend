package com.sky.controller.admin;

import com.sky.dto.GuideAvailabilityDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.GuideAvailabilityService;
import com.sky.vo.GuideAvailabilityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 导游可用性管理
 */
@RestController
@RequestMapping("/admin/guide-availability")
@Api(tags = "导游可用性管理相关接口")
@Slf4j
public class GuideAvailabilityController {

    @Autowired
    private GuideAvailabilityService guideAvailabilityService;

    /**
     * 获取导游可用性列表
     */
    @GetMapping
    @ApiOperation("获取导游可用性列表")
    public Result<List<GuideAvailabilityVO>> getGuideAvailability(
            @RequestParam Integer guideId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("获取导游可用性列表，导游ID：{}，开始日期：{}，结束日期：{}", guideId, startDate, endDate);
        
        List<GuideAvailabilityVO> list = guideAvailabilityService.getGuideAvailability(guideId, startDate, endDate);
        return Result.success(list);
    }

    /**
     * 设置导游可用性
     */
    @PostMapping
    @ApiOperation("设置导游可用性")
    public Result setGuideAvailability(@RequestBody GuideAvailabilityDTO guideAvailabilityDTO) {
        log.info("设置导游可用性：{}", guideAvailabilityDTO);
        
        guideAvailabilityService.setGuideAvailability(guideAvailabilityDTO);
        return Result.success();
    }

    /**
     * 批量设置导游可用性
     */
    @PostMapping("/batch")
    @ApiOperation("批量设置导游可用性")
    public Result batchSetGuideAvailability(@RequestBody GuideAvailabilityDTO guideAvailabilityDTO) {
        log.info("批量设置导游可用性：{}", guideAvailabilityDTO);
        
        guideAvailabilityService.batchSetGuideAvailability(guideAvailabilityDTO);
        return Result.success();
    }

    /**
     * 删除导游可用性设置
     */
    @DeleteMapping
    @ApiOperation("删除导游可用性设置")
    public Result deleteGuideAvailability(
            @RequestParam Integer guideId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("删除导游可用性设置，导游ID：{}，日期：{}", guideId, date);
        
        guideAvailabilityService.deleteGuideAvailability(guideId, date);
        return Result.success();
    }

    /**
     * 获取导游可用性统计
     */
    @GetMapping("/stats")
    @ApiOperation("获取导游可用性统计")
    public Result getGuideAvailabilityStats(
            @RequestParam(required = false) Integer guideId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("获取导游可用性统计，导游ID：{}，开始日期：{}，结束日期：{}", guideId, startDate, endDate);
        
        // 这里可以返回统计信息，比如可用天数、忙碌天数等
        return Result.success();
    }
} 