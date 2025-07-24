package com.sky.mapper;

import com.sky.entity.ServiceSession;
import com.sky.vo.ServiceSessionVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务会话Mapper
 */
@Mapper
public interface ServiceSessionMapper {

    /**
     * 插入新会话
     */
    @Insert("insert into service_session (session_no, user_id, session_status, session_type, subject, start_time, create_time, update_time) " +
            "values (#{sessionNo}, #{userId}, #{sessionStatus}, #{sessionType}, #{subject}, #{startTime}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ServiceSession serviceSession);

    /**
     * 根据ID查询会话
     */
    @Select("select * from service_session where id = #{id}")
    ServiceSession getById(Long id);

    /**
     * 分配客服
     */
    @Update("update service_session set employee_id = #{serviceId}, session_status = 1, update_time = #{updateTime} where id = #{sessionId}")
    void assignService(Long sessionId, Long serviceId, LocalDateTime updateTime);

    /**
     * 更新会话状态
     */
    @Update("update service_session set session_status = #{status}, end_time = #{endTime}, service_duration = #{serviceDuration}, update_time = #{updateTime} where id = #{sessionId}")
    void updateSessionStatus(Long sessionId, Integer status, LocalDateTime endTime, Integer serviceDuration, LocalDateTime updateTime);

    /**
     * 获取等待分配的会话列表
     */
    @Select("select * from service_session where session_status = 0 order by create_time asc")
    List<ServiceSession> getWaitingAssignSessions();

    /**
     * 获取客服的活跃会话列表
     */
    @Select("select ss.*, u.username as user_name, u.first_name as user_avatar, cs.name as service_name " +
            "from service_session ss " +
            "left join users u on ss.user_id = u.user_id " +
            "left join customer_service cs on ss.employee_id = cs.id " +
            "where ss.employee_id = #{serviceId} and ss.session_status = 1 " +
            "order by ss.update_time desc")
    List<ServiceSessionVO> getActiveSessionsByServiceId(Long serviceId);

    /**
     * 获取用户的活跃会话
     */
    @Select("select * from service_session where user_id = #{userId} and session_status in (0, 1)")
    ServiceSession getActiveSessionByUserId(Long userId);

    /**
     * 更新会话评价
     */
    @Update("update service_session set user_rating = #{rating}, user_comment = #{comment}, update_time = #{updateTime} where id = #{sessionId}")
    void updateRating(Long sessionId, Integer rating, String comment, LocalDateTime updateTime);

    /**
     * 更新客服备注
     */
    @Update("update service_session set service_remark = #{remark}, update_time = #{updateTime} where id = #{sessionId}")
    void updateServiceRemark(Long sessionId, String remark, LocalDateTime updateTime);

    /**
     * 更新会话信息
     */
    @Update("update service_session set session_status = #{sessionStatus}, session_type = #{sessionType}, " +
            "subject = #{subject}, employee_id = #{employeeId}, update_time = #{updateTime} where id = #{id}")
    void updateSession(ServiceSession serviceSession);

    /**
     * 按员工ID分页查询会话列表
     */
    List<ServiceSessionVO> pageQueryByEmployeeId(@Param("employeeId") Long employeeId,
                                               @Param("status") Integer status,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate,
                                               @Param("keyword") String keyword,
                                               @Param("offset") Integer offset,
                                               @Param("limit") Integer limit);

    /**
     * 统计员工会话总数
     */
    Integer countByEmployeeId(@Param("employeeId") Long employeeId,
                            @Param("status") Integer status,
                            @Param("startDate") String startDate,
                            @Param("endDate") String endDate,
                            @Param("keyword") String keyword);

    /**
     * 查询所有会话（管理员查看全部）
     */
    List<ServiceSessionVO> pageQueryAll(@Param("status") Integer status,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate,
                                      @Param("keyword") String keyword,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    /**
     * 统计所有会话总数
     */
    Integer countAll(@Param("status") Integer status,
                   @Param("startDate") String startDate,
                   @Param("endDate") String endDate,
                   @Param("keyword") String keyword);
} 