package com.sky.mapper;

import com.sky.entity.AgentCredit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 代理商信用额度Mapper
 */
@Mapper
public interface AgentCreditMapper {

    /**
     * 根据代理商ID获取信用额度记录
     * @param agentId 代理商ID
     * @return 信用额度记录
     */
    @Select("select * from agent_credit where agent_id = #{agentId}")
    AgentCredit getByAgentId(@Param("agentId") Long agentId);

    /**
     * 插入信用额度记录
     * @param agentCredit 信用额度记录
     */
    void insert(AgentCredit agentCredit);

    /**
     * 更新信用额度记录
     * @param agentCredit 信用额度记录
     */
    void update(AgentCredit agentCredit);
    
    /**
     * 带乐观锁的更新信用额度记录（用于支付等关键操作）
     * @param agentCredit 信用额度记录（必须包含version字段）
     * @return 影响的行数（如果为0表示版本号冲突，更新失败）
     */
    int updateWithVersion(AgentCredit agentCredit);

    /**
     * 获取所有代理商信用额度记录
     * @param agentId 代理商ID（可选）
     * @param agentName 代理商名称（可选）
     * @param offset 偏移量
     * @param limit 限制条数
     * @return 代理商信用额度记录列表
     */
    List<AgentCredit> getAll(@Param("agentId") Long agentId, 
                           @Param("agentName") String agentName,
                           @Param("offset") int offset, 
                           @Param("limit") int limit);

    /**
     * 统计所有代理商信用额度记录数量
     * @param agentId 代理商ID（可选）
     * @param agentName 代理商名称（可选）
     * @return 记录数量
     */
    int countAll(@Param("agentId") Long agentId, @Param("agentName") String agentName);

    /**
     * 更新已使用信用额度
     * @param agentId 代理商ID
     * @param amount 变更金额（正数增加，负数减少）
     * @return 影响的行数
     */
    @Update("UPDATE agent_credit SET " +
            "used_credit = used_credit + #{amount}, " +
            "available_credit = available_credit - #{amount}, " +
            "last_updated = NOW() " +
            "WHERE agent_id = #{agentId}")
    int updateUsedCredit(@Param("agentId") Long agentId, @Param("amount") BigDecimal amount);

    /**
     * 更新代理商总信用额度
     *
     * @param agentId 代理商ID
     * @param amount 变动金额，正数为增加总额度，负数为减少总额度
     * @return 影响的行数
     */
    int updateTotalCredit(@Param("agentId") Long agentId, @Param("amount") java.math.BigDecimal amount);

    /**
     * 统计代理商信用额度数量
     * @return 总数
     */
    @Select("select count(id) from agent_credit")
    Integer count();

    /**
     * 根据代理商ID查询信用额度
     * @param agentId 代理商ID
     * @return 信用额度
     */
    @Select("SELECT available_credit FROM agent_credit WHERE agent_id = #{agentId}")
    BigDecimal getCreditBalanceByAgentId(int agentId);

    /**
     * 更新信用额度
     * @param agentId 代理商ID
     * @param newBalance 新余额
     * @return 影响行数
     */
    @Update("UPDATE agent_credit SET available_credit = #{newBalance} WHERE agent_id = #{agentId}")
    int updateCreditBalance(@Param("agentId") int agentId, @Param("newBalance") BigDecimal newBalance);

    /**
     * 创建代理商信用额度记录
     * @param agentId 代理商ID
     * @param initialCredit 初始信用额度
     * @return 影响的行数
     */
    int createCreditRecord(Integer agentId, BigDecimal initialCredit);
} 