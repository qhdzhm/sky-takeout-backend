package com.sky.service;

import com.sky.dto.AgentCreditDTO;
import com.sky.result.PageResult;
import com.sky.vo.AgentCreditVO;
import com.sky.vo.CreditTransactionVO;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 管理员信用额度相关服务接口
 */
public interface AdminCreditService {

    /**
     * 为代理商充值信用额度
     * @param agentId 代理商ID
     * @param amount 金额
     * @param remark 备注
     * @return 是否成功
     */
    boolean topupCredit(Long agentId, BigDecimal amount, String remark);

    /**
     * 获取所有代理商信用额度信息
     * @param agentId 代理商ID（可选）
     * @param agentName 代理商名称（可选）
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 代理商信用额度信息列表
     */
    PageResult getAllAgentCredits(Long agentId, String agentName, Integer page, Integer pageSize);

    /**
     * 统计满足条件的代理商信用额度总数
     * @param agentId 代理商ID（可选）
     * @param agentName 代理商名称（可选）
     * @return 总数
     */
    Integer countAllAgentCredits(Long agentId, String agentName);

    /**
     * 获取特定代理商的信用额度详情
     * @param agentId 代理商ID
     * @return 代理商信用额度详情
     */
    AgentCreditVO getAgentCreditDetail(Long agentId);

    /**
     * 更新代理商信用额度信息
     * @param creditDTO 信用额度更新数据
     * @return 是否成功
     */
    boolean updateAgentCredit(AgentCreditDTO creditDTO);

    /**
     * 获取信用交易记录
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param transactionNo 交易编号（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 交易记录列表
     */
    PageResult getCreditTransactions(Long agentId, String transactionType, String transactionNo, 
                                    LocalDate startDate, LocalDate endDate, 
                                    Integer page, Integer pageSize);

    /**
     * 统计满足条件的信用交易记录总数
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param transactionNo 交易编号（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 总数
     */
    Integer countCreditTransactions(Long agentId, String transactionType, String transactionNo, 
                                   LocalDate startDate, LocalDate endDate);

    /**
     * 获取信用交易统计数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param groupBy 分组依据：day, month, year
     * @return 统计数据
     */
    Map<String, Object> getCreditTransactionStats(LocalDate startDate, LocalDate endDate, String groupBy);

    /**
     * 导出信用交易记录
     * @param response HTTP响应对象
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param transactionNo 交易编号（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     */
    void exportCreditTransactions(HttpServletResponse response, Long agentId, String transactionType, 
                                 String transactionNo, LocalDate startDate, LocalDate endDate);
    
    /**
     * 为没有信用额度记录的现有代理商初始化信用额度
     * @param defaultCredit 默认信用额度
     * @return 初始化成功的代理商数量
     */
    int initializeCreditForExistingAgents(BigDecimal defaultCredit);
}