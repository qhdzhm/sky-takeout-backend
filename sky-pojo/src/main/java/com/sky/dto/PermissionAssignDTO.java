package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 权限分配数据传输对象
 */
@Data
public class PermissionAssignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 职位ID
    private Long positionId;

    // 页面权限列表
    private List<PagePermissionItem> permissions;

    // 操作描述
    private String operationDescription;

    /**
     * 页面权限项
     */
    @Data
    public static class PagePermissionItem implements Serializable {
        
        private static final long serialVersionUID = 1L;

        // 页面ID
        private Long pageId;

        // 是否有权限
        private Boolean hasPermission;
    }
}


