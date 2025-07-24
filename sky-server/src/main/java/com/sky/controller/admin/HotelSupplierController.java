package com.sky.controller.admin;

import com.sky.entity.HotelSupplier;
import com.sky.result.Result;
import com.sky.service.HotelSupplierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 酒店供应商管理Controller
 */
@RestController
@RequestMapping("/admin/hotel-suppliers")
@Api(tags = "酒店供应商管理接口")
@Slf4j
public class HotelSupplierController {

    @Autowired
    private HotelSupplierService hotelSupplierService;

    /**
     * 获取酒店供应商列表
     */
    @GetMapping
    @ApiOperation("获取酒店供应商列表")
    public Result<List<HotelSupplier>> getSuppliers() {
        log.info("获取酒店供应商列表");
        List<HotelSupplier> suppliers = hotelSupplierService.getAllSuppliers();
        return Result.success(suppliers);
    }

    /**
     * 根据ID获取供应商详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取供应商详情")
    public Result<HotelSupplier> getSupplierById(@PathVariable Long id) {
        log.info("根据ID获取供应商详情：{}", id);
        HotelSupplier supplier = hotelSupplierService.getSupplierById(id);
        return Result.success(supplier);
    }

    /**
     * 创建供应商
     */
    @PostMapping
    @ApiOperation("创建供应商")
    public Result<String> createSupplier(@RequestBody HotelSupplier supplier) {
        log.info("创建供应商：{}", supplier);
        hotelSupplierService.createSupplier(supplier);
        return Result.success("创建成功");
    }

    /**
     * 更新供应商（通过请求体中的ID）
     */
    @PutMapping
    @ApiOperation("更新供应商")
    public Result<String> updateSupplier(@RequestBody HotelSupplier supplier) {
        log.info("更新供应商：{}", supplier);
        hotelSupplierService.updateSupplier(supplier);
        return Result.success("更新成功");
    }

    /**
     * 更新供应商（通过路径参数ID）
     */
    @PutMapping("/{id}")
    @ApiOperation("更新供应商")
    public Result<String> updateSupplierById(@PathVariable Long id, @RequestBody HotelSupplier supplier) {
        log.info("更新供应商：{}, {}", id, supplier);
        supplier.setId(id.intValue());
        hotelSupplierService.updateSupplier(supplier);
        return Result.success("更新成功");
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除供应商")
    public Result<String> deleteSupplier(@PathVariable Long id) {
        log.info("删除供应商：{}", id);
        hotelSupplierService.deleteSupplier(id);
        return Result.success("删除成功");
    }
} 