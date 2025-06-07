package com.sky.service;

import com.sky.dto.TourBookingDTO;
import com.sky.dto.TourBookingUpdateDTO;
import com.sky.dto.PaymentDTO;
import com.sky.entity.DayTour;
import com.sky.dto.GroupTourDTO;
import com.sky.vo.TourBookingVO;
import com.sky.vo.PriceDetailVO;
import java.math.BigDecimal;

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
     * 计算订单总价
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param groupSize 团队人数
     * @return 计算得到的总价
     */
    BigDecimal calculateTotalPrice(Integer tourId, String tourType, Long agentId, Integer groupSize);
    
    /**
     * 计算订单总价（带酒店等级参数）
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param groupSize 团队人数
     * @param hotelLevel 酒店等级
     * @return 计算得到的总价
     */
    BigDecimal calculateTotalPrice(Integer tourId, String tourType, Long agentId, Integer groupSize, String hotelLevel);
    
    /**
     * 计算订单总价（带酒店等级、房间数量和用户ID参数）
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param groupSize 团队人数
     * @param hotelLevel 酒店等级
     * @param roomCount 房间数量
     * @param userId 用户ID
     * @return 计算得到的总价
     */
    BigDecimal calculateTotalPrice(Integer tourId, String tourType, Long agentId, Integer groupSize, 
                                  String hotelLevel, Integer roomCount, Long userId);

    /**
     * 计算订单总价（带成人数、儿童数、酒店等级、房间数量和用户ID参数）
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param adultCount 成人数量
     * @param childCount 儿童数量
     * @param hotelLevel 酒店等级
     * @param roomCount 房间数量
     * @param userId 用户ID
     * @return 计算得到的总价
     */
    BigDecimal calculateTotalPrice(Integer tourId, String tourType, Long agentId, Integer adultCount, 
                                  Integer childCount, String hotelLevel, Integer roomCount, Long userId);
    
    /**
     * 计算价格明细
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param adultCount 成人数量
     * @param childCount 儿童数量
     * @param hotelLevel 酒店等级
     * @param roomCount 房间数量
     * @param userId 用户ID
     * @return 价格明细
     */
    PriceDetailVO calculatePriceDetail(Integer tourId, String tourType, Long agentId, Integer adultCount, 
                                   Integer childCount, String hotelLevel, Integer roomCount, Long userId);
    
    /**
     * 计算价格明细（带房型参数）
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param adultCount 成人数量
     * @param childCount 儿童数量
     * @param hotelLevel 酒店等级
     * @param roomCount 房间数量
     * @param userId 用户ID
     * @param roomType 房间类型
     * @return 价格明细
     */
    PriceDetailVO calculatePriceDetail(Integer tourId, String tourType, Long agentId, Integer adultCount, 
                                   Integer childCount, String hotelLevel, Integer roomCount, Long userId, 
                                   String roomType);
    
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