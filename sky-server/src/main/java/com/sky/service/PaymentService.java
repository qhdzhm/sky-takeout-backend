package com.sky.service;

import com.sky.dto.PaymentDTO;
import com.sky.dto.CreditPaymentDTO;
import com.sky.dto.PaymentPageQueryDTO;
import com.sky.result.PageResult;

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
    
    /**
     * 更新支付状态
     * @param id 支付ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updatePaymentStatus(Integer id, String status);
    
    /**
     * 退款处理
     * @param id 原支付ID
     * @param refundDTO 退款信息
     * @return 退款ID
     */
    Integer refundPayment(Integer id, PaymentDTO refundDTO);
    
    /**
     * 代理商信用额度支付
     * @param creditPaymentDTO 信用额度支付信息
     * @return 是否成功
     */
    boolean processCreditPayment(CreditPaymentDTO creditPaymentDTO);
    
    /**
     * 分页查询支付流水
     * @param pageQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult getTransactionPage(PaymentPageQueryDTO pageQueryDTO);
} 