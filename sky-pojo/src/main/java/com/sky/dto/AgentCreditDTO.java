package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 代理商信用额度数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCreditDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 代理商ID
     */
    private Long agentId;
    
    /**
     * 总额度
     */
    private BigDecimal totalCredit;
    
    /**
     * 信用评级
     */
    private String creditRating;
    
    /**
     * 信用利率(%)
     */
    private BigDecimal interestRate;
    
    /**
     * 账单周期日
     */
    private Integer billingCycleDay;
    
    /**
     * 最后结算日期
     */
    private LocalDate lastSettlementDate;
    
    /**
     * 透支次数
     */
    private Integer overdraftCount;
    
    /**
     * 是否冻结:0-正常,1-冻结
     */
    private Boolean isFrozen;
    
    /**
     * 修改备注
     */
    private String note;
} 