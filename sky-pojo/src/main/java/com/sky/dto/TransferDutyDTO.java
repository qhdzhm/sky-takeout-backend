package com.sky.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 转移值班DTO
 */
@Data
public class TransferDutyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 转移给的操作员ID
     */
    @NotNull(message = "目标操作员ID不能为空")
    private Long toOperatorId;

    /**
     * 值班类型（默认为tour_master）
     */
    private String dutyType = "tour_master";

    /**
     * 转移原因
     */
    private String transferReason;

    /**
     * 交接备注
     */
    private String notes;
}
