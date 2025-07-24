package com.sky.controller.admin;

import com.sky.entity.Hotel;
import com.sky.entity.HotelRoomType;
import com.sky.result.Result;
import com.sky.service.HotelService;
import com.sky.service.HotelRoomTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 酒店管理Controller
 */
@RestController
@RequestMapping("/admin/hotels")
@Api(tags = "酒店管理接口")
@Slf4j
public class HotelController {

    @Autowired
    private HotelService hotelService;
    
    @Autowired
    private HotelRoomTypeService hotelRoomTypeService;

    /**
     * 获取酒店列表
     */
    @GetMapping
    @ApiOperation("获取酒店列表")
    public Result<List<Hotel>> getHotels() {
        log.info("获取酒店列表");
        List<Hotel> hotels = hotelService.getAllHotels();
        return Result.success(hotels);
    }

    /**
     * 根据ID获取酒店详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取酒店详情")
    public Result<Hotel> getHotelById(@PathVariable Long id) {
        log.info("根据ID获取酒店详情：{}", id);
        Hotel hotel = hotelService.getHotelById(id);
        return Result.success(hotel);
    }

    /**
     * 创建酒店
     */
    @PostMapping
    @ApiOperation("创建酒店")
    public Result<String> createHotel(@RequestBody Hotel hotel) {
        log.info("创建酒店：{}", hotel);
        hotelService.createHotel(hotel);
        return Result.success("创建成功");
    }

    /**
     * 更新酒店（通过请求体中的ID）
     */
    @PutMapping
    @ApiOperation("更新酒店")
    public Result<String> updateHotel(@RequestBody Hotel hotel) {
        log.info("更新酒店：{}", hotel);
        hotelService.updateHotel(hotel);
        return Result.success("更新成功");
    }

    /**
     * 更新酒店（通过路径参数ID）
     */
    @PutMapping("/{id}")
    @ApiOperation("更新酒店")
    public Result<String> updateHotelById(@PathVariable Long id, @RequestBody Hotel hotel) {
        log.info("更新酒店：{}, {}", id, hotel);
        hotel.setId(id.intValue());
        hotelService.updateHotel(hotel);
        return Result.success("更新成功");
    }

    /**
     * 删除酒店
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除酒店")
    public Result<String> deleteHotel(@PathVariable Long id) {
        log.info("删除酒店：{}", id);
        hotelService.deleteHotel(id);
        return Result.success("删除成功");
    }

    /**
     * 根据酒店ID获取房型列表
     */
    @GetMapping("/{hotelId}/room-types")
    @ApiOperation("根据酒店ID获取房型列表")
    public Result<List<HotelRoomType>> getRoomTypesByHotelId(@PathVariable Long hotelId) {
        log.info("根据酒店ID获取房型列表：{}", hotelId);
        List<HotelRoomType> roomTypes = hotelRoomTypeService.getRoomTypesByHotelId(hotelId);
        return Result.success(roomTypes);
    }
} 