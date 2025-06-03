package com.sky.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 用户密码重置DTO
 */
@Data
@ApiModel(description = "用户密码重置参数")
public class UserPasswordResetDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("新密码")
    private String password;
} 