package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;

import java.util.Map;

/**
 * ClassName: UserMapper
 * Package: com.sky.mapper
 * Description:
 *
 * @Author Tangshifu
 * @Create 2024/7/15 16:10
 * @Version 1.0
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    @Select("select user_id as id, username, password, phone, role, user_type as userType, agent_id as agentId, " +
            "created_at as createTime, updated_at as updateTime from users where username = #{username}")
    User getUserByUsername(String username);

    /**
     * 插入数据
     * @param user
     */
    @Insert("insert into users(username, password, phone, role, user_type, agent_id, created_at)"+
        " values"+
        "(#{username}, #{password}, #{phone}, #{role}, #{userType}, #{agentId}, now())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "user_id")
    void addUser(User user);

    /**
     * 根据ID查询用户
     * @param id
     * @return
     */
    @Select("select user_id as id, username, password, phone, role, user_type as userType, agent_id as agentId, " +
            "created_at as createTime, updated_at as updateTime from users where user_id = #{id}")
    User getById(Long id);

    Integer userCount(Map map);

    /**
     * 更新用户信息
     * @param user
     */
    @Update("update users set phone = #{phone}, user_type = #{userType}, agent_id = #{agentId}, " +
            "updated_at = now() where user_id = #{id}")
    void updateById(User user);

}
