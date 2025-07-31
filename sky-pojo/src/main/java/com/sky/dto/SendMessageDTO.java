package com.sky.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 发送消息DTO
 */
@Data
@ApiModel(description = "发送消息时传递的数据模型")
public class SendMessageDTO implements Serializable {

    @ApiModelProperty("会话ID")
    private Long sessionId;

    @ApiModelProperty("消息类型 1-文本 2-图片 3-文件")
    private Integer messageType;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("媒体文件URL（图片、文件等）")
    private String mediaUrl;

    @ApiModelProperty("发送者类型 1-用户 2-客服")
    private Integer senderType;
} 