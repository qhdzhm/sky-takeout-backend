package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 部门数据传输对象
 */
@Data
public class DepartmentDTO implements Serializable {

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
}

