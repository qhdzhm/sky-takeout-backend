package com.sky.service;

import com.sky.vo.UserCreditVO;
import java.math.BigDecimal;

/**
 * 用户积分服务接口
 */
public interface UserCreditService {
    
    /**
     * 获取用户积分信息
     * @param userId 用户ID
     * @return 用户积分信息
     */
    UserCreditVO getUserCreditInfo(Long userId);
    
    /**
     * 添加用户积分
     * @param userId 用户ID
     * @param amount 积分数量
     * @param type 积分类型 (referral_reward, order_reward, used)
     * @param referenceId 关联ID（订单ID等）
     * @param level 推荐级别 (1表示直接推荐，2表示间接推荐)
     * @param description 描述
     * @return 是否添加成功
     */
    boolean addCredit(Long userId, BigDecimal amount, String type, Integer referenceId, Integer level, String description);
    
    /**
     * 使用用户积分
     * @param userId 用户ID
     * @param amount 积分数量
     * @param referenceId 关联ID（订单ID等）
     * @param description 描述
     * @return 是否使用成功
     */
    boolean useCredit(Long userId, BigDecimal amount, Integer referenceId, String description);
    
    /**
     * 处理订单完成后的推荐积分奖励
     * @param orderId 订单ID
     * @param userId 下单用户ID
     * @param orderAmount 订单金额
     * @return 是否处理成功
     */
    boolean processReferralReward(Integer orderId, Long userId, BigDecimal orderAmount);
    
    /**
     * 初始化用户积分账户
     * @param userId 用户ID
     * @return 是否初始化成功
     */
    boolean initUserCredit(Long userId);
} 