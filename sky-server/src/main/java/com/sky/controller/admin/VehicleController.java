package com.sky.controller.admin;

import com.sky.dto.VehiclePageQueryDTO;
import com.sky.entity.Vehicle;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.VehicleService;
import com.sky.service.VehicleAvailabilityService;
import com.sky.scheduler.VehicleStatusScheduler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/vehicle")
@Slf4j
// CORS现在由全局CorsFilter处理，移除@CrossOrigin注解
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleAvailabilityService vehicleAvailabilityService;

    @Autowired
    private VehicleStatusScheduler vehicleStatusScheduler;

    /**
     * 分页查询车辆
     */
    @GetMapping("/page")
    public Result<PageResult> pageQuery(VehiclePageQueryDTO vehiclePageQueryDTO) {
        log.info("分页查询车辆：{}", vehiclePageQueryDTO);
        PageResult pageResult = vehicleService.pageQuery(vehiclePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询车辆
     */
    @GetMapping("/{id}")
    public Result<Vehicle> getById(@PathVariable Long id) {
        log.info("根据ID查询车辆：id={}", id);
        Vehicle vehicle = vehicleService.getById(id);
        return Result.success(vehicle);
    }

    /**
     * 新增车辆
     */
    @PostMapping
    public Result<String> addVehicle(@RequestBody Vehicle vehicle) {
        log.info("新增车辆：{}", vehicle);
        vehicleService.addVehicle(vehicle);
        return Result.success();
    }

    /**
     * 更新车辆信息
     */
    @PutMapping
    public Result<String> updateVehicle(@RequestBody Vehicle vehicle) {
        log.info("更新车辆信息：{}", vehicle);
        vehicleService.updateVehicle(vehicle);
        return Result.success();
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteVehicle(@PathVariable Long id) {
        log.info("删除车辆：id={}", id);
        vehicleService.deleteVehicle(id);
        return Result.success();
    }

    /**
     * 手动同步所有过期车辆状态到可用性表
     */
    @PostMapping("/sync-expired-status")
    public Result<String> syncExpiredVehicleStatus() {
        log.info("管理员手动触发车辆过期状态同步");
        
        try {
            vehicleStatusScheduler.manualSyncExpiredVehicleStatus();
            return Result.success("车辆状态同步完成");
        } catch (Exception e) {
            log.error("手动同步车辆状态失败", e);
            return Result.error("车辆状态同步失败: " + e.getMessage());
        }
    }

    /**
     * 手动同步单个车辆状态到可用性表
     */
    @PostMapping("/{id}/sync-status")
    public Result<String> syncSingleVehicleStatus(@PathVariable Long id) {
        log.info("手动触发单个车辆状态同步：id={}", id);
        
        try {
            vehicleAvailabilityService.syncSingleVehicleExpiredStatus(id);
            return Result.success("车辆 " + id + " 状态同步完成");
        } catch (Exception e) {
            log.error("手动同步单个车辆状态失败，id={}", id, e);
            return Result.error("车辆状态同步失败: " + e.getMessage());
        }
    }
}