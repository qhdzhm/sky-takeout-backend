package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DayTourDTO;
import com.sky.dto.DayTourPageQueryDTO;
import com.sky.entity.*;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.DayTourService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一日游服务实现类
 */
@Service
@Slf4j
public class DayTourServiceImpl implements DayTourService {

    @Autowired
    private DayTourMapper dayTourMapper;
    
    @Autowired
    private TourMapper tourMapper;
    
    @Autowired
    private DayTourHighlightMapper dayTourHighlightMapper;
    
    @Autowired
    private DayTourInclusionMapper dayTourInclusionMapper;
    
    @Autowired
    private DayTourExclusionMapper dayTourExclusionMapper;
    
    @Autowired
    private DayTourFaqMapper dayTourFaqMapper;
    
    @Autowired
    private DayTourItineraryMapper dayTourItineraryMapper;
    
    @Autowired
    private DayTourTipMapper dayTourTipMapper;
    
    @Autowired
    private DayTourScheduleMapper dayTourScheduleMapper;
    
    @Autowired
    private DayTourThemeMapper dayTourThemeMapper;
    
    @Autowired
    private DayTourSuitableMapper dayTourSuitableMapper;
    
    @Autowired
    private DayTourImageMapper dayTourImageMapper;

    /**
     * 分页查询
     */
    @Override
    public PageResult pageQuery(DayTourPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<DayTourDTO> page = dayTourMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据ID查询一日游
     */
    @Override
    public DayTour getById(Integer id) {
        return dayTourMapper.getById(id);
    }

    /**
     * 保存一日游
     */
    @Override
    @Transactional
    public Integer save(DayTourDTO dayTourDTO) {
        log.info("开始保存一日游，数据：{}", dayTourDTO);
        DayTour dayTour = new DayTour();
        BeanUtils.copyProperties(dayTourDTO, dayTour);
        
        // 设置创建和更新时间
        dayTour.setCreatedAt(LocalDateTime.now());
        dayTour.setUpdatedAt(LocalDateTime.now());
        dayTour.setIsActive(1); // 默认激活
        
        // 打印要插入的数据
        log.info("准备插入day_tours表的数据：name={}, location={}, description={}, price={}, duration={}, imageUrl={}",
                dayTour.getName(), dayTour.getLocation(), dayTour.getDescription(), 
                dayTour.getPrice(), dayTour.getDuration(), dayTour.getImageUrl());
        
        // 保存一日游主表信息
        try {
            log.info("执行insert操作前的DayTour对象：{}", dayTour);
            dayTourMapper.insert(dayTour);
            log.info("insert操作后，获取到的dayTourId：{}", dayTour.getDayTourId());
        } catch (Exception e) {
            log.error("插入day_tours表失败：{}", e.getMessage(), e);
            throw e;
        }
        
        // 保存主题关联
        Integer dayTourId = dayTour.getDayTourId();
        if (dayTourId != null && dayTourDTO.getThemeIds() != null && dayTourDTO.getThemeIds().length > 0) {
            List<Integer> themeIds = new ArrayList<>();
            for (Integer themeId : dayTourDTO.getThemeIds()) {
                themeIds.add(themeId);
            }
            try {
                dayTourThemeMapper.deleteAssociation(dayTourId);
                for (Integer themeId : themeIds) {
                    log.info("关联主题，dayTourId={}，themeId={}", dayTourId, themeId);
                    dayTourThemeMapper.insertAssociation(dayTourId, themeId);
                }
            } catch (Exception e) {
                log.error("保存主题关联失败：{}", e.getMessage(), e);
                throw e;
            }
        } else {
            log.info("没有主题需要关联，dayTourId={}，themeIds={}", dayTourId, dayTourDTO.getThemeIds());
        }
        
        // 保存适合人群关联
        if (dayTourId != null && dayTourDTO.getSuitableIds() != null && dayTourDTO.getSuitableIds().length > 0) {
            List<Integer> suitableIds = new ArrayList<>();
            for (Integer suitableId : dayTourDTO.getSuitableIds()) {
                suitableIds.add(suitableId);
            }
            try {
                dayTourSuitableMapper.deleteRelationByDayTourId(dayTourId);
                for (Integer suitableId : suitableIds) {
                    log.info("关联适合人群，dayTourId={}，suitableId={}", dayTourId, suitableId);
                    dayTourSuitableMapper.insertAssociation(dayTourId, suitableId);
                }
            } catch (Exception e) {
                log.error("保存适合人群关联失败：{}", e.getMessage(), e);
                throw e;
            }
        } else {
            log.info("没有适合人群需要关联，dayTourId={}，suitableIds={}", dayTourId, dayTourDTO.getSuitableIds());
        }
        
        log.info("一日游保存完成，dayTourId={}", dayTourId);
        return dayTourId; // 返回创建的一日游ID
    }

    /**
     * 更新一日游
     */
    @Override
    @Transactional
    public void update(DayTourDTO dayTourDTO) {
        DayTour dayTour = new DayTour();
        BeanUtils.copyProperties(dayTourDTO, dayTour);
        
        // 设置更新时间
        dayTour.setUpdatedAt(LocalDateTime.now());
        
        // 更新一日游主表信息
        dayTourMapper.update(dayTour);
        
        // 更新主题关联
        Integer dayTourId = dayTour.getDayTourId();
        if (dayTourDTO.getThemeIds() != null && dayTourDTO.getThemeIds().length > 0) {
            List<Integer> themeIds = new ArrayList<>();
            for (Integer themeId : dayTourDTO.getThemeIds()) {
                themeIds.add(themeId);
            }
            dayTourThemeMapper.deleteAssociation(dayTourId);
            for (Integer themeId : themeIds) {
                dayTourThemeMapper.insertAssociation(dayTourId, themeId);
            }
        }
        
        // 更新适合人群关联
        if (dayTourDTO.getSuitableIds() != null && dayTourDTO.getSuitableIds().length > 0) {
            List<Integer> suitableIds = new ArrayList<>();
            for (Integer suitableId : dayTourDTO.getSuitableIds()) {
                suitableIds.add(suitableId);
            }
            dayTourSuitableMapper.deleteRelationByDayTourId(dayTourId);
            for (Integer suitableId : suitableIds) {
                dayTourSuitableMapper.insertAssociation(dayTourId, suitableId);
            }
        }
    }

    /**
     * 删除一日游
     */
    @Override
    @Transactional
    public void deleteById(Integer id) {
        // 删除主题关联
        dayTourThemeMapper.deleteAssociation(id);
        
        // 删除适合人群关联
        dayTourSuitableMapper.deleteRelationByDayTourId(id);
        
        // 删除亮点
        dayTourHighlightMapper.deleteByDayTourId(id);
        
        // 删除包含项
        dayTourInclusionMapper.deleteByDayTourId(id);
        
        // 删除不包含项
        dayTourExclusionMapper.deleteByDayTourId(id);
        
        // 删除常见问题
        dayTourFaqMapper.deleteByDayTourId(id);
        
        // 删除行程
        dayTourItineraryMapper.deleteByDayTourId(id);
        
        // 删除旅行提示
        dayTourTipMapper.deleteByDayTourId(id);
        
        // 删除日程安排
        dayTourScheduleMapper.deleteByDayTourId(id);
        
        // 删除一日游主表信息
        dayTourMapper.deleteById(id);
    }

    /**
     * 启用或禁用一日游
     */
    @Override
    public void startOrStop(Integer status, Integer id) {
        dayTourMapper.updateStatus(id, status);
    }

    /**
     * 更新一日游用户端显示状态
     */
    @Override
    public void updateUserSiteVisibility(Integer id, Integer showOnUserSite) {
        dayTourMapper.updateUserSiteVisibility(id, showOnUserSite);
    }

    /**
     * 获取一日游亮点
     */
    @Override
    public List<DayTourHighlight> getHighlightsByDayTourId(Integer dayTourId) {
        return dayTourHighlightMapper.getByDayTourId(dayTourId);
    }

    /**
     * 保存一日游亮点
     */
    @Override
    public void saveHighlight(DayTourHighlight dayTourHighlight) {
        dayTourHighlightMapper.insert(dayTourHighlight);
    }

    /**
     * 删除一日游亮点
     */
    @Override
    public void deleteHighlight(Integer id) {
        dayTourHighlightMapper.deleteById(id);
    }

    /**
     * 获取一日游包含项
     */
    @Override
    public List<DayTourInclusion> getInclusionsByDayTourId(Integer dayTourId) {
        return dayTourInclusionMapper.getByDayTourId(dayTourId);
    }

    /**
     * 保存一日游包含项
     */
    @Override
    public void saveInclusion(DayTourInclusion dayTourInclusion) {
        dayTourInclusionMapper.insert(dayTourInclusion);
    }

    /**
     * 删除一日游包含项
     */
    @Override
    public void deleteInclusion(Integer id) {
        dayTourInclusionMapper.deleteById(id);
    }

    /**
     * 获取一日游不包含项
     */
    @Override
    public List<DayTourExclusion> getExclusionsByDayTourId(Integer dayTourId) {
        return dayTourExclusionMapper.getByDayTourId(dayTourId);
    }

    /**
     * 保存一日游不包含项
     */
    @Override
    public void saveExclusion(DayTourExclusion dayTourExclusion) {
        dayTourExclusionMapper.insert(dayTourExclusion);
    }

    /**
     * 删除一日游不包含项
     */
    @Override
    public void deleteExclusion(Integer id) {
        dayTourExclusionMapper.deleteById(id);
    }

    /**
     * 获取一日游常见问题
     */
    @Override
    public List<DayTourFaq> getFaqsByDayTourId(Integer dayTourId) {
        return dayTourFaqMapper.getByDayTourId(dayTourId);
    }

    /**
     * 保存一日游常见问题
     */
    @Override
    public void saveFaq(DayTourFaq dayTourFaq) {
        dayTourFaqMapper.insert(dayTourFaq);
    }

    /**
     * 删除一日游常见问题
     */
    @Override
    public void deleteFaq(Integer id) {
        dayTourFaqMapper.deleteById(id);
    }

    /**
     * 获取一日游行程
     */
    @Override
    public List<DayTourItinerary> getItinerariesByDayTourId(Integer dayTourId) {
        return dayTourItineraryMapper.getByDayTourId(dayTourId);
    }

    /**
     * 保存一日游行程
     */
    @Override
    public void saveItinerary(DayTourItinerary dayTourItinerary) {
        dayTourItineraryMapper.insert(dayTourItinerary);
    }

    /**
     * 删除一日游行程
     */
    @Override
    public void deleteItinerary(Integer id) {
        dayTourItineraryMapper.deleteById(id);
    }

    /**
     * 获取一日游旅行提示
     */
    @Override
    public List<DayTourTip> getTipsByDayTourId(Integer dayTourId) {
        return dayTourTipMapper.getByDayTourId(dayTourId);
    }

    /**
     * 保存一日游旅行提示
     */
    @Override
    public void saveTip(DayTourTip dayTourTip) {
        dayTourTipMapper.insert(dayTourTip);
    }

    /**
     * 删除一日游旅行提示
     */
    @Override
    public void deleteTip(Integer id) {
        dayTourTipMapper.deleteById(id);
    }

    /**
     * 获取一日游日程安排
     */
    @Override
    public List<DayTourSchedule> getSchedulesByDayTourId(Integer dayTourId) {
        return dayTourScheduleMapper.getByDayTourId(dayTourId);
    }

    /**
     * 保存一日游日程安排
     */
    @Override
    public void saveSchedule(DayTourSchedule dayTourSchedule) {
        dayTourScheduleMapper.insert(dayTourSchedule);
    }

    /**
     * 删除一日游日程安排
     */
    @Override
    public void deleteSchedule(Integer scheduleId) {
        dayTourScheduleMapper.deleteById(scheduleId);
    }

    /**
     * 获取所有一日游
     * @param params 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult getAllDayTours(Map<String, Object> params) {
        // 解析参数
        String name = (String) params.getOrDefault("name", null);
        String location = (String) params.getOrDefault("location", null);
        String category = (String) params.getOrDefault("category", null);
        Integer regionId = params.get("regionId") != null ? Integer.parseInt(params.get("regionId").toString()) : null;
        Double minPrice = params.get("minPrice") != null ? Double.parseDouble(params.get("minPrice").toString()) : null;
        Double maxPrice = params.get("maxPrice") != null ? Double.parseDouble(params.get("maxPrice").toString()) : null;
        String orderBy = (String) params.getOrDefault("orderBy", "id DESC");
        
        // 分页参数
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 50;
        
        // 构建查询DTO
        DayTourPageQueryDTO queryDTO = new DayTourPageQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setPageSize(pageSize);
        queryDTO.setName(name);
        queryDTO.setLocation(location);
        queryDTO.setCategory(category);
        queryDTO.setRegionId(regionId);
        if (minPrice != null) {
            queryDTO.setMinPrice(minPrice);
        }
        if (maxPrice != null) {
            queryDTO.setMaxPrice(maxPrice);
        }
        
        // 从参数中获取是否需要过滤用户端显示状态，如果没有指定则不过滤（管理后台）
        if (params.get("showOnUserSite") != null) {
            queryDTO.setShowOnUserSite(Integer.parseInt(params.get("showOnUserSite").toString()));
        }
        
        // 分页查询
        return pageQuery(queryDTO);
    }

    /**
     * 根据ID获取一日游详情
     * @param id 一日游ID
     * @return 一日游详情
     */
    @Override
    public DayTourDTO getDayTourById(Integer id) {
        log.info("====== 开始查询一日游详情，ID：{} ======", id);
        
        // 1. 获取基础信息
        log.info("1. 准备查询基础信息, SQL: select * from day_tours where day_tour_id = {}", id);
        DayTour dayTour = getById(id);
        if (dayTour == null) {
            log.error("未找到ID为{}的一日游信息", id);
            return null;
        }
        
        log.info("查询到一日游基本信息：ID={}, 名称={}, 价格={}", dayTour.getDayTourId(), dayTour.getName(), dayTour.getPrice());
        
        DayTourDTO dayTourDTO = new DayTourDTO();
        BeanUtils.copyProperties(dayTour, dayTourDTO);
        
        // 确保ID字段的一致性 - 非常重要
        if (dayTourDTO.getId() == null) {
            dayTourDTO.setId(dayTour.getDayTourId());
            log.info("设置id=dayTourId：{}", dayTour.getDayTourId());
        }
        if (dayTourDTO.getDayTourId() == null) {
            dayTourDTO.setDayTourId(dayTour.getDayTourId());
            log.info("设置dayTourId=id：{}", dayTour.getDayTourId());
        }
        
        log.info("拷贝完基本属性后的DTO：dayTourId={}, id={}", dayTourDTO.getDayTourId(), dayTourDTO.getId());
        
        // 确保图片字段的正确设置
        if (dayTour.getImageUrl() != null) {
            dayTourDTO.setImageUrl(dayTour.getImageUrl());
            dayTourDTO.setCoverImage(dayTour.getImageUrl());
            log.info("设置图片URL：{}", dayTour.getImageUrl());
        }
        
        // 设置Banner图片
        if (dayTour.getBannerImage() != null) {
            dayTourDTO.setBannerImage(dayTour.getBannerImage());
            log.info("设置Banner图片URL：{}", dayTour.getBannerImage());
        }
        
        // 设置地区名称
        if (dayTour.getRegionName() != null) {
            dayTourDTO.setRegionName(dayTour.getRegionName());
            log.info("设置地区名称：{}", dayTour.getRegionName());
        }
        
        // 查询亮点
        try {
            // 确保使用正确的ID查询
            Integer dayTourId = dayTour.getDayTourId();
            log.info("2. 准备查询亮点, SQL: select * from day_tour_highlights where day_tour_id = {}", dayTourId);
            List<DayTourHighlight> highlightList = dayTourHighlightMapper.getByDayTourId(dayTourId);
            log.info("查询到亮点数量：{}", highlightList != null ? highlightList.size() : 0);
            
            if (highlightList == null) {
                log.warn("亮点查询结果为null, 检查SQL: select id, day_tour_id as dayTourId, description, position from day_tour_highlights where day_tour_id = {}", dayTourId);
                dayTourDTO.setHighlights(new ArrayList<>());
            } else if (highlightList.isEmpty()) {
                log.warn("未查询到ID={}的一日游亮点，检查数据表是否有对应记录", dayTourId);
                dayTourDTO.setHighlights(new ArrayList<>());
            } else {
                List<String> highlights = new ArrayList<>();
                for (DayTourHighlight highlight : highlightList) {
                    highlights.add(highlight.getDescription());
                    log.info("亮点: {}", highlight.getDescription());
                }
                dayTourDTO.setHighlights(highlights);
            }
        } catch (Exception e) {
            log.error("查询一日游亮点失败，id={}，错误：{}", id, e.getMessage(), e);
            dayTourDTO.setHighlights(new ArrayList<>());
        }
        
        // 查询包含项
        try {
            // 确保使用正确的ID查询
            Integer dayTourId = dayTour.getDayTourId();
            log.info("3. 准备查询包含项, SQL: select * from day_tour_inclusions where day_tour_id = {}", dayTourId);
            List<DayTourInclusion> inclusionList = dayTourInclusionMapper.getByDayTourId(dayTourId);
            log.info("查询到包含项数量：{}", inclusionList != null ? inclusionList.size() : 0);
            
            if (inclusionList == null) {
                log.warn("包含项查询结果为null, 检查SQL: select id, day_tour_id as dayTourId, description, position from day_tour_inclusions where day_tour_id = {}", dayTourId);
                dayTourDTO.setInclusions(new ArrayList<>());
            } else if (inclusionList.isEmpty()) {
                log.warn("未查询到ID={}的一日游包含项，检查数据表是否有对应记录", dayTourId);
                dayTourDTO.setInclusions(new ArrayList<>());
            } else {
                List<String> inclusions = new ArrayList<>();
                for (DayTourInclusion inclusion : inclusionList) {
                    inclusions.add(inclusion.getDescription());
                    log.info("包含项: {}", inclusion.getDescription());
                }
                dayTourDTO.setInclusions(inclusions);
            }
        } catch (Exception e) {
            log.error("查询一日游包含项失败，id={}，错误：{}", id, e.getMessage(), e);
            dayTourDTO.setInclusions(new ArrayList<>());
        }
        
        // 查询不包含项
        try {
            // 确保使用正确的ID查询
            Integer dayTourId = dayTour.getDayTourId();
            log.info("4. 准备查询不包含项, SQL: select * from day_tour_exclusions where day_tour_id = {}", dayTourId);
            List<DayTourExclusion> exclusionList = dayTourExclusionMapper.getByDayTourId(dayTourId);
            log.info("查询到不包含项数量：{}", exclusionList != null ? exclusionList.size() : 0);
            
            if (exclusionList == null) {
                log.warn("不包含项查询结果为null, 检查SQL: select id, day_tour_id as dayTourId, description, position from day_tour_exclusions where day_tour_id = {}", dayTourId);
                dayTourDTO.setExclusions(new ArrayList<>());
            } else if (exclusionList.isEmpty()) {
                log.warn("未查询到ID={}的一日游不包含项，检查数据表是否有对应记录", dayTourId);
                dayTourDTO.setExclusions(new ArrayList<>());
            } else {
                List<String> exclusions = new ArrayList<>();
                for (DayTourExclusion exclusion : exclusionList) {
                    exclusions.add(exclusion.getDescription());
                    log.info("不包含项: {}", exclusion.getDescription());
                }
                dayTourDTO.setExclusions(exclusions);
            }
        } catch (Exception e) {
            log.error("查询一日游不包含项失败，id={}，错误：{}", id, e.getMessage(), e);
            dayTourDTO.setExclusions(new ArrayList<>());
        }
        
        // 查询常见问题
        try {
            // 确保使用正确的ID查询
            Integer dayTourId = dayTour.getDayTourId();
            List<DayTourFaq> faqList = dayTourFaqMapper.getByDayTourId(dayTourId);
            log.info("查询到常见问题数量：{}", faqList != null ? faqList.size() : 0);
            if (faqList != null && !faqList.isEmpty()) {
                List<Map<String, Object>> faqs = new ArrayList<>();
                for (DayTourFaq faq : faqList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("question", faq.getQuestion());
                    map.put("answer", faq.getAnswer());
                    faqs.add(map);
                }
                dayTourDTO.setFaqs(faqs);
            } else {
                log.warn("未查询到ID={}的一日游常见问题", dayTourId);
                // 创建空列表以确保返回值不为null
                dayTourDTO.setFaqs(new ArrayList<>());
            }
        } catch (Exception e) {
            log.error("查询一日游常见问题失败，id={}，错误：{}", id, e.getMessage(), e);
            dayTourDTO.setFaqs(new ArrayList<>());
        }
        
        // 查询旅行提示
        try {
            // 确保使用正确的ID查询
            Integer dayTourId = dayTour.getDayTourId();
            List<DayTourTip> tipList = dayTourTipMapper.getByDayTourId(dayTourId);
            log.info("查询到旅行提示数量：{}", tipList != null ? tipList.size() : 0);
            if (tipList != null && !tipList.isEmpty()) {
                List<String> tips = new ArrayList<>();
                for (DayTourTip tip : tipList) {
                    tips.add(tip.getDescription());
                }
                dayTourDTO.setTips(tips);
            } else {
                log.warn("未查询到ID={}的一日游旅行提示", dayTourId);
                // 创建空列表以确保返回值不为null
                dayTourDTO.setTips(new ArrayList<>());
            }
        } catch (Exception e) {
            log.error("查询一日游旅行提示失败，id={}，错误：{}", id, e.getMessage(), e);
            dayTourDTO.setTips(new ArrayList<>());
        }
        
        // 查询行程安排
        try {
            // 确保使用正确的ID查询
            Integer dayTourId = dayTour.getDayTourId();
            List<DayTourItinerary> itineraryList = dayTourItineraryMapper.getByDayTourId(dayTourId);
            log.info("查询到行程安排数量：{}", itineraryList != null ? itineraryList.size() : 0);
            if (itineraryList != null && !itineraryList.isEmpty()) {
                List<Map<String, Object>> itinerary = new ArrayList<>();
                for (DayTourItinerary item : itineraryList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("time_slot", item.getTimeSlot());
                    map.put("activity", item.getActivity());
                    map.put("location", item.getLocation());
                    map.put("description", item.getDescription());
                    map.put("day_number", 1); // 一日游固定为第1天
                    itinerary.add(map);
                }
                dayTourDTO.setItinerary(itinerary);
            } else {
                log.warn("未查询到ID={}的一日游行程安排", dayTourId);
                // 创建空列表以确保返回值不为null
                dayTourDTO.setItinerary(new ArrayList<>());
            }
        } catch (Exception e) {
            log.error("查询一日游行程安排失败，id={}，错误：{}", id, e.getMessage(), e);
            dayTourDTO.setItinerary(new ArrayList<>());
        }
        
        // 查询图片
        try {
            // 确保使用正确的ID查询
            Integer dayTourId = dayTour.getDayTourId();
            List<DayTourImage> imageList = dayTourImageMapper.selectByDayTourId(dayTourId);
            log.info("查询到图片数量：{}", imageList != null ? imageList.size() : 0);
            if (imageList != null && !imageList.isEmpty()) {
                List<Map<String, Object>> images = new ArrayList<>();
                for (DayTourImage image : imageList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("image_url", image.getImageUrl());
                    map.put("thumbnail_url", image.getThumbnailUrl());
                    map.put("description", image.getDescription());
                    map.put("is_primary", image.getIsPrimary() == 1);
                    images.add(map);
                }
                dayTourDTO.setImages(images);
            } else if (dayTour.getImageUrl() != null) {
                // 如果没有图片列表但有主图URL，创建一个图片对象
                List<Map<String, Object>> images = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("image_url", dayTour.getImageUrl());
                map.put("thumbnail_url", dayTour.getImageUrl());
                map.put("description", dayTour.getName());
                map.put("is_primary", true);
                images.add(map);
                dayTourDTO.setImages(images);
                log.info("使用主图URL创建默认图片：{}", dayTour.getImageUrl());
            } else {
                log.warn("未查询到ID={}的一日游图片，且主图URL为空", dayTourId);
                // 创建空列表以确保返回值不为null
                dayTourDTO.setImages(new ArrayList<>());
            }
        } catch (Exception e) {
            log.error("查询一日游图片失败，id={}，错误：{}", id, e.getMessage(), e);
            // 确保返回非空值
            if (dayTour.getImageUrl() != null) {
                List<Map<String, Object>> images = new ArrayList<>();
                Map<String, Object> map = new HashMap<>();
                map.put("image_url", dayTour.getImageUrl());
                map.put("thumbnail_url", dayTour.getImageUrl());
                map.put("description", dayTour.getName());
                map.put("is_primary", true);
                images.add(map);
                dayTourDTO.setImages(images);
            } else {
                dayTourDTO.setImages(new ArrayList<>());
            }
        }
        
        // 查询主题
        List<Map<String, Object>> themesList = getDayTourThemesByTourId(id);
        log.info("查询到主题数量：{}", themesList != null ? themesList.size() : 0);
        if (themesList != null && !themesList.isEmpty()) {
            List<String> themes = new ArrayList<>();
            Integer[] themeIds = new Integer[themesList.size()];
            
            for (int i = 0; i < themesList.size(); i++) {
                Map<String, Object> theme = themesList.get(i);
                themes.add((String) theme.get("name"));
                themeIds[i] = (Integer) theme.get("id");
            }
            
            dayTourDTO.setThemes(themes);
            dayTourDTO.setThemeIds(themeIds);
        } else if (dayTour.getCategory() != null) {
            // 如果没有主题但有分类，将分类作为主题
            List<String> themes = new ArrayList<>();
            themes.add(dayTour.getCategory());
            dayTourDTO.setThemes(themes);
            log.info("使用分类作为主题：{}", dayTour.getCategory());
        }
        
        // 查询适合人群
        List<SuitableFor> suitables = dayTourSuitableMapper.getByDayTourId(id);
        log.info("查询到适合人群数量：{}", suitables != null ? suitables.size() : 0);
        if (suitables != null && !suitables.isEmpty()) {
            Integer[] suitableIds = new Integer[suitables.size()];
            List<String> suitableFor = new ArrayList<>();
            
            for (int i = 0; i < suitables.size(); i++) {
                SuitableFor suitable = suitables.get(i);
                suitableIds[i] = suitable.getSuitableId();
                suitableFor.add(suitable.getName());
            }
            
            dayTourDTO.setSuitableIds(suitableIds);
            dayTourDTO.setSuitableFor(suitableFor);
        }
        
        // 初始化折扣价格为原价（代理商折扣将在控制器中应用）
        if (dayTour.getPrice() != null && dayTourDTO.getDiscountedPrice() == null) {
            dayTourDTO.setDiscountedPrice(dayTour.getPrice());
            log.info("设置折扣价格为原价：{}", dayTour.getPrice());
        }
        
        log.info("完成一日游详情查询，返回DTO：id={}, name={}", dayTourDTO.getId(), dayTourDTO.getName());
        return dayTourDTO;
    }

    /**
     * 获取一日游行程安排
     * @param tourId 一日游ID
     * @param params 查询参数
     * @return 行程安排列表
     */
    @Override
    public List<Map<String, Object>> getDayTourSchedules(Integer tourId, Map<String, Object> params) {
        // 将日期参数提取出来转换为Map供mybatis使用
        List<DayTourSchedule> schedules = dayTourScheduleMapper.getByDayTourId(tourId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (DayTourSchedule schedule : schedules) {
            Map<String, Object> map = new HashMap<>();
            map.put("scheduleId", schedule.getId());
            map.put("dayTourId", schedule.getDayTourId());
            map.put("date", schedule.getScheduleDate());
            map.put("availableSeats", schedule.getAvailableSeats());
            map.put("status", schedule.getStatus());
            map.put("remarks", schedule.getRemarks());
            result.add(map);
        }
        
        return result;
    }

    /**
     * 获取一日游主题列表
     * @return 主题列表
     */
    @Override
    public List<Map<String, Object>> getDayTourThemes() {
        List<DayTourTheme> themes = dayTourThemeMapper.list();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (DayTourTheme theme : themes) {
            Map<String, Object> map = new HashMap<>();
            map.put("themeId", theme.getThemeId());
            map.put("name", theme.getName());
            result.add(map);
        }
        
        return result;
    }

    /**
     * 根据一日游ID获取主题列表
     * @param tourId 一日游ID
     * @return 主题列表
     */
    @Override
    public List<Map<String, Object>> getDayTourThemesByTourId(Integer tourId) {
        List<DayTourTheme> themes = dayTourThemeMapper.getByDayTourId(tourId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (DayTourTheme theme : themes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", theme.getThemeId());
            map.put("themeId", theme.getThemeId()); 
            map.put("name", theme.getName());
            result.add(map);
        }
        
        return result;
    }

    /**
     * 获取一日游图片列表
     * @param dayTourId 一日游ID
     * @return 图片列表
     */
    @Override
    public List<DayTourImage> getImagesByDayTourId(Integer dayTourId) {
        log.info("获取一日游图片列表，dayTourId：{}", dayTourId);
        return dayTourImageMapper.selectByDayTourId(dayTourId);
    }

    /**
     * 获取一日游行程详情，用于行程安排界面
     * @param tourId 一日游ID
     * @return 行程详情列表
     */
    @Override
    public List<Map<String, Object>> getDayTourItinerary(Integer tourId) {
        // 获取一日游行程
        List<DayTourItinerary> itineraries = getItinerariesByDayTourId(tourId);
        
        // 转换为与团队游行程格式一致的Map格式
        List<Map<String, Object>> result = new ArrayList<>();
        if (itineraries != null && !itineraries.isEmpty()) {
            for (DayTourItinerary itinerary : itineraries) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", itinerary.getId());
                map.put("day_tour_id", tourId);
                map.put("day_number", 1); // 一日游只有一天
                map.put("title", itinerary.getActivity()); // 使用activity作为title
                map.put("description", itinerary.getDescription());
                map.put("time", itinerary.getTimeSlot()); // 使用timeSlot作为time
                result.add(map);
            }
        }
        
        return result;
    }
    
    /**
     * 更新产品展示图片
     */
    @Override
    public void updateProductShowcaseImage(Integer dayTourId, String imageUrl) {
        log.info("更新一日游产品展示图片，ID：{}，图片URL：{}", dayTourId, imageUrl);
        DayTour dayTour = new DayTour();
        dayTour.setId(dayTourId);
        dayTour.setDayTourId(dayTourId);
        dayTour.setProductShowcaseImage(imageUrl);
        dayTour.setUpdatedAt(LocalDateTime.now());
        dayTourMapper.update(dayTour);
    }
} 