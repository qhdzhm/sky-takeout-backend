package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 值班记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DutyShift implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 值班类型：tour_master-排团主管, hotel_operator-酒店专员
     */
    private String dutyType;

    /**
     * 值班开始时间
     */
    private LocalDateTime shiftStart;

    /**
     * 值班结束时间
     */
    private LocalDateTime shiftEnd;

    /**
     * 状态：active-进行中, completed-已完成, transferred-已转移
     */
    private String status;

    /**
     * 转移给谁
     */
    private Long transferredTo;

    /**
     * 转移时间
     */
    private LocalDateTime transferredAt;

    /**
     * 转移原因
     */
    private String transferReason;

    /**
     * 交接备注
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
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 转移接收人姓名
     */
    private String transferredToName;

    /**
     * 值班时长（分钟）
     */
    private Long durationMinutes;
}
