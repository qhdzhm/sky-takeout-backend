package com.sky.mapper;

import com.sky.entity.PaymentAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付审计日志Mapper接口
 */
@Mapper
public interface PaymentAuditLogMapper {

    /**
     * 插入支付审计日志
     * @param paymentAuditLog 支付审计日志
     */
    void insert(PaymentAuditLog paymentAuditLog);

    /**
     * 分页查询支付审计日志（管理后台用）
     * @param agentId 代理商ID
     * @param action 操作类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageSize 页面大小
     * @param offset 偏移量
     * @return 支付审计日志列表
     */
    List<PaymentAuditLog> selectPage(@Param("agentId") Long agentId,
                                   @Param("action") String action,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime,
                                   @Param("pageSize") Integer pageSize,
                                   @Param("offset") Integer offset);

    /**
     * 统计支付审计日志数量（管理后台用）
     * @param agentId 代理商ID
     * @param action 操作类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 总数量
     */
    int countPage(@Param("agentId") Long agentId,
                  @Param("action") String action,
                  @Param("startTime") LocalDateTime startTime,
                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查询代理商支付审计日志（用户端/代理端用）
     * @param agentId 代理商ID
     * @param action 操作类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageSize 页面大小
     * @param offset 偏移量
     * @return 支付审计日志列表
     */
    List<PaymentAuditLog> selectByAgent(@Param("agentId") Long agentId,
                                      @Param("action") String action,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("pageSize") Integer pageSize,
                                      @Param("offset") Integer offset);

    /**
     * 统计代理商支付审计日志数量（用户端/代理端用）
     * @param agentId 代理商ID
     * @param action 操作类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 总数量
     */
    int countByAgent(@Param("agentId") Long agentId,
                     @Param("action") String action,
                     @Param("startTime") LocalDateTime startTime,
                     @Param("endTime") LocalDateTime endTime);

    /**
     * 统计代理商在时间区间内的信用支付总额
     * @param agentId 代理商ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支付总额
     */
    BigDecimal sumAmountByAgent(@Param("agentId") Long agentId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
}