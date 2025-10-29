package com.sky.controller.admin;

import com.sky.entity.HotelPriceDifference;
import com.sky.result.Result;
import com.sky.service.HotelPriceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 酒店价格差异管理Controller
 */
@RestController
@RequestMapping("/admin/hotel-price-differences")
@Api(tags = "酒店价格差异管理接口")
@Slf4j
public class HotelPriceDifferenceController {

    @Autowired
    private HotelPriceService hotelPriceService;

    /**
     * 获取所有酒店价格差异
     */
    @GetMapping
    @ApiOperation("获取所有酒店价格差异")
    public Result<List<HotelPriceDifference>> getAllPriceDifferences() {
        log.info("获取所有酒店价格差异");
        List<HotelPriceDifference> priceDifferences = hotelPriceService.getAllPriceDifferences();
        return Result.success(priceDifferences);
    }

    /**
     * 根据ID获取酒店价格差异
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取酒店价格差异")
    public Result<HotelPriceDifference> getById(@PathVariable Integer id) {
        log.info("根据ID获取酒店价格差异：{}", id);
        HotelPriceDifference priceDifference = hotelPriceService.getById(id);
        if (priceDifference == null) {
            return Result.error("未找到该酒店价格差异记录");
        }
        return Result.success(priceDifference);
    }

    /**
     * 添加酒店价格差异
     */
    @PostMapping
    @ApiOperation("添加酒店价格差异")
    public Result<String> add(@RequestBody HotelPriceDifference hotelPriceDifference) {
        log.info("添加酒店价格差异：{}", hotelPriceDifference);
        boolean success = hotelPriceService.add(hotelPriceDifference);
        if (success) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    /**
     * 更新酒店价格差异
     */
    @PutMapping
    @ApiOperation("更新酒店价格差异")
    public Result<String> update(@RequestBody HotelPriceDifference hotelPriceDifference) {
        log.info("更新酒店价格差异：{}", hotelPriceDifference);
        boolean success = hotelPriceService.update(hotelPriceDifference);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除酒店价格差异
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除酒店价格差异")
    public Result<String> delete(@PathVariable Integer id) {
        log.info("删除酒店价格差异：{}", id);
        boolean success = hotelPriceService.delete(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}







