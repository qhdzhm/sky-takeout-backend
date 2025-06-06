package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.PaymentDTO;
import com.sky.dto.PaymentPageQueryDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 支付数据访问层
 */
@Mapper
public interface PaymentMapper {

    /**
     * 创建支付
     * @param paymentDTO 支付信息
     * @return 影响行数
     */
    @Insert("INSERT INTO payments(booking_id, amount, payment_date, payment_method, transaction_id, status, is_credit_payment, credit_transaction_id) " +
            "VALUES(#{bookingId}, #{amount}, NOW(), #{paymentMethod}, #{transactionId}, #{status}, #{isCreditPayment}, #{creditTransactionId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "payment_id")
    int insert(PaymentDTO paymentDTO);

    /**
     * 根据ID查询支付
     * @param id 支付ID
     * @return 支付信息
     */
    @Select("SELECT * FROM payments WHERE payment_id = #{id}")
    PaymentDTO getById(Integer id);

    /**
     * 根据预订ID查询支付列表
     * @param bookingId 预订ID
     * @return 支付列表
     */
    @Select("SELECT * FROM payments WHERE booking_id = #{bookingId} ORDER BY payment_date DESC")
    List<PaymentDTO> getByBookingId(Integer bookingId);

    /**
     * 更新支付状态
     * @param id 支付ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE payments SET status = #{status} WHERE payment_id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    /**
     * 更新预订支付状态
     * @param bookingId 预订ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE tour_bookings SET payment_status = #{status} WHERE booking_id = #{bookingId}")
    int updateBookingPaymentStatus(@Param("bookingId") Integer bookingId, @Param("status") String status);
    
    /**
     * 分页查询支付流水
     * @param pageQueryDTO 查询条件
     * @return 支付列表
     */
    Page<PaymentDTO> getPaymentPage(PaymentPageQueryDTO pageQueryDTO);
} 