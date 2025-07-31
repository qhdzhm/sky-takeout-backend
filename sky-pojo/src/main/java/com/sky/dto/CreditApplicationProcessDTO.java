package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 信用额度申请处理DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "信用额度申请处理数据传输对象")
public class CreditApplicationProcessDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty("是否批准")
    private Boolean approved;
    
    @ApiModelProperty("批准金额")
    private BigDecimal approvedAmount;
    
    @ApiModelProperty("管理员备注")
    private String comment;
} 