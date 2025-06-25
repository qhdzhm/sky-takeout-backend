package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 行程选项组DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryOptionGroupDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 跟团游ID
     */
    private Integer groupTourId;

    /**
     * 天数（第几天）
     */
    private Integer dayNumber;

    /**
     * 选项组名称（如：第2天选择）
     */
    private String optionGroupName;

    /**
     * 选项组描述
     */
    private String description;

    /**
     * 是否必选（false表示可选）
     */
    private Boolean required;

    /**
     * 该组的选项列表
     */
    private List<ItineraryOptionDTO> options;
} 