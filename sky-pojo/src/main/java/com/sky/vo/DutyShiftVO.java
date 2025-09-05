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
 * 值班记录VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "值班记录信息")
public class DutyShiftVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("值班记录ID")
    private Long id;

    @ApiModelProperty("操作员ID")
    private Long operatorId;

    @ApiModelProperty("操作员姓名")
    private String operatorName;

    @ApiModelProperty("值班类型：tour_master-排团主管, hotel_operator-酒店专员")
    private String dutyType;

    @ApiModelProperty("值班开始时间")
    private LocalDateTime shiftStart;

    @ApiModelProperty("值班结束时间")
    private LocalDateTime shiftEnd;

    @ApiModelProperty("状态：active-进行中, completed-已完成, transferred-已转移")
    private String status;

    @ApiModelProperty("转移给的操作员ID")
    private Long transferredTo;

    @ApiModelProperty("转移给的操作员姓名")
    private String transferredToName;

    @ApiModelProperty("转移时间")
    private LocalDateTime transferredAt;

    @ApiModelProperty("转移原因")
    private String transferReason;

    @ApiModelProperty("值班时长（分钟）")
    private Long durationMinutes;

    @ApiModelProperty("交接备注")
    private String notes;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}
