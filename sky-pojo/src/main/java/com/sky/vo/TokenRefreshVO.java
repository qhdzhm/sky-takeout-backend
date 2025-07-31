package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * Token刷新响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Token刷新响应数据")
public class TokenRefreshVO implements Serializable {

    @ApiModelProperty(value = "新的访问令牌")
    private String accessToken;

    @ApiModelProperty(value = "新的刷新令牌", notes = "如果原refresh token即将过期，会返回新的refresh token")
    private String refreshToken;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户类型", notes = "regular/agent/admin")
    private String userType;

    @ApiModelProperty(value = "访问令牌过期时间（毫秒时间戳）")
    private Long accessTokenExpiry;

    @ApiModelProperty(value = "刷新令牌过期时间（毫秒时间戳）")
    private Long refreshTokenExpiry;

    @ApiModelProperty(value = "是否更新了refresh token")
    private Boolean refreshTokenUpdated;
} 