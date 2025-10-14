package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位页面权限关联实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionPagePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键
    private Long id;

    // 职位ID
    private Long positionId;

    // 页面ID
    private Long pageId;

    // 授权人员工ID
    private Long grantedByEmployeeId;

    // 授权时间
    private LocalDateTime grantedAt;

    // 权限状态 1-有效 0-撤销
    private Integer status;
}


