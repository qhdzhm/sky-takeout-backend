package com.sky.service;

import com.sky.dto.GuidePageQueryDTO;
import com.sky.entity.Guide;
import com.sky.result.PageResult;

/**
 * 导游服务接口
 */
public interface GuideService {

    /**
     * 导游分页查询
     * @param guidePageQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(GuidePageQueryDTO guidePageQueryDTO);

    /**
     * 通过员工ID获取导游信息
     * @param employeeId 员工ID
     * @return 导游信息
     */
    Guide getGuideByEmployeeId(Long employeeId);

    /**
     * 修复导游和员工的关联关系
     */
    void fixEmployeeRelation();

    /**
     * 同步导游表数据到员工表
     */
    void syncGuidesToEmployees();
} 