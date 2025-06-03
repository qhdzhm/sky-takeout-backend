package com.sky.mapper;

import com.sky.entity.CustomerService;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客服Mapper
 */
@Mapper
public interface CustomerServiceMapper {

    /**
     * 根据用户名查询客服
     */
    @Select("select * from customer_service where username = #{username}")
    CustomerService getByUsername(String username);

    /**
     * 根据ID查询客服
     */
    @Select("select * from customer_service where id = #{id}")
    CustomerService getById(Long id);

    /**
     * 更新客服在线状态
     */
    @Update("update customer_service set online_status = #{onlineStatus}, last_active_time = #{lastActiveTime} where id = #{id}")
    void updateOnlineStatus(Long id, Integer onlineStatus, LocalDateTime lastActiveTime);

    /**
     * 更新客服当前服务客户数
     */
    @Update("update customer_service set current_customer_count = #{count} where id = #{id}")
    void updateCurrentCustomerCount(Long id, Integer count);

    /**
     * 获取在线客服列表
     */
    @Select("select * from customer_service where online_status = 1 and status = 1 order by current_customer_count asc, service_level desc")
    List<CustomerService> getOnlineServices();

    /**
     * 根据技能标签获取在线客服
     */
    @Select("select * from customer_service where online_status = 1 and status = 1 and skill_tags like concat('%', #{skillTag}, '%') order by current_customer_count asc, service_level desc")
    List<CustomerService> getOnlineServicesBySkill(String skillTag);

    /**
     * 更新最后登录时间
     */
    @Update("update customer_service set last_login_time = #{lastLoginTime} where id = #{id}")
    void updateLastLoginTime(Long id, LocalDateTime lastLoginTime);
} 