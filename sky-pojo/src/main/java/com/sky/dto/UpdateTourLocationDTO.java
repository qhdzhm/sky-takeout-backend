package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 更新游玩地点请求DTO
 * 用于同车订票拖拽功能
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTourLocationDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Integer orderId;
    
    /**
     * 新的游玩地点
     */
    private String newLocation;
    
    /**
     * 游玩日期
     */
    private LocalDate tourDate;
    
    /**
     * 操作员ID（用于记录操作人）
     */
    private Long operatorId;
    
    /**
     * 备注信息
     */
    private String remarks;
}
