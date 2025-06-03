package com.sky.service;

import com.sky.dto.OrderPageQueryDTO;
import com.sky.dto.OrderUpdateDTO;
import com.sky.dto.PaymentDTO;
import com.sky.entity.TourBooking;
import com.sky.vo.OrderVO;
import com.sky.vo.PageResultVO;

/**
 * 订单管理服务接口
 */
public interface OrderService {

    /**
     * 分页查询订单
     * @param orderPageQueryDTO 查询条件
     * @return 订单分页结果
     */
    PageResultVO<OrderVO> pageQuery(OrderPageQueryDTO orderPageQueryDTO);
    
    /**
     * 根据ID获取订单详情
     * @param bookingId 订单ID
     * @return 订单详情
     */
    OrderVO getById(Integer bookingId);
    
    /**
     * 根据订单号获取订单信息
     * @param orderNumber 订单号
     * @return 订单实体
     */
    TourBooking getByOrderNumber(String orderNumber);
    
    /**
     * 创建订单
     * @param tourBooking 订单信息
     * @return 创建的订单ID
     */
    Integer createOrder(TourBooking tourBooking);
    
    /**
     * 更新订单信息
     * @param tourBooking 订单信息
     * @return 是否更新成功
     */
    boolean updateOrder(TourBooking tourBooking);
    
    /**
     * 确认订单
     * @param bookingId 订单ID
     * @param remark 备注信息
     * @return 是否确认成功
     */
    boolean confirmOrder(Integer bookingId, String remark);
    
    /**
     * 取消订单
     * @param bookingId 订单ID
     * @param remark 取消原因
     * @return 是否取消成功
     */
    boolean cancelOrder(Integer bookingId, String remark);
    
    /**
     * 完成订单
     * @param bookingId 订单ID
     * @param remark 备注信息
     * @return 是否完成成功
     */
    boolean completeOrder(Integer bookingId, String remark);
    
    /**
     * 更新订单状态
     * @param bookingId 订单ID
     * @param orderUpdateDTO 订单状态更新信息
     * @return 是否更新成功
     */
    boolean updateOrderStatus(Integer bookingId, OrderUpdateDTO orderUpdateDTO);
    
    /**
     * 更新支付状态
     * @param bookingId 订单ID
     * @param paymentStatus 支付状态
     * @return 是否更新成功
     */
    boolean updatePaymentStatus(Integer bookingId, String paymentStatus);

    /**
     * 支付订单
     * @param bookingId 订单ID
     * @param paymentDTO 支付信息
     * @return 支付结果
     */
    Boolean payOrder(Integer bookingId, PaymentDTO paymentDTO);
} 