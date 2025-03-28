package com.sky.controller.admin;

import com.sky.dto.VehicleDriverDTO;
import com.sky.entity.Employee;
import com.sky.entity.Vehicle;
import com.sky.result.Result;
import com.sky.service.VehicleDriverService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车辆驾驶员管理控制器
 */
@RestController
@RequestMapping("/admin/vehicleDriver")
@Api(tags = "车辆驾驶员管理接口")
@Slf4j
public class VehicleDriverController {

    @Autowired
    private VehicleDriverService vehicleDriverService;
    
    /**
     * 分配车辆给员工
     */
    @PostMapping("/assign")
    @ApiOperation("分配车辆给员工")
    public Result assign(@RequestBody VehicleDriverDTO vehicleDriverDTO) {
        log.info("分配车辆给员工：{}", vehicleDriverDTO);
        vehicleDriverService.assignVehicle(vehicleDriverDTO);
        return Result.success();
    }
    
    /**
     * 取消分配车辆给员工
     */
    @PostMapping("/unassign")
    @ApiOperation("取消分配车辆给员工")
    public Result unassign(@RequestBody VehicleDriverDTO vehicleDriverDTO) {
        log.info("取消分配车辆给员工：{}", vehicleDriverDTO);
        vehicleDriverService.unassignVehicle(vehicleDriverDTO);
        return Result.success();
    }
    
    /**
     * 获取车辆的所有驾驶员
     */
    @GetMapping("/drivers/{vehicleId}")
    @ApiOperation("获取车辆的所有驾驶员")
    public Result<List<Employee>> getDriversByVehicleId(@PathVariable Long vehicleId) {
        log.info("获取车辆的所有驾驶员，车辆ID：{}", vehicleId);
        List<Employee> drivers = vehicleDriverService.getDriversByVehicleId(vehicleId);
        return Result.success(drivers);
    }
    
    /**
     * 获取员工分配的所有车辆
     */
    @GetMapping("/vehicles/{employeeId}")
    @ApiOperation("获取员工分配的所有车辆")
    public Result<List<Vehicle>> getVehiclesByEmployeeId(@PathVariable Long employeeId) {
        log.info("获取员工分配的所有车辆，员工ID：{}", employeeId);
        List<Vehicle> vehicles = vehicleDriverService.getVehiclesByEmployeeId(employeeId);
        return Result.success(vehicles);
    }
} 