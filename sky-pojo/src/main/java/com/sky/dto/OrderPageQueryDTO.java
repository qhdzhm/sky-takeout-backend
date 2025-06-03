package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 订单分页查询DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "订单分页查询条件")
public class OrderPageQueryDTO {
    
    @ApiModelProperty("页码")
    private int page = 1;
    
    @ApiModelProperty("每页记录数")
    private int pageSize = 10;
    
    @ApiModelProperty("用户ID")
    private Integer userId;
    
    @ApiModelProperty("代理商ID")
    private Integer agentId;
    
    @ApiModelProperty("操作员ID")
    private Long operatorId;
    
    @ApiModelProperty("订单号")
    private String orderNumber;
    
    @ApiModelProperty("订单状态")
    private String status;
    
    @ApiModelProperty("支付状态")
    private String paymentStatus;
    
    @ApiModelProperty("旅行类型")
    private String tourType;
    
    @ApiModelProperty("开始日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @ApiModelProperty("结束日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @ApiModelProperty("联系人")
    private String contactPerson;
    
    @ApiModelProperty("联系电话")
    private String contactPhone;
} 