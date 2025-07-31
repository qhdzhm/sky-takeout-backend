package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 信用额度申请处理结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "信用额度申请处理结果视图对象")
public class CreditApplicationResultVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty("申请ID")
    private Integer applicationId;
    
    @ApiModelProperty("申请状态")
    private String status;
    
    @ApiModelProperty("批准金额")
    private BigDecimal approvedAmount;
} 