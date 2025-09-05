package com.sky.aspect;

import com.sky.annotation.RequireOperatorPermission;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.exception.BaseException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.OperatorAssignmentService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 操作员权限控制切面
 */
@Aspect
@Component
@Order(1) // 确保在其他切面之前执行
@Slf4j
public class OperatorPermissionAspect {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private OperatorAssignmentService operatorAssignmentService;

    @Around("@annotation(requireOperatorPermission)")
    public Object checkOperatorPermission(ProceedingJoinPoint joinPoint, RequireOperatorPermission requireOperatorPermission) throws Throwable {
        log.debug("🔒 权限检查开始：{}", requireOperatorPermission.description());

        Long currentEmployeeId = BaseContext.getCurrentId();
        if (currentEmployeeId == null) {
            throw new BaseException("用户未登录");
        }

        // 1. 获取当前员工信息
        Employee currentEmployee = employeeMapper.getById(currentEmployeeId.intValue());
        if (currentEmployee == null) {
            throw new BaseException("员工信息不存在");
        }

        // 2. 检查操作员类型权限
        if (!checkOperatorType(currentEmployee, requireOperatorPermission.operatorType())) {
            throw new BaseException("权限不足：" + requireOperatorPermission.description() + 
                                  "（需要：" + requireOperatorPermission.operatorType() + "）");
        }

        // 3. 检查排团主管权限
        if (requireOperatorPermission.requireTourMaster()) {
            if (!isTourMaster(currentEmployee)) {
                throw new BaseException("权限不足：需要排团主管权限");
            }
        }

        // 4. 检查分配订单权限
        if (requireOperatorPermission.requireAssignPermission()) {
            if (!Boolean.TRUE.equals(currentEmployee.getCanAssignOrders())) {
                throw new BaseException("权限不足：需要订单分配权限");
            }
        }

        // 5. 检查订单操作权限
        if (requireOperatorPermission.checkOrderPermission()) {
            Integer bookingId = getBookingIdFromRequest(joinPoint, requireOperatorPermission.bookingIdParam());
            if (bookingId != null && !operatorAssignmentService.hasPermission(currentEmployeeId, bookingId)) {
                throw new BaseException("权限不足：无法操作该订单");
            }
        }

        log.debug("✅ 权限检查通过：用户ID={}, 操作员类型={}", currentEmployeeId, currentEmployee.getOperatorType());

        // 执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 检查操作员类型
     */
    private boolean checkOperatorType(Employee employee, String requiredType) {
        if ("any".equals(requiredType)) {
            return true;
        }

        String currentType = employee.getOperatorType();
        if (currentType == null) {
            currentType = "general";
        }

        return requiredType.equals(currentType);
    }

    /**
     * 检查是否为排团主管
     */
    private boolean isTourMaster(Employee employee) {
        return "tour_master".equals(employee.getOperatorType()) && 
               Boolean.TRUE.equals(employee.getIsTourMaster());
    }

    /**
     * 从请求中获取订单ID
     */
    private Integer getBookingIdFromRequest(ProceedingJoinPoint joinPoint, String paramName) {
        try {
            // 1. 从方法参数中获取
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Parameter[] parameters = method.getParameters();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < parameters.length; i++) {
                if (paramName.equals(parameters[i].getName()) && args[i] instanceof Integer) {
                    return (Integer) args[i];
                }
            }

            // 2. 从HTTP请求参数中获取
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String bookingIdStr = request.getParameter(paramName);
                if (bookingIdStr != null && !bookingIdStr.isEmpty()) {
                    return Integer.valueOf(bookingIdStr);
                }
                
                // 3. 从路径变量中获取
                String pathVariable = (String) request.getAttribute("org.springframework.web.servlet.View.PATH_VARIABLES");
                if (pathVariable != null) {
                    // 这里需要更复杂的解析逻辑，暂时跳过
                }
            }

            return null;
        } catch (Exception e) {
            log.warn("获取订单ID失败：{}", e.getMessage());
            return null;
        }
    }
}
