package com.sky.service;

import com.sky.dto.BookingDTO;
import com.sky.entity.TourBooking;

import java.util.List;
import java.util.Map;

/**
 * 预订服务接口
 */
public interface BookingService {

    /**
     * 创建预订
     * @param bookingDTO 预订信息
     * @return 预订ID
     */
    Integer createBooking(BookingDTO bookingDTO);

    /**
     * 获取用户预订列表
     * @return 预订列表
     */
    List<BookingDTO> getUserBookings();

    /**
     * 根据ID获取预订详情
     * @param id 预订ID
     * @return 预订详情
     */
    TourBooking getBookingById(Integer id);

    /**
     * 取消预订
     * @param id 预订ID
     */
    void cancelBooking(Integer id);

    /**
     * 检查可用性
     * @param params 查询参数
     * @return 可用性信息
     */
    Map<String, Object> checkAvailability(Map<String, Object> params);
} 