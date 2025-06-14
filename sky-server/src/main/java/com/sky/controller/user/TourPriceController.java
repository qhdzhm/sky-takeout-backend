package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.TourBookingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 旅游价格计算控制器
 */
@RestController
@RequestMapping("/user/tour-price")
@Api(tags = "旅游价格计算相关接口")
@Slf4j
// CORS现在由全局CorsFilter处理，移除@CrossOrigin注解
public class TourPriceController {

    @Autowired
    private TourBookingService tourBookingService;
    
    /**
     * 计算旅游价格（包含酒店等级差价）
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游类型 (day_tour/group_tour)
     * @param groupSize 团队人数
     * @param agentId 代理商ID (可选)
     * @param hotelLevel 酒店等级 (可选，默认为"4星")
     * @return 计算后的价格信息
     */
    @GetMapping("/calculate")
    @ApiOperation("计算旅游价格（包含酒店等级差价）")
    public Result<Map<String, Object>> calculatePrice(
            @RequestParam Integer tourId,
            @RequestParam String tourType,
            @RequestParam Integer groupSize,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false, defaultValue = "4星") String hotelLevel) {
        
        log.info("计算旅游价格 - tourId: {}, tourType: {}, groupSize: {}, agentId: {}, hotelLevel: {}",
                tourId, tourType, groupSize, agentId, hotelLevel);
        
        try {
            // 调用服务计算价格
            BigDecimal totalPrice = tourBookingService.calculateTotalPrice(
                    tourId, tourType, agentId, groupSize, hotelLevel);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("tourId", tourId);
            result.put("tourType", tourType);
            result.put("groupSize", groupSize);
            result.put("agentId", agentId);
            result.put("hotelLevel", hotelLevel);
            result.put("totalPrice", totalPrice);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("计算旅游价格失败: {}", e.getMessage(), e);
            return Result.error("计算价格失败: " + e.getMessage());
        }
    }
} 