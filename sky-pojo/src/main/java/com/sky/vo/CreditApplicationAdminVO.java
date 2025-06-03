package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信用额度申请管理员视图VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "信用额度申请管理员视图对象")
public class CreditApplicationAdminVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty("申请ID")
    private Integer id;
    
    @ApiModelProperty("申请编号")
    private String applicationNo;
    
    @ApiModelProperty("代理商ID")
    private Integer agentId;
    
    @ApiModelProperty("代理商公司名称")
    private String agentName;
    
    @ApiModelProperty("代理商联系人")
    private String contactPerson;
    
    @ApiModelProperty("代理商联系电话")
    private String contactPhone;
    
    @ApiModelProperty("申请金额")
    private BigDecimal requestedAmount;
    
    @ApiModelProperty("批准金额")
    private BigDecimal approvedAmount;
    
    @ApiModelProperty("申请状态")
    private String status;
    
    @ApiModelProperty("申请原因")
    private String reason;
    
    @ApiModelProperty("管理员备注")
    private String adminComment;
    
    @ApiModelProperty("处理管理员ID")
    private Integer adminId;
    
    @ApiModelProperty("处理管理员名称")
    private String adminName;
    
    @ApiModelProperty("提交时间")
    private LocalDateTime submittedAt;
    
    @ApiModelProperty("处理时间")
    private LocalDateTime processedAt;
} 