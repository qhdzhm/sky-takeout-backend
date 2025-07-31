package com.sky.controller.common;

import com.sky.dto.RegionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 地区信息通用控制器
 * 该控制器提供通用的地区相关接口，同时供前端和后端使用
 */
@RestController
@RequestMapping("/common/regions")
@Api(tags = "地区信息通用接口")
@Slf4j
public class RegionController {

    @Autowired
    private RegionService regionService;

    /**
     * 获取所有地区信息
     * @return 地区列表
     */
    @GetMapping
    @ApiOperation("获取所有地区信息")
    public Result<List<RegionDTO>> getAllRegions() {
        log.info("获取所有地区信息");
        List<RegionDTO> regions = regionService.getAllRegions();
        return Result.success(regions);
    }

    /**
     * 根据ID获取地区信息
     * @param id 地区ID
     * @return 地区信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取地区信息")
    public Result<RegionDTO> getRegionById(@PathVariable Integer id) {
        log.info("根据ID获取地区信息，ID：{}", id);
        RegionDTO region = regionService.getRegionById(id);
        return Result.success(region);
    }
    
    /**
     * 获取地区的旅游产品
     * @param id 地区ID
     * @param params 查询参数
     * @return 旅游产品列表
     */
    @GetMapping("/{id}/tours")
    @ApiOperation("获取地区的旅游产品")
    public Result<PageResult> getRegionTours(
            @PathVariable Integer id,
            @RequestParam Map<String, Object> params) {
        log.info("获取地区的旅游产品，地区ID：{}，参数：{}", id, params);
        PageResult pageResult = regionService.getRegionTours(id, params);
        return Result.success(pageResult);
    }
} 