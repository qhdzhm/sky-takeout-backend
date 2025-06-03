package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单状态更新DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "订单状态更新DTO")
public class OrderUpdateDTO {
    
    @ApiModelProperty("订单状态 (pending-待确认, confirmed-已确认, cancelled-已取消, completed-已完成)")
    private String status;
    
    @ApiModelProperty("支付状态 (unpaid-未支付, partial-部分支付, paid-已支付)")
    private String paymentStatus;
    
    @ApiModelProperty("备注")
    private String remark;
    
    @ApiModelProperty("乘客信息列表，用于同时更新订单中的乘客信息")
    private List<PassengerDTO> passengers;
} 