package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 职位权限配置视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionPermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 职位信息
    private PositionInfo positionInfo;

    // 权限列表
    private List<PagePermission> permissions;

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
        private String positionName;
        private String deptName;
        private Integer positionLevel;
    }

    /**
     * 页面权限
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagePermission implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private Long pageId;
        private String pagePath;
        private String pageName;
        private String pageGroup;
        private Boolean hasPermission;
        private Boolean isRequired;
        private LocalDateTime grantedAt;
    }
}


