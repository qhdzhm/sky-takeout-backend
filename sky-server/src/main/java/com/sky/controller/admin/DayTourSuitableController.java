package com.sky.controller.admin;

import com.sky.entity.SuitableFor;
import com.sky.result.Result;
import com.sky.service.DayTourSuitableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 一日游适合人群管理
 */
@RestController
@RequestMapping("/admin/daytour/suitable")
@Api(tags = "一日游适合人群相关接口")
@Slf4j
public class DayTourSuitableController {

    @Autowired
    private DayTourSuitableService dayTourSuitableService;

    /**
     * 获取所有适合人群
     */
    @GetMapping("/list")
    @ApiOperation("获取所有适合人群")
    public Result<List<SuitableFor>> list() {
        log.info("获取所有适合人群");
        List<SuitableFor> list = dayTourSuitableService.list();
        return Result.success(list);
    }

    /**
     * 根据一日游ID获取适合人群
     */
    @GetMapping("/listByDayTourId/{dayTourId}")
    @ApiOperation("根据一日游ID获取适合人群")
    public Result<List<SuitableFor>> listByDayTourId(@PathVariable Integer dayTourId) {
        log.info("根据一日游ID获取适合人群：{}", dayTourId);
        List<SuitableFor> suitables = dayTourSuitableService.getByDayTourId(dayTourId);
        return Result.success(suitables);
    }

    /**
     * 新增适合人群
     */
    @PostMapping
    @ApiOperation("新增适合人群")
    public Result<SuitableFor> save(@RequestBody SuitableFor suitableFor) {
        log.info("新增适合人群：{}", suitableFor);
        dayTourSuitableService.save(suitableFor);
        return Result.success();
    }

    /**
     * 修改适合人群
     */
    @PutMapping
    @ApiOperation("修改适合人群")
    public Result<SuitableFor> update(@RequestBody SuitableFor suitableFor) {
        log.info("修改适合人群：{}", suitableFor);
        dayTourSuitableService.update(suitableFor);
        return Result.success();
    }

    /**
     * 删除适合人群
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除适合人群")
    public Result<String> delete(@PathVariable Integer id) {
        log.info("删除适合人群：{}", id);
        dayTourSuitableService.deleteById(id);
        return Result.success();
    }

    /**
     * 为一日游关联适合人群
     */
    @PostMapping("/associate")
    @ApiOperation("为一日游关联适合人群")
    public Result<String> associate(@RequestParam Integer dayTourId, @RequestBody List<Integer> suitableIds) {
        log.info("为一日游关联适合人群：dayTourId={}, suitableIds={}", dayTourId, suitableIds);
        dayTourSuitableService.associate(dayTourId, suitableIds);
        return Result.success();
    }
} 