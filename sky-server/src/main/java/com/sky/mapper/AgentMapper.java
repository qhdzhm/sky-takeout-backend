package com.sky.mapper;

import com.sky.entity.Agent;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

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
     */
    void update(Agent agent);
    
    /**
     * 查询所有代理商
     * @return 代理商列表
     */
    List<Agent> list();
} 