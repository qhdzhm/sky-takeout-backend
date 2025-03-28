package com.sky.service;

import com.sky.dto.RegionDTO;
import com.sky.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 地区服务接口
 */
public interface RegionService {

    /**
     * 获取所有地区
     * @return 地区列表
     */
    List<RegionDTO> getAllRegions();

    /**
     * 根据ID获取地区详情
     * @param id 地区ID
     * @return 地区详情
     */
    RegionDTO getRegionById(Integer id);

    /**
     * 获取地区旅游产品
     * @param id 地区ID
     * @param params 查询参数
     * @return 分页结果
     */
    PageResult getRegionTours(Integer id, Map<String, Object> params);
} 