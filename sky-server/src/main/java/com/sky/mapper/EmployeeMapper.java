package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
     * 新增员工
     */
    void insert(Employee employee);

    /**
     * 更新员工信息
     */
    @Update("update employees set name = #{name}, username = #{username}, phone = #{phone}, sex = #{sex}, " +
            "id_number = #{idNumber}, status = #{status}, role = #{role}, work_status = #{workStatus}, " +
            "update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void update(Employee employee);

    void status(Employee employee);
}