package com.sky.mapper;

import com.sky.entity.PriceModificationRequest;
import com.sky.vo.PriceModificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 价格修改请求Mapper接口
 */
@Mapper
public interface PriceModificationRequestMapper {

    /**
     * 插入价格修改请求
     * @param request 价格修改请求
     */
    void insert(PriceModificationRequest request);

    /**
     * 根据ID查询价格修改请求
     * @param id 主键ID
     * @return 价格修改请求
     */
    PriceModificationRequest getById(Long id);

    /**
     * 根据订单ID查询价格修改请求列表
     * @param bookingId 订单ID
     * @return 价格修改请求列表
     */
    List<PriceModificationRequest> getByBookingId(Integer bookingId);

    /**
     * 更新价格修改请求
     * @param request 价格修改请求
     */
    void update(PriceModificationRequest request);

    /**
     * 分页查询价格修改请求（管理后台用）
     * @param status 状态
     * @param modificationType 修改类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageSize 页面大小
     * @param offset 偏移量
     * @return 价格修改请求列表
     */
    List<PriceModificationVO> selectPage(@Param("status") String status,
                                        @Param("modificationType") String modificationType,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("pageSize") Integer pageSize,
                                        @Param("offset") Integer offset);

    /**
     * 统计价格修改请求数量（管理后台用）
     * @param status 状态
     * @param modificationType 修改类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 总数量
     */
    int countPage(@Param("status") String status,
                  @Param("modificationType") String modificationType,
                  @Param("startTime") LocalDateTime startTime,
                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查询代理商的价格修改请求（代理商端用）
     * @param agentId 代理商ID
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageSize 页面大小
     * @param offset 偏移量
     * @return 价格修改请求列表
     */
    List<PriceModificationVO> selectByAgent(@Param("agentId") Long agentId,
                                           @Param("status") String status,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           @Param("pageSize") Integer pageSize,
                                           @Param("offset") Integer offset);

    /**
     * 统计代理商的价格修改请求数量（代理商端用）
     * @param agentId 代理商ID
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 总数量
     */
        int countByAgent(@Param("agentId") Long agentId,
                    @Param("status") String status,
                    @Param("startTime") LocalDateTime startTime,
                    @Param("endTime") LocalDateTime endTime);

    /**
     * 代理商根据订单ID分页查询价格修改请求
     */
    List<PriceModificationVO> selectByBookingId(@Param("agentId") Long agentId,
                                               @Param("bookingId") Long bookingId,
                                               @Param("status") String status,
                                               @Param("pageSize") Integer pageSize,
                                               @Param("offset") Integer offset);

    /**
     * 代理商根据订单ID统计价格修改请求数量
     */
    int countByBookingId(@Param("agentId") Long agentId,
                        @Param("bookingId") Long bookingId,
                        @Param("status") String status);

    /**
     * 更新代理商响应
     * @param id 请求ID
     * @param agentResponse 代理商响应
     * @param agentNote 代理商备注
     * @param processedAt 处理时间
     */
    void updateAgentResponse(@Param("id") Long id,
                            @Param("agentResponse") String agentResponse,
                            @Param("agentNote") String agentNote,
                            @Param("processedAt") LocalDateTime processedAt);

    /**
     * 更新状态
     * @param id 请求ID
     * @param status 新状态
     * @param processedAt 处理时间
     */
    void updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("processedAt") LocalDateTime processedAt);
}
