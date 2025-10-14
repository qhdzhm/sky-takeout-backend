package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量权限操作数据传输对象
 */
@Data
public class BatchPermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 操作类型 GRANT, REVOKE, COPY_TEMPLATE
    private String operationType;

    // 职位ID列表
    private List<Long> positionIds;

    // 页面ID列表
    private List<Long> pageIds;

    // 源职位ID（复制权限时使用）
    private Long sourcePositionId;

    // 操作描述
    private String description;
}


