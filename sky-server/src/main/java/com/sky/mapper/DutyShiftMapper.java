package com.sky.mapper;

import com.sky.entity.DutyShift;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 值班记录Mapper接口
 */
@Mapper
public interface DutyShiftMapper {

    /**
     * 创建新的值班记录
     */
    @Insert("INSERT INTO duty_shifts (operator_id, duty_type, shift_start, status) " +
            "VALUES (#{operatorId}, #{dutyType}, #{shiftStart}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(DutyShift dutyShift);

    /**
     * 根据ID查询值班记录
     */
    @Select("SELECT ds.*, " +
            "       e1.name as operatorName, " +
            "       e2.name as transferredToName, " +
            "       TIMESTAMPDIFF(MINUTE, ds.shift_start, COALESCE(ds.shift_end, NOW())) as durationMinutes " +
            "FROM duty_shifts ds " +
            "LEFT JOIN employees e1 ON ds.operator_id = e1.id " +
            "LEFT JOIN employees e2 ON ds.transferred_to = e2.id " +
            "WHERE ds.id = #{id}")
    DutyShift getById(Long id);

    /**
     * 查询当前有效的值班记录（按值班类型）
     */
    @Select("SELECT ds.*, " +
            "       e1.name as operatorName, " +
            "       e2.name as transferredToName, " +
            "       TIMESTAMPDIFF(MINUTE, ds.shift_start, COALESCE(ds.shift_end, NOW())) as durationMinutes " +
            "FROM duty_shifts ds " +
            "LEFT JOIN employees e1 ON ds.operator_id = e1.id " +
            "LEFT JOIN employees e2 ON ds.transferred_to = e2.id " +
            "WHERE ds.duty_type = #{dutyType} AND ds.status = 'active'")
    DutyShift getCurrentDutyByType(String dutyType);

    /**
     * 查询当前排团主管
     */
    @Select("SELECT ds.*, " +
            "       e1.name as operatorName, " +
            "       e2.name as transferredToName, " +
            "       TIMESTAMPDIFF(MINUTE, ds.shift_start, COALESCE(ds.shift_end, NOW())) as durationMinutes " +
            "FROM duty_shifts ds " +
            "LEFT JOIN employees e1 ON ds.operator_id = e1.id " +
            "LEFT JOIN employees e2 ON ds.transferred_to = e2.id " +
            "WHERE ds.duty_type = 'tour_master' AND ds.status = 'active'")
    DutyShift getCurrentTourMaster();

    /**
     * 根据操作员ID查询值班历史
     */
    @Select("SELECT ds.*, " +
            "       e1.name as operatorName, " +
            "       e2.name as transferredToName, " +
            "       TIMESTAMPDIFF(MINUTE, ds.shift_start, COALESCE(ds.shift_end, NOW())) as durationMinutes " +
            "FROM duty_shifts ds " +
            "LEFT JOIN employees e1 ON ds.operator_id = e1.id " +
            "LEFT JOIN employees e2 ON ds.transferred_to = e2.id " +
            "WHERE ds.operator_id = #{operatorId} " +
            "ORDER BY ds.shift_start DESC")
    List<DutyShift> getByOperatorId(Long operatorId);

    /**
     * 查询所有值班记录
     */
    @Select("SELECT ds.*, " +
            "       e1.name as operatorName, " +
            "       e2.name as transferredToName, " +
            "       TIMESTAMPDIFF(MINUTE, ds.shift_start, COALESCE(ds.shift_end, NOW())) as durationMinutes " +
            "FROM duty_shifts ds " +
            "LEFT JOIN employees e1 ON ds.operator_id = e1.id " +
            "LEFT JOIN employees e2 ON ds.transferred_to = e2.id " +
            "ORDER BY ds.shift_start DESC")
    List<DutyShift> getAll();

    /**
     * 结束值班（完成）
     */
    @Update("UPDATE duty_shifts SET " +
            "status = 'completed', " +
            "shift_end = NOW(), " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    void complete(Long id);

    /**
     * 转移值班
     */
    @Update("UPDATE duty_shifts SET " +
            "status = 'transferred', " +
            "shift_end = NOW(), " +
            "transferred_to = #{transferredTo}, " +
            "transferred_at = NOW(), " +
            "transfer_reason = #{transferReason}, " +
            "notes = #{notes}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    void transfer(Long id, Long transferredTo, String transferReason, String notes);

    /**
     * 更新值班记录
     */
    void update(DutyShift dutyShift);

    /**
     * 检查是否有冲突的值班记录
     */
    @Select("SELECT COUNT(*) FROM duty_shifts " +
            "WHERE operator_id = #{operatorId} " +
            "AND duty_type = #{dutyType} " +
            "AND status = 'active' " +
            "AND ((shift_start <= #{endTime} AND COALESCE(shift_end, NOW()) >= #{startTime}))")
    Integer checkConflict(Long operatorId, String dutyType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询值班统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalShifts, " +
            "SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) as activeShifts, " +
            "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completedShifts, " +
            "SUM(CASE WHEN status = 'transferred' THEN 1 ELSE 0 END) as transferredShifts, " +
            "AVG(TIMESTAMPDIFF(MINUTE, shift_start, COALESCE(shift_end, NOW()))) as avgDurationMinutes " +
            "FROM duty_shifts " +
            "WHERE operator_id = #{operatorId}")
    @Results({
        @Result(property = "totalShifts", column = "totalShifts"),
        @Result(property = "activeShifts", column = "activeShifts"),
        @Result(property = "completedShifts", column = "completedShifts"),
        @Result(property = "transferredShifts", column = "transferredShifts"),
        @Result(property = "avgDurationMinutes", column = "avgDurationMinutes")
    })
    Object getShiftStatistics(Long operatorId);
}
