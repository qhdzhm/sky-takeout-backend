package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作员分配信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "操作员分配信息")
public class OperatorAssignmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("分配记录ID")
    private Long id;

    @ApiModelProperty("订单ID")
    private Integer bookingId;

    @ApiModelProperty("订单号")
    private String orderNumber;

    @ApiModelProperty("联系人")
    private String contactPerson;

    @ApiModelProperty("操作员ID")
    private Long operatorId;

    @ApiModelProperty("操作员姓名")
    private String operatorName;

    @ApiModelProperty("分配人ID")
    private Long assignedBy;

    @ApiModelProperty("分配人姓名")
    private String assignedByName;

    @ApiModelProperty("分配类型")
    private String assignmentType;

    @ApiModelProperty("分配时间")
    private LocalDateTime assignedAt;

    @ApiModelProperty("状态：active-生效中, transferred-已转移, completed-已完成, cancelled-已取消")
    private String status;

    @ApiModelProperty("转移给的操作员ID")
    private Long transferredTo;

    @ApiModelProperty("转移给的操作员姓名")
    private String transferredToName;

    @ApiModelProperty("转移时间")
    private LocalDateTime transferredAt;

    @ApiModelProperty("备注")
    private String notes;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
