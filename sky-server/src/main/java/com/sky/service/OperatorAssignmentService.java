package com.sky.service;

import com.sky.dto.AssignOrderDTO;
import com.sky.dto.OperatorAssignmentPageQueryDTO;
import com.sky.entity.OperatorAssignment;
import com.sky.result.PageResult;
import com.sky.vo.OperatorAssignmentVO;

import java.util.List;

/**
 * 操作员分配服务接口
 */
public interface OperatorAssignmentService {

    /**
     * 分配订单给操作员
     * @param assignOrderDTO 分配订单数据
     * @param assignedBy 分配人ID
     */
    void assignOrder(AssignOrderDTO assignOrderDTO, Long assignedBy);

    /**
     * 重新分配订单
     * @param bookingId 订单ID
     * @param newOperatorId 新的操作员ID
     * @param assignedBy 分配人ID
     * @param notes 备注
     */
    void reassignOrder(Integer bookingId, Long newOperatorId, Long assignedBy, String notes);

    /**
     * 取消分配
     * @param bookingId 订单ID
     * @param operatorId 操作员ID（权限验证）
     */
    void cancelAssignment(Integer bookingId, Long operatorId);

    /**
     * 完成分配任务
     * @param bookingId 订单ID
     * @param operatorId 操作员ID
     */
    void completeAssignment(Integer bookingId, Long operatorId);

    /**
     * 根据订单ID获取当前分配信息
     * @param bookingId 订单ID
     * @return 分配信息
     */
    OperatorAssignmentVO getAssignmentByBookingId(Integer bookingId);

    /**
     * 获取操作员的所有分配任务
     * @param operatorId 操作员ID
     * @return 分配任务列表
     */
    List<OperatorAssignmentVO> getAssignmentsByOperatorId(Long operatorId);

    /**
     * 分页查询分配记录
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(OperatorAssignmentPageQueryDTO queryDTO);

    /**
     * 获取所有有效的分配记录
     * @return 分配记录列表
     */
    List<OperatorAssignmentVO> getAllActiveAssignments();

    /**
     * 检查操作员是否有权限操作指定订单
     * @param operatorId 操作员ID
     * @param bookingId 订单ID
     * @return 是否有权限
     */
    boolean hasPermission(Long operatorId, Integer bookingId);

    /**
     * 检查操作员是否为排团主管
     * @param operatorId 操作员ID
     * @return 是否为排团主管
     */
    boolean isTourMaster(Long operatorId);

    /**
     * 获取操作员工作量统计
     * @param operatorId 操作员ID
     * @return 工作量统计信息
     */
    Object getWorkloadStatistics(Long operatorId);
}
