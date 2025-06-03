package com.sky.mapper;

import com.sky.entity.GuideAssignmentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 导游分配订单Mapper接口
 */
@Mapper
public interface GuideAssignmentOrderMapper {

    /**
     * 批量插入订单分配记录
     */
    void batchInsert(@Param("orders") List<GuideAssignmentOrder> orders);

    /**
     * 根据分配ID查询订单列表
     */
    List<GuideAssignmentOrder> getByAssignmentId(Long assignmentId);

    /**
     * 根据分配ID删除订单记录
     */
    void deleteByAssignmentId(Long assignmentId);

    /**
     * 更新订单状态
     */
    void updateOrderStatus(@Param("id") Long id, @Param("status") String status);
} 