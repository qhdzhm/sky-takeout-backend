package com.sky.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 代理商分页查询DTO
 */
@Data
@ApiModel(description = "代理商分页查询参数")
public class AgentPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页记录数", example = "10")
    private Integer pageSize;

    @ApiModelProperty("公司名称（模糊查询）")
    private String companyName;

    @ApiModelProperty("联系人姓名（模糊查询）")
    private String contactPerson;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty(value = "账号状态：1-活跃，0-禁用", example = "1")
    private Integer status;
} 