package com.sky.mapper;

import com.sky.entity.AgentDiscountLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 中介折扣等级Mapper接口
 */
@Mapper
public interface AgentDiscountLevelMapper {

    /**
     * 查询所有折扣等级
     * @return 折扣等级列表
     */
    List<AgentDiscountLevel> findAll();

    /**
     * 根据ID查询折扣等级
     * @param id 等级ID
     * @return 折扣等级
     */
    AgentDiscountLevel findById(@Param("id") Long id);

    /**
     * 根据等级代码查询折扣等级
     * @param levelCode 等级代码
     * @return 折扣等级
     */
    AgentDiscountLevel findByLevelCode(@Param("levelCode") String levelCode);

    /**
     * 插入折扣等级
     * @param agentDiscountLevel 折扣等级
     * @return 影响行数
     */
    int insert(AgentDiscountLevel agentDiscountLevel);

    /**
     * 更新折扣等级
     * @param agentDiscountLevel 折扣等级
     * @return 影响行数
     */
    int update(AgentDiscountLevel agentDiscountLevel);

    /**
     * 删除折扣等级
     * @param id 等级ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询活跃的折扣等级
     * @return 活跃的折扣等级列表
     */
    List<AgentDiscountLevel> findActiveLevel();
} 