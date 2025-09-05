package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作员分配分页查询DTO
 */
@Data
public class OperatorAssignmentPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private int page = 1;

    /**
     * 每页记录数
     */
    private int pageSize = 10;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 分配人ID
     */
    private Long assignedBy;

    /**
     * 分配状态
     */
    private String status;

    /**
     * 分配类型
     */
    private String assignmentType;

    /**
     * 订单号（模糊查询）
     */
    private String orderNumber;

    /**
     * 联系人（模糊查询）
     */
    private String contactPerson;

    /**
     * 分配开始时间
     */
    private LocalDateTime assignedStartTime;

    /**
     * 分配结束时间
     */
    private LocalDateTime assignedEndTime;
}
