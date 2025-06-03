package com.sky.service;

import com.sky.dto.CreditApplicationDTO;
import com.sky.dto.CreditApplicationProcessDTO;
import com.sky.dto.CreditPaymentDTO;
import com.sky.dto.CreditTopupDTO;
import com.sky.vo.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 信用额度服务接口
 */
public interface CreditService {

    /**
     * 获取代理商信用信息
     * @param agentId 代理商ID
     * @return 信用信息
     */
    AgentCreditVO getAgentCreditInfo(Integer agentId);
    
    /**
     * 提交信用额度申请
     * @param agentId 代理商ID
     * @param applicationDTO 申请信息
     * @return 申请结果
     */
    CreditApplicationVO applyCreditLimit(Integer agentId, CreditApplicationDTO applicationDTO);
    
    /**
     * 获取代理商申请历史
     * @param agentId 代理商ID
     * @param page 页码
     * @param pageSize 每页记录数
     * @param status 申请状态
     * @return 申请历史分页结果
     */
    PageResultVO<CreditApplicationVO> getAgentApplications(Integer agentId, Integer page, Integer pageSize, String status);
    
    /**
     * 获取代理商交易记录
     * @param agentId 代理商ID
     * @param page 页码
     * @param pageSize 每页记录数
     * @param type 交易类型
     * @param startDate 起始日期
     * @param endDate 结束日期
     * @return 交易记录分页结果
     */
    PageResultVO<CreditTransactionVO> getAgentTransactions(Integer agentId, Integer page, Integer pageSize, 
                                                        String type, LocalDate startDate, LocalDate endDate);
    
    /**
     * 使用信用额度支付订单
     * @param agentId 代理商ID
     * @param paymentDTO 支付信息
     * @return 支付结果
     */
    CreditPaymentResultVO payWithCredit(Integer agentId, CreditPaymentDTO paymentDTO);
    
    /**
     * 检查信用余额是否足够
     * @param agentId 代理商ID
     * @param bookingId 订单ID
     * @param amount 金额
     * @return 检查结果
     */
    CreditCheckResultVO checkCreditBalance(Integer agentId, Integer bookingId, BigDecimal amount);
    
    /**
     * 管理员获取所有申请
     * @param page 页码
     * @param pageSize 每页记录数
     * @param status 状态
     * @param agentId 代理商ID
     * @return 申请列表分页结果
     */
    PageResultVO<CreditApplicationAdminVO> getAllApplications(Integer page, Integer pageSize, String status, Integer agentId);
    
    /**
     * 处理信用申请
     * @param applicationId 申请ID
     * @param processDTO 处理信息
     * @param adminId 管理员ID
     * @return 处理结果
     */
    CreditApplicationResultVO processApplication(Integer applicationId, CreditApplicationProcessDTO processDTO, Integer adminId);
    
    /**
     * 为代理商充值信用额度
     * @param topupDTO 充值信息
     * @param adminId 管理员ID
     * @return 充值结果
     */
    CreditTopupResultVO topupAgentCredit(CreditTopupDTO topupDTO, Integer adminId);
    
    /**
     * 获取申请详情
     * @param applicationId 申请ID
     * @return 申请详情
     */
    CreditApplicationAdminVO getApplicationDetail(Integer applicationId);
} 