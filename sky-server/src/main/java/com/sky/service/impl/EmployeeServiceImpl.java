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
import com.sky.exception.BaseException;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.GuideMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.vo.EmployeeWithDeptVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import lombok.extern.slf4j.Slf4j;



@Service
@Slf4j
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
        if (employee.getRole() != null && employee.getRole().contains("导游")) {
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
     * 基于部门权限的分页查询员工
     */
    @Override
    public PageResult pageQueryWithDepartmentPermission(EmployeePageQueryDTO employeePageQueryDTO, Employee currentEmployee) {
        log.info("基于部门权限的员工分页查询：{}, 当前用户: {}", employeePageQueryDTO, currentEmployee.getName());
        
        // 检查当前用户的部门权限
        String currentUserRole = currentEmployee.getRole();
        Long currentUserDeptId = currentEmployee.getDeptId();
        
        // Full权限用户：IT Manager, Chief Executive 可以查看所有员工
        if (hasFullPermission(currentUserRole)) {
            log.info("用户{}具有Full权限，可查看所有员工", currentEmployee.getName());
            return pageQuery(employeePageQueryDTO);
        }
        
        // 部门Manager：只能查看自己部门的员工
        if (isDepartmentManager(currentUserRole)) {
            log.info("用户{}是部门Manager，限制查看部门ID: {}", currentEmployee.getName(), currentUserDeptId);
            
            // 创建新的查询参数，添加部门过滤
            EmployeePageQueryDTO filteredQuery = new EmployeePageQueryDTO();
            BeanUtils.copyProperties(employeePageQueryDTO, filteredQuery);
            
            // 添加部门过滤条件
            filteredQuery.setDeptId(currentUserDeptId);
            
            PageHelper.startPage(filteredQuery.getPage(), filteredQuery.getPageSize());
            Page<Employee> page = employeeMapper.pageQueryByDepartment(filteredQuery);
            return new PageResult(page.getTotal(), page.getResult());
        }
        
        // 普通员工：只能查看自己
        log.info("用户{}是普通员工，只能查看自己", currentEmployee.getName());
        EmployeePageQueryDTO selfQuery = new EmployeePageQueryDTO();
        selfQuery.setPage(1);
        selfQuery.setPageSize(1);
        
        PageHelper.startPage(selfQuery.getPage(), selfQuery.getPageSize());
        Page<Employee> page = employeeMapper.pageQueryBySelfId(Math.toIntExact(currentEmployee.getId().longValue()));
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 检查是否有完全权限（可以管理所有部门）
     */
    private boolean hasFullPermission(String role) {
        if (role == null) return false;
        return role.contains("IT Manager") || 
               role.contains("Chief Executive") ||
               role.contains("Chief");
    }

    /**
     * 检查是否是部门Manager（可以管理本部门员工）
     */
    private boolean isDepartmentManager(String role) {
        if (role == null) return false;
        return role.contains("Manager") || 
               role.contains("经理") ||
               role.contains("主管");
    }

    /**
     * 新增员工
     */
    @Override
    @Transactional
    public void addEmp(EmployeeDTO employeeDTO) {
        // 检查用户名是否已存在
        Employee existingEmployee = employeeMapper.getByUsername(employeeDTO.getUsername());
        if (existingEmployee != null) {
            throw new BaseException("用户名 '" + employeeDTO.getUsername() + "' 已存在，请使用其他用户名");
        }
        
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置初始密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employeeMapper.insert(employee);

        // 如果是导游角色，自动在导游表中创建对应记录
        if (employeeDTO.getRole() != null && employeeDTO.getRole().contains("导游")) {
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
        log.info("🔍 updateEmp 开始: employeeDTO={}", employeeDTO);
        
        // 如果更新了用户名，检查用户名是否已被其他员工使用
        if (employeeDTO.getUsername() != null && !employeeDTO.getUsername().trim().isEmpty()) {
            Employee existingEmployee = employeeMapper.getByUsernameExcludingId(
                employeeDTO.getUsername(), 
                employeeDTO.getId()
            );
            if (existingEmployee != null) {
                throw new BaseException("用户名 '" + employeeDTO.getUsername() + "' 已被其他员工使用，请使用其他用户名");
            }
        }
        
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        
        // 🔧 修复：手动设置status字段（Integer -> Boolean类型转换）
        if (employeeDTO.getStatus() != null) {
            employee.setStatus(employeeDTO.getStatus() == 1);
            log.info("🔧 手动设置status: Integer {} -> Boolean {}", employeeDTO.getStatus(), employee.getStatus());
        }
        
        log.info("🔍 准备调用 employeeMapper.update: employee={}", employee);
        int updateCount = employeeMapper.update(employee);
        log.info("✅ employeeMapper.update 执行完成，影响行数: {}", updateCount);

        // 如果更新了员工状态，同时更新导游表状态（如果是导游）
        if (employeeDTO.getStatus() != null && employee.getId() != null) {
            log.info("🔍 检查是否需要同步导游状态: employeeId={}, status={}", employee.getId(), employeeDTO.getStatus());
            Guide existingGuide = guideMapper.getGuideByEmployeeId(employee.getId());
            log.info("🔍 查询导游记录结果: {}", existingGuide);
            
            if (existingGuide != null) {
                // 员工status: 1=启用, 0=禁用
                // 导游status: 1=可用, 0=不可用
                Integer guideStatus = employeeDTO.getStatus() == 1 ? 1 : 0;
                Boolean isActive = employeeDTO.getStatus() == 1;
                
                log.info("🔍 准备更新导游状态: employeeId={}, guideStatus={}, isActive={}", 
                        employee.getId(), guideStatus, isActive);
                guideMapper.updateGuideStatusByEmployeeId(employee.getId(), guideStatus, isActive);
                log.info("✅ 员工ID:{} 状态更新为:{}, 同步更新导游状态为:{}", 
                        employee.getId(), employeeDTO.getStatus(), guideStatus);
            } else {
                log.info("ℹ️ 员工ID:{} 不是导游，无需同步导游状态", employee.getId());
            }
        }

        // 如果角色变更为导游，且还没有导游记录，则创建
        if (employeeDTO.getRole() != null && employeeDTO.getRole().contains("导游")) {
            Guide existingGuide = guideMapper.getGuideByEmployeeId(employee.getId());
            if (existingGuide == null) {
                createGuideRecord(employee);
            }
        }
        // 如果角色从导游变更为其他角色，可以选择删除导游记录或保持不变
        // 这里选择保持不变，以保留历史数据
        
        log.info("🎉 updateEmp 完成");
    }

    /**
     * 获取所有员工详细信息（包含部门职位信息）
     * 🔧 支持部门权限隔离：部门主管只能查看自己部门的员工（GMO和IT除外）
     */
    @Override
    public java.util.List<EmployeeWithDeptVO> getAllEmployeesWithDeptInfo() {
        // 获取当前登录用户信息
        Long currentUserId = com.sky.context.BaseContext.getCurrentId();
        Employee currentEmployee = employeeMapper.getById(currentUserId.intValue());
        Long currentUserDeptId = currentEmployee.getDeptId();
        
        log.info("当前用户ID: {}, 部门ID: {}", currentUserId, currentUserDeptId);
        
        // 🔧 判断是否为全局管理员（GMO部门ID=1 或 IT部门ID=10）
        boolean isGlobalAdmin = currentUserDeptId == null || 
                                currentUserDeptId == 1L || 
                                currentUserDeptId == 10L;
        
        if (isGlobalAdmin) {
            log.info("当前用户属于全局管理部门（GMO/IT）或无部门限制，可以查看所有员工");
            return employeeMapper.findAllEmployeesWithDeptInfo();
        }
        
        // 🔧 部门权限隔离：其他部门主管只能查看自己部门的员工
        log.info("应用部门权限过滤，只返回部门ID: {} 的员工", currentUserDeptId);
        return employeeMapper.findEmployeesWithDeptInfoByDeptId(currentUserDeptId);
    }

    /**
     * 永久删除员工（仅限已禁用的员工）
     */
    @Override
    @Transactional
    public void deleteEmp(Long id) {
        log.info("尝试删除员工，ID: {}", id);
        
        // 检查员工是否存在
        Employee employee = employeeMapper.getById(id.intValue());
        if (employee == null) {
            throw new BaseException("员工不存在");
        }
        
        // 检查员工是否已禁用，只能删除已禁用的员工
        if (employee.getStatus() == null || employee.getStatus()) {
            throw new BaseException("只能删除已禁用的员工，请先禁用该员工");
        }
        
        // 如果是导游，同时删除导游表中的记录
        if (employee.getRole() != null && employee.getRole().contains("导游")) {
            Guide guide = guideMapper.getGuideByEmployeeId(employee.getId());
            if (guide != null) {
                log.info("删除导游记录，导游ID: {}", guide.getGuideId());
                guideMapper.deleteGuide(guide.getGuideId());
            }
        }
        
        // 删除员工
        employeeMapper.deleteById(id);
        log.info("成功删除员工，ID: {}, 姓名: {}", id, employee.getName());
    }
}