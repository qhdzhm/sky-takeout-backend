package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户登录返回的数据格式")
public class UserLoginVO implements Serializable {

    @ApiModelProperty("用户id")
    private Long id;

    @ApiModelProperty("用户姓名")
    private String name;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("jwt令牌")
    private String token;

    @ApiModelProperty("用户头像")
    private String avatar;

    @ApiModelProperty("用户类型：regular-普通用户, agent-代理商, agent_operator-代理商操作员")
    private String userType;

    @ApiModelProperty("代理商ID（如果用户是代理商或操作员）")
    private Long agentId;

    @ApiModelProperty("操作员ID（如果用户是操作员）")
    private Long operatorId;

    @ApiModelProperty("折扣率")
    private BigDecimal discountRate;

    @ApiModelProperty("是否可以看到折扣信息")
    private Boolean canSeeDiscount;

    @ApiModelProperty("是否可以看到信用额度信息")
    private Boolean canSeeCredit;
}
