package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.PaymentDTO;
import com.sky.exception.CustomException;
import com.sky.mapper.PaymentMapper;
import com.sky.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 支付服务实现类
 */
@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    /**
     * 创建支付
     * @param paymentDTO 支付信息
     * @return 支付ID
     */
    @Override
    @Transactional
    public Integer createPayment(PaymentDTO paymentDTO) {
        // 设置用户ID
        Long currentId = BaseContext.getCurrentId();
        paymentDTO.setUserId(currentId.intValue());
        
        // 设置支付订单号
        if (paymentDTO.getPaymentOrderNo() == null) {
            paymentDTO.setPaymentOrderNo(generatePaymentOrderNo());
        }
        
        // 设置初始状态
        paymentDTO.setStatus("pending");
        
        // 设置支付类型
        if (paymentDTO.getType() == null) {
            paymentDTO.setType("payment");
        }
        
        // 设置创建时间
        paymentDTO.setCreateTime(LocalDateTime.now());
        
        // 创建支付记录
        paymentMapper.insert(paymentDTO);
        
        return paymentDTO.getId();
    }

    /**
     * 根据ID获取支付详情
     * @param id 支付ID
     * @return 支付详情
     */
    @Override
    public PaymentDTO getPaymentById(Integer id) {
        return paymentMapper.getById(id);
    }

    /**
     * 根据预订ID获取支付列表
     * @param bookingId 预订ID
     * @return 支付列表
     */
    @Override
    public List<PaymentDTO> getPaymentsByBookingId(Integer bookingId) {
        return paymentMapper.getByBookingId(bookingId);
    }
    
    /**
     * 生成支付订单号
     * @return 支付订单号
     */
    private String generatePaymentOrderNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }
} 