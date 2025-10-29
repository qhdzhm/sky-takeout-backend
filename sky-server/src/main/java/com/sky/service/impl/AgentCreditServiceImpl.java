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
        // 最大重试次数（乐观锁冲突时重试）
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount <= maxRetries) {
            try {
                // 执行支付逻辑
                return executePaymentWithOptimisticLock(agentId, paymentDTO, retryCount);
            } catch (BusinessException e) {
                // 如果是乐观锁冲突，重试
                if (e.getMessage().contains("并发冲突") && retryCount < maxRetries) {
                    retryCount++;
                    log.warn("⚠️ 信用支付并发冲突，第{}次重试 - 代理商ID: {}, 订单ID: {}", 
                            retryCount, agentId, paymentDTO.getBookingId());
                    // 短暂等待后重试（避免立即重试导致的冲突）
                    try {
                        Thread.sleep(50 * retryCount); // 递增等待时间：50ms, 100ms, 150ms
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException("支付处理被中断");
                    }
                } else {
                    // 其他业务异常或重试次数用完，直接抛出
                    throw e;
                }
            }
        }
        
        // 如果所有重试都失败
        throw new BusinessException("支付失败，系统繁忙请稍后重试");
    }
    
    /**
     * 执行带乐观锁的支付逻辑（内部方法）
     */
    private CreditPaymentResultVO executePaymentWithOptimisticLock(Long agentId, CreditPaymentDTO paymentDTO, int retryCount) {
        // 获取代理商信息
        Agent agent = agentMapper.getById(agentId);
        if (agent == null) {
            throw new BusinessException("代理商信息不存在");
        }
        
        // 读取最新的信用额度信息（每次重试都重新读取）
        AgentCredit agentCredit = agentCreditMapper.getByAgentId(agentId);
        if (agentCredit == null) {
            throw new BusinessException("代理商信用额度信息不存在");
        }
        
        // 检查账户是否被冻结
        if (agentCredit.getIsFrozen() != null && agentCredit.getIsFrozen()) {
            throw new BusinessException(CreditConstants.ERROR_ACCOUNT_FROZEN);
        }
        
        // 保存原始版本号，用于乐观锁检查
        Integer originalVersion = agentCredit.getVersion();
        if (originalVersion == null) {
            originalVersion = 0;
            agentCredit.setVersion(0);
        }
        
        BigDecimal amount = paymentDTO.getAmount();
        // 计算总可用额度：信用额度 + 预存余额
        BigDecimal totalAvailable = agentCredit.getTotalCredit()
            .subtract(agentCredit.getUsedCredit())
            .add(agentCredit.getDepositBalance());
            
        if (totalAvailable.compareTo(amount) < 0) {
            throw new BusinessException("可用额度不足，当前可用: " + totalAvailable + "元，需要: " + amount + "元");
        }
        
        // 生成唯一交易编号（使用UUID + 时间戳，保证全局唯一）
        String transactionNo = generateUniqueTransactionNo(agentId);
        
        log.info("🔐 开始信用支付 - 代理商ID: {}, 订单ID: {}, 金额: {}, 交易号: {}, 版本号: {}, 重试次数: {}", 
                agentId, paymentDTO.getBookingId(), amount, transactionNo, originalVersion, retryCount);
        
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
        
        // 先记录交易（即使后续更新失败也有记录）
        Long currentOperatorId = BaseContext.getCurrentId();
        if (currentOperatorId == null) {
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
        
        // 使用乐观锁更新信用额度（关键步骤）
        int updatedRows = agentCreditMapper.updateWithVersion(agentCredit);
        
        if (updatedRows == 0) {
            // 版本号不匹配，说明有并发冲突
            log.warn("❌ 乐观锁冲突 - 代理商ID: {}, 当前版本号: {}, 期望更新失败", agentId, originalVersion);
            throw new BusinessException("并发冲突，正在重试");
        }
        
        log.info("✅ 信用支付成功 - 代理商ID: {}, 交易号: {}, 金额: {}, 余额: {} -> {}", 
                agentId, transactionNo, amount, balanceBefore, balanceAfter);
        
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
     * 生成唯一交易编号
     * 格式: 账号名(前5位) + UUID(前16位)
     * 示例: LJY00-1234567890ABCDEF
     */
    private String generateUniqueTransactionNo(Long agentId) {
        // 获取代理商信息
        Agent agent = agentMapper.getById(agentId);
        String agentName = agent != null && agent.getUsername() != null 
            ? agent.getUsername() : String.valueOf(agentId);
        
        // 账号名标准化：只取前5位，不足补0
        String agentPrefix = (agentName.length() >= 5) 
            ? agentName.substring(0, 5).toUpperCase() 
            : String.format("%-5s", agentName.toUpperCase()).replace(' ', '0');
        
        // 生成UUID并取前16位（保证唯一性）
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String uuidPart = uuid.substring(0, 16);
        
        // 格式: LJY00-1234567890ABCDEF (总长度22位)
        return agentPrefix + "-" + uuidPart;
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