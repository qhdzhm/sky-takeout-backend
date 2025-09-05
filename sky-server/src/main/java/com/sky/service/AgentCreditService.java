package com.sky.service;

import com.sky.dto.CreditApplicationDTO;
import com.sky.dto.CreditPaymentDTO;
import com.sky.dto.CreditRepaymentDTO;
import com.sky.vo.AgentCreditVO;
import com.sky.vo.CreditApplicationVO;
import com.sky.vo.CreditCheckResultVO;
import com.sky.vo.CreditPaymentResultVO;
import com.sky.vo.CreditRepaymentResultVO;
import com.sky.vo.CreditTransactionVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 代理商信用额度Service
 */
public interface AgentCreditService {

    /**
     * 获取代理商信用额度信息
     *
     * @param agentId 代理商ID
     * @return 信用额度信息
     */
    AgentCreditVO getCreditInfo(Long agentId);

    /**
     * 获取代理商的信用交易记录
     *
     * @param agentId 代理商ID
     * @param type 交易类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 交易记录列表
     */
    List<CreditTransactionVO> getCreditTransactions(Long agentId, String type, 
                                                   LocalDate startDate, LocalDate endDate, 
                                                   int page, int pageSize);

    /**
     * 获取代理商的信用交易记录总数
     *
     * @param agentId 代理商ID
     * @param type 交易类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 交易记录总数
     */
    int countCreditTransactions(Long agentId, String type, LocalDate startDate, LocalDate endDate);

    /**
     * 使用信用额度支付订单
     *
     * @param agentId 代理商ID
     * @param paymentDTO 支付信息
     * @return 支付结果
     */
    CreditPaymentResultVO payWithCredit(Long agentId, CreditPaymentDTO paymentDTO);

    /**
     * 检查信用额度是否足够支付
     *
     * @param agentId 代理商ID
     * @param bookingId 订单ID
     * @param amount 支付金额
     * @return 检查结果
     */
    CreditCheckResultVO checkCreditPayment(Long agentId, Long bookingId, BigDecimal amount);
    
    /**
     * 信用额度还款
     *
     * @param agentId 代理商ID
     * @param repaymentDTO 还款信息
     * @return 还款结果
     */
    CreditRepaymentResultVO repayCredit(Long agentId, CreditRepaymentDTO repaymentDTO);
    
    /**
     * 增加代理商信用余额（用于退款等场景）
     *
     * @param agentId 代理商ID
     * @param amount 增加金额
     * @param note 备注说明
     * @return 是否成功
     */
    boolean addCredit(Long agentId, BigDecimal amount, String note);
} 