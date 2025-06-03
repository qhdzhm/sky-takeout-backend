package com.sky.mapper;

import com.sky.entity.CreditTransaction;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 信用交易记录Mapper
 */
@Mapper
public interface CreditTransactionMapper {

    /**
     * 插入信用交易记录
     * @param transaction 交易记录
     */
    void insert(CreditTransaction transaction);

    /**
     * 根据ID获取交易记录
     * @param id 交易ID
     * @return 交易记录
     */
    CreditTransaction getById(@Param("id") Long id);

    /**
     * 根据订单ID获取交易记录
     * @param bookingId 订单ID
     * @param transactionType 交易类型（可选）
     * @return 交易记录
     */
    List<CreditTransaction> getByBookingId(@Param("bookingId") Long bookingId, 
                                          @Param("transactionType") String transactionType);

    /**
     * 根据代理商ID查询交易记录
     * @param agentId 代理商ID
     * @param transactionType 交易类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param offset 分页起始位置
     * @param limit 每页记录数
     * @return 交易记录列表
     */
    List<CreditTransaction> getByAgentId(@Param("agentId") Long agentId, 
                                        @Param("transactionType") String transactionType,
                                        @Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate,
                                        @Param("offset") int offset, 
                                        @Param("limit") int limit);

    /**
     * 统计代理商交易记录数
     * @param agentId 代理商ID
     * @param transactionType 交易类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 记录数
     */
    int countByAgentId(@Param("agentId") Long agentId, 
                       @Param("transactionType") String transactionType,
                       @Param("startDate") LocalDateTime startDate, 
                       @Param("endDate") LocalDateTime endDate);

    /**
     * 查询所有交易记录（管理员查询）
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param transactionNo 交易编号（可选，模糊匹配）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param offset 分页起始位置
     * @param pageSize 每页记录数
     * @return 交易记录列表
     */
    List<CreditTransaction> getAll(@Param("agentId") Long agentId, 
                                 @Param("transactionType") String transactionType,
                                 @Param("transactionNo") String transactionNo,
                                 @Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate,
                                 @Param("offset") int offset, 
                                 @Param("pageSize") int pageSize);

    /**
     * 统计所有交易记录数（管理员统计）
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param transactionNo 交易编号（可选，模糊匹配）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 记录数
     */
    int countAll(@Param("agentId") Long agentId, 
                @Param("transactionType") String transactionType,
                @Param("transactionNo") String transactionNo,
                @Param("startDate") LocalDateTime startDate, 
                @Param("endDate") LocalDateTime endDate);

    /**
     * 计算交易金额总和
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 交易金额总和
     */
    BigDecimal sumAmount(@Param("agentId") Long agentId, 
                        @Param("transactionType") String transactionType,
                        @Param("startDate") LocalDateTime startDate, 
                        @Param("endDate") LocalDateTime endDate);

    /**
     * 获取所有交易记录用于导出
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param transactionNo 交易编号（可选，模糊匹配）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 交易记录列表
     */
    List<CreditTransaction> getAllForExport(@Param("agentId") Long agentId, 
                                          @Param("transactionType") String transactionType,
                                          @Param("transactionNo") String transactionNo,
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * 创建信用额度交易记录
     * @param transaction 交易记录对象
     * @return 交易ID
     */
    @Insert("INSERT INTO credit_transaction(transaction_no, agent_id, booking_id, amount, transaction_type, balance_before, balance_after, note, created_at) " +
            "VALUES(CONCAT('TX', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 10000), 4, '0')), #{agentId}, #{bookingId}, #{amount}, #{transactionType}, #{balanceBefore}, #{balanceAfter}, #{note}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer createTransaction(CreditTransaction transaction);
} 