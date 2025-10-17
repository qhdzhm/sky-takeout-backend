package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CustomerServicePageQueryDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.vo.CustomerServiceStatisticsVO;
import com.sky.vo.CustomerServiceVO;
import com.sky.vo.EmployeeWithDeptVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     */
    @Select("select * from employees where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 根据用户名查询员工（排除指定ID）
     * 用于检查用户名是否重复
     */
    @Select("SELECT * FROM employees WHERE username = #{username} AND id != #{excludeId}")
    Employee getByUsernameExcludingId(String username, Long excludeId);

    /**
     * 分页查询员工
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 按部门分页查询员工（用于部门权限控制）
     */
    Page<Employee> pageQueryByDepartment(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 查询单个员工（用于普通员工只能查看自己）
     */
    @Select("SELECT * FROM employees WHERE id = #{employeeId}")
    Page<Employee> pageQueryBySelfId(Integer employeeId);

    /**
     * 根据ID查询员工
     */
    @Select("select * from employees where id = #{id}")
    Employee getById(Integer id);

    /**
     * 根据ID查询员工姓名
     */
    @Select("select name from employees where id = #{id}")
    String getNameById(Long id);

    /**
     * 新增员工
     */
    void insert(Employee employee);

    /**
     * 更新员工信息
     * @return 影响的行数
     */
    int update(Employee employee);

    /**
     * 删除员工
     */
    @Select("DELETE FROM employees WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * 更新员工的部门和职位（同时更新role为职位名称）
     */
    @Update("UPDATE employees e " +
            "JOIN positions p ON p.id = #{positionId} " +
            "SET e.dept_id = #{deptId}, " +
            "    e.position_id = #{positionId}, " +
            "    e.direct_supervisor_id = #{directSupervisorId}, " +
            "    e.role = p.position_name, " +
            "    e.update_time = NOW() " +
            "WHERE e.id = #{employeeId}")
    void updateEmployeeDeptAndPosition(Long employeeId, Long deptId, Long positionId, Long directSupervisorId);

    /**
     * 获取所有员工的详细信息（包含部门职位信息）
     * 只返回在职员工（status=1或true）
     */
    @Select("SELECT " +
            "e.id, e.name, e.username, e.role, e.status, " +
            "e.dept_id, d.dept_name, " +
            "e.position_id, p.position_name, p.position_level, " +
            "p.is_management, p.legacy_role_mapping, " +
            "e.create_time, e.update_time " +
            "FROM employees e " +
            "LEFT JOIN departments d ON e.dept_id = d.id " +
            "LEFT JOIN positions p ON e.position_id = p.id " +
            "WHERE (e.status = 1 OR e.status = TRUE) " +
            "ORDER BY e.dept_id, e.position_id, e.name")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "username", column = "username"),
        @Result(property = "role", column = "role"),
        @Result(property = "status", column = "status"),
        @Result(property = "deptId", column = "dept_id"),
        @Result(property = "deptName", column = "dept_name"),
        @Result(property = "positionId", column = "position_id"),
        @Result(property = "positionName", column = "position_name"),
        @Result(property = "positionLevel", column = "position_level"),
        @Result(property = "isManagement", column = "is_management"),
        @Result(property = "legacyRoleMapping", column = "legacy_role_mapping"),
        @Result(property = "createdAt", column = "create_time"),
        @Result(property = "updatedAt", column = "update_time")
    })
    List<EmployeeWithDeptVO> findAllEmployeesWithDeptInfo();

    /**
     * 按部门获取员工的详细信息（包含部门职位信息）
     * 只返回在职员工（status=1或true）且属于指定部门
     */
    @Select("SELECT " +
            "e.id, e.name, e.username, e.role, e.status, " +
            "e.dept_id, d.dept_name, " +
            "e.position_id, p.position_name, p.position_level, " +
            "p.is_management, p.legacy_role_mapping, " +
            "e.create_time, e.update_time " +
            "FROM employees e " +
            "LEFT JOIN departments d ON e.dept_id = d.id " +
            "LEFT JOIN positions p ON e.position_id = p.id " +
            "WHERE (e.status = 1 OR e.status = TRUE) AND e.dept_id = #{deptId} " +
            "ORDER BY e.position_id, e.name")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "username", column = "username"),
        @Result(property = "role", column = "role"),
        @Result(property = "status", column = "status"),
        @Result(property = "deptId", column = "dept_id"),
        @Result(property = "deptName", column = "dept_name"),
        @Result(property = "positionId", column = "position_id"),
        @Result(property = "positionName", column = "position_name"),
        @Result(property = "positionLevel", column = "position_level"),
        @Result(property = "isManagement", column = "is_management"),
        @Result(property = "legacyRoleMapping", column = "legacy_role_mapping"),
        @Result(property = "createdAt", column = "create_time"),
        @Result(property = "updatedAt", column = "update_time")
    })
    List<EmployeeWithDeptVO> findEmployeesWithDeptInfoByDeptId(Long deptId);

    // ===== 客服相关方法 =====

    /**
     * 分页查询客服员工
     */
    Page<CustomerServiceVO> customerServicePageQuery(CustomerServicePageQueryDTO queryDTO);

    /**
     * 根据ID查询客服员工
     */
    @Select("select * from employees where id = #{id} and (role LIKE '%客服%' OR role LIKE '%Service%')")
    Employee getCustomerServiceById(Long id);

    /**
     * 查询所有客服员工
     */
    @Select("select * from v_customer_service_employees")
    List<CustomerServiceVO> getAllCustomerServices();

    /**
     * 更新客服在线状态
     */
    @Update("update employees set online_status = #{onlineStatus}, last_active_time = now(), update_time = now() where id = #{id} and (role LIKE '%客服%' OR role LIKE '%Service%')")
    void updateCustomerServiceOnlineStatus(Long id, Integer onlineStatus);

    /**
     * 新增客服员工
     */
    void insertCustomerService(Employee employee);

    /**
     * 更新客服员工信息
     */
    void updateCustomerService(Employee employee);

    /**
     * 获取客服统计信息
     */
    CustomerServiceStatisticsVO getCustomerServiceStatistics();

    /**
     * 查询可用的客服（根据技能标签）
     */
    @Select("select * from employees where (role LIKE '%客服%' OR role LIKE '%Service%') and online_status = 1 and current_customer_count < max_concurrent_customers " +
            "and (#{skillTag} is null or find_in_set(#{skillTag}, skill_tags) > 0) " +
            "order by current_customer_count asc, service_level desc limit 1")
    Employee getAvailableCustomerService(String skillTag);

    /**
     * 更新客服当前服务客户数
     */
    @Update("update employees set current_customer_count = #{count}, update_time = now() where id = #{id} and (role LIKE '%客服%' OR role LIKE '%Service%')")
    void updateCustomerServiceCurrentCount(Long id, Integer count);

    // ===== Dashboard统计相关方法 =====
    
    /**
     * 获取员工总数
     * @return 员工总数
     */
    @Select("SELECT COUNT(*) FROM employees")
    Integer count();

    /**
     * 根据状态获取员工数量
     * @param status 员工状态
     * @return 员工数量
     */
    @Select("SELECT COUNT(*) FROM employees WHERE status = #{status}")
    Integer countByStatus(Integer status);

    // ===== 操作员分工相关方法 =====

    /**
     * 根据操作员类型查询员工
     * @param operatorType 操作员类型（如 hotel_operator, tour_master）
     * @return 对应类型的员工列表
     */
    @Select("SELECT * FROM employees WHERE operator_type = #{operatorType} AND status = 1 ORDER BY name")
    List<Employee> findByOperatorType(String operatorType);

    /**
     * 清除所有员工的排团主管标记
     */
    @Update("UPDATE employees SET is_tour_master = 0, operator_type = CASE WHEN operator_type = 'tour_master' THEN 'general' ELSE operator_type END WHERE is_tour_master = 1")
    void clearAllTourMasterFlags();

    /**
     * 获取当前排团主管
     */
    @Select("SELECT * FROM employees WHERE is_tour_master = 1 AND status = 1 LIMIT 1")
    Employee getCurrentTourMaster();


}