package com.sky.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 代理商DTO
 */
@Data
@ApiModel(description = "代理商数据传输对象")
public class AgentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("代理商ID")
    private Long id;

    @ApiModelProperty("登录用户名（唯一）")
    private String username;

    @ApiModelProperty("登录密码")
    private String password;

    @ApiModelProperty("公司名称")
    private String companyName;

    @ApiModelProperty("联系人姓名")
    private String contactPerson;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("电子邮箱")
    private String email;

    @ApiModelProperty("折扣率，范围0-1，默认1.00（无折扣）")
    private BigDecimal discountRate;

    @ApiModelProperty("账号状态，默认1（活跃）")
    private Integer status;
} 