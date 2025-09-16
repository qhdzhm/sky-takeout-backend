package com.sky.controller.admin;

import com.sky.entity.AgentDiscountLevel;
import com.sky.entity.AgentDiscountLog;
import com.sky.entity.ProductAgentDiscount;
import com.sky.result.Result;
import com.sky.service.EnhancedDiscountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 折扣管理控制器（管理员使用）
 */
@RestController
@RequestMapping("/admin/discount")
@Api(tags = "折扣管理接口")
@Slf4j
public class DiscountManagementController {

    @Autowired
    private EnhancedDiscountService enhancedDiscountService;

    // ===================== 折扣等级管理 =====================

    @GetMapping("/levels")
    @ApiOperation("获取所有折扣等级")
    public Result<List<AgentDiscountLevel>> getAllDiscountLevels() {
        log.info("管理员查询所有折扣等级");
        List<AgentDiscountLevel> levels = enhancedDiscountService.getAllDiscountLevels();
        return Result.success(levels);
    }

    @GetMapping("/levels/active")
    @ApiOperation("获取活跃的折扣等级")
    public Result<List<AgentDiscountLevel>> getActiveDiscountLevels() {
        log.info("管理员查询活跃折扣等级");
        List<AgentDiscountLevel> levels = enhancedDiscountService.getActiveDiscountLevels();
        return Result.success(levels);
    }

    @GetMapping("/levels/{id}")
    @ApiOperation("根据ID获取折扣等级")
    public Result<AgentDiscountLevel> getDiscountLevelById(@PathVariable Long id) {
        log.info("管理员查询折扣等级，ID: {}", id);
        AgentDiscountLevel level = enhancedDiscountService.getDiscountLevelById(id);
        return Result.success(level);
    }

    @PostMapping("/levels")
    @ApiOperation("创建折扣等级")
    public Result<AgentDiscountLevel> createDiscountLevel(@RequestBody AgentDiscountLevel level) {
        log.info("管理员创建折扣等级: {}", level);
        AgentDiscountLevel created = enhancedDiscountService.createDiscountLevel(level);
        return Result.success(created);
    }

    @PutMapping("/levels")
    @ApiOperation("更新折扣等级")
    public Result<AgentDiscountLevel> updateDiscountLevel(@RequestBody AgentDiscountLevel level) {
        log.info("管理员更新折扣等级: {}", level);
        AgentDiscountLevel updated = enhancedDiscountService.updateDiscountLevel(level);
        return Result.success(updated);
    }

    @DeleteMapping("/levels/{id}")
    @ApiOperation("删除折扣等级")
    public Result<String> deleteDiscountLevel(@PathVariable Long id) {
        log.info("管理员删除折扣等级，ID: {}", id);
        boolean success = enhancedDiscountService.deleteDiscountLevel(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    // ===================== 产品折扣配置管理 =====================

    @GetMapping("/products")
    @ApiOperation("获取产品的所有折扣配置")
    public Result<List<ProductAgentDiscount>> getProductDiscountConfigs(
            @RequestParam String productType,
            @RequestParam Long productId) {
        log.info("管理员查询产品折扣配置，产品类型: {}, 产品ID: {}", productType, productId);
        List<ProductAgentDiscount> configs = enhancedDiscountService.getProductDiscountConfigs(productType, productId);
        return Result.success(configs);
    }

    @GetMapping("/levels/{levelId}/products")
    @ApiOperation("根据等级ID获取所有折扣配置")
    public Result<List<ProductAgentDiscount>> getDiscountConfigsByLevel(@PathVariable Long levelId) {
        log.info("管理员查询等级折扣配置，等级ID: {}", levelId);
        List<ProductAgentDiscount> configs = enhancedDiscountService.getDiscountConfigsByLevel(levelId);
        return Result.success(configs);
    }

    @PostMapping("/products")
    @ApiOperation("创建产品折扣配置")
    public Result<ProductAgentDiscount> createProductDiscount(@RequestBody ProductAgentDiscount discount) {
        log.info("管理员创建产品折扣配置: {}", discount);
        ProductAgentDiscount created = enhancedDiscountService.createProductDiscount(discount);
        return Result.success(created);
    }

    @PutMapping("/products")
    @ApiOperation("更新产品折扣配置")
    public Result<ProductAgentDiscount> updateProductDiscount(@RequestBody ProductAgentDiscount discount) {
        log.info("管理员更新产品折扣配置: {}", discount);
        ProductAgentDiscount updated = enhancedDiscountService.updateProductDiscount(discount);
        return Result.success(updated);
    }

    @DeleteMapping("/products/{id}")
    @ApiOperation("删除产品折扣配置")
    public Result<String> deleteProductDiscount(@PathVariable Long id) {
        log.info("管理员删除产品折扣配置，ID: {}", id);
        boolean success = enhancedDiscountService.deleteProductDiscount(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    @PutMapping("/levels/{levelId}/batch-update")
    @ApiOperation("批量更新某个等级的产品折扣率")
    public Result<String> batchUpdateDiscountRate(
            @PathVariable Long levelId,
            @RequestParam(required = false) String productType,
            @RequestParam BigDecimal discountRate) {
        log.info("管理员批量更新折扣率，等级ID: {}, 产品类型: {}, 折扣率: {}", levelId, productType, discountRate);
        int count = enhancedDiscountService.batchUpdateDiscountRate(levelId, productType, discountRate);
        return Result.success("成功更新 " + count + " 个配置");
    }

    @PostMapping("/products/batch")
    @ApiOperation("批量创建产品折扣配置")
    public Result<String> batchCreateProductDiscounts(@RequestBody List<ProductAgentDiscount> discounts) {
        log.info("管理员批量创建产品折扣配置，数量: {}", discounts.size());
        int count = enhancedDiscountService.batchCreateProductDiscounts(discounts);
        return Result.success("成功创建 " + count + " 个配置");
    }

} 