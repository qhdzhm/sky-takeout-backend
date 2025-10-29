package com.sky.controller.admin;

import com.sky.entity.HotelDailyPrice;
import com.sky.result.Result;
import com.sky.service.HotelDailyPriceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 酒店每日价格管理Controller
 */
@RestController
@RequestMapping("/admin/hotel-daily-prices")
@Api(tags = "酒店每日价格管理接口")
@Slf4j
public class HotelDailyPriceController {

    @Autowired
    private HotelDailyPriceService hotelDailyPriceService;

    /**
     * 根据酒店星级和日期范围查询每日价格
     */
    @GetMapping("/by-level")
    @ApiOperation("根据酒店星级和日期范围查询每日价格")
    public Result<List<HotelDailyPrice>> getByLevelAndDateRange(
            @RequestParam String hotelLevel,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("根据酒店星级和日期范围查询：{}, {}, {}", hotelLevel, startDate, endDate);
        List<HotelDailyPrice> prices = hotelDailyPriceService.getByLevelAndDateRange(hotelLevel, startDate, endDate);
        return Result.success(prices);
    }

    /**
     * 根据日期范围查询所有星级的每日价格
     */
    @GetMapping("/by-date-range")
    @ApiOperation("根据日期范围查询所有星级的每日价格")
    public Result<List<HotelDailyPrice>> getByDateRange(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("根据日期范围查询：{}, {}", startDate, endDate);
        List<HotelDailyPrice> prices = hotelDailyPriceService.getByDateRange(startDate, endDate);
        return Result.success(prices);
    }

    /**
     * 根据酒店星级和具体日期查询价格
     */
    @GetMapping("/by-level-and-date")
    @ApiOperation("根据酒店星级和具体日期查询价格")
    public Result<HotelDailyPrice> getByLevelAndDate(
            @RequestParam String hotelLevel,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate priceDate) {
        log.info("根据酒店星级和日期查询：{}, {}", hotelLevel, priceDate);
        HotelDailyPrice price = hotelDailyPriceService.getByLevelAndDate(hotelLevel, priceDate);
        return Result.success(price);
    }

    /**
     * 添加或更新每日价格
     */
    @PostMapping
    @ApiOperation("添加或更新每日价格")
    public Result<String> saveOrUpdate(@RequestBody HotelDailyPrice hotelDailyPrice) {
        log.info("添加或更新每日价格：{}", hotelDailyPrice);
        boolean success = hotelDailyPriceService.saveOrUpdate(hotelDailyPrice);
        if (success) {
            return Result.success("保存成功");
        }
        return Result.error("保存失败");
    }

    /**
     * 批量添加或更新每日价格
     */
    @PostMapping("/batch")
    @ApiOperation("批量添加或更新每日价格")
    public Result<String> batchSaveOrUpdate(@RequestBody List<HotelDailyPrice> list) {
        log.info("批量添加或更新每日价格，数量：{}", list.size());
        boolean success = hotelDailyPriceService.batchSaveOrUpdate(list);
        if (success) {
            return Result.success("批量保存成功");
        }
        return Result.error("批量保存失败");
    }

    /**
     * 删除每日价格
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除每日价格")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除每日价格，ID：{}", id);
        boolean success = hotelDailyPriceService.delete(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 根据星级和日期删除
     */
    @DeleteMapping("/by-level-and-date")
    @ApiOperation("根据星级和日期删除")
    public Result<String> deleteByLevelAndDate(
            @RequestParam String hotelLevel,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate priceDate) {
        log.info("根据星级和日期删除：{}, {}", hotelLevel, priceDate);
        boolean success = hotelDailyPriceService.deleteByLevelAndDate(hotelLevel, priceDate);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}







