package com.sky.controller.admin;

import com.sky.entity.DayTourTheme;
import com.sky.result.Result;
import com.sky.service.DayTourThemeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 一日游主题管理
 */
@RestController
@RequestMapping("/admin/daytour/theme")
@Api(tags = "一日游主题相关接口")
@Slf4j
public class DayTourThemeController {

    @Autowired
    private DayTourThemeService dayTourThemeService;

    /**
     * 获取所有主题
     */
    @GetMapping("/list")
    @ApiOperation("获取所有主题")
    public Result<List<DayTourTheme>> list() {
        log.info("获取所有主题");
        List<DayTourTheme> themes = dayTourThemeService.list();
        return Result.success(themes);
    }

    /**
     * 根据一日游ID获取主题
     */
    @GetMapping("/listByDayTourId/{dayTourId}")
    @ApiOperation("根据一日游ID获取主题")
    public Result<List<DayTourTheme>> listByDayTourId(@PathVariable Integer dayTourId) {
        log.info("根据一日游ID获取主题：{}", dayTourId);
        List<DayTourTheme> themes = dayTourThemeService.getByDayTourId(dayTourId);
        return Result.success(themes);
    }

    /**
     * 新增主题
     */
    @PostMapping
    @ApiOperation("新增主题")
    public Result<DayTourTheme> save(@RequestBody DayTourTheme dayTourTheme) {
        log.info("新增主题：{}", dayTourTheme);
        dayTourThemeService.save(dayTourTheme);
        return Result.success();
    }

    /**
     * 修改主题
     */
    @PutMapping
    @ApiOperation("修改主题")
    public Result<DayTourTheme> update(@RequestBody DayTourTheme dayTourTheme) {
        log.info("修改主题：{}", dayTourTheme);
        dayTourThemeService.update(dayTourTheme);
        return Result.success();
    }

    /**
     * 删除主题
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除主题")
    public Result<String> delete(@PathVariable Integer id) {
        log.info("删除主题：{}", id);
        dayTourThemeService.deleteById(id);
        return Result.success();
    }

    /**
     * 添加一日游与主题关联
     */
    @PostMapping("/relation")
    @ApiOperation("添加一日游与主题关联")
    public Result<String> addRelation(@RequestBody Map<String, Object> params) {
        log.info("添加一日游与主题关联：{}", params);
        Integer dayTourId = (Integer) params.get("dayTourId");
        Integer themeId = (Integer) params.get("themeId");
        
        // 使用associate方法添加关联
        List<Integer> themeIds = new ArrayList<>();
        themeIds.add(themeId);
        dayTourThemeService.associate(dayTourId, themeIds);
        
        return Result.success();
    }

    /**
     * 一次性关联一日游与多个主题
     */
    @PostMapping("/associate")
    @ApiOperation("一次性关联一日游与多个主题")
    public Result<String> associate(@RequestParam Integer dayTourId, @RequestBody List<Integer> themeIds) {
        log.info("关联一日游与多个主题：dayTourId={}, themeIds={}", dayTourId, themeIds);
        dayTourThemeService.associate(dayTourId, themeIds);
        return Result.success();
    }

    /**
     * 删除一日游与主题关联
     */
    @DeleteMapping("/relation")
    @ApiOperation("删除一日游与主题关联")
    public Result<String> deleteRelation(@RequestParam Integer dayTourId, @RequestParam Integer themeId) {
        log.info("删除一日游与主题关联：dayTourId={}, themeId={}", dayTourId, themeId);
        
        // 使用associate方法更新关联
        // 先获取所有关联的主题
        List<DayTourTheme> themes = dayTourThemeService.getByDayTourId(dayTourId);
        List<Integer> themeIds = new ArrayList<>();
        
        // 重新构建不包含要删除的themeId的列表
        for (DayTourTheme theme : themes) {
            if (!theme.getThemeId().equals(themeId)) {
                themeIds.add(theme.getThemeId());
            }
        }
        
        // 更新关联
        dayTourThemeService.associate(dayTourId, themeIds);
        
        return Result.success();
    }
} 