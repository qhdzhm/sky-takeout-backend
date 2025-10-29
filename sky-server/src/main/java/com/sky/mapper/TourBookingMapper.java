package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.TourBooking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 旅游订单Mapper接口
 */
@Mapper
@Repository
public interface TourBookingMapper {
    
    /**
     * 根据ID查询订单
     * @param id 订单ID
     * @return 订单信息
     */
    @Select("SELECT booking_id, order_number, tour_id, tour_type, user_id, agent_id, operator_id, " +
            "booking_date, flight_number, arrival_departure_time, arrival_landing_time, " +
            "return_flight_number, departure_departure_time, departure_landing_time, " +
            "tour_start_date, tour_end_date, pickup_date, dropoff_date, pickup_location, dropoff_location, " +
            "service_type, group_size, adult_count, child_count, include_hotel, luggage_count, passenger_contact, " +
            "contact_person, contact_phone, hotel_level, room_type, hotel_room_count, " +
            "hotel_check_in_date, hotel_check_out_date, room_details, special_requests, " +
            "itinerary_details, status, payment_status, total_price, created_at, updated_at, " +
            "selected_optional_tours, from_referral, referral_code, user_hidden, user_hidden_at, " +
            "assigned_operator_id, assigned_at, assigned_by, assignment_status, " +
            "group_type, group_size_limit, group_type_price " +
            "FROM tour_bookings WHERE booking_id = #{id}")
    TourBooking getById(Integer id);
    
    /**
     * 根据用户ID查询订单
     * @param userId 用户ID
     * @return 订单列表
     */
    List<TourBooking> getByUserId(Integer userId);
    
    /**
     * 根据代理商ID查询订单
     * @param agentId 代理商ID
     * @return 订单列表
     */
    List<TourBooking> getByAgentId(Integer agentId);
    
    /**
     * 分页查询订单
     * @param userId 用户ID
     * @param agentId 代理商ID
     * @param tourType 旅游类型
     * @param status 订单状态
     * @param paymentStatus 支付状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页订单列表
     */
    Page<TourBooking> pageQuery(
            @Param("userId") Integer userId,
            @Param("agentId") Integer agentId,
            @Param("tourType") String tourType,
            @Param("status") String status,
            @Param("paymentStatus") String paymentStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 插入订单
     * @param tourBooking 订单信息
     */
    void insert(TourBooking tourBooking);
    
    /**
     * 更新订单
     * @param tourBooking 订单信息
     */
    void update(TourBooking tourBooking);

    /**
     * 管理端专用：仅更新订单总价
     * @param bookingId 订单ID
     * @param totalPrice 新总价
     * @return 影响行数
     */
    @Update("UPDATE tour_bookings SET total_price = #{totalPrice}, updated_at = NOW() WHERE booking_id = #{bookingId}")
    int updateTotalPrice(@Param("bookingId") Integer bookingId, @Param("totalPrice") java.math.BigDecimal totalPrice);

    /**
     * 管理端专用：更新可选行程选择(JSON)，不触发价格重算
     */
    @Update("UPDATE tour_bookings SET selected_optional_tours = #{selectedOptionalTours}, updated_at = NOW() WHERE booking_id = #{bookingId}")
    int updateSelectedOptionalTours(@Param("bookingId") Integer bookingId, @Param("selectedOptionalTours") String selectedOptionalTours);

    /**
     * 根据订单ID更新特殊要求
     * @param bookingId 订单ID
     * @param specialRequests 特殊要求
     * @return 更新的记录数
     */
    @Update("UPDATE tour_bookings SET special_requests = #{specialRequests}, updated_at = NOW() WHERE booking_id = #{bookingId}")
    int updateSpecialRequestsByBookingId(@Param("bookingId") Integer bookingId, @Param("specialRequests") String specialRequests);
    
    /**
     * 更新订单状态
     * @param id 订单ID
     * @param status 订单状态
     * @return 更新结果
     */
    @Update("UPDATE tour_bookings SET status = #{status} WHERE booking_id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status);
    
    /**
     * 更新支付状态
     * @param id 订单ID
     * @param paymentStatus 支付状态
     * @return 更新结果
     */
    @Update("UPDATE tour_bookings SET payment_status = #{paymentStatus} WHERE booking_id = #{id}")
    int updatePaymentStatus(@Param("id") Integer id, @Param("paymentStatus") String paymentStatus);
    
    /**
     * 根据ID删除订单
     * @param bookingId 订单ID
     */
    void deleteById(Integer bookingId);

    /**
     * 根据订单号查询订单
     * @param orderNumber 订单号
     * @return 订单信息
     */
    @Select("SELECT * FROM tour_bookings WHERE order_number = #{orderNumber}")
    TourBooking getByOrderNumber(String orderNumber);

    /**
     * 根据联系人姓名模糊查询订单列表
     * @param contactPerson 联系人姓名
     * @return 订单列表
     */
    @Select("SELECT * FROM tour_bookings WHERE contact_person LIKE CONCAT('%', #{contactPerson}, '%')")
    List<TourBooking> getByContactPersonLike(@Param("contactPerson") String contactPerson);

    /**
     * 通过乘客姓名查询订单（支持中文和英文姓名）
     * @param passengerName 乘客姓名
     * @return 订单列表
     */
    List<TourBooking> getByPassengerName(@Param("passengerName") String passengerName);

    /**
     * 条件查询订单列表
     * 
     * @param map 查询条件
     * @return 订单列表
     */
    List<TourBooking> list(Map<String, Object> map);
    
    /**
     * 获取订单实际金额
     * @param bookingId 订单ID
     * @return 订单金额
     */
    @Select("SELECT total_price FROM tour_bookings WHERE booking_id = #{bookingId}")
    java.math.BigDecimal getOrderAmount(Integer bookingId);

    /**
     * 通过ID获取订单信息
     * @param bookingId 订单ID
     * @return 订单信息
     */
    @Select("SELECT * FROM tour_bookings WHERE booking_id = #{bookingId}")
    Map<String, Object> getBookingInfoById(Integer bookingId);
    
    /**
     * 🆕 获取所有订单ID列表
     * @return 所有订单ID列表
     */
    @Select("SELECT booking_id FROM tour_bookings ORDER BY booking_id")
    List<Integer> getAllBookingIds();
    
    /**
     * 🔒 管理员确认订单（安全更新状态和价格）
     * 只允许从 pending -> confirmed 的状态转换
     * 包含业务逻辑验证和权限检查
     * 
     * @param bookingId 订单ID
     * @param newStatus 新状态（必须是confirmed）
     * @param newPrice 新价格（可选）
     * @param specialRequests 更新后的特殊要求（记录价格调整原因）
     * @param operatorInfo 操作员信息（用于审计）
     * @return 更新的记录数
     */
    int confirmOrderByAdmin(@Param("bookingId") Integer bookingId,
                           @Param("newStatus") String newStatus,
                           @Param("newPrice") Double newPrice,
                           @Param("specialRequests") String specialRequests,
                           @Param("operatorInfo") String operatorInfo);

    // ===== Dashboard统计相关方法 =====
    
    /**
     * 获取指定日期范围的营业数据
     * @param begin 开始日期
     * @param end 结束日期
     * @return 营业数据列表
     */
    List<Map<String, Object>> getBusinessDataByDateRange(@Param("begin") java.time.LocalDate begin, 
                                                         @Param("end") java.time.LocalDate end);

    /**
     * 获取指定时间范围的订单数据
     * @param start 开始时间
     * @param end 结束时间
     * @return 订单数据
     */
    Map<String, Object> getOrderDataByDateRange(@Param("start") java.time.LocalDateTime start, 
                                               @Param("end") java.time.LocalDateTime end);

    /**
     * 获取订单状态分布统计
     * @return 订单状态统计数据
     */
    Map<String, Object> getOrderStatusDistribution();

    /**
     * 获取最受欢迎的产品
     * @return 热门产品信息
     */
    Map<String, Object> getMostPopularTour();

    /**
     * 根据产品类型获取销售数据
     * @param tourType 产品类型
     * @return 销售数据
     */
    Map<String, Object> getSalesByTourType(@Param("tourType") String tourType);

    /**
     * 获取前N个热门产品（按类型）
     * @param tourType 产品类型
     * @param limit 限制数量
     * @return 热门产品列表
     */
    List<Map<String, Object>> getTopToursByType(@Param("tourType") String tourType, 
                                               @Param("limit") int limit);

    /**
     * 获取平均评分
     * @return 平均评分
     */
    Double getAverageRating();

    /**
     * 获取热门目的地统计
     * @return 热门目的地数据
     */
    Map<String, Object> getPopularDestinations();

    /**
     * 用户隐藏订单（软删除）
     * @param bookingId 订单ID
     * @param userId 用户ID（权限验证）
     * @return 影响行数
     */
    @Update("UPDATE tour_bookings SET user_hidden = true, user_hidden_at = NOW() WHERE booking_id = #{bookingId} AND (user_id = #{userId} OR agent_id = #{userId})")
    int hideOrderByUser(@Param("bookingId") Integer bookingId, @Param("userId") Integer userId);

    /**
     * 用户恢复已隐藏的订单
     * @param bookingId 订单ID
     * @param userId 用户ID（权限验证）
     * @return 影响行数
     */
    @Update("UPDATE tour_bookings SET user_hidden = false, user_hidden_at = NULL WHERE booking_id = #{bookingId} AND (user_id = #{userId} OR agent_id = #{userId})")
    int restoreOrderByUser(@Param("bookingId") Integer bookingId, @Param("userId") Integer userId);

    /**
     * 获取用户可见的订单（排除隐藏订单）
     * @param userId 用户ID
     * @return 订单列表
     */
    @Select("SELECT * FROM tour_bookings WHERE (user_id = #{userId} OR agent_id = #{userId}) AND (user_hidden IS NULL OR user_hidden = false) ORDER BY created_at DESC")
    List<TourBooking> getVisibleOrdersByUser(@Param("userId") Integer userId);

    /**
     * 管理员确认订单（支持价格调整和团型设置）
     * @param bookingId 订单ID
     * @param status 订单状态
     * @param adjustedPrice 调整后的价格
     * @param specialRequests 特殊要求
     * @param operatorInfo 操作员信息
     * @param groupType 团型类型
     * @param groupSizeLimit 团型人数限制
     * @return 影响行数
     */
    int confirmOrderByAdmin(@Param("bookingId") Integer bookingId, 
                           @Param("status") String status,
                           @Param("adjustedPrice") Double adjustedPrice,
                           @Param("specialRequests") String specialRequests,
                           @Param("operatorInfo") String operatorInfo,
                           @Param("groupType") String groupType,
                           @Param("groupSizeLimit") Integer groupSizeLimit);
} 