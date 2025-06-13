package com.sky.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Refresh Token请求DTO
 */
@Data
@ApiModel(description = "Refresh Token请求数据传输对象")
public class RefreshTokenDTO implements Serializable {

    @ApiModelProperty(value = "Refresh Token", required = true)
    @NotBlank(message = "Refresh Token不能为空")
    private String refreshToken;

    @ApiModelProperty(value = "用户类型", required = false, notes = "regular/agent/admin，可选，用于验证")
    private String userType;
} 