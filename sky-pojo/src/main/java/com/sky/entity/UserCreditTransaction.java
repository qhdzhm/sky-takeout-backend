package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户积分交易记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreditTransaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 积分数量
     */
    private BigDecimal amount;
    
    /**
     * 交易类型: referral_reward, order_reward, used
     */
    private String type;
    
    /**
     * 关联ID（订单ID等）
     */
    private Integer referenceId;
    
    /**
     * 推荐级别：1表示直接推荐，2表示间接推荐
     */
    private Integer level;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 