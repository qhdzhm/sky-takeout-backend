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
     * 根据订单ID查询折扣日志
     * @param orderId 订单ID
     * @return 折扣日志
     */
    AgentDiscountLog findByOrderId(@Param("orderId") Long orderId);
} 