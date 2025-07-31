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
    @Select("SELECT * FROM tour_bookings WHERE booking_id = #{id}")
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
} 