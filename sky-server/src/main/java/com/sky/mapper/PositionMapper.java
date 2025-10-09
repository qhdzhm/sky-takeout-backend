package com.sky.mapper;

import com.sky.entity.Position;
import com.sky.vo.PositionVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 职位Mapper接口
 */
@Mapper
public interface PositionMapper {

    /**
     * 查询所有职位
     */
    @Select("SELECT * FROM positions WHERE status = 1 ORDER BY dept_id, position_level")
    List<Position> findAll();

    /**
     * 根据ID查询职位
     */
    @Select("SELECT * FROM positions WHERE id = #{id}")
    Position findById(Long id);

    /**
     * 根据部门ID查询职位
     */
    @Select("SELECT * FROM positions WHERE dept_id = #{deptId} AND status = 1 ORDER BY position_level")
    List<Position> findByDeptId(Long deptId);

    /**
     * 根据职位代码查询职位
     */
    @Select("SELECT * FROM positions WHERE position_code = #{positionCode}")
    Position findByPositionCode(String positionCode);

    /**
     * 插入职位
     */
    @Insert("INSERT INTO positions (position_code, position_name, position_name_en, dept_id, " +
            "position_level, is_management, legacy_role_mapping, can_assign_orders, operator_type, " +
            "page_permissions, description, status, created_at, updated_at) " +
            "VALUES (#{positionCode}, #{positionName}, #{positionNameEn}, #{deptId}, " +
            "#{positionLevel}, #{isManagement}, #{legacyRoleMapping}, #{canAssignOrders}, #{operatorType}, " +
            "#{pagePermissions}, #{description}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Position position);

    /**
     * 更新职位
     */
    @Update("UPDATE positions SET position_code = #{positionCode}, position_name = #{positionName}, " +
            "position_name_en = #{positionNameEn}, dept_id = #{deptId}, position_level = #{positionLevel}, " +
            "is_management = #{isManagement}, legacy_role_mapping = #{legacyRoleMapping}, " +
            "can_assign_orders = #{canAssignOrders}, operator_type = #{operatorType}, " +
            "page_permissions = #{pagePermissions}, description = #{description}, status = #{status}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    void update(Position position);

    /**
     * 删除职位
     */
    @Update("UPDATE positions SET status = 0, updated_at = NOW() WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 查询职位详细信息（包含部门信息和统计数据）
     */
    @Select("SELECT p.*, d.dept_name, " +
            "COUNT(DISTINCT e.id) as employee_count, " +
            "COUNT(DISTINCT ppp.id) as permission_count, " +
            "MAX(ppp.granted_at) as last_permission_update " +
            "FROM positions p " +
            "LEFT JOIN departments d ON p.dept_id = d.id " +
            "LEFT JOIN employees e ON p.id = e.position_id AND e.status = 1 " +
            "LEFT JOIN position_page_permissions ppp ON p.id = ppp.position_id AND ppp.status = 1 " +
            "WHERE p.status = 1 " +
            "GROUP BY p.id " +
            "ORDER BY d.sort_order, p.position_level")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "positionCode", column = "position_code"),
        @Result(property = "positionName", column = "position_name"),
        @Result(property = "positionNameEn", column = "position_name_en"),
        @Result(property = "deptId", column = "dept_id"),
        @Result(property = "deptName", column = "dept_name"),
        @Result(property = "positionLevel", column = "position_level"),
        @Result(property = "isManagement", column = "is_management"),
        @Result(property = "legacyRoleMapping", column = "legacy_role_mapping"),
        @Result(property = "canAssignOrders", column = "can_assign_orders"),
        @Result(property = "operatorType", column = "operator_type"),
        @Result(property = "description", column = "description"),
        @Result(property = "status", column = "status"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "employeeCount", column = "employee_count"),
        @Result(property = "permissionCount", column = "permission_count"),
        @Result(property = "lastPermissionUpdate", column = "last_permission_update")
    })
    List<PositionVO> findPositionDetails();

    /**
     * 根据部门ID查询职位详细信息
     */
    @Select("SELECT p.*, d.dept_name, " +
            "COUNT(DISTINCT e.id) as employee_count, " +
            "COUNT(DISTINCT ppp.id) as permission_count " +
            "FROM positions p " +
            "LEFT JOIN departments d ON p.dept_id = d.id " +
            "LEFT JOIN employees e ON p.id = e.position_id AND e.status = 1 " +
            "LEFT JOIN position_page_permissions ppp ON p.id = ppp.position_id AND ppp.status = 1 " +
            "WHERE p.dept_id = #{deptId} AND p.status = 1 " +
            "GROUP BY p.id " +
            "ORDER BY p.position_level")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "positionCode", column = "position_code"),
        @Result(property = "positionName", column = "position_name"),
        @Result(property = "deptName", column = "dept_name"),
        @Result(property = "positionLevel", column = "position_level"),
        @Result(property = "isManagement", column = "is_management"),
        @Result(property = "employeeCount", column = "employee_count"),
        @Result(property = "permissionCount", column = "permission_count")
    })
    List<PositionVO> findPositionDetailsByDeptId(Long deptId);
}
