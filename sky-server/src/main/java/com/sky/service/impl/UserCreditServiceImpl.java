package com.sky.service.impl;

import com.sky.entity.User;
import com.sky.entity.UserCredit;
import com.sky.entity.UserCreditTransaction;
import com.sky.exception.BusinessException;
import com.sky.mapper.UserCreditMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.UserCreditService;
import com.sky.vo.UserCreditVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户积分服务实现类
 */
@Service
@Slf4j
public class UserCreditServiceImpl implements UserCreditService {
    
    @Autowired
    private UserCreditMapper userCreditMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Value("${credit.referral.direct-rate:0.05}")
    private BigDecimal directReferralRate; // 直接推荐奖励比例，默认5%
    
    @Value("${credit.referral.indirect-rate:0.02}")
    private BigDecimal indirectReferralRate; // 间接推荐奖励比例，默认2%
    
    @Override
    public UserCreditVO getUserCreditInfo(Long userId) {
        log.info("获取用户积分信息: userId={}", userId);
        
        // 获取用户积分记录
        UserCredit userCredit = userCreditMapper.getByUserId(userId);
        
        // 如果用户积分记录不存在，初始化一个
        if (userCredit == null) {
            userCredit = initUserCreditInternal(userId);
        }
        
        // 查询用户信息，获取邀请码和推荐人
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 查询推荐人信息
        String referrerUsername = null;
        if (user.getReferredBy() != null) {
            User referrer = userMapper.getById(user.getReferredBy());
            if (referrer != null) {
                referrerUsername = referrer.getUsername();
            }
        }
        
        // 转换为VO
        UserCreditVO vo = new UserCreditVO();
        BeanUtils.copyProperties(userCredit, vo);
        vo.setInviteCode(user.getInviteCode());
        vo.setReferredBy(user.getReferredBy());
        vo.setReferrerUsername(referrerUsername);
        
        // TODO: 查询推荐的用户数量
        vo.setReferralsCount(0);
        
        return vo;
    }
    
    @Override
    @Transactional
    public boolean addCredit(Long userId, BigDecimal amount, String type, Integer referenceId, Integer level, String description) {
        log.info("添加用户积分: userId={}, amount={}, type={}, referenceId={}, level={}, description={}",
                userId, amount, type, referenceId, level, description);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("积分数量必须大于0");
            return false;
        }
        
        // 获取用户积分记录
        UserCredit userCredit = userCreditMapper.getByUserId(userId);
        
        // 如果用户积分记录不存在，初始化一个
        if (userCredit == null) {
            userCredit = initUserCreditInternal(userId);
        }
        
        // 更新积分余额
        userCredit.setBalance(userCredit.getBalance().add(amount));
        userCredit.setTotalEarned(userCredit.getTotalEarned().add(amount));
        userCredit.setUpdatedAt(LocalDateTime.now());
        
        // 更新用户积分记录
        userCreditMapper.update(userCredit);
        
        // 创建交易记录
        UserCreditTransaction transaction = UserCreditTransaction.builder()
                .userId(userId)
                .amount(amount)
                .type(type)
                .referenceId(referenceId)
                .level(level)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        
        userCreditMapper.insertTransaction(transaction);
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean useCredit(Long userId, BigDecimal amount, Integer referenceId, String description) {
        log.info("使用用户积分: userId={}, amount={}, referenceId={}, description={}",
                userId, amount, referenceId, description);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("积分数量必须大于0");
            return false;
        }
        
        // 获取用户积分记录
        UserCredit userCredit = userCreditMapper.getByUserId(userId);
        
        // 如果用户积分记录不存在或积分不足
        if (userCredit == null || userCredit.getBalance().compareTo(amount) < 0) {
            log.error("用户积分不足: userId={}, balance={}, amount={}",
                    userId, userCredit != null ? userCredit.getBalance() : "null", amount);
            return false;
        }
        
        // 更新积分余额
        userCredit.setBalance(userCredit.getBalance().subtract(amount));
        userCredit.setTotalUsed(userCredit.getTotalUsed().add(amount));
        userCredit.setUpdatedAt(LocalDateTime.now());
        
        // 更新用户积分记录
        userCreditMapper.update(userCredit);
        
        // 创建交易记录
        UserCreditTransaction transaction = UserCreditTransaction.builder()
                .userId(userId)
                .amount(amount.negate()) // 使用积分记录为负数
                .type("used")
                .referenceId(referenceId)
                .level(null)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        
        userCreditMapper.insertTransaction(transaction);
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean processReferralReward(Integer orderId, Long userId, BigDecimal orderAmount) {
        log.info("处理推荐奖励: orderId={}, userId={}, orderAmount={}", orderId, userId, orderAmount);
        
        // 检查订单是否已处理过推荐奖励
        int count = userCreditMapper.countReferralRewardsByOrderId(orderId);
        if (count > 0) {
            log.info("订单已处理过推荐奖励: orderId={}", orderId);
            return true;
        }
        
        // 获取用户信息
        User user = userMapper.getById(userId);
        if (user == null) {
            log.error("用户不存在: userId={}", userId);
            return false;
        }
        
        // 查看是否有推荐人
        Long referrerId = user.getReferredBy();
        if (referrerId == null) {
            log.info("用户没有推荐人，无需处理推荐奖励: userId={}", userId);
            return true;
        }
        
        // 获取直接推荐人
        User directReferrer = userMapper.getById(referrerId);
        if (directReferrer == null) {
            log.error("推荐人不存在: referrerId={}", referrerId);
            return false;
        }
        
        // 计算直接推荐人奖励
        BigDecimal directReward = orderAmount.multiply(directReferralRate)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        
        // 给直接推荐人添加积分
        String description = String.format("推荐用户 %s 完成订单 #%d 的奖励", user.getUsername(), orderId);
        addCredit(referrerId, directReward, "referral_reward", orderId, 1, description);
        
        // 查看直接推荐人是否有上级推荐人
        Long indirectReferrerId = directReferrer.getReferredBy();
        if (indirectReferrerId != null) {
            // 获取间接推荐人
            User indirectReferrer = userMapper.getById(indirectReferrerId);
            if (indirectReferrer != null) {
                // 计算间接推荐人奖励
                BigDecimal indirectReward = orderAmount.multiply(indirectReferralRate)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                
                // 给间接推荐人添加积分
                String indirectDescription = String.format("二级推荐用户 %s 完成订单 #%d 的奖励", user.getUsername(), orderId);
                addCredit(indirectReferrerId, indirectReward, "referral_reward", orderId, 2, indirectDescription);
            }
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean initUserCredit(Long userId) {
        log.info("初始化用户积分账户: userId={}", userId);
        return initUserCreditInternal(userId) != null;
    }
    
    /**
     * 内部方法：初始化用户积分账户
     * @param userId 用户ID
     * @return 用户积分记录
     */
    private UserCredit initUserCreditInternal(Long userId) {
        // 检查用户是否存在
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 创建积分账户
        UserCredit userCredit = UserCredit.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .totalEarned(BigDecimal.ZERO)
                .totalUsed(BigDecimal.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();
        
        userCreditMapper.insert(userCredit);
        
        return userCredit;
    }
} 