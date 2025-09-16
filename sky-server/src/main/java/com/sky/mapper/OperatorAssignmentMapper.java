package com.sky.mapper;

import com.sky.entity.OperatorAssignment;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

import java.util.List;

/**
 * 操作员分配Mapper接口
 */
@Mapper
public interface OperatorAssignmentMapper {

    /**
     * 创建新的分配记录
     */
    @Insert("INSERT INTO operator_assignments (booking_id, operator_id, assigned_by, assignment_type, notes) " +
            "VALUES (#{bookingId}, #{operatorId}, #{assignedBy}, #{assignmentType}, #{notes})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(OperatorAssignment operatorAssignment);

    /**
     * 根据ID查询分配记录
     */
    @Select("SELECT oa.*, " +
            "       e1.name as operatorName, " +
            "       e2.name as assignedByName, " +
            "       tb.order_number, " +
            "       tb.contact_person " +
            "FROM operator_assignments oa " +
            "LEFT JOIN employees e1 ON oa.operator_id = e1.id " +
            "LEFT JOIN employees e2 ON oa.assigned_by = e2.id " +
            "LEFT JOIN tour_bookings tb ON oa.booking_id = tb.booking_id " +
            "WHERE oa.id = #{id}")
    OperatorAssignment getById(Long id);

    /**
     * 根据订单ID查询当前有效的分配记录
     */
    @Select("SELECT oa.*, " +
            "       e1.name as operatorName, " +
            "       e2.name as assignedByName, " +
            "       tb.order_number, " +
            "       tb.contact_person " +
            "FROM operator_assignments oa " +
            "LEFT JOIN employees e1 ON oa.operator_id = e1.id " +
            "LEFT JOIN employees e2 ON oa.assigned_by = e2.id " +
            "LEFT JOIN tour_bookings tb ON oa.booking_id = tb.booking_id " +
            "WHERE oa.booking_id = #{bookingId} AND oa.status = 'active'")
    OperatorAssignment getActiveByBookingId(Integer bookingId);

    /**
     * 根据操作员ID查询分配给该操作员的所有有效订单
     */
    @Select("SELECT oa.*, " +
            "       e1.name as operatorName, " +
            "       e2.name as assignedByName, " +
            "       tb.order_number, " +
            "       tb.contact_person " +
            "FROM operator_assignments oa " +
            "LEFT JOIN employees e1 ON oa.operator_id = e1.id " +
            "LEFT JOIN employees e2 ON oa.assigned_by = e2.id " +
            "LEFT JOIN tour_bookings tb ON oa.booking_id = tb.booking_id " +
            "WHERE oa.operator_id = #{operatorId} AND oa.status = 'active' " +
            "ORDER BY oa.assigned_at DESC")
    List<OperatorAssignment> getActiveByOperatorId(Long operatorId);

    /**
     * 查询所有有效的分配记录
     */
    @Select("SELECT oa.*, " +
            "       e1.name as operatorName, " +
            "       e2.name as assignedByName, " +
            "       tb.order_number, " +
            "       tb.contact_person " +
            "FROM operator_assignments oa " +
            "LEFT JOIN employees e1 ON oa.operator_id = e1.id " +
            "LEFT JOIN employees e2 ON oa.assigned_by = e2.id " +
            "LEFT JOIN tour_bookings tb ON oa.booking_id = tb.booking_id " +
            "WHERE oa.status = 'active' " +
            "ORDER BY oa.assigned_at DESC")
    List<OperatorAssignment> getAllActive();

    /**
     * 更新分配状态
     */
    @Update("UPDATE operator_assignments SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    void updateStatus(Long id, String status);

    /**
     * 转移分配
     */
    @Update("UPDATE operator_assignments SET " +
            "status = 'transferred', " +
            "transferred_to = #{transferredTo}, " +
            "transferred_at = NOW(), " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    void transfer(Long id, Long transferredTo);

    /**
     * 查询操作员的工作量统计
     */
    @Select("SELECT operator_id, " +
            "       COUNT(*) as totalAssignments, " +
            "       SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) as activeAssignments, " +
            "       SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completedAssignments " +
            "FROM operator_assignments " +
            "WHERE operator_id = #{operatorId} " +
            "GROUP BY operator_id")
    @Results({
        @Result(property = "operatorId", column = "operator_id"),
        @Result(property = "totalAssignments", column = "totalAssignments"),
        @Result(property = "activeAssignments", column = "activeAssignments"),
        @Result(property = "completedAssignments", column = "completedAssignments")
    })
    Object getWorkloadStatistics(Long operatorId);

    /**
     * 根据分配状态查询记录数量
     */
    @Select("SELECT COUNT(*) FROM operator_assignments WHERE status = #{status}")
    Integer countByStatus(String status);

    /**
     * 统计操作员分配的订单数
     */
    @Select("SELECT COUNT(*) FROM operator_assignments WHERE operator_id = #{operatorId} AND status != 'cancelled'")
    Integer countAssignedOrders(Long operatorId);

    /**
     * 统计操作员完成的订单数
     */
    @Select("SELECT COUNT(*) FROM operator_assignments WHERE operator_id = #{operatorId} AND status = 'completed'")
    Integer countCompletedOrders(Long operatorId);

    /**
     * 统计操作员待处理的订单数
     */
    @Select("SELECT COUNT(*) FROM operator_assignments WHERE operator_id = #{operatorId} AND status = 'active'")
    Integer countPendingOrders(Long operatorId);

    /**
     * 获取操作员最后分配时间
     */
    @Select("SELECT MAX(assigned_at) FROM operator_assignments WHERE operator_id = #{operatorId}")
    LocalDateTime getLastAssignedTime(Long operatorId);

    /**
     * 获取操作员最后完成时间
     */
    @Select("SELECT MAX(completed_at) FROM operator_assignments WHERE operator_id = #{operatorId} AND status = 'completed'")
    LocalDateTime getLastCompletedTime(Long operatorId);
}
