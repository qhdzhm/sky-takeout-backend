package com.sky.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 转人工服务DTO
 */
@Data
@ApiModel(description = "转人工服务时传递的数据模型")
public class TransferToServiceDTO implements Serializable {

    @ApiModelProperty("会话类型 1-主动咨询 2-AI转人工 3-投诉建议")
    private Integer sessionType;

    @ApiModelProperty("问题描述或会话主题")
    private String subject;

    @ApiModelProperty("AI对话上下文ID，用于获取历史对话")
    private String aiContextId;

    @ApiModelProperty("优先级 1-普通 2-紧急 3-非常紧急")
    private Integer priority;
} 