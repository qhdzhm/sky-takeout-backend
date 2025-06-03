package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.TourDTO;
import com.sky.mapper.TourMapper;
import com.sky.result.PageResult;
import com.sky.service.TourService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 通用旅游服务实现类
 */
@Service
@Slf4j
public class TourServiceImpl implements TourService {

    @Autowired
    private TourMapper tourMapper;

    /**
     * 获取所有旅游产品
     * @param params 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult getAllTours(Map<String, Object> params) {
        // 解析参数
        String keyword = (String) params.getOrDefault("keyword", null);
        String location = (String) params.getOrDefault("location", null);
        String category = (String) params.getOrDefault("category", null);
        Integer regionId = params.get("regionId") != null ? Integer.parseInt(params.get("regionId").toString()) : null;
        Double minPrice = params.get("minPrice") != null ? Double.parseDouble(params.get("minPrice").toString()) : null;
        Double maxPrice = params.get("maxPrice") != null ? Double.parseDouble(params.get("maxPrice").toString()) : null;
        String tourType = (String) params.getOrDefault("tourType", "all"); // all, day_tour, group_tour
        
        // 分页参数
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 50;
        
        // 分页查询
        PageHelper.startPage(page, pageSize);
        Page<TourDTO> tours = (Page<TourDTO>) tourMapper.pageQuery(keyword, location, category, regionId, minPrice, maxPrice, tourType);
        
        return new PageResult(tours.getTotal(), tours.getResult());
    }

    /**
     * 根据ID获取旅游产品
     * @param id 旅游产品ID
     * @return 旅游产品信息
     */
    @Override
    public TourDTO getTourById(Integer id) {
        // 默认查询所有类型
        return tourMapper.getById(id, null);
    }

    /**
     * 根据ID和类型获取旅游产品
     * @param id 旅游产品ID
     * @param tourType 旅游类型（day或group）
     * @return 旅游产品信息
     */
    @Override
    public TourDTO getTourById(Integer id, String tourType) {
        // 调用mapper获取指定类型的旅游产品
        return tourMapper.getById(id, tourType);
    }

    /**
     * 搜索旅游产品
     * @param params 查询参数
     * @return 旅游产品列表
     */
    @Override
    public List<TourDTO> searchTours(Map<String, Object> params) {
        // 解析参数
        String keyword = (String) params.getOrDefault("keyword", null);
        String location = (String) params.getOrDefault("location", null);
        String category = (String) params.getOrDefault("category", null);
        Double minPrice = params.get("minPrice") != null ? Double.parseDouble(params.get("minPrice").toString()) : null;
        Double maxPrice = params.get("maxPrice") != null ? Double.parseDouble(params.get("maxPrice").toString()) : null;
        String tourType = (String) params.getOrDefault("tourType", "all"); // all, day_tour, group_tour
        
        return tourMapper.search(keyword, location, category, minPrice, maxPrice, tourType);
    }

    /**
     * 获取热门旅游产品
     * @param limit 限制数量
     * @return 热门旅游产品列表
     */
    @Override
    public List<TourDTO> getHotTours(Integer limit) {
        // 合并一日游和跟团游的热门产品
        List<TourDTO> dayTours = tourMapper.getHotDayTours(limit / 2);
        List<TourDTO> groupTours = tourMapper.getHotGroupTours(limit / 2);
        
        dayTours.addAll(groupTours);
        return dayTours.subList(0, Math.min(limit, dayTours.size()));
    }

    /**
     * 获取推荐旅游产品
     * @param limit 限制数量
     * @return 推荐旅游产品列表
     */
    @Override
    public List<TourDTO> getRecommendedTours(Integer limit) {
        // 合并一日游和跟团游的推荐产品
        List<TourDTO> dayTours = tourMapper.getRecommendedDayTours(limit / 2);
        List<TourDTO> groupTours = tourMapper.getRecommendedGroupTours(limit / 2);
        
        dayTours.addAll(groupTours);
        return dayTours.subList(0, Math.min(limit, dayTours.size()));
    }

    /**
     * 获取热门一日游
     * @param limit 限制数量
     * @return 热门一日游列表
     */
    @Override
    public List<TourDTO> getHotDayTours(Integer limit) {
        return tourMapper.getHotDayTours(limit);
    }

    /**
     * 获取热门跟团游
     * @param limit 限制数量
     * @return 热门跟团游列表
     */
    @Override
    public List<TourDTO> getHotGroupTours(Integer limit) {
        return tourMapper.getHotGroupTours(limit);
    }

    /**
     * 获取推荐一日游
     * @param limit 限制数量
     * @return 推荐一日游列表
     */
    @Override
    public List<TourDTO> getRecommendedDayTours(Integer limit) {
        return tourMapper.getRecommendedDayTours(limit);
    }

    /**
     * 获取推荐跟团游
     * @param limit 限制数量
     * @return 推荐跟团游列表
     */
    @Override
    public List<TourDTO> getRecommendedGroupTours(Integer limit) {
        return tourMapper.getRecommendedGroupTours(limit);
    }

    /**
     * 获取适合人群选项
     * @return 适合人群选项列表
     */
    @Override
    public List<Map<String, Object>> getSuitableForOptions() {
        log.info("获取适合人群选项");
        return tourMapper.getSuitableOptions();
    }

    /**
     * 根据旅游ID和类型获取适合人群
     * @param tourId 旅游ID
     * @param tourType 旅游类型 (day_tour/group_tour)
     * @return 适合人群列表
     */
    @Override
    public List<Map<String, Object>> getSuitableForByTourId(Integer tourId, String tourType) {
        return tourMapper.getSuitableForByTourId(tourId, tourType);
    }
} 