package com.sky.controller.admin;

import com.sky.dto.VehicleAvailabilityDTO;
import com.sky.result.Result;
import com.sky.service.VehicleAvailabilityService;
import com.sky.vo.VehicleAvailabilityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 车辆可用性管理
 */
@RestController
@RequestMapping("/admin/vehicle-availability")
@Api(tags = "车辆可用性管理相关接口")
@Slf4j
public class VehicleAvailabilityController {

    @Autowired
    private VehicleAvailabilityService vehicleAvailabilityService;

    /**
     * 获取车辆可用性列表
     */
    @GetMapping
    @ApiOperation("获取车辆可用性列表")
    public Result<List<VehicleAvailabilityVO>> getVehicleAvailability(
            @RequestParam Long vehicleId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("获取车辆可用性列表，车辆ID：{}，开始日期：{}，结束日期：{}", vehicleId, startDate, endDate);
        
        List<VehicleAvailabilityVO> list = vehicleAvailabilityService.getVehicleAvailability(vehicleId, startDate, endDate);
        return Result.success(list);
    }

    /**
     * 设置车辆可用性
     */
    @PostMapping
    @ApiOperation("设置车辆可用性")
    public Result setVehicleAvailability(@RequestBody VehicleAvailabilityDTO vehicleAvailabilityDTO) {
        log.info("设置车辆可用性：{}", vehicleAvailabilityDTO);
        
        vehicleAvailabilityService.setVehicleAvailability(vehicleAvailabilityDTO);
        return Result.success();
    }

    /**
     * 批量设置车辆可用性
     */
    @PostMapping("/batch")
    @ApiOperation("批量设置车辆可用性")
    public Result batchSetVehicleAvailability(@RequestBody VehicleAvailabilityDTO vehicleAvailabilityDTO) {
        log.info("批量设置车辆可用性：{}", vehicleAvailabilityDTO);
        
        vehicleAvailabilityService.batchSetVehicleAvailability(vehicleAvailabilityDTO);
        return Result.success();
    }

    /**
     * 删除车辆可用性设置
     */
    @DeleteMapping
    @ApiOperation("删除车辆可用性设置")
    public Result deleteVehicleAvailability(
            @RequestParam Long vehicleId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("删除车辆可用性设置，车辆ID：{}，日期：{}", vehicleId, date);
        
        vehicleAvailabilityService.deleteVehicleAvailability(vehicleId, date);
        return Result.success();
    }

    /**
     * 获取车辆可用性统计
     */
    @GetMapping("/stats")
    @ApiOperation("获取车辆可用性统计")
    public Result getVehicleAvailabilityStats(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("获取车辆可用性统计，车辆ID：{}，开始日期：{}，结束日期：{}", vehicleId, startDate, endDate);
        
        // 这里可以返回统计信息，比如可用天数、维护天数等
        return Result.success();
    }
} 