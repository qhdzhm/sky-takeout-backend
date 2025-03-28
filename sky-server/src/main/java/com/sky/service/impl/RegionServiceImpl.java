package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.RegionDTO;
import com.sky.mapper.RegionMapper;
import com.sky.result.PageResult;
import com.sky.service.RegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 地区服务实现类
 */
@Service
@Slf4j
public class RegionServiceImpl implements RegionService {

    @Autowired
    private RegionMapper regionMapper;

    /**
     * 获取所有地区
     * @return 地区列表
     */
    @Override
    public List<RegionDTO> getAllRegions() {
        List<RegionDTO> regions = regionMapper.getAll();
        
        // 计算每个地区的旅游产品数量
        for (RegionDTO region : regions) {
            Integer dayTourCount = regionMapper.countDayTours(region.getId());
            Integer groupTourCount = regionMapper.countGroupTours(region.getId());
            region.setTourCount(dayTourCount + groupTourCount);
        }
        
        return regions;
    }

    /**
     * 根据ID获取地区详情
     * @param id 地区ID
     * @return 地区详情
     */
    @Override
    public RegionDTO getRegionById(Integer id) {
        RegionDTO region = regionMapper.getById(id);
        if (region != null) {
            Integer dayTourCount = regionMapper.countDayTours(region.getId());
            Integer groupTourCount = regionMapper.countGroupTours(region.getId());
            region.setTourCount(dayTourCount + groupTourCount);
        }
        return region;
    }

    /**
     * 获取地区旅游产品
     * @param id 地区ID
     * @param params 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult getRegionTours(Integer id, Map<String, Object> params) {
        // 解析参数
        String name = (String) params.getOrDefault("name", null);
        String title = (String) params.getOrDefault("title", null);
        String category = (String) params.getOrDefault("category", null);
        Double minPrice = params.get("minPrice") != null ? Double.parseDouble(params.get("minPrice").toString()) : null;
        Double maxPrice = params.get("maxPrice") != null ? Double.parseDouble(params.get("maxPrice").toString()) : null;
        String tourType = (String) params.getOrDefault("tourType", "all"); // all, day_tour, group_tour
        
        // 分页参数
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;
        
        // 分页查询
        PageHelper.startPage(page, pageSize);
        Page<Object> tours;
        
        if ("day_tour".equals(tourType)) {
            // 只查询一日游
            tours = (Page<Object>) regionMapper.getDayTours(id, name, category, minPrice, maxPrice);
        } else if ("group_tour".equals(tourType)) {
            // 只查询跟团游
            tours = (Page<Object>) regionMapper.getGroupTours(id, title, category, minPrice, maxPrice);
        } else {
            // 查询所有类型
            // 这里简化处理，实际应该合并两种查询结果
            tours = (Page<Object>) regionMapper.getDayTours(id, name, category, minPrice, maxPrice);
        }
        
        return new PageResult(tours.getTotal(), tours.getResult());
    }
} 