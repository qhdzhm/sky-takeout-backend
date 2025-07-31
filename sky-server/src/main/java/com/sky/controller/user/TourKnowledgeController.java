package com.sky.controller.user;

import com.sky.dto.TourDTO;
import com.sky.result.Result;
import com.sky.service.TourKnowledgeService;
import com.sky.vo.TourRecommendationResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 旅游产品知识控制器（用于测试）
 */
@RestController
@RequestMapping("/user/tour-knowledge")
@Api(tags = "旅游产品知识相关接口")
@Slf4j
public class TourKnowledgeController {
    
    @Autowired
    private TourKnowledgeService tourKnowledgeService;
    
    /**
     * 测试产品推荐
     */
    @PostMapping("/recommendations")
    @ApiOperation("获取产品推荐")
    public Result<TourRecommendationResponse> getRecommendations(
            @RequestParam String query,
            @RequestParam(required = false) Long agentId) {
        log.info("测试产品推荐，查询: {}, 代理商ID: {}", query, agentId);
        
        TourRecommendationResponse response = tourKnowledgeService.getProductRecommendations(query, agentId);
        return Result.success(response);
    }
    
    /**
     * 测试关键词搜索
     */
    @GetMapping("/search")
    @ApiOperation("关键词搜索产品")
    public Result<List<TourDTO>> searchByKeywords(
            @RequestParam String keywords,
            @RequestParam(defaultValue = "all") String tourType,
            @RequestParam(required = false) Long agentId) {
        log.info("测试关键词搜索，关键词: {}, 类型: {}, 代理商ID: {}", keywords, tourType, agentId);
        
        List<TourDTO> tours = tourKnowledgeService.searchToursByKeywords(keywords, tourType, agentId);
        return Result.success(tours);
    }
    
    /**
     * 测试热门产品
     */
    @GetMapping("/hot")
    @ApiOperation("获取热门产品")
    public Result<List<TourDTO>> getHotTours(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Long agentId) {
        log.info("测试热门产品，限制: {}, 代理商ID: {}", limit, agentId);
        
        List<TourDTO> tours = tourKnowledgeService.getHotTours(limit, agentId);
        return Result.success(tours);
    }
    
    /**
     * 测试地区产品
     */
    @GetMapping("/region/{regionName}")
    @ApiOperation("按地区获取产品")
    public Result<List<TourDTO>> getToursByRegion(
            @PathVariable String regionName,
            @RequestParam(defaultValue = "all") String tourType,
            @RequestParam(required = false) Long agentId) {
        log.info("测试地区产品，地区: {}, 类型: {}, 代理商ID: {}", regionName, tourType, agentId);
        
        List<TourDTO> tours = tourKnowledgeService.getToursByRegion(regionName, tourType, agentId);
        return Result.success(tours);
    }
    
    /**
     * 获取AI系统提示
     */
    @GetMapping("/ai-prompt")
    @ApiOperation("获取AI系统提示")
    public Result<String> getAIPrompt() {
        log.info("获取AI系统提示");
        
        String prompt = tourKnowledgeService.generateAISystemPrompt();
        return Result.success(prompt);
    }
    
    /**
     * 获取所有主题
     */
    @GetMapping("/themes")
    @ApiOperation("获取所有主题")
    public Result<List<Map<String, Object>>> getAllThemes() {
        log.info("获取所有主题");
        
        List<Map<String, Object>> themes = tourKnowledgeService.getAllThemes();
        return Result.success(themes);
    }
} 