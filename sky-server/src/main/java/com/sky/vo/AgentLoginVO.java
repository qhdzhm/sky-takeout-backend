package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 代理商登录返回VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentLoginVO implements Serializable {

    /**
     * 代理商id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 折扣率
     */
    private BigDecimal discountRate;

    /**
     * jwt令牌
     */
    private String token;
} 