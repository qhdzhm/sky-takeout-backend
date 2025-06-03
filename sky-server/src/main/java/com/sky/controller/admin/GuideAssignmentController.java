package com.sky.controller.admin;

import com.sky.dto.GuideAssignmentDTO;
import com.sky.dto.GuideAssignmentQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.GuideAssignmentService;
import com.sky.vo.GuideAssignmentVO;
import com.sky.vo.GuideAvailabilityVO;
import com.sky.vo.VehicleAvailabilityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 导游分配管理控制器
 */
@RestController
@RequestMapping("/admin/guide-assignment")
@Api(tags = "导游分配管理接口")
@Slf4j
public class GuideAssignmentController {

    @Autowired
    private GuideAssignmentService guideAssignmentService;

    /**
     * 分页查询导游分配记录
     */
    @GetMapping("/page")
    @ApiOperation("分页查询导游分配记录")
    public Result<PageResult> page(GuideAssignmentQueryDTO queryDTO) {
        log.info("分页查询导游分配记录：{}", queryDTO);
        PageResult pageResult = guideAssignmentService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取可用导游列表
     */
    @GetMapping("/available-guides")
    @ApiOperation("获取可用导游列表")
    public Result<List<GuideAvailabilityVO>> getAvailableGuides(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
            @RequestParam String location) {
        log.info("获取可用导游列表：日期={}, 开始时间={}, 结束时间={}, 地点={}", date, startTime, endTime, location);
        List<GuideAvailabilityVO> guides = guideAssignmentService.getAvailableGuides(date, startTime, endTime, location);
        return Result.success(guides);
    }

    /**
     * 获取可用车辆列表
     */
    @GetMapping("/available-vehicles")
    @ApiOperation("获取可用车辆列表")
    public Result<List<VehicleAvailabilityVO>> getAvailableVehicles(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
            @RequestParam Integer peopleCount) {
        log.info("获取可用车辆列表：日期={}, 开始时间={}, 结束时间={}, 人数={}", date, startTime, endTime, peopleCount);
        List<VehicleAvailabilityVO> vehicles = guideAssignmentService.getAvailableVehicles(date, startTime, endTime, peopleCount);
        return Result.success(vehicles);
    }

    /**
     * 自动分配导游和车辆
     */
    @PostMapping("/auto-assign")
    @ApiOperation("自动分配导游和车辆")
    public Result<GuideAssignmentVO> autoAssign(@RequestBody GuideAssignmentDTO assignmentDTO) {
        log.info("自动分配导游和车辆：{}", assignmentDTO);
        GuideAssignmentVO result = guideAssignmentService.autoAssign(assignmentDTO);
        return Result.success(result);
    }

    /**
     * 手动分配导游和车辆
     */
    @PostMapping("/manual-assign")
    @ApiOperation("手动分配导游和车辆")
    public Result<GuideAssignmentVO> manualAssign(@RequestBody GuideAssignmentDTO assignmentDTO) {
        log.info("手动分配导游和车辆：{}", assignmentDTO);
        GuideAssignmentVO result = guideAssignmentService.manualAssign(assignmentDTO);
        return Result.success(result);
    }

    /**
     * 更新分配信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新分配信息")
    public Result<Void> update(@PathVariable Long id, @RequestBody GuideAssignmentDTO assignmentDTO) {
        log.info("更新分配信息：id={}, data={}", id, assignmentDTO);
        assignmentDTO.setId(id);
        guideAssignmentService.update(assignmentDTO);
        return Result.success();
    }

    /**
     * 取消分配
     */
    @DeleteMapping("/{id}")
    @ApiOperation("取消分配")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        log.info("取消分配：id={}, reason={}", id, reason);
        guideAssignmentService.cancel(id, reason);
        return Result.success();
    }

    /**
     * 获取分配详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取分配详情")
    public Result<GuideAssignmentVO> getById(@PathVariable Long id) {
        log.info("获取分配详情：id={}", id);
        GuideAssignmentVO assignment = guideAssignmentService.getById(id);
        return Result.success(assignment);
    }

    /**
     * 根据日期获取分配列表
     */
    @GetMapping("/by-date")
    @ApiOperation("根据日期获取分配列表")
    public Result<List<GuideAssignmentVO>> getByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("根据日期获取分配列表：date={}", date);
        List<GuideAssignmentVO> assignments = guideAssignmentService.getByDate(date);
        return Result.success(assignments);
    }

    /**
     * 批量分配
     */
    @PostMapping("/batch-assign")
    @ApiOperation("批量分配")
    public Result<List<GuideAssignmentVO>> batchAssign(@RequestBody List<GuideAssignmentDTO> assignmentDTOs) {
        log.info("批量分配：count={}", assignmentDTOs.size());
        List<GuideAssignmentVO> results = guideAssignmentService.batchAssign(assignmentDTOs);
        return Result.success(results);
    }
} 