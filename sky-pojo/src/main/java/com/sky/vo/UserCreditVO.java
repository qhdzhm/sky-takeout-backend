package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户积分信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreditVO implements Serializable {
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
    
    /**
     * 邀请码
     */
    private String inviteCode;
    
    /**
     * 推荐人ID（可能为空）
     */
    private Long referredBy;
    
    /**
     * 推荐人用户名（可能为空）
     */
    private String referrerUsername;
    
    /**
     * 推荐的用户数量
     */
    private Integer referralsCount;
} 