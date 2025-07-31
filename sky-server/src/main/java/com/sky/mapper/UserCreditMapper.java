package com.sky.mapper;

import com.sky.entity.UserCredit;
import com.sky.entity.UserCreditTransaction;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户积分Mapper接口
 */
@Mapper
public interface UserCreditMapper {

    /**
     * 根据用户ID获取积分信息
     * @param userId 用户ID
     * @return 积分信息
     */
    @Select("SELECT * FROM user_credit WHERE user_id = #{userId}")
    UserCredit getByUserId(@Param("userId") Long userId);
    
    /**
     * 初始化用户积分账户
     * @param userCredit 用户积分信息
     * @return 影响行数
     */
    @Insert("INSERT INTO user_credit (user_id, balance, total_earned, total_used, updated_at) " +
            "VALUES (#{userId}, #{balance}, #{totalEarned}, #{totalUsed}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserCredit userCredit);
    
    /**
     * 更新用户积分
     * @param userCredit 用户积分信息
     * @return 影响行数
     */
    @Update("UPDATE user_credit SET " +
            "balance = #{balance}, " +
            "total_earned = #{totalEarned}, " +
            "total_used = #{totalUsed}, " +
            "updated_at = NOW() " +
            "WHERE user_id = #{userId}")
    int update(UserCredit userCredit);
    
    /**
     * 添加积分交易记录
     * @param transaction 交易记录
     * @return 影响行数
     */
    @Insert("INSERT INTO user_credit_transaction (user_id, amount, type, reference_id, level, description, created_at) " +
            "VALUES (#{userId}, #{amount}, #{type}, #{referenceId}, #{level}, #{description}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTransaction(UserCreditTransaction transaction);
    
    /**
     * 根据用户ID和交易类型查询交易记录
     * @param userId 用户ID
     * @param type 交易类型
     * @param referenceId 关联ID
     * @return 交易记录列表
     */
    @Select("SELECT * FROM user_credit_transaction " +
            "WHERE user_id = #{userId} " +
            "AND type = #{type} " +
            "AND reference_id = #{referenceId} " +
            "ORDER BY created_at DESC")
    List<UserCreditTransaction> getTransactionsByTypeAndRef(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("referenceId") Integer referenceId);
    
    /**
     * 获取用户的所有交易记录
     * @param userId 用户ID
     * @return 交易记录列表
     */
    @Select("SELECT * FROM user_credit_transaction " +
            "WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC")
    List<UserCreditTransaction> getUserTransactions(@Param("userId") Long userId);
    
    /**
     * 检查订单是否已经处理过推荐奖励
     * @param orderId 订单ID
     * @return 交易记录数量
     */
    @Select("SELECT COUNT(*) FROM user_credit_transaction " +
            "WHERE type = 'referral_reward' " +
            "AND reference_id = #{orderId}")
    int countReferralRewardsByOrderId(@Param("orderId") Integer orderId);
} 