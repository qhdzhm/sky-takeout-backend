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
 * 信用申请DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "信用额度申请数据传输对象")
public class CreditApplicationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 代理商ID
     */
    @ApiModelProperty("代理商ID")
    private Long agentId;
    
    /**
     * 申请额度
     */
    @ApiModelProperty("申请额度")
    private BigDecimal creditAmount;
    
    /**
     * 申请原因
     */
    @ApiModelProperty("申请原因")
    private String reason;
    
    /**
     * 处理结果: approved(批准), rejected(拒绝)
     */
    @ApiModelProperty("处理结果: approved(批准), rejected(拒绝)")
    private String status;
    
    /**
     * 处理备注
     */
    @ApiModelProperty("处理备注")
    private String remark;
    
    /**
     * 管理员ID
     */
    @ApiModelProperty("管理员ID")
    private Long adminId;
    
    /**
     * 管理员名称
     */
    @ApiModelProperty("管理员名称")
    private String adminName;
} 