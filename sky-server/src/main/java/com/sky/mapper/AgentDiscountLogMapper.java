package com.sky.mapper;

import com.sky.entity.AgentDiscountLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 中介折扣日志Mapper接口
 */
@Mapper
public interface AgentDiscountLogMapper {

    /**
     * 插入折扣计算日志
     * @param log 折扣日志
     * @return 影响行数
     */
    int insert(AgentDiscountLog log);

    /**
     * 根据中介ID查询折扣日志
     * @param agentId 中介ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 折扣日志列表
     */
    List<AgentDiscountLog> findByAgentId(@Param("agentId") Long agentId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 根据订单ID查询折扣日志
     * @param orderId 订单ID
     * @return 折扣日志
     */
    AgentDiscountLog findByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据产品查询折扣使用统计
     * @param productType 产品类型
     * @param productId 产品ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 折扣日志列表
     */
    List<AgentDiscountLog> findByProduct(@Param("productType") String productType,
                                        @Param("productId") Long productId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 查询折扣使用统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 折扣日志列表
     */
    List<AgentDiscountLog> findDiscountStats(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
} 