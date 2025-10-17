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
import com.sky.context.BaseContext;
import com.sky.exception.BaseException;
import com.sky.utils.CookieUtil;
import com.sky.vo.EmployeeLoginVO;
import com.sky.vo.TokenRefreshVO;
import com.sky.vo.EmployeeWithDeptVO;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

            // 创建员工JWT令牌（根据实际角色设置userType）
            String actualUserType = getRoleBasedUserType(employee.getRole());
            
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
            claims.put(JwtClaimsConstant.USERNAME, employee.getUsername());
            claims.put(JwtClaimsConstant.USER_TYPE, actualUserType);
            claims.put("username", employee.getUsername());
            claims.put("userType", actualUserType);
            claims.put("roleId", employee.getRole()); // 添加原始角色ID
            
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
            // 管理后台Access Token（4小时）
            CookieUtil.setAdminCookieWithMultiplePaths(response, "adminToken", token, true, 4 * 60 * 60);
            
            // 管理后台Refresh Token（长期，7天）
            Map<String, Object> refreshClaims = new HashMap<>();
            refreshClaims.put(JwtClaimsConstant.EMP_ID, employee.getId());
            refreshClaims.put(JwtClaimsConstant.USERNAME, employee.getUsername());
            refreshClaims.put(JwtClaimsConstant.USER_TYPE, actualUserType);
            
            String refreshToken = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                7 * 24 * 60 * 60 * 1000L, // 7天
                refreshClaims
            );
            
            CookieUtil.setAdminCookieWithMultiplePaths(response, "adminRefreshToken", refreshToken, true, 7 * 24 * 60 * 60);

            // 设置管理后台用户信息Cookie（非HttpOnly，供前端读取）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", employee.getId());
            userInfo.put("username", employee.getUsername());
            userInfo.put("name", employee.getName());
            userInfo.put("userType", actualUserType);
            userInfo.put("role", actualUserType);
            userInfo.put("roleId", employee.getRole()); // 添加原始角色ID
            userInfo.put("isAuthenticated", true);
            userInfo.put("empId", employee.getId());
            
            String userInfoJson = com.alibaba.fastjson.JSON.toJSONString(userInfo);
            // 使用专门的管理后台Cookie名称，4小时与adminToken保持一致
            setAdminUserInfoCookie(response, userInfoJson, 4 * 60 * 60);
            
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
     * 刷新管理员Token - 使用Refresh Token获取新的Access Token
     */
    @PostMapping("/refresh")
    public Result<TokenRefreshVO> refreshAdminToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("开始刷新管理员Token");
        
        try {
            // 从HttpOnly Cookie中获取admin refresh token
            String adminRefreshToken = getAdminRefreshTokenFromCookie(request);
            
            if (adminRefreshToken == null || adminRefreshToken.isEmpty()) {
                log.warn("AdminRefreshToken为空");
                return Result.error("Refresh Token不存在，请重新登录");
            }

            // 验证refresh token
            Claims claims;
            try {
                claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), adminRefreshToken);
                log.debug("AdminRefreshToken验证成功");
            } catch (Exception e) {
                log.warn("AdminRefreshToken无效或已过期: {}", e.getMessage());
                // 清除无效的refresh token cookie
                CookieUtil.clearCookie(response, "adminRefreshToken", "/");
                CookieUtil.clearCookie(response, "adminRefreshToken", "/admin");
                return Result.error("Refresh Token无效或已过期，请重新登录");
            }

            // 从refresh token中提取管理员信息
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            String username = claims.get(JwtClaimsConstant.USERNAME) != null ? 
                             claims.get(JwtClaimsConstant.USERNAME).toString() : null;
            String userType = claims.get(JwtClaimsConstant.USER_TYPE) != null ?
                             claims.get(JwtClaimsConstant.USER_TYPE).toString() : "admin";

            if (empId == null || username == null) {
                log.warn("无法从AdminRefreshToken中提取管理员信息");
                return Result.error("Token信息不完整，请重新登录");
            }

            // 验证管理员是否仍然存在且有效
            Employee employee = employeeService.getEmp(Math.toIntExact(empId));
            if (employee == null) {
                log.warn("管理员不存在: {}", empId);
                CookieUtil.clearCookie(response, "adminRefreshToken", "/");
                CookieUtil.clearCookie(response, "adminRefreshToken", "/admin");
                return Result.error("管理员账户不存在，请重新登录");
            }

            // 生成新的access token
            Map<String, Object> accessClaims = new HashMap<>();
            accessClaims.put(JwtClaimsConstant.EMP_ID, empId);
            accessClaims.put(JwtClaimsConstant.USERNAME, username);
            accessClaims.put(JwtClaimsConstant.USER_TYPE, userType);
            accessClaims.put("username", username);
            accessClaims.put("userType", userType);
            accessClaims.put("roleId", employee.getRole());

            String newAccessToken = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(), // 4小时
                accessClaims
            );

            // 检查refresh token是否需要更新（如果剩余时间少于1小时）
            long currentTime = System.currentTimeMillis() / 1000;
            long refreshExp = claims.getExpiration().getTime() / 1000;
            long refreshTimeUntilExpiry = refreshExp - currentTime;
            
            boolean refreshTokenUpdated = false;
            String newRefreshToken = adminRefreshToken;

            if (refreshTimeUntilExpiry < 1 * 60 * 60) { // 如果refresh token在1小时内过期
                Map<String, Object> refreshClaims = new HashMap<>();
                refreshClaims.put(JwtClaimsConstant.EMP_ID, empId);
                refreshClaims.put(JwtClaimsConstant.USERNAME, username);
                refreshClaims.put(JwtClaimsConstant.USER_TYPE, userType);

                newRefreshToken = JwtUtil.createJWT(
                    jwtProperties.getAdminSecretKey(),
                    7 * 24 * 60 * 60 * 1000L, // 7天
                    refreshClaims
                );

                // 设置新的refresh token cookie
                CookieUtil.setAdminCookieWithMultiplePaths(response, "adminRefreshToken", newRefreshToken, true, 7 * 24 * 60 * 60);
                
                refreshTokenUpdated = true;
                log.info("AdminRefreshToken已更新，管理员: {}", username);
            }

            // 设置新的access token cookie（4小时有效期）
            CookieUtil.setAdminCookieWithMultiplePaths(response, "adminToken", newAccessToken, true, 4 * 60 * 60);

            // 更新管理后台用户信息cookie
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", employee.getId());
            userInfo.put("username", employee.getUsername());
            userInfo.put("name", employee.getName());
            userInfo.put("userType", getRoleBasedUserType(employee.getRole()));
            userInfo.put("role", getRoleBasedUserType(employee.getRole()));
            userInfo.put("roleId", employee.getRole());
            userInfo.put("isAuthenticated", true);
            userInfo.put("empId", employee.getId());
            
            String userInfoJson = com.alibaba.fastjson.JSON.toJSONString(userInfo);
            setAdminUserInfoCookie(response, userInfoJson, 4 * 60 * 60); // 4小时，与adminToken同步

            // 构建响应
            TokenRefreshVO tokenRefreshVO = TokenRefreshVO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshTokenUpdated ? newRefreshToken : null) // 只有更新时才返回
                    .userId(empId)
                    .username(username)
                    .userType(userType)
                    .accessTokenExpiry(System.currentTimeMillis() + jwtProperties.getAdminTtl())
                    .refreshTokenExpiry(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)) // 7天
                    .refreshTokenUpdated(refreshTokenUpdated)
                    .build();

            log.info("管理员Token刷新成功，用户: {}, RefreshToken更新: {}", username, refreshTokenUpdated);
            return Result.success(tokenRefreshVO);

        } catch (Exception e) {
            log.error("管理员Token刷新失败", e);
            // 清除可能有问题的cookies
            CookieUtil.clearCookie(response, "adminRefreshToken", "/");
            CookieUtil.clearCookie(response, "adminRefreshToken", "/admin");
            CookieUtil.clearCookie(response, "adminToken", "/");
            CookieUtil.clearCookie(response, "adminToken", "/admin");
            return Result.error("Token刷新失败：" + e.getMessage());
        }
    }
    
    /**
     * 从管理后台专用Cookie中获取refresh token
     */
    private String getAdminRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            String[] adminRefreshTokenNames = {"adminRefreshToken", "admin_refresh_token"};

            for (String cookieName : adminRefreshTokenNames) {
                for (javax.servlet.http.Cookie cookie : request.getCookies()) {
                    if (cookieName.equals(cookie.getName())) {
                        String tokenValue = cookie.getValue();
                        if (tokenValue != null && !tokenValue.trim().isEmpty() && 
                            !"null".equals(tokenValue) && !"undefined".equals(tokenValue)) {
                            log.debug("从管理后台Cookie中找到RefreshToken: {}", cookieName);
                            return tokenValue;
                        }
                    }
                }
            }
        }

        log.debug("未从管理后台Cookie中找到RefreshToken");
        return null;
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
        
        // 获取当前登录的员工信息
        Long currentEmployeeId = BaseContext.getCurrentId();
        Employee currentEmployee = employeeService.getEmp(Math.toIntExact(currentEmployeeId));
        
        // 根据当前用户的角色和部门权限过滤员工数据
        PageResult pageResult = employeeService.pageQueryWithDepartmentPermission(employeePageQueryDTO, currentEmployee);
        return Result.success(pageResult);
    }

    /**
     * 获取所有员工详细信息（包含部门职位信息）
     */
    @GetMapping("/with-dept-position")
    public Result<java.util.List<EmployeeWithDeptVO>> getEmployeesWithDeptInfo() {
        log.info("获取员工详细信息（包含部门职位）");
        java.util.List<EmployeeWithDeptVO> employees = employeeService.getAllEmployeesWithDeptInfo();
        return Result.success(employees);
    }

    /**
     * 新增员工
     */
    @PostMapping
    public Result<String> addEmp(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工：{}", employeeDTO);
        
        // 获取当前登录的员工信息
        Long currentEmployeeId = BaseContext.getCurrentId();
        Employee currentEmployee = employeeService.getEmp(Math.toIntExact(currentEmployeeId));
        
        // 根据当前用户的权限验证是否可以创建员工
        validateAddEmployeePermission(currentEmployee, employeeDTO);
        
        employeeService.addEmp(employeeDTO);
        return Result.success();
    }

    /**
     * 验证添加员工的权限
     */
    private void validateAddEmployeePermission(Employee currentEmployee, EmployeeDTO employeeDTO) {
        String currentUserRole = currentEmployee.getRole();
        Long currentUserDeptId = currentEmployee.getDeptId();
        
        // 🔧 GMO（ID=1）和IT部门（ID=10）拥有全局管理权限
        boolean isGlobalAdmin = currentUserDeptId == null || 
                                currentUserDeptId == 1L || 
                                currentUserDeptId == 10L;
        
        if (isGlobalAdmin) {
            log.info("用户{}属于全局管理部门（GMO/IT），可创建任何部门员工", currentEmployee.getName());
            return;
        }
        
        // Full权限用户可以创建任何部门的员工
        if (hasFullPermission(currentUserRole)) {
            log.info("用户{}具有Full权限，可创建任何部门员工", currentEmployee.getName());
            return;
        }
        
        // 部门Manager只能创建自己部门的员工
        if (isDepartmentManager(currentUserRole)) {
            if (employeeDTO.getDeptId() == null || !employeeDTO.getDeptId().equals(currentUserDeptId)) {
                throw new BaseException("权限不足：只能在自己的部门(" + getDepartmentName(currentUserDeptId) + ")中创建员工");
            }
            log.info("用户{}在自己的部门中创建员工", currentEmployee.getName());
            return;
        }
        
        // 普通员工无权限创建员工
        throw new BaseException("权限不足：您无权限创建员工");
    }

    /**
     * 检查是否有完全权限
     */
    private boolean hasFullPermission(String role) {
        if (role == null) return false;
        return role.contains("IT Manager") || 
               role.contains("Chief Executive") ||
               role.contains("Chief");
    }

    /**
     * 检查是否是部门Manager
     */
    private boolean isDepartmentManager(String role) {
        if (role == null) return false;
        return role.contains("Manager") || 
               role.contains("经理") ||
               role.contains("主管");
    }

    /**
     * 获取部门名称（用于错误提示）
     */
    private String getDepartmentName(Long deptId) {
        if (deptId == null) return "未知部门";
        // 这里可以添加部门查询逻辑，暂时返回部门ID
        return "部门ID:" + deptId;
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
    public Result<String> updateEmp(@RequestBody EmployeeDTO employeeDTO) {
        log.info("更新员工：{}", employeeDTO);
        employeeService.updateEmp(employeeDTO);
        return Result.success();
    }

    /**
     * 获取当前登录管理员的信息
     */
    @GetMapping("/current")
    public Result<Employee> getCurrentAdmin() {
        try {
            // 从当前上下文获取员工ID
            Long empId = com.sky.context.BaseContext.getCurrentId();
            if (empId == null) {
                return Result.error("未获取到当前用户信息");
            }
            
            Employee employee = employeeService.getEmp(Math.toIntExact(empId));
            if (employee == null) {
                return Result.error("用户不存在");
            }
            
            log.info("获取当前管理员信息: empId={}, name={}", empId, employee.getName());
            return Result.success(employee);
            
        } catch (Exception e) {
            log.error("❌ 获取当前管理员信息失败", e);
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新当前登录管理员的个人信息
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody EmployeeDTO employeeDTO, HttpServletResponse response) {
        log.info("更新管理员个人信息：{}", employeeDTO);
        
        try {
            // 从当前上下文获取员工ID
            Long empId = com.sky.context.BaseContext.getCurrentId();
            if (empId == null) {
                return Result.error("未获取到当前用户信息");
            }
            
            // 🔧 如果用户名为空，从数据库获取当前用户名（因为前端字段是disabled的）
            if (employeeDTO.getUsername() == null || employeeDTO.getUsername().trim().isEmpty()) {
                Employee currentEmployee = employeeService.getEmp(Math.toIntExact(empId));
                if (currentEmployee != null) {
                    employeeDTO.setUsername(currentEmployee.getUsername());
                    log.info("🔧 自动填充用户名: {}", currentEmployee.getUsername());
                }
            }
            
            
            log.info("🔍 更新前调试信息: empId={}, DTO.username={}, DTO.email={}, DTO.avatar={}", 
                    empId, employeeDTO.getUsername(), employeeDTO.getEmail(), employeeDTO.getAvatar());
            
            // 设置员工ID，确保只能更新自己的信息
            employeeDTO.setId(empId);
            
            // 设置更新时间和更新人
            employeeDTO.setUpdateTime(java.time.LocalDateTime.now());
            employeeDTO.setUpdateUser(String.valueOf(empId));
            
            // 更新员工信息
            employeeService.updateEmp(employeeDTO);
            
            // 获取更新后的员工信息
            Employee updatedEmployee = employeeService.getEmp(Math.toIntExact(empId));
            
            // 更新Cookie中的用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", updatedEmployee.getId());
            userInfo.put("username", updatedEmployee.getUsername());
            userInfo.put("name", updatedEmployee.getName());
            userInfo.put("email", updatedEmployee.getEmail());
            userInfo.put("phone", updatedEmployee.getPhone());
            userInfo.put("avatar", updatedEmployee.getAvatar());
            userInfo.put("userType", getRoleBasedUserType(updatedEmployee.getRole()));
            userInfo.put("role", getRoleBasedUserType(updatedEmployee.getRole()));
            userInfo.put("roleId", updatedEmployee.getRole());
            userInfo.put("lastLoginTime", updatedEmployee.getLastLoginTime());
            userInfo.put("isAuthenticated", true);
            userInfo.put("empId", updatedEmployee.getId());
            
            log.info("🔍 更新后调试信息: name={}, email={}, avatar={}, role={}, lastLoginTime={}", 
                    updatedEmployee.getName(), updatedEmployee.getEmail(), updatedEmployee.getAvatar(), 
                    updatedEmployee.getRole(), updatedEmployee.getLastLoginTime());
            
            String userInfoJson = com.alibaba.fastjson.JSON.toJSONString(userInfo);
            setAdminUserInfoCookie(response, userInfoJson, 2 * 60 * 60);
            
            log.info("✅ 管理员个人信息更新成功: empId={}, name={}", empId, updatedEmployee.getName());
            return Result.success("个人信息更新成功");
            
        } catch (Exception e) {
            log.error("❌ 更新管理员个人信息失败", e);
            return Result.error("更新个人信息失败：" + e.getMessage());
        }
    }

    /**
     * 🔒 根据员工角色获取对应的用户类型（String版本）
     * @param role 角色名称：如"A级导游"、"Operating Manager"等
     * @return 用户类型字符串
     */
    private String getRoleBasedUserType(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "admin"; // 默认管理员
        }
        
        // 根据角色名称判断用户类型
        if (role.contains("导游")) {
            return "guide";     // 导游
        } else if (role.contains("Manager") || role.contains("经理") || role.contains("Chief")) {
            return "admin";     // 管理员
        } else if (role.contains("Operation") || role.contains("运营") || role.contains("Leader")) {
            return "operator";  // 操作员
        } else if (role.contains("Service") || role.contains("客服")) {
            return "service";   // 客服
        } else {
            log.warn("未知的员工角色: {}, 默认设置为admin", role);
            return "admin";
        }
    }
    
    /**
     * 🔒 根据员工角色获取对应的用户类型（Integer版本，保持向下兼容）
     * @param roleId 角色ID：0-导游，1-操作员，2-管理员，3-客服
     * @return 用户类型字符串
     */
    private String getRoleBasedUserType(Integer roleId) {
        if (roleId == null) {
            return "admin"; // 默认管理员
        }
        
        switch (roleId) {
            case 0:
                return "guide";     // 导游
            case 1:
                return "operator";  // 操作员  
            case 2:
                return "admin";     // 管理员
            case 3:
                return "service";   // 客服
            default:
                log.warn("未知的员工角色ID: {}, 默认设置为admin", roleId);
                return "admin";
        }
    }

    /**
     * 启用/禁用员工状态
     */
    @PutMapping("/status/{id}")
    public Result<String> enableOrDisable(@PathVariable Integer id, @RequestParam Integer status) {
        log.info("启用/禁用员工：id={}, status={}", id, status);
        try {
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setId(Long.valueOf(id));
            employeeDTO.setStatus(status);
            
            // 设置更新时间和更新人
            employeeDTO.setUpdateTime(java.time.LocalDateTime.now());
            Long currentUserId = com.sky.context.BaseContext.getCurrentId();
            employeeDTO.setUpdateUser(String.valueOf(currentUserId));
            
            log.info("🔍 准备更新员工状态: employeeDTO={}", employeeDTO);
            employeeService.updateEmp(employeeDTO);
            log.info("✅ 员工状态更新成功: id={}, status={}", id, status);
            return Result.success(status == 1 ? "启用员工成功" : "禁用员工成功");
        } catch (Exception e) {
            log.error("❌ 更新员工状态失败", e);
            return Result.error("更新员工状态失败：" + e.getMessage());
        }
    }

    /**
     * 永久删除员工（仅限已禁用的员工）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteEmployee(@PathVariable Long id) {
        log.info("删除员工：id={}", id);
        try {
            employeeService.deleteEmp(id);
            return Result.success("删除员工成功");
        } catch (BaseException e) {
            log.error("❌ 删除员工失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("❌ 删除员工失败", e);
            return Result.error("删除员工失败：" + e.getMessage());
        }
    }

}