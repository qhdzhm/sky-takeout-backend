package com.sky.controller.admin;

import com.sky.dto.VehiclePageQueryDTO;
import com.sky.entity.Vehicle;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.VehicleService;
import com.sky.vo.VehicleWithDriversVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/vehicle")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

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
    public Result addVehicle(@RequestBody Vehicle vehicle) {
        log.info("新增车辆：{}", vehicle);
        vehicleService.addVehicle(vehicle);
        return Result.success();
    }

    /**
     * 更新车辆信息
     */
    @PutMapping
    public Result updateVehicle(@RequestBody Vehicle vehicle) {
        log.info("更新车辆信息：{}", vehicle);
        vehicleService.updateVehicle(vehicle);
        return Result.success();
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/{id}")
    public Result deleteVehicle(@PathVariable Long id) {
        log.info("删除车辆：id={}", id);
        vehicleService.deleteVehicle(id);
        return Result.success();
    }
    
    /**
     * 获取车辆详情（包含驾驶员信息）
     */
    @GetMapping("/details/{id}")
    public Result<VehicleWithDriversVO> getVehicleWithDrivers(@PathVariable Long id) {
        log.info("获取车辆详情（包含驾驶员信息）：id={}", id);
        VehicleWithDriversVO vo = vehicleService.getVehicleWithDrivers(id);
        return Result.success(vo);
    }
    
    /**
     * 获取所有可分配车辆
     */
    @GetMapping("/available")
    public Result<List<VehicleWithDriversVO>> getAllAvailableVehicles() {
        log.info("获取所有可分配车辆");
        List<VehicleWithDriversVO> list = vehicleService.getAllAvailableVehicles();
        return Result.success(list);
    }
}