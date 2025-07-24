package com.sky.service;

import com.sky.dto.TourBookingDTO;
import com.sky.dto.TourBookingUpdateDTO;
import com.sky.dto.PaymentDTO;
import com.sky.entity.DayTour;
import com.sky.dto.GroupTourDTO;
import com.sky.vo.TourBookingVO;
import com.sky.vo.PriceDetailVO;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 旅游订单服务接口
 */
public interface TourBookingService {

    /**
     * 根据ID查询旅游订单
     * 
     * @param bookingId 订单ID
     * @return 订单详细信息
     */
    TourBookingVO getById(Integer bookingId);

    /**
     * 根据订单号查询订单
     * 
     * @param orderNumber 订单号
     * @return 订单详细信息
     */
    TourBookingVO getByOrderNumber(String orderNumber);

    /**
     * 保存旅游订单
     * 
     * @param tourBookingDTO 订单信息
     * @return 订单ID
     */
    Integer save(TourBookingDTO tourBookingDTO);

    /**
     * 更新旅游订单
     * 
     * @param tourBookingDTO 订单信息
     * @return 是否成功
     */
    Boolean update(TourBookingDTO tourBookingDTO);

    /**
     * 取消订单
     * 
     * @param bookingId 订单ID
     * @return 是否成功
     */
    Boolean cancel(Integer bookingId);

    /**
     * 确认订单
     * 
     * @param bookingId 订单ID
     * @return 是否成功
     */
    Boolean confirm(Integer bookingId);

    /**
     * 完成订单
     * 
     * @param bookingId 订单ID
     * @return 是否成功
     */
    Boolean complete(Integer bookingId);

    /**
     * 删除订单（只能删除已取消的订单）
     * 
     * @param bookingId 订单ID
     * @return 是否成功
     */
    Boolean delete(Integer bookingId);
    
    /**
     * 统一的价格计算方法（支持所有功能）
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param adultCount 成人数量
     * @param childCount 儿童数量
     * @param hotelLevel 酒店等级
     * @param roomCount 房间数量
     * @param userId 用户ID
     * @param roomTypes 房间类型数组（JSON字符串格式，如：["大床房","双人间"]，单房型可传单个字符串）
     * @param childrenAges 儿童年龄数组（逗号分隔，如："3,5,8"）
     * @param selectedOptionalTours 用户选择的可选项目（JSON字符串，如：{"1":25,"2":26}）
     * @return 统一的价格计算结果
     */
    Map<String, Object> calculateUnifiedPrice(Integer tourId, String tourType, Long agentId, 
                                            Integer adultCount, Integer childCount, String hotelLevel, 
                                            Integer roomCount, Long userId, String roomTypes, 
                                            String childrenAges, String selectedOptionalTours);

    /**
     * 根据ID获取一日游信息
     * 
     * @param tourId 一日游ID
     * @return 一日游信息
     */
    DayTour getDayTourById(Integer tourId);
    
    /**
     * 根据ID获取跟团游信息
     * 
     * @param tourId 跟团游ID
     * @return 跟团游信息
     */
    GroupTourDTO getGroupTourById(Integer tourId);
    
    /**
     * 获取代理商折扣率
     * 
     * @param agentId 代理商ID
     * @return 折扣率（0-1之间的小数）
     */
    BigDecimal getAgentDiscountRate(Long agentId);

    /**
     * 支付订单
     * 
     * @param bookingId 订单ID
     * @param paymentDTO 支付信息
     * @return 是否支付成功
     */
    Boolean payBooking(Integer bookingId, PaymentDTO paymentDTO);

    /**
     * 更新旅游订单详细信息（适用于代理商修改订单）
     * 
     * @param updateDTO 订单更新信息
     * @return 是否成功
     */
    Boolean updateBookingDetails(TourBookingUpdateDTO updateDTO);
    
    /**
     * 自动同步订单数据到排团表
     * 供订单创建时自动调用
     * 
     * @param bookingId 订单ID
     */
    void autoSyncOrderToScheduleTable(Integer bookingId);
} 