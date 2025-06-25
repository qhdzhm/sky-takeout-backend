package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 保存行程选择DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveItineraryChoicesDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预订ID
     */
    private Integer bookingId;

    /**
     * 选择列表
     */
    private List<ItineraryChoiceItemDTO> choices;

    /**
     * 行程选择项DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItineraryChoiceItemDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 天数（第几天）
         */
        private Integer dayNumber;

        /**
         * 选择的一日游ID
         */
        private Integer selectedDayTourId;

        /**
         * 选项组名称
         */
        private String optionGroupName;
    }
} 