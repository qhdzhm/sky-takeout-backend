package com.sky.mapper;

import com.sky.entity.PositionPagePermission;
import com.sky.vo.PositionPermissionVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 职位页面权限关联Mapper接口
 */
@Mapper
public interface PositionPagePermissionMapper {

    /**
     * 根据职位ID查询权限
     */
    @Select("SELECT * FROM position_page_permissions WHERE position_id = #{positionId} AND status = 1")
    List<PositionPagePermission> findByPositionId(Long positionId);

    /**
     * 根据页面ID查询权限
     */
    @Select("SELECT * FROM position_page_permissions WHERE page_id = #{pageId} AND status = 1")
    List<PositionPagePermission> findByPageId(Long pageId);

    /**
     * 查询职位的页面权限详情
     */
    @Select("SELECT sp.id as page_id, sp.page_path, sp.page_name, sp.page_group, " +
            "CASE WHEN ppp.id IS NOT NULL THEN 1 ELSE 0 END as has_permission, " +
            "sp.is_required, ppp.granted_at " +
            "FROM system_pages sp " +
            "LEFT JOIN position_page_permissions ppp ON sp.id = ppp.page_id " +
            "AND ppp.position_id = #{positionId} AND ppp.status = 1 " +
            "WHERE sp.status = 1 " +
            "ORDER BY sp.page_group, sp.sort_order")
    @Results({
        @Result(property = "pageId", column = "page_id"),
        @Result(property = "pagePath", column = "page_path"),
        @Result(property = "pageName", column = "page_name"),
        @Result(property = "pageGroup", column = "page_group"),
        @Result(property = "hasPermission", column = "has_permission"),
        @Result(property = "isRequired", column = "is_required"),
        @Result(property = "grantedAt", column = "granted_at")
    })
    List<PositionPermissionVO.PagePermission> findPositionPagePermissions(Long positionId);

    /**
     * 插入权限
     */
    @Insert("INSERT INTO position_page_permissions (position_id, page_id, granted_by_employee_id, " +
            "granted_at, status) VALUES (#{positionId}, #{pageId}, #{grantedByEmployeeId}, " +
            "#{grantedAt}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PositionPagePermission permission);

    /**
     * 批量插入权限
     */
    @Insert("<script>" +
            "INSERT INTO position_page_permissions (position_id, page_id, granted_by_employee_id, granted_at, status) VALUES " +
            "<foreach collection='permissions' item='permission' separator=','>" +
            "(#{permission.positionId}, #{permission.pageId}, #{permission.grantedByEmployeeId}, " +
            "#{permission.grantedAt}, #{permission.status})" +
            "</foreach>" +
            "</script>")
    void batchInsert(@Param("permissions") List<PositionPagePermission> permissions);

    /**
     * 更新权限状态
     */
    @Update("UPDATE position_page_permissions SET status = #{status} " +
            "WHERE position_id = #{positionId} AND page_id = #{pageId}")
    void updateStatus(@Param("positionId") Long positionId, @Param("pageId") Long pageId, @Param("status") Integer status);

    /**
     * 删除职位的所有权限
     */
    @Update("UPDATE position_page_permissions SET status = 0 WHERE position_id = #{positionId}")
    void deleteByPositionId(Long positionId);

    /**
     * 删除特定权限
     */
    @Update("UPDATE position_page_permissions SET status = 0 " +
            "WHERE position_id = #{positionId} AND page_id = #{pageId}")
    void deleteByPositionAndPage(@Param("positionId") Long positionId, @Param("pageId") Long pageId);

    /**
     * 批量删除权限
     */
    @Update("<script>" +
            "UPDATE position_page_permissions SET status = 0 WHERE " +
            "<foreach collection='positionIds' item='positionId' separator=' OR '>" +
            "(position_id = #{positionId}" +
            "<if test='pageIds != null and pageIds.size() > 0'> AND page_id IN " +
            "<foreach collection='pageIds' item='pageId' open='(' separator=',' close=')'>#{pageId}</foreach>" +
            "</if>)" +
            "</foreach>" +
            "</script>")
    void batchDelete(@Param("positionIds") List<Long> positionIds, @Param("pageIds") List<Long> pageIds);

    /**
     * 检查权限是否存在
     */
    @Select("SELECT COUNT(*) FROM position_page_permissions " +
            "WHERE position_id = #{positionId} AND page_id = #{pageId} AND status = 1")
    int checkPermissionExists(@Param("positionId") Long positionId, @Param("pageId") Long pageId);

    /**
     * 复制权限（从一个职位到另一个职位）
     */
    @Insert("INSERT INTO position_page_permissions (position_id, page_id, granted_by_employee_id, granted_at, status) " +
            "SELECT #{targetPositionId}, page_id, #{grantedByEmployeeId}, NOW(), 1 " +
            "FROM position_page_permissions " +
            "WHERE position_id = #{sourcePositionId} AND status = 1")
    void copyPermissions(@Param("sourcePositionId") Long sourcePositionId, 
                        @Param("targetPositionId") Long targetPositionId, 
                        @Param("grantedByEmployeeId") Long grantedByEmployeeId);

    /**
     * 根据员工ID查询其所有页面权限路径
     */
    @Select("SELECT DISTINCT sp.page_path " +
            "FROM employees e " +
            "JOIN positions p ON e.position_id = p.id " +
            "JOIN position_page_permissions ppp ON p.id = ppp.position_id " +
            "JOIN system_pages sp ON ppp.page_id = sp.id " +
            "WHERE e.id = #{employeeId} AND e.status = 1 AND p.status = 1 " +
            "AND ppp.status = 1 AND sp.status = 1 " +
            "UNION " +
            "SELECT sp.page_path " +
            "FROM system_pages sp " +
            "WHERE sp.is_required = 1 AND sp.status = 1")
    List<String> findEmployeePagePaths(Long employeeId);

    /**
     * 检查员工是否有特定页面权限
     */
    @Select("SELECT COUNT(*) > 0 " +
            "FROM employees e " +
            "JOIN positions p ON e.position_id = p.id " +
            "JOIN position_page_permissions ppp ON p.id = ppp.position_id " +
            "JOIN system_pages sp ON ppp.page_id = sp.id " +
            "WHERE e.id = #{employeeId} AND sp.page_path = #{pagePath} " +
            "AND e.status = 1 AND p.status = 1 AND ppp.status = 1 AND sp.status = 1 " +
            "UNION ALL " +
            "SELECT COUNT(*) > 0 " +
            "FROM system_pages sp " +
            "WHERE sp.page_path = #{pagePath} AND sp.is_required = 1 AND sp.status = 1 " +
            "LIMIT 1")
    Boolean checkEmployeePagePermission(@Param("employeeId") Long employeeId, @Param("pagePath") String pagePath);

    /**
     * 插入或更新权限（使用ON DUPLICATE KEY UPDATE避免重复键冲突）
     */
    @Insert("INSERT INTO position_page_permissions (position_id, page_id, granted_by_employee_id, granted_at, status) " +
            "VALUES (#{positionId}, #{pageId}, #{grantedByEmployeeId}, #{grantedAt}, #{status}) " +
            "ON DUPLICATE KEY UPDATE granted_by_employee_id = VALUES(granted_by_employee_id), " +
            "granted_at = VALUES(granted_at), status = VALUES(status)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void upsertPermission(PositionPagePermission permission);
}
