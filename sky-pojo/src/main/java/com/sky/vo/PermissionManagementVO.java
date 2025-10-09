package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限管理视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionManagementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 部门列表
    private List<DepartmentInfo> departments;

    // 页面分组列表
    private List<PageGroup> pageGroups;

    // 统计信息
    private Statistics statistics;

    /**
     * 部门信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentInfo implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private Long id;
        private String deptCode;
        private String deptName;
        private Integer deptLevel;
        private List<PositionInfo> positions;
    }

    /**
     * 职位信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionInfo implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private Long id;
        private String positionCode;
        private String positionName;
        private Integer positionLevel;
        private Boolean isManagement;
        private Integer permissionCount;
        private LocalDateTime lastUpdated;
    }

    /**
     * 页面分组
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageGroup implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private String groupName;
        private String groupIcon;
        private List<PageInfo> pages;
    }

    /**
     * 页面信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private Long id;
        private String pagePath;
        private String pageName;
        private String permissionLevel;
        private Boolean isRequired;
        private String description;
    }

    /**
     * 统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private Integer totalPositions;
        private Integer configuredPositions;
        private Integer totalPages;
        private Integer todayChanges;
    }
}

