package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键
    private Long id;

    // 部门代码
    private String deptCode;

    // 部门名称
    private String deptName;

    // 部门英文名称
    private String deptNameEn;

    // 上级部门ID
    private Long parentDeptId;

    // 部门层级
    private Integer deptLevel;

    // 排序
    private Integer sortOrder;

    // 部门描述
    private String description;

    // 状态 1-启用 0-禁用
    private Integer status;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;
}


