package com.sky.controller.user;

import com.sky.entity.HotelPriceDifference;
import com.sky.result.Result;
import com.sky.service.HotelPriceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 酒店价格相关接口
 */
@RestController
@RequestMapping("/user/hotel-prices")
@Api(tags = "酒店价格相关接口")
@Slf4j
public class HotelPriceController {

    @Autowired
    private HotelPriceService hotelPriceService;

    /**
     * 获取所有酒店星级价格差异
     * @return 所有酒店星级价格差异
     */
    @GetMapping
    @ApiOperation("获取所有酒店星级价格差异")
    public Result<List<HotelPriceDifference>> getHotelPrices() {
        log.info("获取所有酒店星级价格差异");
        return Result.success(hotelPriceService.getAllPriceDifferences());
    }

    /**
     * 获取基准酒店星级
     * @return 基准酒店星级
     */
    @GetMapping("/base-level")
    @ApiOperation("获取基准酒店星级")
    public Result<String> getBaseHotelLevel() {
        log.info("获取基准酒店星级");
        return Result.success(hotelPriceService.getBaseHotelLevel());
    }
} 