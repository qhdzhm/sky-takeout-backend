package com.sky.mapper;

import com.sky.entity.CreditApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 信用额度申请Mapper
 */
@Mapper
public interface CreditApplicationMapper {

    /**
     * 插入信用额度申请
     *
     * @param application 申请信息
     */
    void insert(CreditApplication application);

    /**
     * 更新信用额度申请
     *
     * @param application 申请信息
     */
    void update(CreditApplication application);

    /**
     * 根据ID查询信用额度申请
     *
     * @param id 申请ID
     * @return 申请信息
     */
    CreditApplication getById(@Param("id") Long id);

    /**
     * 查询代理商的信用额度申请列表
     *
     * @param agentId 代理商ID
     * @param status 状态（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 申请列表
     */
    List<CreditApplication> getByAgentId(@Param("agentId") Long agentId, 
                                         @Param("status") String status,
                                         @Param("offset") int offset, 
                                         @Param("limit") int limit);

    /**
     * 查询代理商的信用额度申请总数
     *
     * @param agentId 代理商ID
     * @param status 状态（可选）
     * @return 申请总数
     */
    int countByAgentId(@Param("agentId") Long agentId, @Param("status") String status);

    /**
     * 查询所有信用额度申请列表
     *
     * @param status 状态（可选）
     * @param agentId 代理商ID（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 申请列表
     */
    List<CreditApplication> getAll(@Param("status") String status,
                                   @Param("agentId") Long agentId,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    /**
     * 查询所有信用额度申请总数
     *
     * @param status 状态（可选）
     * @param agentId 代理商ID（可选）
     * @return 申请总数
     */
    int countAll(@Param("status") String status, @Param("agentId") Long agentId);
}