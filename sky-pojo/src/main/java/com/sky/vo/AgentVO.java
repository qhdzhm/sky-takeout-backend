package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 代理商VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "代理商详细信息")
public class AgentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("代理商ID")
    private Long id;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("公司名称")
    private String companyName;
    
    @ApiModelProperty("联系人姓名")
    private String contactPerson;
    
    @ApiModelProperty("联系电话")
    private String phone;
    
    @ApiModelProperty("电子邮箱")
    private String email;
    
    @ApiModelProperty("折扣率")
    private BigDecimal discountRate;
    
    @ApiModelProperty("账号状态：1-活跃，0-禁用")
    private Integer status;
    
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
} 