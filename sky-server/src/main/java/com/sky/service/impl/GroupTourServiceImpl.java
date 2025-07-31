package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.GroupTourDTO;
import com.sky.mapper.GroupTourMapper;
import com.sky.mapper.TourMapper;
import com.sky.result.PageResult;
import com.sky.service.GroupTourService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 跟团游服务实现类
 */
@Service
@Slf4j
public class GroupTourServiceImpl implements GroupTourService {

    @Autowired
    private GroupTourMapper groupTourMapper;
    
    @Autowired
    private TourMapper tourMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取所有跟团游
     * @param params 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult getAllGroupTours(Map<String, Object> params) {
        // 解析参数
        String title = (String) params.getOrDefault("title", null);
        String location = (String) params.getOrDefault("location", null);
        String category = (String) params.getOrDefault("category", null);
        Double minPrice = params.get("minPrice") != null ? Double.parseDouble(params.get("minPrice").toString()) : null;
        Double maxPrice = params.get("maxPrice") != null ? Double.parseDouble(params.get("maxPrice").toString()) : null;
        Integer minDays = params.get("minDays") != null ? Integer.parseInt(params.get("minDays").toString()) : null;
        Integer maxDays = params.get("maxDays") != null ? Integer.parseInt(params.get("maxDays").toString()) : null;
        
        // 分页参数
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 50;
        
        // 分页查询
        PageHelper.startPage(page, pageSize);
        Page<GroupTourDTO> groupTours = (Page<GroupTourDTO>) groupTourMapper.pageQuery(title, location, category, minPrice, maxPrice, minDays, maxDays);
        
        // 处理主题和适合人群
        for (GroupTourDTO groupTour : groupTours) {
            // 查询主题
            List<Map<String, Object>> themeInfoList = groupTourMapper.getThemesByTourId(groupTour.getId());
            List<String> themes = new ArrayList<>();
            if (themeInfoList != null && !themeInfoList.isEmpty()) {
                for (Map<String, Object> themeInfo : themeInfoList) {
                    themes.add((String) themeInfo.get("name"));
                }
            }
            groupTour.setThemes(themes);
            
            // 查询适合人群
            List<Map<String, Object>> suitableForList = tourMapper.getSuitableForByTourId(groupTour.getId(), "group_tour");
            List<String> suitableFor = new ArrayList<>();
            if (suitableForList != null && !suitableForList.isEmpty()) {
                for (Map<String, Object> suitable : suitableForList) {
                    suitableFor.add((String) suitable.get("name"));
                }
            }
            groupTour.setSuitableFor(suitableFor);
        }
        
        return new PageResult(groupTours.getTotal(), groupTours.getResult());
    }

    /**
     * 根据ID获取跟团游详情
     * @param id 跟团游ID
     * @return 跟团游详情
     */
    @Override
    public GroupTourDTO getGroupTourById(Integer id) {
        GroupTourDTO groupTour = groupTourMapper.getById(id);
        if (groupTour != null) {
            // 查询主题信息
            List<Map<String, Object>> themeInfoList = groupTourMapper.getThemesByTourId(id);
            List<String> themes = new ArrayList<>();
            List<Integer> themeIds = new ArrayList<>();
            if (themeInfoList != null && !themeInfoList.isEmpty()) {
                for (Map<String, Object> themeInfo : themeInfoList) {
                    themes.add((String) themeInfo.get("name"));
                    // 添加主题ID
                    themeIds.add((Integer) themeInfo.get("id"));
                }
            }
            groupTour.setThemes(themes);
            groupTour.setThemeIds(themeIds); // 设置主题ID列表
            
            // 查询适合人群信息
            List<Map<String, Object>> suitableForList = tourMapper.getSuitableForByTourId(id, "group_tour");
            List<String> suitableFor = new ArrayList<>();
            List<Integer> suitableIds = new ArrayList<>();
            if (suitableForList != null && !suitableForList.isEmpty()) {
                for (Map<String, Object> suitable : suitableForList) {
                    suitableFor.add((String) suitable.get("name"));
                    // 添加适合人群ID
                    suitableIds.add((Integer) suitable.get("id"));
                }
            }
            groupTour.setSuitableFor(suitableFor);
            groupTour.setSuitableIds(suitableIds); // 设置适合人群ID列表
            
            // 查询亮点
            List<String> highlights = groupTourMapper.getHighlights(id);
            groupTour.setHighlights(highlights);
            
            // 查询包含项目
            List<String> inclusions = groupTourMapper.getInclusions(id);
            groupTour.setInclusions(inclusions);
            
            // 查询不包含项目
            List<String> exclusions = groupTourMapper.getExclusions(id);
            groupTour.setExclusions(exclusions);
            
            // 查询贴士
            List<String> tips = groupTourMapper.getTips(id);
            groupTour.setTips(tips);
            
            // 查询常见问题
            List<Map<String, Object>> faqs = groupTourMapper.getFaqs(id);
            groupTour.setFaqs(faqs);
            
            // 查询图片
            List<Map<String, Object>> images = groupTourMapper.getImages(id);
            groupTour.setImages(images);
            
            // 查询行程
            List<Map<String, Object>> itinerary = groupTourMapper.getItinerary(id);
            groupTour.setItinerary(itinerary);
            
            // 确保没有null值，以避免前端显示问题
            if (groupTour.getHighlights() == null) groupTour.setHighlights(new ArrayList<>());
            if (groupTour.getInclusions() == null) groupTour.setInclusions(new ArrayList<>());
            if (groupTour.getExclusions() == null) groupTour.setExclusions(new ArrayList<>());
            if (groupTour.getTips() == null) groupTour.setTips(new ArrayList<>());
            if (groupTour.getFaqs() == null) groupTour.setFaqs(new ArrayList<>());
            if (groupTour.getImages() == null) groupTour.setImages(new ArrayList<>());
            if (groupTour.getItinerary() == null) groupTour.setItinerary(new ArrayList<>());
        }
        return groupTour;
    }

    /**
     * 获取跟团游行程安排
     * @param tourId 跟团游ID
     * @return 行程安排列表
     */
    @Override
    public List<Map<String, Object>> getGroupTourItinerary(Integer tourId) {
        log.info("获取跟团游行程安排，ID：{}", tourId);
        
        // 1. 优先获取关联的一日游数据
        List<Map<String, Object>> dayTourRelations = getGroupTourDayTours(tourId);
        
        if (dayTourRelations != null && !dayTourRelations.isEmpty()) {
            log.info("使用关联一日游生成行程，关联数量：{}", dayTourRelations.size());
            
            // 按天数分组
            Map<Integer, List<Map<String, Object>>> dayToursMap = new HashMap<>();
            for (Map<String, Object> relation : dayTourRelations) {
                Integer dayNumber = (Integer) relation.get("day_number");
                dayToursMap.computeIfAbsent(dayNumber, k -> new ArrayList<>()).add(relation);
            }
            
            // 生成行程列表
            List<Map<String, Object>> itinerary = new ArrayList<>();
            for (Map.Entry<Integer, List<Map<String, Object>>> entry : dayToursMap.entrySet()) {
                Integer dayNumber = entry.getKey();
                List<Map<String, Object>> dayTours = entry.getValue();
                
                Map<String, Object> dayItinerary = new HashMap<>();
                dayItinerary.put("day", dayNumber);
                dayItinerary.put("day_number", dayNumber);
                
                if (dayTours.size() == 1) {
                    // 单个一日游
                    Map<String, Object> dayTour = dayTours.get(0);
                    dayItinerary.put("title", "第" + dayNumber + "天: " + dayTour.get("day_tour_name"));
                    dayItinerary.put("description", dayTour.get("day_tour_description"));
                    dayItinerary.put("location", dayTour.get("location"));
                    dayItinerary.put("is_optional", dayTour.get("is_optional"));
                } else {
                    // 多个一日游（可选）
                    Map<String, Object> mainTour = dayTours.get(0);
                    int otherCount = dayTours.size() - 1;
                    
                    dayItinerary.put("title", "第" + dayNumber + "天: " + mainTour.get("day_tour_name") + 
                                           " (含" + otherCount + "个其他可选项目)");
                    
                    // 构建描述，包含所有选项
                    StringBuilder description = new StringBuilder();
                    description.append("🎯 主要选项：").append(mainTour.get("day_tour_description")).append("\n\n");
                    description.append("🔄 其他可选项目：\n");
                    
                    for (int i = 1; i < dayTours.size(); i++) {
                        Map<String, Object> tour = dayTours.get(i);
                        description.append("• ").append(tour.get("day_tour_name")).append("\n");
                    }
                    
                    dayItinerary.put("description", description.toString());
                    dayItinerary.put("location", mainTour.get("location"));
                    dayItinerary.put("is_optional", true);
                    dayItinerary.put("optional_tours", dayTours); // 包含所有可选项目
                }
                
                // 默认餐食和住宿
                dayItinerary.put("meals", "早餐");
                dayItinerary.put("accommodation", "酒店");
                
                itinerary.add(dayItinerary);
            }
            
            // 按天数排序
            itinerary.sort((a, b) -> Integer.compare((Integer) a.get("day_number"), (Integer) b.get("day_number")));
            
            log.info("基于关联一日游生成行程完成，天数：{}", itinerary.size());
            return itinerary;
        }
        
        // 2. 回退到标准行程
        log.info("没有关联一日游，使用标准行程");
        List<Map<String, Object>> standardItinerary = groupTourMapper.getItinerary(tourId);
        
        if (standardItinerary != null && !standardItinerary.isEmpty()) {
            log.info("使用标准行程，天数：{}", standardItinerary.size());
            return standardItinerary;
        }
        
        // 3. 如果都没有，返回空列表
        log.warn("跟团游{}没有任何行程数据", tourId);
        return new ArrayList<>();
    }

    /**
     * 获取跟团游可用日期
     * @param tourId 跟团游ID
     * @param params 查询参数
     * @return 可用日期列表
     */
    @Override
    public List<Map<String, Object>> getGroupTourAvailableDates(Integer tourId, Map<String, Object> params) {
        String startDate = (String) params.getOrDefault("startDate", null);
        String endDate = (String) params.getOrDefault("endDate", null);
        return groupTourMapper.getAvailableDates(tourId, startDate, endDate);
    }

    /**
     * 获取跟团游主题列表
     * @return 主题列表
     */
    @Override
    public List<Map<String, Object>> getGroupTourThemes() {
        return groupTourMapper.getThemes();
    }

    /**
     * 根据跟团游ID获取主题列表
     * @param tourId 跟团游ID
     * @return 主题列表
     */
    @Override
    public List<Map<String, Object>> getGroupTourThemesByTourId(Integer tourId) {
        return groupTourMapper.getThemesByTourId(tourId);
    }

    /**
     * 获取跟团游亮点
     * @param tourId 跟团游ID
     * @return 亮点列表
     */
    @Override
    public List<String> getGroupTourHighlights(Integer tourId) {
        return groupTourMapper.getHighlights(tourId);
    }

    /**
     * 获取跟团游包含项目
     * @param tourId 跟团游ID
     * @return 包含项目列表
     */
    @Override
    public List<String> getGroupTourInclusions(Integer tourId) {
        return groupTourMapper.getInclusions(tourId);
    }

    /**
     * 获取跟团游不包含项目
     * @param tourId 跟团游ID
     * @return 不包含项目列表
     */
    @Override
    public List<String> getGroupTourExclusions(Integer tourId) {
        return groupTourMapper.getExclusions(tourId);
    }

    /**
     * 获取跟团游常见问题
     * @param tourId 跟团游ID
     * @return 常见问题列表
     */
    @Override
    public List<Map<String, Object>> getGroupTourFaqs(Integer tourId) {
        return groupTourMapper.getFaqs(tourId);
    }

    /**
     * 获取跟团游贴士
     * @param tourId 跟团游ID
     * @return 贴士列表
     */
    @Override
    public List<String> getGroupTourTips(Integer tourId) {
        return groupTourMapper.getTips(tourId);
    }

    /**
     * 获取跟团游图片
     * @param tourId 跟团游ID
     * @return 图片列表
     */
    @Override
    public List<Map<String, Object>> getGroupTourImages(Integer tourId) {
        return groupTourMapper.getImages(tourId);
    }

    @Override
    public List<Map<String, Object>> getGroupTourDayTours(Integer groupTourId) {
        log.info("查询团队游关联的一日游，ID：{}", groupTourId);
        List<Map<String, Object>> resultList = groupTourMapper.getGroupTourDayTours(groupTourId);
        
        // 转换布尔值
        for (Map<String, Object> item : resultList) {
            // 添加类型安全的转换逻辑
            if (item.get("is_optional") instanceof Integer) {
                item.put("is_optional", ((Integer)item.get("is_optional")) == 1);
            } else if (item.get("is_optional") instanceof Boolean) {
                // 已经是布尔类型，无需转换
            } else if (item.get("is_optional") != null) {
                // 其他类型情况，尝试解析为布尔值
                String valueStr = item.get("is_optional").toString();
                if ("1".equals(valueStr) || "true".equalsIgnoreCase(valueStr)) {
                    item.put("is_optional", true);
                } else {
                    item.put("is_optional", false);
                }
            }
        }
        
        log.info("查询团队游关联的一日游成功，结果数量：{}", resultList.size());
        return resultList;
    }

    @Override
    @Transactional
    public void saveGroupTourDayTours(Integer groupTourId, List<Map<String, Object>> dayTourData) {
        log.info("保存团队游关联的一日游，ID：{}，数据数量：{}", groupTourId, dayTourData.size());
        
        // 先删除现有关联
        groupTourMapper.deleteGroupTourDayTours(groupTourId);
        
        // 保存新的关联
        for (Map<String, Object> data : dayTourData) {
            Integer dayTourId = Integer.valueOf(data.get("dayTourId").toString());
            Integer dayNumber = Integer.valueOf(data.get("dayNumber").toString());
            
            // 处理价格差异
            BigDecimal priceDifference = BigDecimal.ZERO;
            if (data.get("priceDifference") != null) {
                try {
                    Object priceDiffObj = data.get("priceDifference");
                    if (priceDiffObj instanceof Number) {
                        priceDifference = new BigDecimal(priceDiffObj.toString());
                    } else if (priceDiffObj instanceof String) {
                        String priceDiffStr = (String) priceDiffObj;
                        if (!priceDiffStr.isEmpty()) {
                            priceDifference = new BigDecimal(priceDiffStr);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("价格差异格式错误，使用默认值0: {}", data.get("priceDifference"));
                    priceDifference = BigDecimal.ZERO;
                }
            }
            
            log.info("保存一日游关联 - 团队游ID: {}, 一日游ID: {}, 天数: {}, 价格差异: {}", 
                    groupTourId, dayTourId, dayNumber, priceDifference);
            
            groupTourMapper.saveGroupTourDayTourWithPriceOnly(groupTourId, dayTourId, dayNumber, priceDifference);
        }
        
        log.info("团队游关联一日游保存完成");
    }

    /**
     * 添加团队游行程安排
     */
    @Override
    @Transactional
    public void addGroupTourItinerary(Integer groupTourId, Integer dayNumber, String title, String description, String meals, String accommodation) {
        log.info("添加团队游行程安排：groupTourId={}, dayNumber={}, title={}", groupTourId, dayNumber, title);
        
        try {
            // 检查是否已存在相同天数的行程
            List<Map<String, Object>> existingItineraries = groupTourMapper.getItinerary(groupTourId);
            boolean dayExists = false;
            
            for (Map<String, Object> existing : existingItineraries) {
                Integer existingDay = (Integer) existing.get("day");
                if (existingDay != null && existingDay.equals(dayNumber)) {
                    dayExists = true;
                    break;
                }
            }
            
            if (dayExists) {
                log.warn("第{}天的行程已存在，将覆盖现有行程", dayNumber);
                // 删除现有行程
                for (Map<String, Object> existing : existingItineraries) {
                    Integer existingDay = (Integer) existing.get("day");
                    if (existingDay != null && existingDay.equals(dayNumber)) {
                        Integer itineraryId = (Integer) existing.get("id");
                        groupTourMapper.deleteItineraryByTourIdAndDay(groupTourId, dayNumber);
                    }
                }
            }
            
            // 添加新行程
            groupTourMapper.insertItinerary(
                groupTourId,
                dayNumber,
                title,
                description,
                meals,
                accommodation
            );
            
            log.info("添加团队游行程安排成功");
        } catch (Exception e) {
            log.error("添加团队游行程安排失败：", e);
            throw new RuntimeException("添加团队游行程安排失败", e);
        }
    }
    
    /**
     * 更新团队游行程安排
     */
    @Override
    @Transactional
    public void updateGroupTourItinerary(Integer itineraryId, Integer groupTourId, Integer dayNumber, String title, String description, String meals, String accommodation) {
        log.info("更新团队游行程安排：itineraryId={}, groupTourId={}, dayNumber={}", itineraryId, groupTourId, dayNumber);
        
        try {
            // 检查行程是否存在
            List<Map<String, Object>> existingItineraries = groupTourMapper.getItinerary(groupTourId);
            boolean itineraryExists = false;
            
            for (Map<String, Object> existing : existingItineraries) {
                Integer existingId = (Integer) existing.get("id");
                if (existingId.equals(itineraryId)) {
                    itineraryExists = true;
                    break;
                }
            }
            
            if (!itineraryExists) {
                log.warn("ID为{}的行程不存在，将创建新行程", itineraryId);
                // 添加新行程
                groupTourMapper.insertItinerary(
                    groupTourId,
                    dayNumber,
                    title,
                    description,
                    meals,
                    accommodation
                );
            } else {
                // 更新现有行程
                groupTourMapper.updateItinerary(
                    itineraryId,
                    groupTourId,
                    dayNumber,
                    title,
                    description,
                    meals,
                    accommodation
                );
            }
            
            log.info("更新团队游行程安排成功");
        } catch (Exception e) {
            log.error("更新团队游行程安排失败：", e);
            throw new RuntimeException("更新团队游行程安排失败", e);
        }
    }

    /**
     * 更新跟团游信息
     * @param groupTourDTO 跟团游信息
     */
    @Override
    @Transactional
    public void updateGroupTour(GroupTourDTO groupTourDTO) {
        log.info("更新跟团游信息: {}", groupTourDTO);
        
        // 更新基本信息
        groupTourMapper.update(groupTourDTO);
        
        Integer tourId = groupTourDTO.getId();
        
        // 如果有主题信息，更新主题
        List<Integer> themeIds = groupTourDTO.getThemeIds();
        if (themeIds == null || themeIds.isEmpty()) {
            // 如果前端没有传递themeIds，则查询现有的主题ID
            themeIds = groupTourMapper.getThemeIds(tourId);
        }
        
        if (themeIds != null && !themeIds.isEmpty()) {
            // 删除旧的主题关联
            groupTourMapper.deleteThemesByTourId(tourId);
            
            // 添加新的主题关联
            for (Integer themeId : themeIds) {
                groupTourMapper.insertTourTheme(tourId, themeId);
            }
        }
        
        // 如果有适合人群信息，更新适合人群
        List<Integer> suitableIds = groupTourDTO.getSuitableIds();
        if (suitableIds == null || suitableIds.isEmpty()) {
            // 如果前端没有传递suitableIds，则查询现有的适合人群ID
            suitableIds = groupTourMapper.getSuitableIds(tourId);
        }
        
        if (suitableIds != null && !suitableIds.isEmpty()) {
            // 删除旧的适合人群关联
            tourMapper.deleteSuitableByTourId(tourId, "group_tour");
            
            // 添加新的适合人群关联
            for (Integer suitableId : suitableIds) {
                tourMapper.insertTourSuitable(tourId, suitableId, "group_tour");
            }
        }
        
        // 更新亮点信息
        if (groupTourDTO.getHighlights() != null) {
            // 删除旧的亮点
            groupTourMapper.deleteHighlights(tourId);
            
            // 添加新的亮点
            for (String highlight : groupTourDTO.getHighlights()) {
                if (highlight != null && !highlight.trim().isEmpty()) {
                    groupTourMapper.insertHighlight(tourId, highlight);
                }
            }
        }
        
        // 更新包含项目
        if (groupTourDTO.getInclusions() != null) {
            // 删除旧的包含项目
            groupTourMapper.deleteInclusions(tourId);
            
            // 添加新的包含项目
            for (String inclusion : groupTourDTO.getInclusions()) {
                if (inclusion != null && !inclusion.trim().isEmpty()) {
                    groupTourMapper.insertInclusion(tourId, inclusion);
                }
            }
        }
        
        // 更新不包含项目
        if (groupTourDTO.getExclusions() != null) {
            // 删除旧的不包含项目
            groupTourMapper.deleteExclusions(tourId);
            
            // 添加新的不包含项目
            for (String exclusion : groupTourDTO.getExclusions()) {
                if (exclusion != null && !exclusion.trim().isEmpty()) {
                    groupTourMapper.insertExclusion(tourId, exclusion);
                }
            }
        }
        
        // 更新贴士
        if (groupTourDTO.getTips() != null) {
            // 删除旧的贴士
            groupTourMapper.deleteTips(tourId);
            
            // 添加新的贴士
            for (String tip : groupTourDTO.getTips()) {
                if (tip != null && !tip.trim().isEmpty()) {
                    groupTourMapper.insertTip(tourId, tip);
                }
            }
        }
        
        // 更新常见问题
        if (groupTourDTO.getFaqs() != null) {
            // 删除旧的常见问题
            groupTourMapper.deleteFaqs(tourId);
            
            // 添加新的常见问题
            for (Map<String, Object> faq : groupTourDTO.getFaqs()) {
                String question = (String) faq.get("question");
                String answer = (String) faq.get("answer");
                if (question != null && !question.trim().isEmpty() && answer != null && !answer.trim().isEmpty()) {
                    groupTourMapper.insertFaq(tourId, question, answer);
                }
            }
        }
        
        // 更新行程安排
        if (groupTourDTO.getItinerary() != null) {
            // 删除旧的行程安排
            groupTourMapper.deleteItinerary(tourId);
            
            // 添加新的行程安排
            for (Map<String, Object> dayItem : groupTourDTO.getItinerary()) {
                Integer dayNumber = (Integer) dayItem.get("day_number");
                String title = (String) dayItem.get("title");
                String description = (String) dayItem.get("description");
                String meals = (String) dayItem.get("meals");
                String accommodation = (String) dayItem.get("accommodation");
                
                if (dayNumber != null && title != null && !title.trim().isEmpty()) {
                    groupTourMapper.insertItinerary(tourId, dayNumber, title, description, meals, accommodation);
                }
            }
        }
    }

    /**
     * 删除团队游行程安排
     * @param itineraryId 行程ID
     */
    @Override
    @Transactional
    public void deleteGroupTourItinerary(Integer itineraryId) {
        log.info("删除团队游行程安排，行程ID：{}", itineraryId);
        
        try {
            // 删除指定的行程安排
            groupTourMapper.deleteItineraryById(itineraryId);
            log.info("删除团队游行程安排成功，行程ID：{}", itineraryId);
        } catch (Exception e) {
            log.error("删除团队游行程安排失败，错误：{}", e.getMessage(), e);
            throw new RuntimeException("删除团队游行程安排失败", e);
        }
    }
    
    /**
     * 获取所有可用的一日游
     * @return 一日游列表
     */
    @Override
    public List<Map<String, Object>> getAvailableDayTours() {
        log.info("获取所有可用的一日游");
        // 查询所有活跃状态的一日游
        String sql = "SELECT dt.day_tour_id as id, dt.name, dt.price, dt.duration, dt.location, dt.image_url " +
                     "FROM day_tours dt " +
                     "WHERE dt.is_active = 1 " +
                     "ORDER BY dt.day_tour_id";
        
        return jdbcTemplate.queryForList(sql);
    }
    
    /**
     * 保存新的团队游
     * @param groupTourDTO 团队游信息
     * @return 新创建的团队游ID
     */
    @Override
    @Transactional
    public Integer saveGroupTour(GroupTourDTO groupTourDTO) {
        log.info("保存新的团队游：{}", groupTourDTO);
        
        try {
            // 插入团队游基本信息
            Integer groupTourId = groupTourMapper.insert(groupTourDTO);
            if (groupTourId == null || groupTourId <= 0) {
                throw new RuntimeException("插入团队游失败");
            }
            
            // 设置ID
            groupTourDTO.setId(groupTourId);
            
            // 处理主题信息
            List<Integer> themeIds = groupTourDTO.getThemeIds();
            if (themeIds != null && !themeIds.isEmpty()) {
                for (Integer themeId : themeIds) {
                    groupTourMapper.insertTourTheme(groupTourId, themeId);
                }
            }
            
            // 处理适合人群信息
            List<Integer> suitableIds = groupTourDTO.getSuitableIds();
            if (suitableIds != null && !suitableIds.isEmpty()) {
                for (Integer suitableId : suitableIds) {
                    tourMapper.insertTourSuitable(groupTourId, suitableId, "group_tour");
                }
            }
            
            // 处理亮点信息
            if (groupTourDTO.getHighlights() != null) {
                for (String highlight : groupTourDTO.getHighlights()) {
                    if (highlight != null && !highlight.trim().isEmpty()) {
                        groupTourMapper.insertHighlight(groupTourId, highlight);
                    }
                }
            }
            
            // 处理包含项目
            if (groupTourDTO.getInclusions() != null) {
                for (String inclusion : groupTourDTO.getInclusions()) {
                    if (inclusion != null && !inclusion.trim().isEmpty()) {
                        groupTourMapper.insertInclusion(groupTourId, inclusion);
                    }
                }
            }
            
            // 处理不包含项目
            if (groupTourDTO.getExclusions() != null) {
                for (String exclusion : groupTourDTO.getExclusions()) {
                    if (exclusion != null && !exclusion.trim().isEmpty()) {
                        groupTourMapper.insertExclusion(groupTourId, exclusion);
                    }
                }
            }
            
            // 处理贴士
            if (groupTourDTO.getTips() != null) {
                for (String tip : groupTourDTO.getTips()) {
                    if (tip != null && !tip.trim().isEmpty()) {
                        groupTourMapper.insertTip(groupTourId, tip);
                    }
                }
            }
            
            // 处理常见问题
            if (groupTourDTO.getFaqs() != null) {
                for (Map<String, Object> faq : groupTourDTO.getFaqs()) {
                    String question = (String) faq.get("question");
                    String answer = (String) faq.get("answer");
                    if (question != null && !question.trim().isEmpty() && answer != null && !answer.trim().isEmpty()) {
                        groupTourMapper.insertFaq(groupTourId, question, answer);
                    }
                }
            }
            
            // 处理行程安排
            if (groupTourDTO.getItinerary() != null) {
                for (Map<String, Object> dayItem : groupTourDTO.getItinerary()) {
                    Integer dayNumber = (Integer) dayItem.get("day_number");
                    String title = (String) dayItem.get("title");
                    String description = (String) dayItem.get("description");
                    String meals = (String) dayItem.get("meals");
                    String accommodation = (String) dayItem.get("accommodation");
                    
                    if (dayNumber != null && title != null && !title.trim().isEmpty()) {
                        groupTourMapper.insertItinerary(groupTourId, dayNumber, title, description, meals, accommodation);
                    }
                }
            }
            
            log.info("保存新的团队游成功，ID：{}", groupTourId);
            return groupTourId;
        } catch (Exception e) {
            log.error("保存团队游失败，错误：{}", e.getMessage(), e);
            throw new RuntimeException("保存团队游失败", e);
        }
    }
    
    /**
     * 删除团队游
     * @param id 团队游ID
     */
    @Override
    @Transactional
    public void deleteGroupTour(Integer id) {
        log.info("删除团队游，ID：{}", id);
        
        try {
            // 1. 删除团队游关联的一日游
            groupTourMapper.deleteGroupTourDayTours(id);
            
            // 2. 删除团队游主题关联
            groupTourMapper.deleteThemesByTourId(id);
            
            // 3. 删除团队游适合人群关联
            tourMapper.deleteSuitableByTourId(id, "group_tour");
            
            // 4. 删除团队游亮点
            groupTourMapper.deleteHighlights(id);
            
            // 5. 删除团队游包含项目
            groupTourMapper.deleteInclusions(id);
            
            // 6. 删除团队游不包含项目
            groupTourMapper.deleteExclusions(id);
            
            // 7. 删除团队游贴士
            groupTourMapper.deleteTips(id);
            
            // 8. 删除团队游常见问题
            groupTourMapper.deleteFaqs(id);
            
            // 9. 删除团队游行程
            groupTourMapper.deleteItinerary(id);
            
            // 10. 删除团队游图片
            groupTourMapper.deleteImages(id);
            
            // 11. 删除团队游基本信息
            groupTourMapper.deleteById(id);
            
            log.info("删除团队游成功，ID：{}", id);
        } catch (Exception e) {
            log.error("删除团队游失败，错误：{}", e.getMessage(), e);
            throw new RuntimeException("删除团队游失败", e);
        }
    }
    
    /**
     * 团队游上架/下架
     * @param status 状态（0-下架，1-上架）
     * @param id 团队游ID
     */
    @Override
    public void enableOrDisableGroupTour(Integer status, Integer id) {
        log.info("团队游上架/下架，状态：{}，ID：{}", status, id);
        
        try {
            groupTourMapper.updateStatus(id, status);
            log.info("团队游状态更新成功，ID：{}，状态：{}", id, status);
        } catch (Exception e) {
            log.error("团队游状态更新失败，错误：{}", e.getMessage(), e);
            throw new RuntimeException("团队游状态更新失败", e);
        }
    }
    
    /**
     * 更新产品展示图片
     */
    @Override
    public void updateProductShowcaseImage(Integer groupTourId, String imageUrl) {
        log.info("更新团体游产品展示图片，ID：{}，图片URL：{}", groupTourId, imageUrl);
        try {
            String sql = "UPDATE group_tours SET product_showcase_image = ? WHERE group_tour_id = ?";
            jdbcTemplate.update(sql, imageUrl, groupTourId);
            log.info("更新团体游产品展示图片成功");
        } catch (Exception e) {
            log.error("更新团体游产品展示图片失败：{}", e.getMessage(), e);
            throw new RuntimeException("更新团体游产品展示图片失败", e);
        }
    }
} 