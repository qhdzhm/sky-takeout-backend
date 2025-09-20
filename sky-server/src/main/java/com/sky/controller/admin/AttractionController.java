package com.sky.controller.admin;

import com.sky.entity.Attraction;
import com.sky.result.Result;
import com.sky.service.AttractionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 景点管理Controller - 基于酒店管理Controller设计
 */
@RestController
@RequestMapping("/admin/attractions")
@Api(tags = "景点管理接口")
@Slf4j
public class AttractionController {

    @Autowired
    private AttractionService attractionService;

    /**
     * 获取景点列表
     */
    @GetMapping
    @ApiOperation("获取景点列表")
    public Result<List<Attraction>> getAttractions() {
        log.info("获取景点列表");
        List<Attraction> attractions = attractionService.getAllAttractions();
        return Result.success(attractions);
    }

    /**
     * 获取活跃景点列表
     */
    @GetMapping("/active")
    @ApiOperation("获取活跃景点列表")
    public Result<List<Attraction>> getActiveAttractions() {
        log.info("获取活跃景点列表");
        List<Attraction> attractions = attractionService.getAllActiveAttractions();
        return Result.success(attractions);
    }

    /**
     * 根据ID获取景点详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取景点详情")
    public Result<Attraction> getAttractionById(@PathVariable Long id) {
        log.info("根据ID获取景点详情：{}", id);
        Attraction attraction = attractionService.getAttractionById(id);
        return Result.success(attraction);
    }

    /**
     * 创建景点
     */
    @PostMapping
    @ApiOperation("创建景点")
    public Result<String> createAttraction(@RequestBody Attraction attraction) {
        log.info("创建景点：{}", attraction);
        attractionService.createAttraction(attraction);
        return Result.success("创建成功");
    }

    /**
     * 更新景点（通过请求体中的ID）
     */
    @PutMapping
    @ApiOperation("更新景点")
    public Result<String> updateAttraction(@RequestBody Attraction attraction) {
        log.info("更新景点：{}", attraction);
        attractionService.updateAttraction(attraction);
        return Result.success("更新成功");
    }

    /**
     * 更新景点（通过路径参数ID）
     */
    @PutMapping("/{id}")
    @ApiOperation("更新景点")
    public Result<String> updateAttractionById(@PathVariable Long id, @RequestBody Attraction attraction) {
        log.info("更新景点：id={}, attraction={}", id, attraction);
        attraction.setId(id);
        attractionService.updateAttraction(attraction);
        return Result.success("更新成功");
    }

    /**
     * 删除景点
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除景点")
    public Result<String> deleteAttraction(@PathVariable Long id) {
        log.info("删除景点：{}", id);
        attractionService.deleteAttraction(id);
        return Result.success("删除成功");
    }

    /**
     * 根据预订方式获取景点列表
     */
    @GetMapping("/booking-type/{bookingType}")
    @ApiOperation("根据预订方式获取景点列表")
    public Result<List<Attraction>> getAttractionsByBookingType(@PathVariable String bookingType) {
        log.info("根据预订方式获取景点列表：{}", bookingType);
        List<Attraction> attractions = attractionService.getAttractionsByBookingType(bookingType);
        return Result.success(attractions);
    }

    /**
     * 根据位置获取景点列表
     */
    @GetMapping("/location/{location}")
    @ApiOperation("根据位置获取景点列表")
    public Result<List<Attraction>> getAttractionsByLocation(@PathVariable String location) {
        log.info("根据位置获取景点列表：{}", location);
        List<Attraction> attractions = attractionService.getAttractionsByLocation(location);
        return Result.success(attractions);
    }

    /**
     * 根据景点名称搜索
     */
    @GetMapping("/search")
    @ApiOperation("根据景点名称搜索")
    public Result<List<Attraction>> searchAttractionsByName(@RequestParam String name) {
        log.info("根据景点名称搜索：{}", name);
        List<Attraction> attractions = attractionService.searchAttractionsByName(name);
        return Result.success(attractions);
    }

    /**
     * 更新景点状态
     */
    @PutMapping("/{id}/status")
    @ApiOperation("更新景点状态")
    public Result<String> updateAttractionStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新景点状态：id={}, status={}", id, status);
        attractionService.updateAttractionStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 根据条件搜索景点
     */
    @GetMapping("/search/advanced")
    @ApiOperation("根据条件搜索景点")
    public Result<List<Attraction>> searchAttractions(@RequestParam(required = false) String attractionName,
                                                     @RequestParam(required = false) String location,
                                                     @RequestParam(required = false) String bookingType,
                                                     @RequestParam(required = false) String status) {
        log.info("根据条件搜索景点：attractionName={}, location={}, bookingType={}, status={}", 
                attractionName, location, bookingType, status);
        List<Attraction> attractions = attractionService.searchAttractions(attractionName, location, bookingType, status);
        return Result.success(attractions);
    }
}

