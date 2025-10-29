package com.sky.service;

import com.sky.entity.HotelPriceDifference;
import java.math.BigDecimal;
import java.util.List;

/**
 * 酒店价格服务接口
 */
public interface HotelPriceService {
    
    /**
     * 获取所有酒店价格差异
     * @return 酒店价格差异列表
     */
    List<HotelPriceDifference> getAllPriceDifferences();
    
    /**
     * 根据酒店星级获取价格差异
     * @param hotelLevel 酒店星级
     * @return 价格差异
     */
    BigDecimal getPriceDifferenceByLevel(String hotelLevel);
    
    /**
     * 获取基准酒店星级
     * @return 基准酒店星级
     */
    String getBaseHotelLevel();
    
    /**
     * 根据酒店星级获取单房差
     * @param hotelLevel 酒店星级
     * @return 单房差金额（每人每晚）
     */
    BigDecimal getDailySingleRoomSupplementByLevel(String hotelLevel);
    
    /**
     * 根据酒店星级和房间类型计算房间价格
     * @param hotelLevel 酒店星级
     * @param roomType 房间类型（双床房、三人房等）
     * @return 房间价格（每间每晚）= 单房差 × 人数
     */
    BigDecimal calculateRoomPriceByTypeAndLevel(String hotelLevel, String roomType);
    
    /**
     * 根据酒店星级获取双床房/大床房价格
     * @param hotelLevel 酒店星级
     * @return 双床房价格（每间每晚）= 单房差 × 2
     */
    BigDecimal getDoubleRoomPriceByLevel(String hotelLevel);
    
    /**
     * 根据酒店星级获取三人房价格
     * @param hotelLevel 酒店星级
     * @return 三人房价格（每间每晚）= 单房差 × 3
     */
    BigDecimal getTripleRoomPriceByLevel(String hotelLevel);
    
    /**
     * 根据酒店星级获取三人房差价（三人房价格 - 双床房价格）
     * @param hotelLevel 酒店星级
     * @return 三人房差价 = 单房差 × 1
     */
    BigDecimal getTripleBedRoomPriceDifferenceByLevel(String hotelLevel);
    
    /**
     * 根据ID获取酒店价格差异
     * @param id ID
     * @return 酒店价格差异
     */
    HotelPriceDifference getById(Integer id);
    
    /**
     * 添加酒店价格差异
     * @param hotelPriceDifference 酒店价格差异
     * @return 是否成功
     */
    boolean add(HotelPriceDifference hotelPriceDifference);
    
    /**
     * 更新酒店价格差异
     * @param hotelPriceDifference 酒店价格差异
     * @return 是否成功
     */
    boolean update(HotelPriceDifference hotelPriceDifference);
    
    /**
     * 删除酒店价格差异
     * @param id ID
     * @return 是否成功
     */
    boolean delete(Integer id);
} 