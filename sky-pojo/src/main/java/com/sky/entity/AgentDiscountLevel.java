package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 中介折扣等级实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDiscountLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键
    private Long id;
    
    // 等级代码：A、B、C
    private String levelCode;
    
    // 等级名称：如A级代理、B级代理、C级代理
    private String levelName;
    
    // 等级描述
    private String levelDescription;
    
    // 排序顺序，数字越小等级越高
    private Integer sortOrder;
    
    // 是否激活：1-激活，0-停用
    private Integer isActive;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
} 