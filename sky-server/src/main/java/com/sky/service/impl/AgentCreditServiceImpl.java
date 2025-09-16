package com.sky.service.impl;

import com.sky.constant.CreditConstants;
import com.sky.context.BaseContext;
import com.sky.dto.CreditPaymentDTO;
import com.sky.dto.CreditRepaymentDTO;
import com.sky.entity.AgentCredit;
import com.sky.entity.CreditTransaction;
import com.sky.entity.Agent;
import com.sky.exception.BusinessException;
import com.sky.mapper.AgentCreditMapper;
import com.sky.mapper.CreditTransactionMapper;
import com.sky.mapper.AgentMapper;
import com.sky.service.AgentCreditService;
import com.sky.vo.AgentCreditVO;
import com.sky.vo.CreditCheckResultVO;
import com.sky.vo.CreditPaymentResultVO;
import com.sky.vo.CreditRepaymentResultVO;
import com.sky.vo.CreditTransactionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代理商信用额度Service实现类
 */
@Service
@Slf4j
public class AgentCreditServiceImpl implements AgentCreditService {

    @Autowired
    private AgentCreditMapper agentCreditMapper;

    @Autowired
    private CreditTransactionMapper creditTransactionMapper;

    @Autowired
    private AgentMapper agentMapper;

    /**
     * 获取代理商信用额度信息
     *
     * @param agentId 代理商ID
     * @return 信用额度信息
     */
    @Override
    public AgentCreditVO getCreditInfo(Long agentId) {
        log.info("🔍 AgentCreditService.getCreditInfo - 开始获取代理商信用信息, agentId: {}", agentId);
        
        AgentCredit agentCredit = agentCreditMapper.getByAgentId(agentId);
        if (agentCredit == null) {
            log.error("❌ 代理商信用额度信息不存在, agentId: {}", agentId);
            throw new BusinessException("代理商信用额度信息不存在");
        }
        
        log.info("🔍 从数据库获取到的原始信用数据: id={}, agentId={}, totalCredit={}, usedCredit={}, availableCredit={}, depositBalance={}", 
                agentCredit.getId(), agentCredit.getAgentId(), agentCredit.getTotalCredit(), 
                agentCredit.getUsedCredit(), agentCredit.getAvailableCredit(), agentCredit.getDepositBalance());
        
        // 计算额度使用百分比需要的数据
        BigDecimal totalCredit = agentCredit.getTotalCredit() != null ? agentCredit.getTotalCredit() : BigDecimal.ZERO;
        BigDecimal usedCredit = agentCredit.getUsedCredit() != null ? agentCredit.getUsedCredit() : BigDecimal.ZERO;
        
        AgentCreditVO vo = new AgentCreditVO();
        BeanUtils.copyProperties(agentCredit, vo);
        
        // 计算额度使用百分比
        if (totalCredit.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentage = usedCredit.divide(totalCredit, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            vo.setUsagePercentage(percentage);
        } else {
            vo.setUsagePercentage(BigDecimal.ZERO);
        }
        
        log.info("🔍 AgentCreditService.getCreditInfo - 返回的VO数据: id={}, agentId={}, totalCredit={}, usedCredit={}, availableCredit={}, depositBalance={}, usagePercentage={}", 
                vo.getId(), vo.getAgentId(), vo.getTotalCredit(), 
                vo.getUsedCredit(), vo.getAvailableCredit(), vo.getDepositBalance(), vo.getUsagePercentage());
        
        return vo;
    }

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
    @Override
    public List<CreditTransactionVO> getCreditTransactions(Long agentId, String type, 
                                                         LocalDate startDate, LocalDate endDate, 
                                                         int page, int pageSize) {
        LocalDateTime startDateTime = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime endDateTime = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;
        
        List<CreditTransaction> transactions = creditTransactionMapper.getByAgentId(
                agentId, type, startDateTime, endDateTime, (page - 1) * pageSize, pageSize);
        
        return transactions.stream().map(tx -> {
            CreditTransactionVO vo = new CreditTransactionVO();
            BeanUtils.copyProperties(tx, vo);
            
            // 手动复制字段名不同的属性
            vo.setDescription(tx.getNote());
            vo.setCreatedAt(tx.getCreatedAt());
            
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取代理商的信用交易记录总数
     *
     * @param agentId 代理商ID
     * @param type 交易类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 交易记录总数
     */
    @Override
    public int countCreditTransactions(Long agentId, String type, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime endDateTime = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;
        
        return creditTransactionMapper.countByAgentId(agentId, type, startDateTime, endDateTime);
    }

    /**
     * 使用信用额度支付订单
     *
     * @param agentId 代理商ID
     * @param paymentDTO 支付信息
     * @return 支付结果
     */
    @Override
    @Transactional
    public CreditPaymentResultVO payWithCredit(Long agentId, CreditPaymentDTO paymentDTO) {
        // 获取代理商信息
        Agent agent = agentMapper.getById(agentId);
        if (agent == null) {
            throw new BusinessException("代理商信息不存在");
        }
        
        // 检查信用额度是否足够
        AgentCredit agentCredit = agentCreditMapper.getByAgentId(agentId);
        if (agentCredit == null) {
            throw new BusinessException("代理商信用额度信息不存在");
        }
        
        // 检查账户是否被冻结
        if (agentCredit.getIsFrozen() != null && agentCredit.getIsFrozen()) {
            throw new BusinessException(CreditConstants.ERROR_ACCOUNT_FROZEN);
        }
        
        BigDecimal amount = paymentDTO.getAmount();
        // 计算总可用额度：信用额度 + 预存余额
        BigDecimal totalAvailable = agentCredit.getTotalCredit()
            .subtract(agentCredit.getUsedCredit())
            .add(agentCredit.getDepositBalance());
            
        if (totalAvailable.compareTo(amount) < 0) {
            throw new BusinessException("可用额度不足，请充值后再支付");
        }
        
        // 生成交易编号
        String transactionNo = "TX" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) 
                + String.format("%04d", (int)(Math.random() * 10000));
        
        // 记录交易前余额/后余额（总可用=信用+押金-已用）
        BigDecimal balanceBefore = totalAvailable;
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        
        // 优先使用预存余额
        BigDecimal remainingAmount = amount;
        if (agentCredit.getDepositBalance().compareTo(BigDecimal.ZERO) > 0) {
            if (agentCredit.getDepositBalance().compareTo(remainingAmount) >= 0) {
                // 预存余额足够支付
                agentCredit.setDepositBalance(agentCredit.getDepositBalance().subtract(remainingAmount));
                remainingAmount = BigDecimal.ZERO;
            } else {
                // 预存余额不足，全部用完
                remainingAmount = remainingAmount.subtract(agentCredit.getDepositBalance());
                agentCredit.setDepositBalance(BigDecimal.ZERO);
            }
        }
        
        // 如果还有剩余金额，使用信用额度
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            agentCredit.setUsedCredit(agentCredit.getUsedCredit().add(remainingAmount));
        }
        
        // 更新可用额度
        agentCredit.setAvailableCredit(agentCredit.getTotalCredit()
            .subtract(agentCredit.getUsedCredit())
            .add(agentCredit.getDepositBalance()));
            
        agentCredit.setLastUpdated(LocalDateTime.now());
        
        // 记录交易
        // 获取当前实际操作人ID（可能是agent、operator或user）
        Long currentOperatorId = BaseContext.getCurrentId();
        if (currentOperatorId == null) {
            // 如果无法获取当前操作人ID，则使用agentId作为fallback
            currentOperatorId = agentId;
        }
        
        CreditTransaction transaction = CreditTransaction.builder()
                .transactionNo(transactionNo)
                .agentId(agentId)
                .amount(amount)
                .transactionType(CreditConstants.TRANSACTION_TYPE_PAYMENT)
                .bookingId(paymentDTO.getBookingId())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .note(paymentDTO.getNote())
                .createdBy(currentOperatorId)
                .createdAt(LocalDateTime.now())
                .build();
        
        creditTransactionMapper.insert(transaction);
        
        // 更新信用额度
        agentCreditMapper.update(agentCredit);
        
        // 返回支付结果
        return CreditPaymentResultVO.builder()
                .transactionId(transaction.getId())
                .transactionNo(transaction.getTransactionNo())
                .bookingId(transaction.getBookingId())
                .amount(transaction.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(transaction.getBalanceAfter())
                .paymentStatus("paid")
                .build();
    }

    /**
     * 检查信用额度是否足够支付
     *
     * @param agentId 代理商ID
     * @param bookingId 订单ID
     * @param amount 支付金额
     * @return 检查结果
     */
    @Override
    public CreditCheckResultVO checkCreditPayment(Long agentId, Long bookingId, BigDecimal amount) {
        AgentCredit agentCredit = agentCreditMapper.getByAgentId(agentId);
        if (agentCredit == null) {
            throw new BusinessException("代理商信用额度信息不存在");
        }
        
        // 检查账户是否被冻结
        if (agentCredit.getIsFrozen() != null && agentCredit.getIsFrozen()) {
            return CreditCheckResultVO.builder()
                    .sufficient(false)
                    .availableCredit(agentCredit.getAvailableCredit())
                    .requiredAmount(amount)
                    .shortageAmount(amount)
                    .frozen(true)
                    .message(CreditConstants.ERROR_ACCOUNT_FROZEN)
                    .build();
        }
        
        boolean sufficient = agentCredit.getAvailableCredit().compareTo(amount) >= 0;
        BigDecimal shortageAmount = sufficient ? BigDecimal.ZERO 
                : amount.subtract(agentCredit.getAvailableCredit());
        
        return CreditCheckResultVO.builder()
                .sufficient(sufficient)
                .availableCredit(agentCredit.getAvailableCredit())
                .requiredAmount(amount)
                .shortageAmount(shortageAmount)
                .frozen(false)
                .build();
    }
    
    /**
     * 信用额度还款
     *
     * @param agentId 代理商ID
     * @param repaymentDTO 还款信息
     * @return 还款结果
     */
    @Override
    @Transactional
    public CreditRepaymentResultVO repayCredit(Long agentId, CreditRepaymentDTO repaymentDTO) {
        // 获取代理商信息
        Agent agent = agentMapper.getById(agentId);
        if (agent == null) {
            throw new BusinessException("代理商信息不存在");
        }
        
        // 获取信用额度信息
        AgentCredit agentCredit = agentCreditMapper.getByAgentId(agentId);
        if (agentCredit == null) {
            throw new BusinessException("代理商信用额度信息不存在");
        }
        
        // 检查账户是否被冻结 - 允许冻结账户还款，但添加警告日志
        if (agentCredit.getIsFrozen() != null && agentCredit.getIsFrozen()) {
            log.warn("冻结账户【{}】正在进行还款操作，金额: {}", agentId, repaymentDTO.getAmount());
        }
        
        BigDecimal amount = repaymentDTO.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("还款金额必须大于0");
        }
        
        // 检查已用额度是否足够还款
        if (agentCredit.getUsedCredit().compareTo(amount) < 0) {
            // 如果还款金额大于已用额度，则只还已用的部分
            amount = agentCredit.getUsedCredit();
        }
        
        // 生成交易编号
        String transactionNo = "TX" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) 
                + String.format("%04d", (int)(Math.random() * 10000));
        
        // 记录还款交易
        CreditTransaction transaction = CreditTransaction.builder()
                .transactionNo(transactionNo)
                .agentId(agentId)
                .amount(amount)
                .transactionType(CreditConstants.TRANSACTION_TYPE_TOPUP)
                .bookingId(null) // 还款不关联订单
                .balanceBefore(agentCredit.getAvailableCredit())
                .balanceAfter(agentCredit.getAvailableCredit().add(amount))
                .note(repaymentDTO.getNote() != null ? repaymentDTO.getNote() : "还款")
                .createdBy(agentId)
                .createdAt(LocalDateTime.now())
                .build();
        
        creditTransactionMapper.insert(transaction);
        
        // 更新信用额度 - 减少已用额度，增加可用额度
        agentCredit.setUsedCredit(agentCredit.getUsedCredit().subtract(amount));
        agentCredit.setAvailableCredit(agentCredit.getAvailableCredit().add(amount));
        agentCredit.setLastUpdated(LocalDateTime.now());
        agentCreditMapper.update(agentCredit);
        
        // 返回还款结果
        return CreditRepaymentResultVO.builder()
                .transactionId(transaction.getId())
                .transactionNo(transaction.getTransactionNo())
                .amount(amount)
                .balanceAfter(transaction.getBalanceAfter())
                .repaymentStatus("completed")
                .build();
    }
    
    /**
     * 增加代理商信用余额（用于退款等场景）
     */
    @Override
    @Transactional
    public boolean addCredit(Long agentId, BigDecimal amount, String note) {
        log.info("增加代理商信用余额: agentId={}, amount={}, note={}", agentId, amount, note);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("增加金额必须大于0: {}", amount);
            throw new BusinessException("增加金额必须大于0");
        }
        
        // 获取代理商信用信息
        AgentCredit agentCredit = agentCreditMapper.getByAgentId(agentId);
        if (agentCredit == null) {
            log.error("代理商信用额度信息不存在, agentId: {}", agentId);
            throw new BusinessException("代理商信用额度信息不存在");
        }
        
        // 计算操作前后的余额
        BigDecimal balanceBefore = agentCredit.getAvailableCredit();
        
        // 增加余额到预存余额中
        agentCredit.setDepositBalance(agentCredit.getDepositBalance().add(amount));
        
        // 重新计算可用额度
        agentCredit.setAvailableCredit(agentCredit.getTotalCredit()
                .subtract(agentCredit.getUsedCredit())
                .add(agentCredit.getDepositBalance()));
        
        BigDecimal balanceAfter = agentCredit.getAvailableCredit();
        agentCredit.setLastUpdated(LocalDateTime.now());
        
        // 生成交易号
        String transactionNo = "CR" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
        
        // 记录交易
        CreditTransaction transaction = CreditTransaction.builder()
                .transactionNo(transactionNo)
                .agentId(agentId)
                .amount(amount)
                .transactionType(CreditConstants.TRANSACTION_TYPE_ADJUSTMENT)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .note(note != null ? note : "信用余额调整")
                .createdBy(agentId)
                .createdAt(LocalDateTime.now())
                .build();
                
        creditTransactionMapper.insert(transaction);
        
        // 更新信用额度
        agentCreditMapper.update(agentCredit);
        
        log.info("信用余额增加成功: agentId={}, amount={}, balanceBefore={}, balanceAfter={}", 
                agentId, amount, balanceBefore, balanceAfter);
        
        return true;
    }
} 