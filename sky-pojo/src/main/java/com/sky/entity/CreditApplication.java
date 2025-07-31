package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信用额度申请
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditApplication implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 申请ID
     */
    private Long id;
    
    /**
     * 申请编号
     */
    private String applicationNo;
    
    /**
     * 代理商ID
     */
    private Long agentId;
    
    /**
     * 代理商名称
     */
    private String agentName;
    
    /**
     * 联系人
     */
    private String contactPerson;
    
    /**
     * 联系人电话
     */
    private String contactPhone;
    
    /**
     * 申请金额
     */
    private BigDecimal requestedAmount;
    
    /**
     * 批准金额
     */
    private BigDecimal approvedAmount;
    
    /**
     * 申请状态（pending：待处理，approved：已批准，rejected：已拒绝）
     */
    private String status;
    
    /**
     * 申请原因
     */
    private String reason;
    
    /**
     * 管理员备注
     */
    private String adminComment;
    
    /**
     * 处理管理员ID
     */
    private Long adminId;
    
    /**
     * 处理管理员名称
     */
    private String adminName;
    
    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;
    
    /**
     * 处理时间
     */
    private LocalDateTime processedAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 