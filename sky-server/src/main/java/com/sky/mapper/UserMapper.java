package com.sky.mapper;

import com.sky.dto.UserPageQueryDTO;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;
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
            "created_at as createTime, updated_at as updateTime, IFNULL(status, 1) as status, first_name as firstName, last_name as lastName, " +
            "wx_openid as openid, wx_unionid as unionid, wx_nickname as wxNickname, wx_avatar as wxAvatar, wx_last_login as wxLastLogin, " +
            "invite_code as inviteCode, referred_by as referredBy " +
            "from users where username = #{username}")
    User getUserByUsername(String username);
    
    /**
     * 根据微信OpenID查询用户
     * @param openid 微信OpenID
     * @return 用户对象
     */
    @Select("select user_id as id, username, password, phone, role, user_type as userType, agent_id as agentId, " +
            "created_at as createTime, updated_at as updateTime, IFNULL(status, 1) as status, first_name as firstName, last_name as lastName, " +
            "wx_openid as openid, wx_unionid as unionid, wx_nickname as wxNickname, wx_avatar as wxAvatar, wx_last_login as wxLastLogin, " +
            "invite_code as inviteCode, referred_by as referredBy " +
            "from users where wx_openid = #{openid}")
    User getUserByOpenid(String openid);
    
    /**
     * 根据邀请码查询用户
     * @param inviteCode 邀请码
     * @return 用户对象
     */
    @Select("select user_id as id, username, password, phone, role, user_type as userType, agent_id as agentId, " +
            "created_at as createTime, updated_at as updateTime, IFNULL(status, 1) as status, first_name as firstName, last_name as lastName, " +
            "wx_openid as openid, wx_unionid as unionid, wx_nickname as wxNickname, wx_avatar as wxAvatar, wx_last_login as wxLastLogin, " +
            "invite_code as inviteCode, referred_by as referredBy " +
            "from users where invite_code = #{inviteCode}")
    User getUserByInviteCode(String inviteCode);

    /**
     * 插入数据
     * @param user
     */
    @Insert("insert into users(username, password, phone, role, user_type, agent_id, created_at, first_name, last_name, email, " +
            "wx_openid, wx_unionid, wx_nickname, wx_avatar, wx_last_login, invite_code, referred_by)"+
        " values"+
        "(#{username}, #{password}, #{phone}, #{role}, #{userType}, #{agentId}, now(), #{firstName}, #{lastName}, #{email}, " +
        "#{openid}, #{unionid}, #{wxNickname}, #{wxAvatar}, #{wxLastLogin}, #{inviteCode}, #{referredBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "user_id")
    void addUser(User user);

    /**
     * 根据ID查询用户
     * @param id
     * @return
     */
    @Select("select user_id as id, username, password, phone, role, user_type as userType, agent_id as agentId, " +
            "created_at as createTime, updated_at as updateTime, IFNULL(status, 1) as status, first_name as firstName, last_name as lastName, " +
            "wx_openid as openid, wx_unionid as unionid, wx_nickname as wxNickname, wx_avatar as wxAvatar, wx_last_login as wxLastLogin, " +
            "invite_code as inviteCode, referred_by as referredBy " +
            "from users where user_id = #{id}")
    User getById(Long id);

    /**
     * 获取用户总数
     * @param map
     * @return
     */
    Integer userCount(Map map);

    /**
     * 更新用户信息
     * @param user
     */
    @Update("update users set phone = #{phone}, user_type = #{userType}, agent_id = #{agentId}, first_name = #{firstName}, " +
            "last_name = #{lastName}, wx_openid = #{openid}, wx_unionid = #{unionid}, wx_nickname = #{wxNickname}, " +
            "wx_avatar = #{wxAvatar}, wx_last_login = #{wxLastLogin}, " +
            "updated_at = now() where user_id = #{id}")
    void updateById(User user);
    
    /**
     * 更新用户微信登录信息
     * @param user
     */
    @Update("update users set wx_nickname = #{wxNickname}, wx_avatar = #{wxAvatar}, " +
            "wx_last_login = now(), updated_at = now() where wx_openid = #{openid}")
    void updateWxLoginInfo(User user);
    
    /**
     * 分页查询用户
     * @param userPageQueryDTO
     * @return
     */
    List<User> pageQuery(UserPageQueryDTO userPageQueryDTO);
    
    /**
     * 根据条件统计用户数量
     * @param userPageQueryDTO
     * @return
     */
    Integer countUser(UserPageQueryDTO userPageQueryDTO);
    
    /**
     * 根据ID删除用户
     * @param id
     */
    @Delete("delete from users where user_id = #{id}")
    void deleteById(Long id);
    
    /**
     * 修改用户状态
     * @param id
     * @param status
     */
    @Update("update users set status = #{status}, updated_at = now() where user_id = #{id}")
    void updateStatus(Long id, Integer status);
    
    /**
     * 修改用户密码
     * @param id
     * @param password
     */
    @Update("update users set password = #{password}, updated_at = now() where user_id = #{id}")
    void updatePassword(Long id, String password);

    /**
     * 根据名称关键字查询用户
     * @param keyword 名称关键字
     * @return 用户列表
     */
    List<User> getUsersByNameKeyword(@Param("keyword") String keyword);
}
