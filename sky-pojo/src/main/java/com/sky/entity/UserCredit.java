package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户积分信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCredit implements Serializable {
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
     * 积分余额
     */
    private BigDecimal balance;
    
    /**
     * 累计获得积分
     */
    private BigDecimal totalEarned;
    
    /**
     * 累计使用积分
     */
    private BigDecimal totalUsed;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;
} 