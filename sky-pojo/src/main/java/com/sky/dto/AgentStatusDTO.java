package com.sky.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 代理商状态更新DTO
 */
@Data
@ApiModel(description = "代理商状态更新参数")
public class AgentStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("代理商ID")
    private Long id;

    @ApiModelProperty(value = "状态：1-启用，0-禁用", example = "1")
    private Integer status;
} 