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
 * 当前值班状态VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "当前值班状态信息")
public class CurrentDutyStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("当前排团主管ID")
    private Long currentTourMasterId;

    @ApiModelProperty("当前排团主管姓名")
    private String currentTourMasterName;

    @ApiModelProperty("值班开始时间")
    private LocalDateTime shiftStart;

    @ApiModelProperty("值班时长（分钟）")
    private Long durationMinutes;

    @ApiModelProperty("是否可以转移值班")
    private Boolean canTransfer;

    @ApiModelProperty("备注")
    private String notes;
}
