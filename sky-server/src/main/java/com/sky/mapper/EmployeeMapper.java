package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CustomerServicePageQueryDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.vo.CustomerServiceStatisticsVO;
import com.sky.vo.CustomerServiceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     */
    @Select("select * from employees where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 分页查询员工
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

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
     */
    @Update("update employees set name = #{name}, username = #{username}, phone = #{phone}, sex = #{sex}, " +
            "id_number = #{idNumber}, role = #{role}, work_status = #{workStatus}, " +
            "update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void update(Employee employee);

    // ===== 客服相关方法 =====

    /**
     * 分页查询客服员工
     */
    Page<CustomerServiceVO> customerServicePageQuery(CustomerServicePageQueryDTO queryDTO);

    /**
     * 根据ID查询客服员工
     */
    @Select("select * from employees where id = #{id} and role = 3")
    Employee getCustomerServiceById(Long id);

    /**
     * 查询所有客服员工
     */
    @Select("select * from v_customer_service_employees")
    List<CustomerServiceVO> getAllCustomerServices();

    /**
     * 更新客服在线状态
     */
    @Update("update employees set online_status = #{onlineStatus}, last_active_time = now(), update_time = now() where id = #{id} and role = 3")
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
    @Select("select * from employees where role = 3 and online_status = 1 and current_customer_count < max_concurrent_customers " +
            "and (#{skillTag} is null or find_in_set(#{skillTag}, skill_tags) > 0) " +
            "order by current_customer_count asc, service_level desc limit 1")
    Employee getAvailableCustomerService(String skillTag);

    /**
     * 更新客服当前服务客户数
     */
    @Update("update employees set current_customer_count = #{count}, update_time = now() where id = #{id} and role = 3")
    void updateCustomerServiceCurrentCount(Long id, Integer count);
}