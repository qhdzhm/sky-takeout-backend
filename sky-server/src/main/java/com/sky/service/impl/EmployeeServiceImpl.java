package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.entity.Guide;
import com.sky.exception.AccountNotFoundException;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.GuideMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;



@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private GuideMapper guideMapper;

    /**
     * 员工登录
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes());

        Employee employee = employeeMapper.getByUsername(username);
        if (employee == null || !password.equals(employee.getPassword())) {
            throw new AccountNotFoundException("账号或密码错误");
        }

        // 如果是导游角色，检查导游表中的状态
        if (employee.getRole() == 0) {
            Guide guide = guideMapper.getGuideByEmployeeId(employee.getId());
            if (guide == null || !guide.getIsActive() || guide.getStatus() != 1) {
                throw new AccountNotFoundException("导游账号已禁用");
            }
        }

        return employee;
    }

    /**
     * 分页查询员工
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增员工
     */
    @Override
    @Transactional
    public void addEmp(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置初始密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employeeMapper.insert(employee);

        // 如果是导游角色，自动在导游表中创建对应记录
        if (employeeDTO.getRole() != null && employeeDTO.getRole() == 0) {
            createGuideRecord(employee);
        }
    }

    /**
     * 为导游角色员工创建导游记录
     */
    private void createGuideRecord(Employee employee) {
        Guide guide = Guide.builder()
                .name(employee.getName())
                .phone(employee.getPhone())
                .email(employee.getUsername() + "@example.com")
                .employeeId(employee.getId())
                .isActive(true)
                .status(1)
                .maxGroups(1)
                .experienceYears(0)
                .languages("中文")
                .build();
        
        guideMapper.insertGuide(guide);
    }

    /**
     * 根据ID查询员工
     */
    @Override
    public Employee getEmp(Integer id) {
        return employeeMapper.getById(id);
    }

    /**
     * 更新员工信息
     */
    @Override
    @Transactional
    public void updateEmp(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employeeMapper.update(employee);

        // 如果角色变更为导游，且还没有导游记录，则创建
        if (employeeDTO.getRole() != null && employeeDTO.getRole() == 0) {
            Guide existingGuide = guideMapper.getGuideByEmployeeId(employee.getId());
            if (existingGuide == null) {
                createGuideRecord(employee);
            }
        }
        // 如果角色从导游变更为其他角色，可以选择删除导游记录或保持不变
        // 这里选择保持不变，以保留历史数据
    }
}