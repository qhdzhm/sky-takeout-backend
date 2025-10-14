package com.sky.mapper;

import com.sky.entity.Department;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 部门Mapper接口
 */
@Mapper
public interface DepartmentMapper {

    /**
     * 查询所有部门
     */
    @Select("SELECT * FROM departments WHERE status = 1 ORDER BY sort_order")
    List<Department> findAll();

    /**
     * 根据ID查询部门
     */
    @Select("SELECT * FROM departments WHERE id = #{id}")
    Department findById(Long id);

    /**
     * 根据部门代码查询部门
     */
    @Select("SELECT * FROM departments WHERE dept_code = #{deptCode}")
    Department findByDeptCode(String deptCode);

    /**
     * 插入部门
     */
    @Insert("INSERT INTO departments (dept_code, dept_name, dept_name_en, parent_dept_id, dept_level, " +
            "sort_order, description, status, created_at, updated_at) " +
            "VALUES (#{deptCode}, #{deptName}, #{deptNameEn}, #{parentDeptId}, #{deptLevel}, " +
            "#{sortOrder}, #{description}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Department department);

    /**
     * 更新部门
     */
    @Update("UPDATE departments SET dept_code = #{deptCode}, dept_name = #{deptName}, " +
            "dept_name_en = #{deptNameEn}, parent_dept_id = #{parentDeptId}, dept_level = #{deptLevel}, " +
            "sort_order = #{sortOrder}, description = #{description}, status = #{status}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    void update(Department department);

    /**
     * 删除部门
     */
    @Update("UPDATE departments SET status = 0, updated_at = NOW() WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 查询部门统计信息
     */
    @Select("SELECT d.id, d.dept_name, COUNT(p.id) as position_count, COUNT(e.id) as employee_count " +
            "FROM departments d " +
            "LEFT JOIN positions p ON d.id = p.dept_id AND p.status = 1 " +
            "LEFT JOIN employees e ON d.id = e.dept_id AND e.status = 1 " +
            "WHERE d.status = 1 " +
            "GROUP BY d.id, d.dept_name " +
            "ORDER BY d.sort_order")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "deptName", column = "dept_name"),
        @Result(property = "positionCount", column = "position_count"),
        @Result(property = "employeeCount", column = "employee_count")
    })
    List<Department> findDepartmentStatistics();
}


