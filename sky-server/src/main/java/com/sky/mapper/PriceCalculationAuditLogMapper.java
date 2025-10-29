package com.sky.mapper;

import com.sky.entity.PriceCalculationAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 价格计算审计日志Mapper
 */
@Mapper
public interface PriceCalculationAuditLogMapper {

    /**
     * 插入审计日志
     * @param log 审计日志
     */
    void insert(PriceCalculationAuditLog log);

    /**
     * 根据ID查询审计日志
     * @param id 日志ID
     * @return 审计日志
     */
    PriceCalculationAuditLog selectById(@Param("id") Long id);

    /**
     * 查询用户的审计日志
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计日志列表
     */
    List<PriceCalculationAuditLog> selectByUserId(
            @Param("userId") Integer userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询可疑的审计日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计日志列表
     */
    List<PriceCalculationAuditLog> selectSuspicious(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计用户在指定时间内的请求次数
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 请求次数
     */
    Integer countByUserIdAndTime(
            @Param("userId") Integer userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}







