package com.sky.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分配订单DTO
 */
@Data
public class AssignOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Integer bookingId;

    /**
     * 操作员ID（被分配的酒店专员）
     */
    @NotNull(message = "操作员ID不能为空")
    private Long operatorId;

    /**
     * 分配类型（默认为hotel_management）
     */
    private String assignmentType = "hotel_management";

    /**
     * 备注
     */
    private String notes;
}
