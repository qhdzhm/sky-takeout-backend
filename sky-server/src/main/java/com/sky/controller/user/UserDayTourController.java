package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.DayTourDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DayTourService;
import com.sky.service.DiscountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端一日游控制器
 */
@RestController
@RequestMapping("/user/day-tours")
@Api(tags = "用户端一日游相关接口")
@Slf4j
// CORS现在由全局CorsFilter处理，移除@CrossOrigin注解
public class UserDayTourController {

    @Autowired
    private DayTourService dayTourService;
    
    @Autowired
    private DiscountService discountService;

    /**
     * 获取所有一日游
     * @param params 查询参数
     * @return 分页结果
     */
    @GetMapping
    @ApiOperation("获取所有一日游")
    public Result<PageResult> getDayTours(@RequestParam Map<String, Object> params) {
        log.info("获取所有一日游，参数：{}", params);
        PageResult pageResult = dayTourService.getAllDayTours(params);
        return Result.success(pageResult);
    }

    /**
     * 获取一日游详情
     * @param id 一日游ID
     * @param agentId 代理商ID（可选）
     * @return 一日游详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取一日游详情")
    public Result<DayTourDTO> getDayTour(@PathVariable Integer id, 
                                         @RequestParam(required = false) Long agentId) {
        log.info("开始获取一日游详情，id：{}，agentId：{}", id, agentId);

        // 如果没有明确提供代理商ID，尝试从上下文获取
        if (agentId == null) {
            agentId = BaseContext.getCurrentId();
            log.info("从上下文获取代理商ID：{}", agentId);
        }
        
        DayTourDTO dayTourDTO = dayTourService.getDayTourById(id);
        
        if (dayTourDTO == null) {
            log.error("找不到ID为{}的一日游", id);
            return Result.error("找不到指定的一日游");
        }
        
        // 确保ID字段一致性 - 最后的保障措施
        if (dayTourDTO.getId() == null && dayTourDTO.getDayTourId() != null) {
            dayTourDTO.setId(dayTourDTO.getDayTourId());
            log.info("Controller中设置id=dayTourId：{}", dayTourDTO.getDayTourId());
        } else if (dayTourDTO.getDayTourId() == null && dayTourDTO.getId() != null) {
            dayTourDTO.setDayTourId(dayTourDTO.getId());
            log.info("Controller中设置dayTourId=id：{}", dayTourDTO.getId());
        } else if (dayTourDTO.getId() == null && dayTourDTO.getDayTourId() == null) {
            // 两个ID都为空，直接使用传入的ID参数
            dayTourDTO.setId(id);
            dayTourDTO.setDayTourId(id);
            log.warn("ID字段都为空，使用请求参数ID：{}", id);
        }
        
        // 应用代理商折扣
        if (agentId != null && dayTourDTO.getPrice() != null) {
            try {
                BigDecimal discountedPrice = discountService.getDiscountedPrice(dayTourDTO.getPrice(), agentId);
                dayTourDTO.setDiscountedPrice(discountedPrice);
                log.info("应用代理商折扣，原价：{}，折扣价：{}", dayTourDTO.getPrice(), discountedPrice);
            } catch (Exception e) {
                log.error("应用折扣失败：{}", e.getMessage());
                // 如果折扣应用失败，使用原价
                dayTourDTO.setDiscountedPrice(dayTourDTO.getPrice());
            }
        } else if (dayTourDTO.getPrice() != null && dayTourDTO.getDiscountedPrice() == null) {
            // 如果不是代理商或无法应用折扣，使用原价
            dayTourDTO.setDiscountedPrice(dayTourDTO.getPrice());
        }
        
        // 检查数据完整性
        log.info("返回一日游详情，dayTourId：{}，id：{}，name：{}，价格：{}，折扣价：{}", 
                dayTourDTO.getDayTourId(), 
                dayTourDTO.getId(), 
                dayTourDTO.getName(), 
                dayTourDTO.getPrice(), 
                dayTourDTO.getDiscountedPrice());
                
        if (dayTourDTO.getImages() != null) {
            log.info("图片数量：{}", dayTourDTO.getImages().size());
        } else {
            log.warn("没有图片数据");
        }
        
        if (dayTourDTO.getItinerary() != null) {
            log.info("行程数量：{}", dayTourDTO.getItinerary().size());
        } else {
            log.warn("没有行程数据");
        }
        
        // 确保基本返回字段的完整性
        if (dayTourDTO.getHighlights() == null) {
            dayTourDTO.setHighlights(new ArrayList<>());
        }
        if (dayTourDTO.getInclusions() == null) {
            dayTourDTO.setInclusions(new ArrayList<>());
        }
        if (dayTourDTO.getExclusions() == null) {
            dayTourDTO.setExclusions(new ArrayList<>());
        }
        if (dayTourDTO.getFaqs() == null) {
            dayTourDTO.setFaqs(new ArrayList<>());
        }
        if (dayTourDTO.getTips() == null) {
            dayTourDTO.setTips(new ArrayList<>());
        }
        if (dayTourDTO.getItinerary() == null) {
            dayTourDTO.setItinerary(new ArrayList<>());
        }
        if (dayTourDTO.getImages() == null && dayTourDTO.getImageUrl() != null) {
            // 如果没有images但有imageUrl，创建一个包含主图的images列表
            List<Map<String, Object>> images = new ArrayList<>();
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("image_url", dayTourDTO.getImageUrl());
            imageMap.put("thumbnail_url", dayTourDTO.getImageUrl());
            imageMap.put("description", dayTourDTO.getName());
            imageMap.put("is_primary", true);
            images.add(imageMap);
            dayTourDTO.setImages(images);
            log.info("在Controller中使用imageUrl创建默认images: {}", dayTourDTO.getImageUrl());
        }
        if (dayTourDTO.getThemes() == null && dayTourDTO.getThemeIds() != null && dayTourDTO.getThemeIds().length > 0) {
            // 如果有themeIds但没有themes，使用默认名称
            List<String> themes = new ArrayList<>();
            themes.add(dayTourDTO.getCategory() != null ? dayTourDTO.getCategory() : "自然风光");
            dayTourDTO.setThemes(themes);
            log.info("在Controller中创建默认themes: {}", dayTourDTO.getThemes());
        }
        if (dayTourDTO.getSuitableFor() == null && dayTourDTO.getSuitableIds() != null && dayTourDTO.getSuitableIds().length > 0) {
            // 如果有suitableIds但没有suitableFor，使用默认名称
            List<String> suitableFor = new ArrayList<>();
            suitableFor.add("适合所有人");
            dayTourDTO.setSuitableFor(suitableFor);
            log.info("在Controller中创建默认suitableFor: {}", dayTourDTO.getSuitableFor());
        }
        
        return Result.success(dayTourDTO);
    }

    /**
     * 获取一日游行程安排
     * @param id 一日游ID
     * @param params 查询参数
     * @return 行程安排列表
     */
    @GetMapping("/{id}/schedules")
    @ApiOperation("获取一日游行程安排")
    public Result<List<Map<String, Object>>> getDayTourSchedules(@PathVariable Integer id, @RequestParam Map<String, Object> params) {
        log.info("获取一日游行程安排，id：{}，参数：{}", id, params);
        List<Map<String, Object>> schedules = dayTourService.getDayTourSchedules(id, params);
        return Result.success(schedules);
    }


    /**
     * 获取一日游主题列表
     * @return 主题列表
     */
    @GetMapping("/themes")
    @ApiOperation("获取一日游主题列表")
    public Result<List<Map<String, Object>>> getDayTourThemes() {
        log.info("获取一日游主题列表");
        List<Map<String, Object>> themes = dayTourService.getDayTourThemes();
        return Result.success(themes);
    }

    /**
     * 根据一日游ID获取主题列表
     * @param id 一日游ID
     * @return 主题列表
     */
    @GetMapping("/{id}/themes")
    @ApiOperation("根据一日游ID获取主题列表")
    public Result<List<Map<String, Object>>> getDayTourThemesByTourId(@PathVariable Integer id) {
        log.info("根据一日游ID获取主题列表，id：{}", id);
        List<Map<String, Object>> themes = dayTourService.getDayTourThemesByTourId(id);
        return Result.success(themes);
    }

    /**
     * 按主题获取一日游
     * @param theme 主题名称
     * @param params 查询参数
     * @return 分页结果
     */
    @GetMapping("/theme/{theme}")
    @ApiOperation("按主题获取一日游")
    public Result<PageResult> getDayToursByTheme(@PathVariable String theme, @RequestParam Map<String, Object> params) {
        log.info("按主题获取一日游，theme：{}，参数：{}", theme, params);
        
        // 将主题添加到查询参数中
        params.put("category", theme);
        
        PageResult pageResult = dayTourService.getAllDayTours(params);
        return Result.success(pageResult);
    }
} 