package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.GroupTourDTO;
import com.sky.dto.TourDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DiscountService;
import com.sky.service.GroupTourService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户端跟团游控制器
 */
@RestController
@RequestMapping("/user/group-tours")
@Api(tags = "用户端跟团游相关接口")
@Slf4j
public class UserGroupTourController {

    @Autowired
    private GroupTourService groupTourService;
    
    @Autowired
    private DiscountService discountService;

    /**
     * 获取所有跟团游产品
     * @param params 查询参数
     * @return 分页结果
     */
    @GetMapping
    @ApiOperation("获取所有跟团游产品")
    public Result<PageResult> getAllGroupTours(@RequestParam Map<String, Object> params) {
        log.info("获取所有跟团游产品，参数：{}", params);
        
        // 获取代理商ID（如果是代理商用户）
        Long agentId = getAgentIdFromRequest(params);
        
        // 调用服务获取跟团游列表
        PageResult pageResult = groupTourService.getAllGroupTours(params);
        
        // 应用代理商折扣
        if (agentId != null && pageResult != null && pageResult.getRecords() != null) {
            applyDiscountToTours(pageResult.getRecords(), agentId);
        }
        
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取跟团游详情
     * @param id 跟团游ID
     * @param agentId 代理商ID（可选）
     * @return 跟团游详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取跟团游详情")
    public Result<GroupTourDTO> getGroupTourById(
            @PathVariable Integer id,
            @RequestParam(required = false) Long agentId) {
        log.info("根据ID获取跟团游详情，ID：{}，代理商ID：{}", id, agentId);
        
        // 如果没有明确提供代理商ID，尝试从上下文获取
        if (agentId == null) {
            agentId = BaseContext.getCurrentAgentId();
        }
        
        // 调用服务获取跟团游详情
        GroupTourDTO groupTourDTO = groupTourService.getGroupTourById(id);
        
        // 应用代理商折扣
        if (agentId != null && groupTourDTO != null && groupTourDTO.getPrice() != null) {
            BigDecimal discountedPrice = discountService.getDiscountedPrice(groupTourDTO.getPrice(), agentId);
            groupTourDTO.setDiscountedPrice(discountedPrice);
        }
        
        return Result.success(groupTourDTO);
    }

    /**
     * 获取跟团游行程
     * @param tourId 跟团游ID
     * @return 跟团游行程
     */
    @GetMapping("/{id}/itinerary")
    @ApiOperation("获取跟团游行程")
    public Result<List<Map<String, Object>>> getGroupTourItinerary(@PathVariable("id") Integer tourId) {
        log.info("获取跟团游行程，ID：{}", tourId);
        List<Map<String, Object>> itinerary = groupTourService.getGroupTourItinerary(tourId);
        return Result.success(itinerary);
    }

    /**
     * 获取跟团游包含项
     * @param tourId 跟团游ID
     * @return 跟团游包含项
     */
    @GetMapping("/{id}/inclusions")
    @ApiOperation("获取跟团游包含项")
    public Result<List<String>> getGroupTourInclusions(@PathVariable("id") Integer tourId) {
        log.info("获取跟团游包含项，ID：{}", tourId);
        List<String> inclusions = groupTourService.getGroupTourInclusions(tourId);
        return Result.success(inclusions);
    }

    /**
     * 获取跟团游不包含项
     * @param tourId 跟团游ID
     * @return 跟团游不包含项
     */
    @GetMapping("/{id}/exclusions")
    @ApiOperation("获取跟团游不包含项")
    public Result<List<String>> getGroupTourExclusions(@PathVariable("id") Integer tourId) {
        log.info("获取跟团游不包含项，ID：{}", tourId);
        List<String> exclusions = groupTourService.getGroupTourExclusions(tourId);
        return Result.success(exclusions);
    }

    /**
     * 获取跟团游常见问题
     * @param tourId 跟团游ID
     * @return 跟团游常见问题
     */
    @GetMapping("/{id}/faqs")
    @ApiOperation("获取跟团游常见问题")
    public Result<List<Map<String, Object>>> getGroupTourFaqs(@PathVariable("id") Integer tourId) {
        log.info("获取跟团游常见问题，ID：{}", tourId);
        List<Map<String, Object>> faqs = groupTourService.getGroupTourFaqs(tourId);
        return Result.success(faqs);
    }

    /**
     * 获取跟团游主题
     * @return 跟团游主题列表
     */
    @GetMapping("/themes")
    @ApiOperation("获取跟团游主题")
    public Result<List<Map<String, Object>>> getGroupTourThemes() {
        log.info("获取跟团游主题");
        List<Map<String, Object>> themes = groupTourService.getGroupTourThemes();
        return Result.success(themes);
    }

    /**
     * 获取跟团游亮点
     * @param tourId 跟团游ID
     * @return 跟团游亮点
     */
    @GetMapping("/{id}/highlights")
    @ApiOperation("获取跟团游亮点")
    public Result<List<String>> getGroupTourHighlights(@PathVariable("id") Integer tourId) {
        log.info("获取跟团游亮点，ID：{}", tourId);
        List<String> highlights = groupTourService.getGroupTourHighlights(tourId);
        return Result.success(highlights);
    }

    /**
     * 获取跟团游可用日期
     * @param tourId 跟团游ID
     * @param params 查询参数
     * @return 可用日期列表
     */
    @GetMapping("/{id}/available-dates")
    @ApiOperation("获取跟团游可用日期")
    public Result<List<Map<String, Object>>> getGroupTourAvailableDates(
            @PathVariable("id") Integer tourId,
            @RequestParam Map<String, Object> params) {
        log.info("获取跟团游可用日期，ID：{}，参数：{}", tourId, params);
        List<Map<String, Object>> availableDates = groupTourService.getGroupTourAvailableDates(tourId, params);
        return Result.success(availableDates);
    }

    /**
     * 获取跟团游图片
     * @param tourId 跟团游ID
     * @return 图片列表
     */
    @GetMapping("/{id}/images")
    @ApiOperation("获取跟团游图片")
    public Result<List<Map<String, Object>>> getGroupTourImages(@PathVariable("id") Integer tourId) {
        log.info("获取跟团游图片，ID：{}", tourId);
        List<Map<String, Object>> images = groupTourService.getGroupTourImages(tourId);
        return Result.success(images);
    }

    /**
     * 获取跟团游旅行提示
     * @param tourId 跟团游ID
     * @return 旅行提示列表
     */
    @GetMapping("/{id}/tips")
    @ApiOperation("获取跟团游旅行提示")
    public Result<List<String>> getGroupTourTips(@PathVariable("id") Integer tourId) {
        log.info("获取跟团游旅行提示，ID：{}", tourId);
        List<String> tips = groupTourService.getGroupTourTips(tourId);
        return Result.success(tips);
    }
    
    /**
     * 为旅游列表应用折扣
     * @param tours 旅游列表
     * @param agentId 代理商ID
     */
    private void applyDiscountToTours(List<?> tours, Long agentId) {
        if (tours == null || tours.isEmpty() || agentId == null) {
            return;
        }
        
        for (Object tour : tours) {
            try {
                if (tour instanceof GroupTourDTO) {
                    GroupTourDTO tourDTO = (GroupTourDTO) tour;
                    if (tourDTO.getPrice() != null) {
                        BigDecimal discountedPrice = discountService.getDiscountedPrice(tourDTO.getPrice(), agentId);
                        tourDTO.setDiscountedPrice(discountedPrice);
                    }
                } else if (tour instanceof TourDTO) {
                    TourDTO tourDTO = (TourDTO) tour;
                    if (tourDTO.getPrice() != null) {
                        BigDecimal discountedPrice = discountService.getDiscountedPrice(tourDTO.getPrice(), agentId);
                        tourDTO.setDiscountedPrice(discountedPrice);
                    }
                }
            } catch (Exception e) {
                log.error("应用折扣失败：", e);
            }
        }
    }
    
    /**
     * 从请求参数中获取代理商ID
     * @param params 请求参数
     * @return 代理商ID或null
     */
    private Long getAgentIdFromRequest(Map<String, Object> params) {
        // 首先检查请求参数中是否包含代理商ID
        if (params != null && params.containsKey("agentId") && params.get("agentId") != null) {
            try {
                return Long.valueOf(params.get("agentId").toString());
            } catch (NumberFormatException e) {
                log.warn("无效的代理商ID参数：{}", params.get("agentId"));
            }
        }
        
        // 如果请求参数中没有代理商ID，尝试从当前用户上下文中获取
        try {
            Long agentId = BaseContext.getCurrentAgentId();
            if (agentId != null) {
                return agentId;
            }
        } catch (Exception e) {
            log.warn("从用户上下文获取代理商ID失败：", e);
        }
        
        return null;
    }
}
