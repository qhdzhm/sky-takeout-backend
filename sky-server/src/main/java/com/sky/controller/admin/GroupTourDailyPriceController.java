package com.sky.controller.admin;

import com.sky.entity.GroupTourDailyPrice;
import com.sky.result.Result;
import com.sky.service.GroupTourDailyPriceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 团队游每日价格管理Controller
 */
@RestController("adminGroupTourDailyPriceController")
@RequestMapping("/admin/group-tour-daily-prices")
@Api(tags = "团队游每日价格管理接口")
@Slf4j
public class GroupTourDailyPriceController {

    @Autowired
    private GroupTourDailyPriceService groupTourDailyPriceService;

    /**
     * 根据团队游ID和日期范围查询每日价格
     */
    @GetMapping("/tour/{groupTourId}")
    @ApiOperation("根据团队游ID和日期范围查询每日价格")
    public Result<List<GroupTourDailyPrice>> getByTourIdAndDateRange(
            @ApiParam(value = "团队游ID", required = true) @PathVariable Integer groupTourId,
            @ApiParam(value = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("查询团队游ID={}在{}到{}的每日价格", groupTourId, startDate, endDate);
        List<GroupTourDailyPrice> prices = groupTourDailyPriceService.getByTourIdAndDateRange(groupTourId, startDate, endDate);
        return Result.success(prices);
    }

    /**
     * 根据日期范围查询所有团队游的每日价格
     */
    @GetMapping
    @ApiOperation("根据日期范围查询所有团队游的每日价格")
    public Result<List<GroupTourDailyPrice>> getByDateRange(
            @ApiParam(value = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("查询{}到{}所有团队游的每日价格", startDate, endDate);
        List<GroupTourDailyPrice> prices = groupTourDailyPriceService.getByDateRange(startDate, endDate);
        return Result.success(prices);
    }

    /**
     * 添加或更新每日价格
     */
    @PostMapping
    @ApiOperation("添加或更新每日价格")
    public Result<String> saveOrUpdate(@RequestBody GroupTourDailyPrice groupTourDailyPrice) {
        log.info("保存或更新团队游每日价格：{}", groupTourDailyPrice);
        boolean success = groupTourDailyPriceService.saveOrUpdate(groupTourDailyPrice);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    /**
     * 批量添加或更新每日价格
     */
    @PostMapping("/batch")
    @ApiOperation("批量添加或更新每日价格")
    public Result<String> batchSaveOrUpdate(@RequestBody List<GroupTourDailyPrice> list) {
        log.info("批量保存或更新团队游每日价格，数量：{}", list.size());
        boolean success = groupTourDailyPriceService.batchSaveOrUpdate(list);
        return success ? Result.success("批量保存成功") : Result.error("批量保存失败");
    }

    /**
     * 删除每日价格
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除每日价格")
    public Result<String> delete(@ApiParam(value = "价格ID", required = true) @PathVariable Long id) {
        log.info("删除团队游每日价格，ID：{}", id);
        boolean success = groupTourDailyPriceService.delete(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}







