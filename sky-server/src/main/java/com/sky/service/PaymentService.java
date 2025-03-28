package com.sky.service;

import com.sky.dto.PaymentDTO;

import java.util.List;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 创建支付
     * @param paymentDTO 支付信息
     * @return 支付ID
     */
    Integer createPayment(PaymentDTO paymentDTO);

    /**
     * 根据ID获取支付详情
     * @param id 支付ID
     * @return 支付详情
     */
    PaymentDTO getPaymentById(Integer id);

    /**
     * 根据预订ID获取支付列表
     * @param bookingId 预订ID
     * @return 支付列表
     */
    List<PaymentDTO> getPaymentsByBookingId(Integer bookingId);
} 