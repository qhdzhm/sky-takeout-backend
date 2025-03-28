package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 一日游常见问题DTO
 */
@Data
public class DayTourFaqDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Integer id;

    /**
     * 一日游ID
     */
    private Integer dayTourId;

    /**
     * 问题
     */
    private String question;

    /**
     * 回答
     */
    private String answer;
} 