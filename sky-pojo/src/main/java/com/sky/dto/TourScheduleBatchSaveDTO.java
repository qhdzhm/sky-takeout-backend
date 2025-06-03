package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 批量保存行程排序DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourScheduleBatchSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Integer bookingId;

    /**
     * 行程排序列表
     */
    private List<TourScheduleOrderDTO> schedules;
} 