package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);
        Employee employee = employeeService.login(employeeLoginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 分页查询（包含车辆信息）
     */
    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用/禁用员工
     */
    @PutMapping("/status/{id}")
    public Result updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status // 接收状态值
    ) {
        employeeService.status(id, status);
        return Result.success();
    }
    /**
     * 新增员工
     */
    @PostMapping
    public Result addEmp(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工：{}", employeeDTO);
        employeeService.addEmp(employeeDTO);
        return Result.success();
    }

    /**
     * 根据ID查询员工（包含车辆信息）
     */
    @GetMapping("/{id}")
    public Result<Employee> getEmpById(@PathVariable Integer id) {
        log.info("查询员工：id={}", id);
        Employee employee = employeeService.getEmp(id);
        return Result.success(employee);
    }

    /**
     * 编辑员工信息
     */
    @PutMapping
    public Result updateEmp(@RequestBody EmployeeDTO employeeDTO) {
        log.info("更新员工：{}", employeeDTO);
        employeeService.updateEmp(employeeDTO);
        return Result.success();
    }


    /**
     * 退出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }
}