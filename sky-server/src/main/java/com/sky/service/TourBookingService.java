package com.sky.service;

import com.sky.dto.TourBookingDTO;
import com.sky.dto.TourBookingUpdateDTO;
import com.sky.dto.PaymentDTO;
import com.sky.entity.DayTour;
import com.sky.dto.GroupTourDTO;
import com.sky.vo.TourBookingVO;
import com.sky.vo.PriceDetailVO;
import java.math.BigDecimal;
import java.time.LocalDate;
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
     * @param includeHotel 是否包含酒店
     * @param startDate 行程出发日期（可选，用于计算每日酒店价格）
     * @param endDate 行程返回日期（可选，住宿夜数 = endDate - startDate）
     * @param isSmallGroup 是否小团（可选，用于计算小团差价）
     * @return 统一的价格计算结果
     */
    Map<String, Object> calculateUnifiedPrice(Integer tourId, String tourType, Long agentId, 
                                            Integer adultCount, Integer childCount, String hotelLevel, 
                                            Integer roomCount, Long userId, String roomTypes, 
                                            String childrenAges, String selectedOptionalTours, Boolean includeHotel,
                                            LocalDate startDate, LocalDate endDate, Boolean isSmallGroup);

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

    /**
     * 管理员确认订单（支持价格调整和团型设置）
     * 
     * @param bookingId 订单ID
     * @param adjustedPrice 调整后的价格（可选）
     * @param adjustmentReason 价格调整原因（可选）
     * @param groupType 团型类型（可选）：standard, small_12, small_14, luxury
     * @param groupSizeLimit 团型人数限制（可选）
     * @return 是否成功
     */
    Boolean confirmOrderByAdmin(Integer bookingId, Double adjustedPrice, String adjustmentReason, String groupType, Integer groupSizeLimit);

    /**
     * 用户隐藏订单（软删除）
     * 
     * @param bookingId 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean hideOrder(Integer bookingId, Integer userId);

    /**
     * 用户恢复已隐藏的订单
     * 
     * @param bookingId 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean restoreOrder(Integer bookingId, Integer userId);
    
    // ==================== 🔒 安全功能 ====================
    
    /**
     * 🔒 P0安全功能：验证价格一致性
     * 在订单创建时重新计算价格，确保与前端传来的价格一致
     * 
     * @param bookingDTO 订单DTO（包含前端计算的价格）
     * @return 重新计算的价格
     * @throws com.sky.exception.PriceChangedException 如果价格不一致
     */
    BigDecimal validateAndRecalculatePrice(TourBookingDTO bookingDTO);
    
    /**
     * 🔒 P1安全功能：保存价格快照
     * 在订单创建时保存完整的价格计算明细，便于追溯和纠纷处理
     * 
     * @param bookingId 订单ID
     * @param priceResult 价格计算结果
     */
    void savePriceSnapshot(Integer bookingId, Map<String, Object> priceResult);
} 