package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 代理商密码重置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "代理商密码重置参数")
public class AgentPasswordResetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("代理商ID")
    private Long id;

    @ApiModelProperty("新密码")
    private String newPassword;
} 