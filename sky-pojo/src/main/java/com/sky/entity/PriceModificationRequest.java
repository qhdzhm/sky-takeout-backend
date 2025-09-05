package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格修改请求实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceModificationRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Integer bookingId;

    /**
     * 原价格
     */
    private BigDecimal originalPrice;

    /**
     * 新价格
     */
    private BigDecimal newPrice;

    /**
     * 价格差异（正数=涨价，负数=降价）
     */
    private BigDecimal priceDifference;

    /**
     * 修改类型：increase-涨价，decrease-降价
     */
    private String modificationType;

    /**
     * 状态：pending-待处理，approved-已同意，rejected-已拒绝，completed-已完成，auto_processed-自动处理
     */
    private String status;

    /**
     * 修改原因
     */
    private String reason;

    /**
     * 创建管理员ID
     */
    private Integer createdByAdmin;

    /**
     * 代理商响应：approved-同意，rejected-拒绝
     */
    private String agentResponse;

    /**
     * 代理商备注
     */
    private String agentNote;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 处理时间
     */
    private LocalDateTime processedAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

