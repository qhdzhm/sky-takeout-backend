package com.sky.service;

import com.sky.entity.Hotel;
import java.util.List;

/**
 * 酒店服务接口
 */
public interface HotelService {

    /**
     * 获取所有酒店列表
     */
    List<Hotel> getAllHotels();

    /**
     * 根据ID获取酒店详情
     */
    Hotel getHotelById(Long id);

    /**
     * 创建酒店
     */
    void createHotel(Hotel hotel);

    /**
     * 更新酒店
     */
    void updateHotel(Hotel hotel);

    /**
     * 删除酒店
     */
    void deleteHotel(Long id);

    /**
     * 根据供应商ID获取酒店列表
     */

} 