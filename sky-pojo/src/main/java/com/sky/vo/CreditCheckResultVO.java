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
 * 信用额度检查结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "信用额度检查结果视图对象")
public class CreditCheckResultVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty("是否足够")
    private Boolean sufficient;
    
    @ApiModelProperty("可用额度")
    private BigDecimal availableCredit;
    
    @ApiModelProperty("所需金额")
    private BigDecimal requiredAmount;
    
    @ApiModelProperty("不足金额（如果不足）")
    private BigDecimal shortageAmount;
    
    @ApiModelProperty("账户是否被冻结")
    private Boolean frozen;
    
    @ApiModelProperty("错误信息或提示")
    private String message;
} 