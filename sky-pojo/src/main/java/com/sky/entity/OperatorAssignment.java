package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作员分配记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Integer bookingId;

    /**
     * 被分配的操作员ID
     */
    private Long operatorId;

    /**
     * 分配人ID
     */
    private Long assignedBy;

    /**
     * 分配类型：hotel_management-酒店管理, tour_arrangement-排团管理
     */
    private String assignmentType;

    /**
     * 分配时间
     */
    private LocalDateTime assignedAt;

    /**
     * 状态：active-生效中, transferred-已转移, completed-已完成, cancelled-已取消
     */
    private String status;

    /**
     * 转移给谁（如果有转移）
     */
    private Long transferredTo;

    /**
     * 转移时间
     */
    private LocalDateTime transferredAt;

    /**
     * 备注
     */
    private String notes;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // ====== 关联查询字段（非数据库字段） ======

    /**
     * 被分配操作员姓名
     */
    private String operatorName;

    /**
     * 分配人姓名
     */
    private String assignedByName;

    /**
     * 转移接收人姓名
     */
    private String transferredToName;

    /**
     * 订单号
     */
    private String orderNumber;

    /**
     * 联系人
     */
    private String contactPerson;
}
