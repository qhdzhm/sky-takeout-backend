package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付数据传输对象
 */
@Data
public class PaymentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 支付ID
    private Integer id;

    // 用户ID
    private Integer userId;

    // 预订ID
    private Integer bookingId;

    // 支付金额
    private BigDecimal amount;

    // 支付订单号
    private String paymentOrderNo;

    // 支付状态（pending, success, failed, refunded）
    private String status;

    // 支付类型（payment, refund）
    private String type;

    // 关联的支付ID（用于退款）
    private Integer relatedPaymentId;

    // 支付方式（wechat, alipay, credit_card）
    private String paymentMethod;

    // 支付时间
    private LocalDateTime paymentTime;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 交易ID（第三方支付平台返回的交易号）
    private String transactionId;

    // 是否使用信用额度支付
    private Boolean isCreditPayment;

    // 信用额度交易ID
    private Integer creditTransactionId;
} 