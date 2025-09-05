package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.AgentPageQueryDTO;
import com.sky.entity.Agent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 代理商Mapper接口
 */
@Mapper
public interface AgentMapper {

    /**
     * 根据用户名查询代理商
     * @param username 用户名
     * @return 代理商对象
     */
    Agent getByUsername(String username);
    
    /**
     * 根据代理商ID查询代理商
     * @param id 代理商ID
     * @return 代理商对象
     */
    Agent getById(Long id);
    
    /**
     * 添加代理商
     * @param agent 代理商对象
     */
    void insert(Agent agent);
    
    /**
     * 更新代理商信息
     * @param agent 代理商对象
     * @return 更新的记录数
     */
    int update(Agent agent);
    
    /**
     * 查询所有代理商
     * @return 代理商列表
     */
    List<Agent> list();
    
    /**
     * 分页查询代理商
     * @param agentPageQueryDTO 查询条件
     * @return 代理商分页结果
     */
    Page<Agent> pageQuery(AgentPageQueryDTO agentPageQueryDTO);
    
    /**
     * 根据ID删除代理商
     * @param id 代理商ID
     */
    void deleteById(Long id);
    
    /**
     * 根据ID修改代理商状态
     * @param id 代理商ID
     * @param status 状态(0-禁用，1-启用)
     */
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 根据ID重置代理商密码
     * @param id 代理商ID
     * @param password 新密码(已加密)
     */
    void resetPassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 根据名称关键字查询代理商
     * @param keyword 名称关键字
     * @return 代理商列表
     */
    List<Agent> getAgentsByNameKeyword(@Param("keyword") String keyword);
    
    /**
     * 统计代理商的订单数量
     * @param agentId 代理商ID
     * @return 订单数量
     */
    int countOrdersByAgentId(@Param("agentId") Long agentId);
    
    /**
     * 获取代理商的销售数据
     * @param agentId 代理商ID
     * @return 销售数据，包含totalSales和savedAmount
     */
    Map<String, BigDecimal> getSalesDataByAgentId(@Param("agentId") Long agentId);
    
    /**
     * 根据代理商ID获取邮箱地址
     * @param agentId 代理商ID
     * @return 邮箱地址
     */
    String getEmailById(@Param("agentId") Long agentId);
    
    /**
     * 更新代理商折扣等级
     * @param agentId 代理商ID
     * @param discountLevelId 折扣等级ID
     */
    void updateDiscountLevel(@Param("agentId") Long agentId, @Param("discountLevelId") Long discountLevelId);
    
    /**
     * 更新代理商头像
     * @param agentId 代理商ID
     * @param avatar 头像URL
     */
    void updateAvatar(@Param("agentId") Long agentId, @Param("avatar") String avatar);

    // ===== Dashboard统计相关方法 =====
    
    /**
     * 获取代理商总数
     * @return 代理商总数
     */
    Integer count();

    /**
     * 获取指定时间范围内的新代理商数量
     * @param start 开始时间
     * @param end 结束时间
     * @return 新代理商数量
     */
    Integer getNewAgentsByDateRange(@Param("start") java.time.LocalDateTime start, 
                                   @Param("end") java.time.LocalDateTime end);

    /**
     * 获取指定时间范围内的活跃代理商数量
     * @param start 开始时间
     * @param end 结束时间
     * @return 活跃代理商数量
     */
    Integer getActiveAgentsByDateRange(@Param("start") java.time.LocalDateTime start, 
                                      @Param("end") java.time.LocalDateTime end);

    /**
     * 获取代理商表现数据
     * @return 代理商表现统计
     */
    Map<String, Object> getAgentPerformanceData();
} 