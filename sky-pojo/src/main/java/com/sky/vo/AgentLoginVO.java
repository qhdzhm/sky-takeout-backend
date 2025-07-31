package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 代理商登录VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 代理商ID
    private Long id;
    
    // 公司名称
    private String companyName;
    
    // 联系人姓名
    private String contactPerson;
    
    // 用户名
    private String username;
    
    // 代理商折扣率
    private BigDecimal discountRate;
    
    // JWT令牌
    private String token;
} 