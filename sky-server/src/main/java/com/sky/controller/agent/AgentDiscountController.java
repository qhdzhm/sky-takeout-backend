package com.sky.controller.agent;

import com.sky.context.BaseContext;
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
import java.util.Map;

/**
 * 代理商折扣查询控制器
 */
@RestController
@RequestMapping("/agent/discount")
@Api(tags = "代理商折扣查询接口")
@Slf4j
public class AgentDiscountController {

    @Autowired
    private EnhancedDiscountService enhancedDiscountService;

    @GetMapping("/my-configs")
    @ApiOperation("查询我的所有折扣配置")
    public Result<List<ProductAgentDiscount>> getMyDiscountConfigs() {
        Long agentId = BaseContext.getCurrentId();
        log.info("代理商查询自己的折扣配置，代理商ID: {}", agentId);
        
        List<ProductAgentDiscount> configs = enhancedDiscountService.getAgentDiscountConfigs(agentId);
        return Result.success(configs);
    }

    @GetMapping("/product-rate")
    @ApiOperation("查询对特定产品的折扣率")
    public Result<BigDecimal> getProductDiscountRate(
            @RequestParam String productType,
            @RequestParam Long productId) {
        Long agentId = BaseContext.getCurrentId();
        log.info("代理商查询产品折扣率，代理商ID: {}, 产品类型: {}, 产品ID: {}", agentId, productType, productId);
        
        BigDecimal discountRate = enhancedDiscountService.getAgentProductDiscountRate(agentId, productType, productId);
        return Result.success(discountRate);
    }

    @GetMapping("/calculate")
    @ApiOperation("计算产品折扣价格")
    public Result<Map<String, Object>> calculateProductDiscount(
            @RequestParam String productType,
            @RequestParam Long productId,
            @RequestParam BigDecimal originalPrice) {
        Long agentId = BaseContext.getCurrentId();
        log.info("代理商计算产品折扣，代理商ID: {}, 产品类型: {}, 产品ID: {}, 原价: {}", 
                agentId, productType, productId, originalPrice);
        
        Map<String, Object> result = enhancedDiscountService.calculateProductDiscount(
                productType, productId, originalPrice, agentId, null);
        return Result.success(result);
    }

    @GetMapping("/my-logs")
    @ApiOperation("查询我的折扣使用记录")
    public Result<List<AgentDiscountLog>> getMyDiscountLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Long agentId = BaseContext.getCurrentId();
        log.info("代理商查询自己的折扣使用记录，代理商ID: {}, 开始时间: {}, 结束时间: {}", agentId, startTime, endTime);
        
        List<AgentDiscountLog> logs = enhancedDiscountService.getAgentDiscountLogs(agentId, startTime, endTime);
        return Result.success(logs);
    }
} 