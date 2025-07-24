package com.sky.service;

import com.sky.entity.HotelRoomType;
import java.util.List;

/**
 * 酒店房型服务接口
 */
public interface HotelRoomTypeService {

    /**
     * 获取所有房型列表
     */
    List<HotelRoomType> getAllRoomTypes();

    /**
     * 根据酒店ID获取房型列表
     */
    List<HotelRoomType> getRoomTypesByHotelId(Long hotelId);

    /**
     * 根据ID获取房型详情
     */
    HotelRoomType getRoomTypeById(Long id);

    /**
     * 创建房型
     */
    void createRoomType(HotelRoomType roomType);

    /**
     * 更新房型
     */
    void updateRoomType(HotelRoomType roomType);

    /**
     * 删除房型
     */
    void deleteRoomType(Long id);
} 