package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrderPageQueryDTO;
import com.sky.entity.TourBooking;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper {
    
    /**
     * 分页查询订单
     * @param orderPageQueryDTO 查询条件
     * @return 分页订单列表
     */
    Page<OrderVO> pageQuery(OrderPageQueryDTO orderPageQueryDTO);
    
    /**
     * 根据ID查询订单详情
     * @param bookingId 订单ID
     * @return 订单详情
     */
    OrderVO getById(@Param("bookingId") Integer bookingId);
    
    /**
     * 根据订单号查询订单
     * @param orderNumber 订单号
     * @return 订单信息
     */
    TourBooking getByOrderNumber(@Param("orderNumber") String orderNumber);
    
    /**
     * 插入订单
     * @param tourBooking 订单信息
     */
    void insert(TourBooking tourBooking);
    
    /**
     * 更新订单
     * @param tourBooking 订单信息
     * @return 影响的行数
     */
    int update(TourBooking tourBooking);
    
    /**
     * 更新订单状态
     * @param bookingId 订单ID
     * @param status 订单状态
     * @return 影响的行数
     */
    int updateStatus(@Param("bookingId") Integer bookingId, @Param("status") String status);
    
    /**
     * 更新支付状态
     * @param bookingId 订单ID
     * @param paymentStatus 支付状态
     * @return 影响的行数
     */
    int updatePaymentStatus(@Param("bookingId") Integer bookingId, @Param("paymentStatus") String paymentStatus);
} 