package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.BatchPermissionDTO;
import com.sky.dto.PermissionAssignDTO;
import com.sky.entity.PositionPagePermission;
import com.sky.entity.SystemPage;
import com.sky.mapper.*;
import com.sky.service.PermissionManagementService;
import com.sky.vo.PermissionManagementVO;
import com.sky.vo.PositionPermissionVO;
import com.sky.vo.PositionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限管理Service实现类
 */
@Service
@Slf4j
public class PermissionManagementServiceImpl implements PermissionManagementService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private SystemPageMapper systemPageMapper;

    @Autowired
    private PositionPagePermissionMapper positionPagePermissionMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public PermissionManagementVO getPermissionManagementOverview() {
        log.info("获取权限管理概览数据");

        // 获取当前登录用户信息
        Long currentUserId = BaseContext.getCurrentId();
        com.sky.entity.Employee currentEmployee = employeeMapper.getById(currentUserId.intValue());
        Long currentUserDeptId = currentEmployee.getDeptId();
        
        log.info("当前用户ID: {}, 部门ID: {}", currentUserId, currentUserDeptId);

        // 🔧 判断是否为全局管理员（GMO部门ID=1 或 IT部门ID=10）
        boolean isGlobalAdmin = currentUserDeptId == null || 
                                currentUserDeptId == 1L || 
                                currentUserDeptId == 10L;
        
        if (isGlobalAdmin) {
            log.info("当前用户属于全局管理部门（GMO/IT）或无部门限制，拥有全部权限");
        }

        // 获取所有部门和职位
        List<PositionVO> positionDetails = positionMapper.findPositionDetails();
        
        // 🔧 部门权限隔离：只返回当前用户所在部门的职位（GMO和IT除外）
        if (!isGlobalAdmin) {
            positionDetails = positionDetails.stream()
                    .filter(pos -> currentUserDeptId.equals(pos.getDeptId()))
                    .collect(Collectors.toList());
            log.info("应用部门权限过滤，部门ID: {}，职位数量: {}", currentUserDeptId, positionDetails.size());
        }
        
        Map<Long, List<PositionVO>> positionsByDept = positionDetails.stream()
                .collect(Collectors.groupingBy(PositionVO::getDeptId));

        // 构建部门信息 - 🔧 只返回当前用户所在的部门（GMO和IT除外）
        List<PermissionManagementVO.DepartmentInfo> departments = departmentMapper.findAll().stream()
                .filter(dept -> isGlobalAdmin || currentUserDeptId.equals(dept.getId()))
                .map(dept -> {
                    List<PositionVO> deptPositions = positionsByDept.getOrDefault(dept.getId(), new ArrayList<>());
                    List<PermissionManagementVO.PositionInfo> positions = deptPositions.stream()
                            .map(pos -> PermissionManagementVO.PositionInfo.builder()
                                    .id(pos.getId())
                                    .positionCode(pos.getPositionCode())
                                    .positionName(pos.getPositionName())
                                    .positionLevel(pos.getPositionLevel())
                                    .isManagement(pos.getIsManagement() == 1)
                                    .permissionCount(pos.getPermissionCount())
                                    .lastUpdated(pos.getLastPermissionUpdate())
                                    .build())
                            .collect(Collectors.toList());

                    return PermissionManagementVO.DepartmentInfo.builder()
                            .id(dept.getId())
                            .deptCode(dept.getDeptCode())
                            .deptName(dept.getDeptName())
                            .deptLevel(dept.getDeptLevel())
                            .positions(positions)
                            .build();
                })
                .collect(Collectors.toList());

        // 获取页面分组信息
        List<SystemPage> allPages = systemPageMapper.findAll();
        Map<String, List<SystemPage>> pagesByGroup = allPages.stream()
                .collect(Collectors.groupingBy(SystemPage::getPageGroup));

        List<PermissionManagementVO.PageGroup> pageGroups = pagesByGroup.entrySet().stream()
                .map(entry -> {
                    String groupName = entry.getKey();
                    List<SystemPage> pages = entry.getValue();
                    String groupIcon = pages.isEmpty() ? null : pages.get(0).getPageGroupIcon();

                    List<PermissionManagementVO.PageInfo> pageInfos = pages.stream()
                            .map(page -> PermissionManagementVO.PageInfo.builder()
                                    .id(page.getId())
                                    .pagePath(page.getPagePath())
                                    .pageName(page.getPageName())
                                    .permissionLevel(page.getPermissionLevel())
                                    .isRequired(page.getIsRequired() == 1)
                                    .description(page.getDescription())
                                    .build())
                            .collect(Collectors.toList());

                    return PermissionManagementVO.PageGroup.builder()
                            .groupName(groupName)
                            .groupIcon(groupIcon)
                            .pages(pageInfos)
                            .build();
                })
                .collect(Collectors.toList());

        // 构建统计信息
        int totalPositions = positionDetails.size();
        int configuredPositions = (int) positionDetails.stream()
                .filter(pos -> pos.getPermissionCount() != null && pos.getPermissionCount() > 0)
                .count();
        int totalPages = allPages.size();
        
        // 这里简化处理，实际可以查询权限变更日志表
        int todayChanges = 0;

        PermissionManagementVO.Statistics statistics = PermissionManagementVO.Statistics.builder()
                .totalPositions(totalPositions)
                .configuredPositions(configuredPositions)
                .totalPages(totalPages)
                .todayChanges(todayChanges)
                .build();

        return PermissionManagementVO.builder()
                .departments(departments)
                .pageGroups(pageGroups)
                .statistics(statistics)
                .build();
    }

    @Override
    public PositionPermissionVO getPositionPermissions(Long positionId) {
        log.info("获取职位权限配置，职位ID：{}", positionId);

        // 获取职位信息
        PositionVO position = positionMapper.findPositionDetails().stream()
                .filter(pos -> pos.getId().equals(positionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("职位不存在"));

        PositionPermissionVO.PositionInfo positionInfo = PositionPermissionVO.PositionInfo.builder()
                .id(position.getId())
                .positionName(position.getPositionName())
                .deptName(position.getDeptName())
                .positionLevel(position.getPositionLevel())
                .build();

        // 获取权限列表
        List<PositionPermissionVO.PagePermission> permissions = 
                positionPagePermissionMapper.findPositionPagePermissions(positionId);

        return PositionPermissionVO.builder()
                .positionInfo(positionInfo)
                .permissions(permissions)
                .build();
    }

    @Override
    @Transactional
    public void updatePositionPermissions(PermissionAssignDTO permissionAssignDTO) {
        log.info("更新职位权限配置：{}", permissionAssignDTO);

        Long positionId = permissionAssignDTO.getPositionId();
        Long operatorId = BaseContext.getCurrentId();

        // 处理每个权限项
        for (PermissionAssignDTO.PagePermissionItem item : permissionAssignDTO.getPermissions()) {
            Long pageId = item.getPageId();
            Boolean hasPermission = item.getHasPermission();

            if (hasPermission) {
                // 授权：使用upsert确保权限存在
                PositionPagePermission permission = PositionPagePermission.builder()
                        .positionId(positionId)
                        .pageId(pageId)
                        .grantedByEmployeeId(operatorId)
                        .grantedAt(LocalDateTime.now())
                        .status(1)
                        .build();
                positionPagePermissionMapper.upsertPermission(permission);
            } else {
                // 撤权：删除权限
                positionPagePermissionMapper.deleteByPositionAndPage(positionId, pageId);
            }
        }

        log.info("职位权限配置更新完成，职位ID：{}，操作员ID：{}", positionId, operatorId);
    }

    @Override
    @Transactional
    public void batchPermissionOperation(BatchPermissionDTO batchPermissionDTO) {
        log.info("批量权限操作：{}", batchPermissionDTO);

        Long operatorId = BaseContext.getCurrentId();
        String operationType = batchPermissionDTO.getOperationType();

        if ("GRANT".equals(operationType)) {
            // 批量授权
            List<PositionPagePermission> permissions = new ArrayList<>();
            for (Long positionId : batchPermissionDTO.getPositionIds()) {
                for (Long pageId : batchPermissionDTO.getPageIds()) {
                    // 检查是否已存在
                    if (positionPagePermissionMapper.checkPermissionExists(positionId, pageId) == 0) {
                        permissions.add(PositionPagePermission.builder()
                                .positionId(positionId)
                                .pageId(pageId)
                                .grantedByEmployeeId(operatorId)
                                .grantedAt(LocalDateTime.now())
                                .status(1)
                                .build());
                    }
                }
            }
            if (!permissions.isEmpty()) {
                positionPagePermissionMapper.batchInsert(permissions);
            }
        } else if ("REVOKE".equals(operationType)) {
            // 批量撤权
            positionPagePermissionMapper.batchDelete(
                    batchPermissionDTO.getPositionIds(), 
                    batchPermissionDTO.getPageIds()
            );
        } else if ("COPY_TEMPLATE".equals(operationType)) {
            // 复制权限模板
            Long sourcePositionId = batchPermissionDTO.getSourcePositionId();
            for (Long targetPositionId : batchPermissionDTO.getPositionIds()) {
                // 先清空目标职位权限
                positionPagePermissionMapper.deleteByPositionId(targetPositionId);
                // 复制源职位权限
                positionPagePermissionMapper.copyPermissions(sourcePositionId, targetPositionId, operatorId);
            }
        }

        log.info("批量权限操作完成：{}", operationType);
    }

    @Override
    @Transactional
    public void copyPositionPermissions(Long sourcePositionId, List<Long> targetPositionIds, Long operatorId) {
        log.info("复制职位权限，源职位：{}，目标职位：{}，操作员：{}", 
                sourcePositionId, targetPositionIds, operatorId);

        for (Long targetPositionId : targetPositionIds) {
            positionPagePermissionMapper.copyPermissions(sourcePositionId, targetPositionId, operatorId);
        }

        log.info("职位权限复制完成");
    }

    @Override
    public Boolean checkEmployeePagePermission(Long employeeId, String pagePath) {
        log.debug("检查员工页面权限，员工ID：{}，页面：{}", employeeId, pagePath);

        try {
            // 使用Mapper的专门查询方法
            Boolean hasPermission = positionPagePermissionMapper.checkEmployeePagePermission(employeeId, pagePath);
            log.debug("员工权限检查结果：{}，员工ID：{}，页面：{}", hasPermission, employeeId, pagePath);
            return hasPermission != null ? hasPermission : false;
            
        } catch (Exception e) {
            log.error("检查员工页面权限失败，员工ID：{}，页面：{}", employeeId, pagePath, e);
            return false;
        }
    }

    @Override
    public List<String> getEmployeePagePermissions(Long employeeId) {
        log.info("获取员工页面权限列表，员工ID：{}", employeeId);

        try {
            // 使用Mapper的专门查询方法获取员工的所有页面权限
            List<String> pagePaths = positionPagePermissionMapper.findEmployeePagePaths(employeeId);
            log.info("员工权限查询完成，员工ID：{}，权限数量：{}", employeeId, pagePaths.size());
            return pagePaths != null ? pagePaths : new ArrayList<>();
                    
        } catch (Exception e) {
            log.error("获取员工页面权限失败，员工ID：{}", employeeId, e);
            return new ArrayList<>();
        }
    }
}
