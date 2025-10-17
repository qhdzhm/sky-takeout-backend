package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.TourDTO;
import com.sky.dto.TourRequestParams;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DiscountService;
import com.sky.service.TourService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用旅游控制器
 */
@RestController
@RequestMapping("/user/tours")
@Api(tags = "通用旅游相关接口")
@Slf4j
public class TourController {

    @Autowired
    private TourService tourService;
    
    @Autowired
    private DiscountService discountService;

    /**
     * 获取所有旅游产品
     * @param params 查询参数
     * @return 分页结果
     */
    @GetMapping
    @ApiOperation("获取所有旅游产品")
    public Result<PageResult> getAllTours(@RequestParam Map<String, Object> params) {
        log.info("获取所有旅游产品，参数：{}", params);
        
        // 获取代理商ID（如果是代理商用户）
        Long agentId = getAgentIdFromRequest(params);
        
        // 获取旅游产品列表
        PageResult pageResult = tourService.getAllTours(params);
        
        // 应用代理商折扣
        if (agentId != null && pageResult != null && pageResult.getRecords() != null) {
            applyDiscountToTours(pageResult.getRecords(), agentId);
        }
        
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取旅游产品
     * @param id 旅游产品ID
     * @param tourType 旅游类型（day或group）
     * @param agentId 代理商ID（可选）
     * @return 旅游产品信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取旅游产品")
    public Result<TourDTO> getTourById(
            @PathVariable Integer id,
            @RequestParam(required = true) String tourType,
            @RequestParam(required = false) Long agentId) {
        log.info("根据ID获取旅游产品，ID：{}，类型：{}，代理商ID：{}", id, tourType, agentId);
        
        // 如果没有明确提供代理商ID，尝试从上下文获取
        if (agentId == null) {
            agentId = BaseContext.getCurrentId();
        }
        
        // 验证tourType参数
        if (tourType == null || (!tourType.equals("day") && !tourType.equals("group"))) {
            log.warn("无效的旅游类型: {}", tourType);
            return Result.error("旅游类型必须是'day'或'group'");
        }
        
        // 获取旅游产品
        TourDTO tourDTO = tourService.getTourById(id, tourType);
        
        // 应用代理商折扣
        if (agentId != null && tourDTO != null && tourDTO.getPrice() != null) {
            BigDecimal discountedPrice = discountService.getDiscountedPrice(tourDTO.getPrice(), agentId);
            tourDTO.setDiscountedPrice(discountedPrice);
        }
        
        return Result.success(tourDTO);
    }

    /**
     * 搜索旅游产品
     * @param requestParams 请求参数
     * @return 旅游产品列表
     */
    @PostMapping("/search")
    @ApiOperation("搜索旅游产品")
    public Result<List<TourDTO>> searchTours(@RequestBody TourRequestParams requestParams) {
        log.info("搜索旅游产品，参数：{}", requestParams);
        
        // 将请求参数转换为Map
        Map<String, Object> params = convertRequestParamsToMap(requestParams);
        
        // 获取代理商ID
        Long agentId = requestParams.getAgentId();
        
        // 搜索旅游产品
        List<TourDTO> tours = tourService.searchTours(params);
        
        // 应用代理商折扣
        if (agentId != null && tours != null) {
            applyDiscountToTours(tours, agentId);
        }
        
        return Result.success(tours);
    }

    /**
     * 获取热门旅游产品
     * @param limit 限制数量
     * @param agentId 代理商ID（可选）
     * @return 热门旅游产品列表
     */
    @GetMapping("/hot")
    @ApiOperation("获取热门旅游产品")
    public Result<List<TourDTO>> getHotTours(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Long agentId) {
        log.info("获取热门旅游产品，限制数量：{}，代理商ID：{}", limit, agentId);
        
        // 获取热门旅游产品
        List<TourDTO> tours = tourService.getHotTours(limit);
        
        // 应用代理商折扣
        if (agentId != null && tours != null) {
            applyDiscountToTours(tours, agentId);
        }
        
        return Result.success(tours);
    }

    /**
     * 获取推荐旅游产品
     * @param limit 限制数量
     * @param agentId 代理商ID（可选）
     * @return 推荐旅游产品列表
     */
    @GetMapping("/recommended")
    @ApiOperation("获取推荐旅游产品")
    public Result<List<TourDTO>> getRecommendedTours(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Long agentId) {
        log.info("获取推荐旅游产品，限制数量：{}，代理商ID：{}", limit, agentId);
        
        // 获取推荐旅游产品
        List<TourDTO> tours = tourService.getRecommendedTours(limit);
        
        // 应用代理商折扣
        if (agentId != null && tours != null) {
            applyDiscountToTours(tours, agentId);
        }
        
        return Result.success(tours);
    }

    /**
     * 获取热门一日游
     * @param limit 限制数量
     * @return 热门一日游列表
     */
    @GetMapping("/day-tours/hot")
    @ApiOperation("获取热门一日游")
    public Result<List<TourDTO>> getHotDayTours(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门一日游，限制数量：{}", limit);
        List<TourDTO> tours = tourService.getHotDayTours(limit);
        return Result.success(tours);
    }

    /**
     * 获取热门跟团游
     * @param limit 限制数量
     * @return 热门跟团游列表
     */
    @GetMapping("/group-tours/hot")
    @ApiOperation("获取热门跟团游")
    public Result<List<TourDTO>> getHotGroupTours(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门跟团游，限制数量：{}", limit);
        List<TourDTO> tours = tourService.getHotGroupTours(limit);
        return Result.success(tours);
    }

    /**
     * 获取基于订单统计的热门产品（近7天）
     * @param days 统计天数，默认7天
     * @param limit 限制数量，默认6个
     * @return 热门产品列表
     */
    @GetMapping("/popular-by-orders")
    @ApiOperation("获取基于订单统计的热门产品")
    public Result<List<Map<String, Object>>> getPopularToursByOrders(
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "6") Integer limit) {
        log.info("获取基于订单统计的热门产品，统计天数：{}，限制数量：{}", days, limit);
        List<Map<String, Object>> tours = tourService.getPopularToursByOrders(days, limit);
        return Result.success(tours);
    }

    /**
     * 获取推荐一日游
     * @param limit 限制数量
     * @return 推荐一日游列表
     */
    @GetMapping("/day-tours/recommended")
    @ApiOperation("获取推荐一日游")
    public Result<List<TourDTO>> getRecommendedDayTours(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取推荐一日游，限制数量：{}", limit);
        List<TourDTO> tours = tourService.getRecommendedDayTours(limit);
        return Result.success(tours);
    }

    /**
     * 获取推荐跟团游
     * @param limit 限制数量
     * @return 推荐跟团游列表
     */
    @GetMapping("/group-tours/recommended")
    @ApiOperation("获取推荐跟团游")
    public Result<List<TourDTO>> getRecommendedGroupTours(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取推荐跟团游，限制数量：{}", limit);
        List<TourDTO> tours = tourService.getRecommendedGroupTours(limit);
        return Result.success(tours);
    }

    /**
     * 获取适合人群选项
     * @return 适合人群选项列表
     */
    @GetMapping("/suitable-for-options")
    @ApiOperation("获取适合人群选项")
    public Result<List<Map<String, Object>>> getSuitableForOptions() {
        log.info("获取适合人群选项");
        List<Map<String, Object>> options = tourService.getSuitableForOptions();
        return Result.success(options);
    }

    /**
     * 获取特定旅游产品的适合人群
     * @param tourId 旅游ID
     * @param tourType 旅游类型
     * @return 适合人群列表
     */
    @GetMapping("/{tourType}/{tourId}/suitable-for")
    @ApiOperation("获取特定旅游产品的适合人群")
    public Result<List<Map<String, Object>>> getSuitableForByTourId(
            @PathVariable("tourId") Integer tourId,
            @PathVariable("tourType") String tourType) {
        log.info("获取旅游产品适合人群，ID：{}，类型：{}", tourId, tourType);
        List<Map<String, Object>> options = tourService.getSuitableForByTourId(tourId, tourType);
        return Result.success(options);
    }
    
    /**
     * 应用代理商折扣到旅游产品列表
     * @param tours 旅游产品列表
     * @param agentId 代理商ID
     */
    private void applyDiscountToTours(List<?> tours, Long agentId) {
        for (Object obj : tours) {
            if (obj instanceof TourDTO) {
                TourDTO tour = (TourDTO) obj;
                if (tour.getPrice() != null) {
                    BigDecimal discountedPrice = discountService.getDiscountedPrice(tour.getPrice(), agentId);
                    tour.setDiscountedPrice(discountedPrice);
                }
            }
        }
    }
    
    /**
     * 从请求参数中获取代理商ID
     * @param params 请求参数
     * @return 代理商ID，如果不存在则返回null
     */
    private Long getAgentIdFromRequest(Map<String, Object> params) {
        // 首先尝试从请求参数中获取
        if (params.containsKey("agentId")) {
            try {
                return Long.valueOf(params.get("agentId").toString());
            } catch (Exception e) {
                log.warn("解析代理商ID失败: {}", e.getMessage());
            }
        }
        
        // 如果请求参数中没有，尝试从线程上下文中获取（代理商JWT拦截器会设置）
        try {
            return BaseContext.getCurrentId();
        } catch (Exception e) {
            log.warn("从上下文获取代理商ID失败: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 将请求参数对象转换为Map
     * @param requestParams 请求参数对象
     * @return Map形式的参数
     */
    private Map<String, Object> convertRequestParamsToMap(TourRequestParams requestParams) {
        // 创建参数Map
        Map<String, Object> params = new HashMap<>();
        
        // 将对象字段添加到Map中，跳过null值
        if (requestParams.getPage() != null) params.put("page", requestParams.getPage());
        if (requestParams.getPageSize() != null) params.put("pageSize", requestParams.getPageSize());
        if (requestParams.getKeyword() != null) params.put("keyword", requestParams.getKeyword());
        if (requestParams.getTourType() != null) params.put("tourType", requestParams.getTourType());
        if (requestParams.getRegionId() != null) params.put("regionId", requestParams.getRegionId());
        if (requestParams.getLocation() != null) params.put("location", requestParams.getLocation());
        if (requestParams.getThemes() != null) params.put("themes", requestParams.getThemes());
        if (requestParams.getMinRating() != null) params.put("minRating", requestParams.getMinRating());
        if (requestParams.getMinPrice() != null) params.put("minPrice", requestParams.getMinPrice());
        if (requestParams.getMaxPrice() != null) params.put("maxPrice", requestParams.getMaxPrice());
        if (requestParams.getMinDuration() != null) params.put("minDuration", requestParams.getMinDuration());
        if (requestParams.getMaxDuration() != null) params.put("maxDuration", requestParams.getMaxDuration());
        if (requestParams.getSuitableFor() != null) params.put("suitableFor", requestParams.getSuitableFor());
        if (requestParams.getStartDate() != null) params.put("startDate", requestParams.getStartDate());
        if (requestParams.getEndDate() != null) params.put("endDate", requestParams.getEndDate());
        
        return params;
    }
} 