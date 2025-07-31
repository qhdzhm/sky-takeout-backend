package com.sky.controller.user;

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
 * 用户端地区控制器
 */
@RestController
@RequestMapping("/user/regions")
@Api(tags = "用户端地区相关接口")
@Slf4j
public class UserRegionController {

    @Autowired
    private RegionService regionService;

    /**
     * 获取所有地区
     * @return 地区列表
     */
    @GetMapping
    @ApiOperation("获取所有地区")
    public Result<List<RegionDTO>> getAllRegions() {
        log.info("获取所有地区");
        List<RegionDTO> regions = regionService.getAllRegions();
        return Result.success(regions);
    }

    /**
     * 根据ID获取地区详情
     * @param id 地区ID
     * @return 地区详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取地区详情")
    public Result<RegionDTO> getRegionById(@PathVariable Integer id) {
        log.info("获取地区详情，ID：{}", id);
        RegionDTO regionDTO = regionService.getRegionById(id);
        return Result.success(regionDTO);
    }

    /**
     * 获取地区旅游产品
     * @param id 地区ID
     * @param params 查询参数
     * @return 分页结果
     */
    @GetMapping("/{id}/tours")
    @ApiOperation("获取地区旅游产品")
    public Result<PageResult> getRegionTours(@PathVariable Integer id, @RequestParam Map<String, Object> params) {
        log.info("获取地区旅游产品，ID：{}，参数：{}", id, params);
        PageResult pageResult = regionService.getRegionTours(id, params);
        return Result.success(pageResult);
    }
} 