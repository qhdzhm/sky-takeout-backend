package com.sky.constant;

/**
 * 信用额度相关常量
 */
public class CreditConstants {
    
    // 申请状态
    public static final String APPLICATION_STATUS_PENDING = "pending";
    public static final String APPLICATION_STATUS_APPROVED = "approved";
    public static final String APPLICATION_STATUS_REJECTED = "rejected";
    
    // 交易类型
    public static final String TRANSACTION_TYPE_PAYMENT = "payment";
    public static final String TRANSACTION_TYPE_REFUND = "refund";
    public static final String TRANSACTION_TYPE_ADJUSTMENT = "adjustment";
    public static final String TRANSACTION_TYPE_TOPUP = "topup";
    
    // 账户状态
    public static final Integer ACCOUNT_STATUS_NORMAL = 0;  // 账户正常
    public static final Integer ACCOUNT_STATUS_FROZEN = 1;  // 账户冻结
    
    // 错误信息
    public static final String ERROR_ACCOUNT_FROZEN = "该账户已被冻结，无法使用信用额度进行支付";
} 