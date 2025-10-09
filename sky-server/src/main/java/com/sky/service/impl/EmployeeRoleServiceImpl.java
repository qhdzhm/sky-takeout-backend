package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.exception.BaseException;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.OperatorAssignmentMapper;
import com.sky.service.EmployeeRoleService;
import com.sky.vo.OperatorStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工角色职责管理服务实现
 */
@Service
@Slf4j
public class EmployeeRoleServiceImpl implements EmployeeRoleService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private OperatorAssignmentMapper operatorAssignmentMapper;

    /**
     * 更新员工操作员类型
     */
    @Override
    @Transactional
    public void updateOperatorType(Long employeeId, String operatorType) {
        log.info("更新员工操作员类型：employeeId={}, operatorType={}", employeeId, operatorType);
        
        Employee employee = employeeMapper.getById(employeeId.intValue());
        if (employee == null) {
            throw new BaseException("员工不存在");
        }

        // 更新操作员类型
        if ("tour_master".equals(operatorType)) {
            // 设置为排团主管
            Employee updateEmployee = new Employee();
            updateEmployee.setId(employeeId);
            updateEmployee.setOperatorType(operatorType);
            updateEmployee.setIsTourMaster(true);
            updateEmployee.setCanAssignOrders(true);
            updateEmployee.setUpdateTime(LocalDateTime.now());
            updateEmployee.setUpdateUser(BaseContext.getCurrentId());
            
            employeeMapper.update(updateEmployee);
            log.info("✅ 员工操作员类型更新成功：employeeId={}, operatorType={}", employeeId, operatorType);
        } else {
            // 更新操作员类型
            Employee updateEmployee = new Employee();
            updateEmployee.setId(employeeId);
            updateEmployee.setOperatorType(operatorType);
            updateEmployee.setUpdateTime(LocalDateTime.now());
            updateEmployee.setUpdateUser(BaseContext.getCurrentId());
            
            // 如果不是排团主管，则清除排团主管标记
            updateEmployee.setIsTourMaster(false);
            
            // 根据操作员类型设置分配权限
            if ("hotel_operator".equals(operatorType)) {
                updateEmployee.setCanAssignOrders(true); // 酒店操作员可以分配自己的任务
            } else {
                updateEmployee.setCanAssignOrders(false);
            }
            
            employeeMapper.update(updateEmployee);
            log.info("✅ 员工操作员类型更新成功：employeeId={}, operatorType={}", employeeId, operatorType);
        }
    }


    /**
     * 批量更新员工分配权限
     */
    @Override
    @Transactional
    public void batchUpdateAssignPermission(List<Long> employeeIds, Boolean canAssignOrders) {
        log.info("批量更新员工分配权限：employeeIds={}, canAssignOrders={}", employeeIds, canAssignOrders);
        
        for (Long employeeId : employeeIds) {
            Employee updateEmployee = new Employee();
            updateEmployee.setId(employeeId);
            updateEmployee.setCanAssignOrders(canAssignOrders);
            updateEmployee.setUpdateTime(LocalDateTime.now());
            updateEmployee.setUpdateUser(BaseContext.getCurrentId());
            
            employeeMapper.update(updateEmployee);
        }
        
        log.info("✅ 批量更新员工分配权限成功");
    }

    /**
     * 获取操作员统计信息
     */
    @Override
    public List<OperatorStatisticsVO> getOperatorStatistics() {
        log.info("获取操作员统计信息");
        
        // 使用新的基于字符串角色的筛选逻辑
        com.sky.dto.EmployeePageQueryDTO queryDTO = new com.sky.dto.EmployeePageQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setPageSize(200);
        
        com.github.pagehelper.Page<Employee> pageResult = employeeMapper.pageQuery(queryDTO);
        List<Employee> allEmployees = pageResult.getResult();
        
        // 筛选出运营相关员工
        List<Employee> operators = new ArrayList<>();
        for (Employee employee : allEmployees) {
            String role = employee.getRole();
            if (role != null) {
                // 包括所有运营相关员工
                boolean isOperationRelated = 
                    role.contains("Senior Operation") ||
                    role.contains("Operating Manager") ||
                    role.contains("Chief Executive") ||
                    role.contains("FIT Team Leader") ||
                    "hotel_operator".equals(employee.getOperatorType()) ||
                    (role.contains("Operation") && !role.contains("导游"));
                
                // 排除导游和非运营部门员工
                boolean isExcluded = 
                    role.contains("导游") || 
                    role.contains("Guide") ||
                    role.contains("IT Manager") ||
                    role.contains("Marketing Manager") ||
                    role.contains("HR Manager") ||
                    role.contains("Finance Manager") ||
                    role.contains("Legal Manager") ||
                    role.contains("Supply Chain Manager") ||
                    role.contains("Customer Service");
                
                if (isOperationRelated && !isExcluded) {
                    operators.add(employee);
                }
            }
        }
        List<OperatorStatisticsVO> statistics = new ArrayList<>();
        
        for (Employee employee : operators) {
            // 获取统计数据
            Map<String, Object> statsData = getEmployeeStatistics(employee.getId());
            
            OperatorStatisticsVO stat = OperatorStatisticsVO.builder()
                    .employeeId(employee.getId())
                    .employeeName(employee.getName())
                    .operatorType(employee.getOperatorType())
                    .operatorTypeDesc(getOperatorTypeDesc(employee.getOperatorType()))
                    .isTourMaster(employee.getIsTourMaster())
                    .canAssignOrders(employee.getCanAssignOrders())
                    .workStatus(employee.getWorkStatus())
                    .workStatusDesc(getWorkStatusDesc(employee.getWorkStatus()))
                    .assignedOrderCount((Integer) statsData.getOrDefault("assignedCount", 0))
                    .completedOrderCount((Integer) statsData.getOrDefault("completedCount", 0))
                    .pendingOrderCount((Integer) statsData.getOrDefault("pendingCount", 0))
                    .completionRate((Double) statsData.getOrDefault("completionRate", 0.0))
                    .lastAssignedTime((LocalDateTime) statsData.get("lastAssignedTime"))
                    .lastCompletedTime((LocalDateTime) statsData.get("lastCompletedTime"))
                    .workloadLevel(calculateWorkloadLevel((Integer) statsData.getOrDefault("pendingCount", 0)))
                    .maxConcurrentOrders(5) // 默认值
                    .isOnline(employee.getWorkStatus() != null && employee.getWorkStatus() != 2) // 非休假状态视为在线
                    .lastActiveTime(employee.getLastActiveTime())
                    .build();
            
            statistics.add(stat);
        }
        
        return statistics;
    }


    /**
     * 获取酒店操作员列表
     */
    @Override
    public List<Employee> getHotelOperators() {
        log.info("获取酒店操作员列表");
        
        try {
            // 获取所有员工，筛选出酒店操作员
            com.sky.dto.EmployeePageQueryDTO queryDTO = new com.sky.dto.EmployeePageQueryDTO();
            queryDTO.setPage(1);
            queryDTO.setPageSize(200);
            
            com.github.pagehelper.Page<Employee> pageResult = employeeMapper.pageQuery(queryDTO);
            List<Employee> allEmployees = pageResult.getResult();
            List<Employee> hotelOperators = new ArrayList<>();
            
            for (Employee employee : allEmployees) {
                if ("hotel_operator".equals(employee.getOperatorType())) {
                    hotelOperators.add(employee);
                }
            }
            
            log.info("找到 {} 个酒店操作员", hotelOperators.size());
            return hotelOperators;
            
        } catch (Exception e) {
            log.error("获取酒店操作员失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 重置员工职责
     */
    @Override
    @Transactional
    public void resetEmployeeRole(Long employeeId) {
        log.info("重置员工职责：employeeId={}", employeeId);
        
        Employee employee = employeeMapper.getById(employeeId.intValue());
        if (employee == null) {
            throw new BaseException("员工不存在");
        }

        // 如果是排团主管，需要先确认有其他人可以接替
        if (Boolean.TRUE.equals(employee.getIsTourMaster())) {
            throw new BaseException("请先指定新的排团主管，然后再重置该员工职责");
        }

        // 重置为普通员工
        Employee updateEmployee = new Employee();
        updateEmployee.setId(employeeId);
        updateEmployee.setOperatorType("general");
        updateEmployee.setIsTourMaster(false);
        updateEmployee.setCanAssignOrders(false);
        updateEmployee.setUpdateTime(LocalDateTime.now());
        updateEmployee.setUpdateUser(BaseContext.getCurrentId());
        
        employeeMapper.update(updateEmployee);
        
        log.info("✅ 员工职责重置成功：employeeId={}", employeeId);
    }

    /**
     * 获取员工角色变更历史
     */
    @Override
    public List<Object> getRoleChangeHistory(Long employeeId) {
        log.info("获取员工角色变更历史：employeeId={}", employeeId);
        
        // 这里可以实现变更历史记录功能
        // 暂时返回空列表
        return new ArrayList<>();
    }



    // === 私有辅助方法 ===

    /**
     * 获取员工统计数据
     */
    private Map<String, Object> getEmployeeStatistics(Long employeeId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 这里可以调用相应的Mapper方法获取统计数据
            stats.put("assignedCount", operatorAssignmentMapper.countAssignedOrders(employeeId));
            stats.put("completedCount", operatorAssignmentMapper.countCompletedOrders(employeeId));
            stats.put("pendingCount", operatorAssignmentMapper.countPendingOrders(employeeId));
            
            Integer assignedCount = (Integer) stats.get("assignedCount");
            Integer completedCount = (Integer) stats.get("completedCount");
            
            if (assignedCount > 0) {
                double completionRate = (double) completedCount / assignedCount * 100;
                stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
            } else {
                stats.put("completionRate", 0.0);
            }
            
            stats.put("lastAssignedTime", operatorAssignmentMapper.getLastAssignedTime(employeeId));
            stats.put("lastCompletedTime", operatorAssignmentMapper.getLastCompletedTime(employeeId));
            
        } catch (Exception e) {
            log.warn("获取员工统计数据失败：employeeId={}, error={}", employeeId, e.getMessage());
            // 设置默认值
            stats.put("assignedCount", 0);
            stats.put("completedCount", 0);
            stats.put("pendingCount", 0);
            stats.put("completionRate", 0.0);
        }
        
        return stats;
    }

    /**
     * 获取操作员类型描述
     */
    private String getOperatorTypeDesc(String operatorType) {
        if (operatorType == null) {
            return "普通员工";
        }
        
        switch (operatorType) {
            case "tour_master":
                return "排团主管";
            case "hotel_operator":
                return "酒店专员";
            case "general":
            default:
                return "普通员工";
        }
    }

    /**
     * 获取工作状态描述
     */
    private String getWorkStatusDesc(Integer workStatus) {
        if (workStatus == null) {
            return "空闲";
        }
        
        switch (workStatus) {
            case 0:
                return "空闲";
            case 1:
                return "忙碌";
            case 2:
                return "休假";
            case 3:
                return "出团";
            case 4:
                return "待命";
            default:
                return "未知";
        }
    }

    /**
     * 计算工作负荷等级
     */
    private String calculateWorkloadLevel(Integer pendingCount) {
        if (pendingCount == null || pendingCount == 0) {
            return "light";
        } else if (pendingCount <= 3) {
            return "medium";
        } else {
            return "heavy";
        }
    }

}

