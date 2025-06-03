package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 代理商简要信息VO（用于下拉选项等）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "代理商简要信息")
public class AgentSimpleVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("代理商ID")
    private Long id;

    @ApiModelProperty("代理商名称")
    private String companyName;
    
    @ApiModelProperty("代理商名称(与companyName相同，为前端兼容)")
    private String name;

    @ApiModelProperty("联系人")
    private String contactPerson;

    @ApiModelProperty("联系电话")
    private String contactPhone;
} 