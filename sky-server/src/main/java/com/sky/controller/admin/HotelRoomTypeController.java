package com.sky.controller.admin;

import com.sky.entity.HotelRoomType;
import com.sky.result.Result;
import com.sky.service.HotelRoomTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 酒店房型管理Controller
 */
@RestController
@RequestMapping("/admin/hotel-room-types")
@Api(tags = "酒店房型管理接口")
@Slf4j
public class HotelRoomTypeController {

    @Autowired
    private HotelRoomTypeService hotelRoomTypeService;

    /**
     * 获取房型列表
     */
    @GetMapping
    @ApiOperation("获取房型列表")
    public Result<List<HotelRoomType>> getRoomTypes(
            @RequestParam(required = false) Long hotelId) {
        log.info("获取房型列表，酒店ID：{}", hotelId);
        
        List<HotelRoomType> roomTypes;
        if (hotelId != null) {
            roomTypes = hotelRoomTypeService.getRoomTypesByHotelId(hotelId);
        } else {
            roomTypes = hotelRoomTypeService.getAllRoomTypes();
        }
        
        return Result.success(roomTypes);
    }



    /**
     * 根据ID获取房型详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取房型详情")
    public Result<HotelRoomType> getRoomTypeById(@PathVariable Long id) {
        log.info("根据ID获取房型详情：{}", id);
        HotelRoomType roomType = hotelRoomTypeService.getRoomTypeById(id);
        return Result.success(roomType);
    }

    /**
     * 创建房型
     */
    @PostMapping
    @ApiOperation("创建房型")
    public Result<String> createRoomType(@RequestBody HotelRoomType roomType) {
        log.info("创建房型：{}", roomType);
        hotelRoomTypeService.createRoomType(roomType);
        return Result.success("创建成功");
    }

    /**
     * 更新房型（通过请求体中的ID）
     */
    @PutMapping
    @ApiOperation("更新房型")
    public Result<String> updateRoomType(@RequestBody HotelRoomType roomType) {
        log.info("更新房型：{}", roomType);
        hotelRoomTypeService.updateRoomType(roomType);
        return Result.success("更新成功");
    }

    /**
     * 更新房型（通过路径参数ID）
     */
    @PutMapping("/{id}")
    @ApiOperation("更新房型")
    public Result<String> updateRoomTypeById(@PathVariable Long id, @RequestBody HotelRoomType roomType) {
        log.info("更新房型：{}, {}", id, roomType);
        roomType.setId(id.intValue());
        hotelRoomTypeService.updateRoomType(roomType);
        return Result.success("更新成功");
    }

    /**
     * 删除房型
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除房型")
    public Result<String> deleteRoomType(@PathVariable Long id) {
        log.info("删除房型：{}", id);
        hotelRoomTypeService.deleteRoomType(id);
        return Result.success("删除成功");
    }
} 