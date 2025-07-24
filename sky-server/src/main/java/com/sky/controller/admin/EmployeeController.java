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
import com.sky.utils.CookieUtil;
import com.sky.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
// CORS现在由全局CorsFilter处理，移除@CrossOrigin注解
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 管理员登录 - 支持Cookie-only模式，与用户端完全隔离
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO, HttpServletResponse response) {
        log.info("管理员登录：{}", employeeLoginDTO);
        
        try {
            Employee employee = employeeService.login(employeeLoginDTO);

            // 创建管理员JWT令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
            claims.put(JwtClaimsConstant.USERNAME, employee.getUsername());
            claims.put(JwtClaimsConstant.USER_TYPE, "admin");
            claims.put("username", employee.getUsername());
            claims.put("userType", "admin");
            
            String token = JwtUtil.createJWT(
                    jwtProperties.getAdminSecretKey(),
                    jwtProperties.getAdminTtl(),
                    claims);

            // 构建返回结果
            EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                    .id(employee.getId())
                    .userName(employee.getUsername())
                    .name(employee.getName())
                    .token(token)
                    .build();

            // 🔧 新增：设置管理后台专用Cookie，与用户端完全隔离
            // 管理后台Access Token（短期，15分钟）
            CookieUtil.setAdminCookieWithMultiplePaths(response, "adminToken", token, true, 15 * 60);
            
            // 管理后台Refresh Token（长期，8小时）
            Map<String, Object> refreshClaims = new HashMap<>();
            refreshClaims.put(JwtClaimsConstant.EMP_ID, employee.getId());
            refreshClaims.put(JwtClaimsConstant.USERNAME, employee.getUsername());
            refreshClaims.put(JwtClaimsConstant.USER_TYPE, "admin");
            
            String refreshToken = JwtUtil.createRefreshJWT(
                jwtProperties.getAdminSecretKey(),
                8 * 60 * 60 * 1000L, // 8小时
                refreshClaims
            );
            
            CookieUtil.setAdminCookieWithMultiplePaths(response, "adminRefreshToken", refreshToken, true, 8 * 60 * 60);

            // 设置管理后台用户信息Cookie（非HttpOnly，供前端读取）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", employee.getId());
            userInfo.put("username", employee.getUsername());
            userInfo.put("name", employee.getName());
            userInfo.put("userType", "admin");
            userInfo.put("role", "admin");
            userInfo.put("isAuthenticated", true);
            userInfo.put("empId", employee.getId());
            
            String userInfoJson = com.alibaba.fastjson.JSON.toJSONString(userInfo);
            // 使用专门的管理后台Cookie名称
            setAdminUserInfoCookie(response, userInfoJson, 15 * 60);
            
            log.info("✅ 管理员登录成功，已设置Cookie-only认证: empId={}, username={}", 
                    employee.getId(), employee.getUsername());

            return Result.success(employeeLoginVO);
            
        } catch (Exception e) {
            log.error("❌ 管理员登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 设置管理后台专用用户信息Cookie
     */
    private void setAdminUserInfoCookie(HttpServletResponse response, String userInfoJson, int maxAge) {
        try {
            // URL编码防止特殊字符问题
            String encodedUserInfo = java.net.URLEncoder.encode(userInfoJson, "UTF-8");
            
            // 设置管理后台专用Cookie名称，避免与用户端冲突
            String cookiePaths[] = {"/", "/admin"};
            
            for (String path : cookiePaths) {
                // 使用Set-Cookie头部设置（非HttpOnly，前端可读取）
                response.addHeader("Set-Cookie", String.format(
                    "adminUserInfo=%s; Path=%s; Max-Age=%d; SameSite=Lax",
                    encodedUserInfo, path, maxAge));
                
                log.debug("设置管理后台用户信息Cookie: path={}, maxAge={}", path, maxAge);
            }
            
        } catch (Exception e) {
            log.error("设置管理后台用户信息Cookie失败", e);
        }
    }
    
    /**
     * 管理员退出 - 清理管理后台专用Cookie
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletResponse response) {
        log.info("管理员退出登录");
        
        try {
            // 清理管理后台专用Cookie
            clearAdminCookies(response);
            
            log.info("✅ 管理员退出成功，已清理所有Cookie");
            return Result.success("退出成功");
            
        } catch (Exception e) {
            log.error("❌ 管理员退出失败", e);
            // 即使失败也尝试清理Cookie
            clearAdminCookies(response);
            return Result.error("退出失败：" + e.getMessage());
        }
    }
    
    /**
     * 清理管理后台专用Cookie
     */
    private void clearAdminCookies(HttpServletResponse response) {
        // 使用统一的管理后台Cookie清理方法
        CookieUtil.clearAllAdminCookies(response);
        
        log.info("已清理管理后台相关Cookie");
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
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
     * 根据ID查询员工
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
}