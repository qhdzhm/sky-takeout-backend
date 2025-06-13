package com.sky.service;

import com.sky.dto.GroupTourDTO;
import com.sky.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 跟团游服务接口
 */
public interface GroupTourService {

    /**
     * 获取所有跟团游
     * @param params 查询参数
     * @return 分页结果
     */
    PageResult getAllGroupTours(Map<String, Object> params);

    /**
     * 根据ID获取跟团游详情
     * @param id 跟团游ID
     * @return 跟团游详情
     */
    GroupTourDTO getGroupTourById(Integer id);

    /**
     * 更新跟团游信息
     * @param groupTourDTO 跟团游信息
     */
    void updateGroupTour(GroupTourDTO groupTourDTO);

    /**
     * 获取跟团游行程安排
     * @param tourId 跟团游ID
     * @return 行程安排列表
     */
    List<Map<String, Object>> getGroupTourItinerary(Integer tourId);

    /**
     * 获取跟团游可用日期
     * @param tourId 跟团游ID
     * @param params 查询参数
     * @return 可用日期列表
     */
    List<Map<String, Object>> getGroupTourAvailableDates(Integer tourId, Map<String, Object> params);

    /**
     * 获取跟团游主题列表
     * @return 主题列表
     */
    List<Map<String, Object>> getGroupTourThemes();

    /**
     * 根据跟团游ID获取主题列表
     * @param tourId 跟团游ID
     * @return 主题列表
     */
    List<Map<String, Object>> getGroupTourThemesByTourId(Integer tourId);

    /**
     * 获取跟团游亮点
     * @param tourId 跟团游ID
     * @return 亮点列表
     */
    List<String> getGroupTourHighlights(Integer tourId);

    /**
     * 获取跟团游包含项目
     * @param tourId 跟团游ID
     * @return 包含项目列表
     */
    List<String> getGroupTourInclusions(Integer tourId);

    /**
     * 获取跟团游不包含项目
     * @param tourId 跟团游ID
     * @return 不包含项目列表
     */
    List<String> getGroupTourExclusions(Integer tourId);

    /**
     * 获取跟团游常见问题
     * @param tourId 跟团游ID
     * @return 常见问题列表
     */
    List<Map<String, Object>> getGroupTourFaqs(Integer tourId);

    /**
     * 获取跟团游贴士
     * @param tourId 跟团游ID
     * @return 贴士列表
     */
    List<String> getGroupTourTips(Integer tourId);

    /**
     * 获取跟团游图片
     * @param tourId 跟团游ID
     * @return 图片列表
     */
    List<Map<String, Object>> getGroupTourImages(Integer tourId);

    /**
     * 获取团队游关联的一日游
     * @param groupTourId 团队游ID
     * @return 关联的一日游列表
     */
    List<Map<String, Object>> getGroupTourDayTours(Integer groupTourId);

    /**
     * 保存团队游关联的一日游
     * @param groupTourId 团队游ID
     * @param dayTours 关联的一日游数据
     */
    void saveGroupTourDayTours(Integer groupTourId, List<Map<String, Object>> dayTours);

    /**
     * 添加团队游行程安排
     * @param groupTourId 团队游ID
     * @param dayNumber 天数
     * @param title 标题
     * @param description 描述
     * @param meals 餐食
     * @param accommodation 住宿
     */
    void addGroupTourItinerary(Integer groupTourId, Integer dayNumber, String title, String description, String meals, String accommodation);
    
    /**
     * 更新团队游行程安排
     * @param itineraryId 行程ID
     * @param groupTourId 团队游ID
     * @param dayNumber 天数
     * @param title 标题
     * @param description 描述
     * @param meals 餐食
     * @param accommodation 住宿
     */
    void updateGroupTourItinerary(Integer itineraryId, Integer groupTourId, Integer dayNumber, String title, String description, String meals, String accommodation);
    
    /**
     * 删除团队游行程安排
     * @param itineraryId 行程ID
     */
    void deleteGroupTourItinerary(Integer itineraryId);
    
    /**
     * 获取所有可用的一日游
     * @return 一日游列表
     */
    List<Map<String, Object>> getAvailableDayTours();
    
    /**
     * 保存新的团队游
     * @param groupTourDTO 团队游信息
     * @return 新创建的团队游ID
     */
    Integer saveGroupTour(GroupTourDTO groupTourDTO);
    
    /**
     * 删除团队游
     * @param id 团队游ID
     */
    void deleteGroupTour(Integer id);
    
    /**
     * 团队游上架/下架
     * @param status 状态（0-下架，1-上架）
     * @param id 团队游ID
     */
    void enableOrDisableGroupTour(Integer status, Integer id);
    
    /**
     * 更新产品展示图片
     * @param groupTourId 团体游ID
     * @param imageUrl 图片URL
     */
    void updateProductShowcaseImage(Integer groupTourId, String imageUrl);
} 