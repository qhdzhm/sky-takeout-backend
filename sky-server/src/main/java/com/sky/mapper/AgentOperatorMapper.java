package com.sky.mapper;

import com.sky.entity.AgentOperator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 代理商操作员Mapper
 */
@Mapper
public interface AgentOperatorMapper {

    /**
     * 根据用户名查询操作员
     */
    @Select("SELECT * FROM agent_operators WHERE username = #{username}")
    AgentOperator getByUsername(String username);

    /**
     * 根据ID查询操作员
     */
    @Select("SELECT * FROM agent_operators WHERE id = #{id}")
    AgentOperator getById(Long id);

    /**
     * 根据代理商ID查询操作员列表
     */
    @Select("SELECT * FROM agent_operators WHERE agent_id = #{agentId} AND status = 1")
    List<AgentOperator> getByAgentId(Long agentId);

    /**
     * 插入操作员
     */
    @Insert("INSERT INTO agent_operators (agent_id, username, password, name, email, phone, status, permissions, created_at, updated_at) " +
            "VALUES (#{agentId}, #{username}, #{password}, #{name}, #{email}, #{phone}, #{status}, #{permissions}, #{createdAt}, #{updatedAt})")
    void insert(AgentOperator agentOperator);

    /**
     * 更新操作员信息
     */
    @Update("UPDATE agent_operators SET name = #{name}, email = #{email}, phone = #{phone}, " +
            "permissions = #{permissions}, updated_at = #{updatedAt} WHERE id = #{id}")
    void update(AgentOperator agentOperator);

    /**
     * 更新操作员密码
     */
    @Update("UPDATE agent_operators SET password = #{password}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updatePassword(Long id, String password, java.time.LocalDateTime updatedAt);

    /**
     * 更新操作员状态
     */
    @Update("UPDATE agent_operators SET status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    void updateStatus(Long id, Integer status, java.time.LocalDateTime updatedAt);

    /**
     * 删除操作员
     */
    @Delete("DELETE FROM agent_operators WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM agent_operators WHERE username = #{username}")
    int countByUsername(String username);

    /**
     * 检查用户名是否存在（排除指定ID）
     */
    @Select("SELECT COUNT(*) FROM agent_operators WHERE username = #{username} AND id != #{id}")
    int countByUsernameExcludeId(String username, Long id);
    
    /**
     * 根据操作员ID获取邮箱地址
     */
    @Select("SELECT email FROM agent_operators WHERE id = #{operatorId}")
    String getEmailById(Long operatorId);
} 