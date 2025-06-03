package com.sky.service.impl;

import com.sky.dto.TourDTO;
import com.sky.dto.DayTourDTO;
import com.sky.dto.GroupTourDTO;
import com.sky.dto.RegionDTO;
import com.sky.service.*;
import com.sky.vo.TourRecommendationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 旅游产品知识服务实现类
 */
@Service
@Slf4j
public class TourKnowledgeServiceImpl implements TourKnowledgeService {
    
    @Autowired
    private TourService tourService;
    
    @Autowired
    private DayTourService dayTourService;
    
    @Autowired
    private GroupTourService groupTourService;
    
    @Autowired
    private RegionService regionService;
    
    @Autowired
    private DiscountService discountService;
    
    // 地区关键词映射 - 扩展更多同义词和景点名
    private static final Map<String, String> REGION_KEYWORDS = new HashMap<>();
    static {
        REGION_KEYWORDS.put("南部", "南部");
        REGION_KEYWORDS.put("南", "南部");
        REGION_KEYWORDS.put("北部", "北部");
        REGION_KEYWORDS.put("北", "北部");
        REGION_KEYWORDS.put("东部", "东部");
        REGION_KEYWORDS.put("东", "东部");
        REGION_KEYWORDS.put("西部", "西部");
        REGION_KEYWORDS.put("西", "西部");
        REGION_KEYWORDS.put("霍巴特", "霍巴特");
        REGION_KEYWORDS.put("朗塞斯顿", "朗塞斯顿");
        REGION_KEYWORDS.put("酒杯湾", "南部");
        REGION_KEYWORDS.put("摇篮山", "北部");
        REGION_KEYWORDS.put("布鲁尼岛", "南部");
        // 扩展更多景点和地区同义词
        REGION_KEYWORDS.put("菲尔德山", "南部");
        REGION_KEYWORDS.put("亚瑟港", "南部");
        REGION_KEYWORDS.put("塔斯曼半岛", "南部");
        REGION_KEYWORDS.put("里奇蒙", "南部");
        REGION_KEYWORDS.put("MONA", "南部");
        REGION_KEYWORDS.put("萨拉曼卡", "南部");
        REGION_KEYWORDS.put("惠灵顿山", "南部");
        REGION_KEYWORDS.put("德文港", "北部");
        REGION_KEYWORDS.put("伯尼", "北部");
        REGION_KEYWORDS.put("塔玛河谷", "北部");
        REGION_KEYWORDS.put("薰衣草农场", "北部");
        REGION_KEYWORDS.put("费林德斯岛", "北部");
        REGION_KEYWORDS.put("国王岛", "北部");
        REGION_KEYWORDS.put("斯特拉恩", "西部");
        REGION_KEYWORDS.put("戈登河", "西部");
        REGION_KEYWORDS.put("费辛纳", "东部");
        REGION_KEYWORDS.put("比奇诺", "东部");
    }
    
    // 天数关键词
    private static final Map<String, Integer> DURATION_KEYWORDS = new HashMap<>();
    static {
        DURATION_KEYWORDS.put("一日游", 1);
        DURATION_KEYWORDS.put("一天", 1);
        DURATION_KEYWORDS.put("1日", 1);
        DURATION_KEYWORDS.put("1天", 1);
        DURATION_KEYWORDS.put("两日游", 2);
        DURATION_KEYWORDS.put("二日游", 2);
        DURATION_KEYWORDS.put("2日", 2);
        DURATION_KEYWORDS.put("2天", 2);
        DURATION_KEYWORDS.put("三日游", 3);
        DURATION_KEYWORDS.put("3日", 3);
        DURATION_KEYWORDS.put("3天", 3);
        DURATION_KEYWORDS.put("四日游", 4);
        DURATION_KEYWORDS.put("4日", 4);
        DURATION_KEYWORDS.put("4天", 4);
        DURATION_KEYWORDS.put("五日游", 5);
        DURATION_KEYWORDS.put("5日", 5);
        DURATION_KEYWORDS.put("5天", 5);
        DURATION_KEYWORDS.put("六日游", 6);
        DURATION_KEYWORDS.put("6日", 6);
        DURATION_KEYWORDS.put("6天", 6);
        DURATION_KEYWORDS.put("七日游", 7);
        DURATION_KEYWORDS.put("7日", 7);
        DURATION_KEYWORDS.put("7天", 7);
    }
    
    // 产品类型关键词
    private static final Map<String, String> TYPE_KEYWORDS = new HashMap<>();
    static {
        TYPE_KEYWORDS.put("一日游", "day");
        TYPE_KEYWORDS.put("跟团游", "group");
        TYPE_KEYWORDS.put("多日游", "group");
        TYPE_KEYWORDS.put("包车", "day");
        TYPE_KEYWORDS.put("自由行", "day");
    }
    
    // 主题关键词映射 - 新增
    private static final Map<String, String> THEME_KEYWORDS = new HashMap<>();
    static {
        THEME_KEYWORDS.put("美食", "美食");
        THEME_KEYWORDS.put("吃", "美食");
        THEME_KEYWORDS.put("品尝", "美食");
        THEME_KEYWORDS.put("生蚝", "美食");
        THEME_KEYWORDS.put("威士忌", "美食");
        THEME_KEYWORDS.put("芝士", "美食");
        THEME_KEYWORDS.put("摄影", "摄影");
        THEME_KEYWORDS.put("拍照", "摄影");
        THEME_KEYWORDS.put("拍摄", "摄影");
        THEME_KEYWORDS.put("风景", "摄影");
        THEME_KEYWORDS.put("冒险", "冒险");
        THEME_KEYWORDS.put("刺激", "冒险");
        THEME_KEYWORDS.put("徒步", "冒险");
        THEME_KEYWORDS.put("登山", "冒险");
        THEME_KEYWORDS.put("家庭", "家庭");
        THEME_KEYWORDS.put("亲子", "家庭");
        THEME_KEYWORDS.put("小孩", "家庭");
        THEME_KEYWORDS.put("儿童", "家庭");
        THEME_KEYWORDS.put("文化", "文化");
        THEME_KEYWORDS.put("历史", "文化");
        THEME_KEYWORDS.put("博物馆", "文化");
        THEME_KEYWORDS.put("艺术", "文化");
        THEME_KEYWORDS.put("自然", "自然");
        THEME_KEYWORDS.put("野生动物", "自然");
        THEME_KEYWORDS.put("国家公园", "自然");
        THEME_KEYWORDS.put("生态", "自然");
    }
    
    // 价格相关关键词 - 新增
    private static final Set<String> PRICE_KEYWORDS = new HashSet<>();
    static {
        PRICE_KEYWORDS.add("价格");
        PRICE_KEYWORDS.add("费用");
        PRICE_KEYWORDS.add("多少钱");
        PRICE_KEYWORDS.add("贵不贵");
        PRICE_KEYWORDS.add("便宜");
        PRICE_KEYWORDS.add("优惠");
        PRICE_KEYWORDS.add("折扣");
        PRICE_KEYWORDS.add("特价");
        PRICE_KEYWORDS.add("促销");
        PRICE_KEYWORDS.add("团费");
        PRICE_KEYWORDS.add("报价");
    }
    
    // 时间相关关键词 - 新增
    private static final Set<String> TIME_KEYWORDS = new HashSet<>();
    static {
        TIME_KEYWORDS.add("什么时候");
        TIME_KEYWORDS.add("几点");
        TIME_KEYWORDS.add("时间");
        TIME_KEYWORDS.add("出发时间");
        TIME_KEYWORDS.add("集合时间");
        TIME_KEYWORDS.add("接送时间");
        TIME_KEYWORDS.add("行程时间");
        TIME_KEYWORDS.add("游玩时间");
        TIME_KEYWORDS.add("返回时间");
    }
    
    @Override
    public TourRecommendationResponse getProductRecommendations(String userMessage, Long agentId) {
        log.info("开始处理产品推荐请求: {}", userMessage);
        
        try {
            // 1. 解析用户意图
            QueryIntent intent = parseUserIntent(userMessage);
            log.info("解析到的用户意图: {}", intent);
            
            // 2. 根据意图查询产品
            List<TourDTO> tours = queryToursByIntent(intent, agentId);
            log.info("查询到 {} 个产品", tours.size());
            
            // 3. 应用代理商折扣
            if (agentId != null && tours != null) {
                applyDiscountToTours(tours, agentId);
            }
            
            // 4. 生成推荐响应
            return TourRecommendationResponse.builder()
                    .recommendedTours(tours.size() > 8 ? tours.subList(0, 8) : tours)
                    .recommendationReason(generateRecommendationReason(intent, tours.size()))
                    .queryType(intent.tourType)
                    .extractedKeywords(intent.keywords)
                    .hasMore(tours.size() > 8)
                    .totalCount(tours.size())
                    .searchSuggestion(generateSearchSuggestion(intent, tours.size()))
                    .build();
                    
        } catch (Exception e) {
            log.error("处理产品推荐失败: {}", e.getMessage(), e);
            return TourRecommendationResponse.builder()
                    .recommendedTours(new ArrayList<>())
                    .recommendationReason("抱歉，暂时无法为您推荐合适的产品，请稍后再试")
                    .queryType("unknown")
                    .extractedKeywords(new ArrayList<>())
                    .hasMore(false)
                    .totalCount(0)
                    .build();
        }
    }
    
    @Override
    public List<TourDTO> searchToursByKeywords(String keywords, String tourType, Long agentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keywords);
        params.put("pageNum", 1);
        params.put("pageSize", 20);
        
        List<TourDTO> tours = new ArrayList<>();
        
        if ("day".equals(tourType)) {
            tours.addAll(convertDayToursToTourDTO(dayTourService.getAllDayTours(params).getRecords()));
        } else if ("group".equals(tourType)) {
            tours.addAll(convertGroupToursToTourDTO(groupTourService.getAllGroupTours(params).getRecords()));
        } else {
            // 搜索所有类型
            tours.addAll(convertDayToursToTourDTO(dayTourService.getAllDayTours(params).getRecords()));
            tours.addAll(convertGroupToursToTourDTO(groupTourService.getAllGroupTours(params).getRecords()));
        }
        
        if (agentId != null) {
            applyDiscountToTours(tours, agentId);
        }
        
        return tours;
    }
    
    @Override
    public List<TourDTO> getHotTours(Integer limit, Long agentId) {
        List<TourDTO> tours = tourService.getHotTours(limit != null ? limit : 10);
        
        if (agentId != null) {
            applyDiscountToTours(tours, agentId);
        }
        
        return tours;
    }
    
    @Override
    public List<TourDTO> getToursByRegion(String regionName, String tourType, Long agentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("region", regionName);
        params.put("pageNum", 1);
        params.put("pageSize", 20);
        
        List<TourDTO> tours = new ArrayList<>();
        
        if ("day".equals(tourType)) {
            tours.addAll(convertDayToursToTourDTO(dayTourService.getAllDayTours(params).getRecords()));
        } else if ("group".equals(tourType)) {
            tours.addAll(convertGroupToursToTourDTO(groupTourService.getAllGroupTours(params).getRecords()));
        } else {
            tours.addAll(convertDayToursToTourDTO(dayTourService.getAllDayTours(params).getRecords()));
            tours.addAll(convertGroupToursToTourDTO(groupTourService.getAllGroupTours(params).getRecords()));
        }
        
        if (agentId != null) {
            applyDiscountToTours(tours, agentId);
        }
        
        return tours;
    }
    
    @Override
    public List<TourDTO> getToursByDuration(Integer days, Long agentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("duration", days);
        params.put("pageNum", 1);
        params.put("pageSize", 20);
        
        List<TourDTO> tours = new ArrayList<>();
        
        if (days == 1) {
            // 一日游
            tours.addAll(convertDayToursToTourDTO(dayTourService.getAllDayTours(params).getRecords()));
        } else {
            // 多日游
            tours.addAll(convertGroupToursToTourDTO(groupTourService.getAllGroupTours(params).getRecords()));
        }
        
        if (agentId != null) {
            applyDiscountToTours(tours, agentId);
        }
        
        return tours;
    }
    
    @Override
    public List<TourDTO> getToursByTheme(String theme, Long agentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("theme", theme);
        params.put("pageNum", 1);
        params.put("pageSize", 20);
        
        List<TourDTO> tours = new ArrayList<>();
        tours.addAll(convertDayToursToTourDTO(dayTourService.getAllDayTours(params).getRecords()));
        tours.addAll(convertGroupToursToTourDTO(groupTourService.getAllGroupTours(params).getRecords()));
        
        if (agentId != null) {
            applyDiscountToTours(tours, agentId);
        }
        
        return tours;
    }
    
    @Override
    public List<RegionDTO> getAllRegions() {
        return regionService.getAllRegions();
    }
    
    @Override
    public List<Map<String, Object>> getAllThemes() {
        List<Map<String, Object>> allThemes = new ArrayList<>();
        allThemes.addAll(dayTourService.getDayTourThemes());
        allThemes.addAll(groupTourService.getGroupTourThemes());
        return allThemes;
    }
    
    @Override
    public String generateAISystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是Happy Tassie Travel的AI旅游助手，专门为客户推荐塔斯马尼亚的旅游产品。\n\n");
        prompt.append("【产品类型】\n");
        prompt.append("1. 一日游 (day tours) - 单日行程，适合时间有限的游客\n");
        prompt.append("2. 跟团游 (group tours) - 多日行程，深度体验塔斯马尼亚\n\n");
        
        prompt.append("【地区分布】\n");
        List<RegionDTO> regions = getAllRegions();
        for (RegionDTO region : regions) {
            prompt.append("- ").append(region.getName()).append(": ").append(region.getDescription()).append("\n");
        }
        
        prompt.append("\n【热门主题】\n");
        List<Map<String, Object>> themes = getAllThemes();
        Set<String> uniqueThemes = new HashSet<>();
        for (Map<String, Object> theme : themes) {
            String themeName = (String) theme.get("name");
            if (themeName != null && uniqueThemes.add(themeName)) {
                prompt.append("- ").append(themeName).append("\n");
            }
        }
        
        prompt.append("\n【服务原则】\n");
        prompt.append("1. 只提供查询服务，不处理预订、修改、删除操作\n");
        prompt.append("2. 根据用户需求推荐最合适的产品\n");
        prompt.append("3. 提供详细的产品信息和旅游建议\n");
        prompt.append("4. 对于具体预订需求，引导用户联系客服\n\n");
        
        prompt.append("【回答格式】\n");
        prompt.append("当用户询问产品时，请：\n");
        prompt.append("1. 分析用户需求（地区、天数、主题等）\n");
        prompt.append("2. 推荐合适的产品\n");
        prompt.append("3. 简要介绍产品亮点\n");
        prompt.append("4. 如需更多信息或预订，建议联系客服\n");
        
        return prompt.toString();
    }
    
    /**
     * 解析用户查询意图
     */
    private QueryIntent parseUserIntent(String userMessage) {
        QueryIntent intent = new QueryIntent();
        intent.keywords = new ArrayList<>();
        intent.originalMessage = userMessage;
        
        // 提取地区信息
        for (Map.Entry<String, String> entry : REGION_KEYWORDS.entrySet()) {
            if (userMessage.contains(entry.getKey())) {
                intent.region = entry.getValue();
                intent.keywords.add(entry.getKey());
                break;
            }
        }
        
        // 提取天数信息 - 改进匹配逻辑，优先精确匹配
        Integer exactDuration = null;
        String matchedDurationKeyword = null;
        
        // 按优先级顺序检查天数关键词
        String[] durationPriority = {"五日游", "5日", "5天", "四日游", "4日", "4天", "三日游", "3日", "3天", "六日游", "6日", "6天", "七日游", "7日", "7天", "二日游", "两日游", "2日", "2天", "一日游", "一天", "1日", "1天"};
        
        for (String durationKey : durationPriority) {
            if (userMessage.contains(durationKey)) {
                exactDuration = DURATION_KEYWORDS.get(durationKey);
                matchedDurationKeyword = durationKey;
                break;
            }
        }
        
        if (exactDuration != null) {
            intent.duration = exactDuration;
            intent.keywords.add(matchedDurationKeyword);
            log.info("精确匹配到天数: {} -> {} 天", matchedDurationKeyword, exactDuration);
        }
        
        // 提取产品类型
        for (Map.Entry<String, String> entry : TYPE_KEYWORDS.entrySet()) {
            if (userMessage.contains(entry.getKey())) {
                intent.tourType = entry.getValue();
                intent.keywords.add(entry.getKey());
                break;
            }
        }
        
        // 默认类型判断
        if (intent.tourType == null) {
            if (intent.duration != null && intent.duration == 1) {
                intent.tourType = "day";
            } else if (intent.duration != null && intent.duration > 1) {
                intent.tourType = "group";
            } else {
                intent.tourType = "all";
            }
        }
        
        // 提取其他关键词
        String[] commonKeywords = {"景点", "美食", "文化", "自然", "冒险", "家庭", "情侣", "摄影"};
        for (String keyword : commonKeywords) {
            if (userMessage.contains(keyword)) {
                intent.keywords.add(keyword);
            }
        }
        
        return intent;
    }
    
    /**
     * 根据意图查询产品
     */
    private List<TourDTO> queryToursByIntent(QueryIntent intent, Long agentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", 1);
        params.put("pageSize", 20);
        
        if (intent.region != null) {
            params.put("region", intent.region);
        }
        
        if (intent.duration != null) {
            params.put("duration", intent.duration);
        }
        
        List<TourDTO> tours = new ArrayList<>();
        
        if ("day".equals(intent.tourType)) {
            tours.addAll(convertDayToursToTourDTO(dayTourService.getAllDayTours(params).getRecords()));
        } else if ("group".equals(intent.tourType)) {
            tours.addAll(convertGroupToursToTourDTO(groupTourService.getAllGroupTours(params).getRecords()));
        } else {
            // 查询所有类型，但优先推荐匹配的
            tours.addAll(convertDayToursToTourDTO(dayTourService.getAllDayTours(params).getRecords()));
            tours.addAll(convertGroupToursToTourDTO(groupTourService.getAllGroupTours(params).getRecords()));
        }
        
        // 如果指定了天数，过滤出精确匹配的产品
        if (intent.duration != null && intent.duration > 1) {
            log.info("过滤前产品数量: {}", tours.size());
            tours = tours.stream()
                    .filter(tour -> {
                        // 检查产品名称是否包含对应的天数
                        String name = tour.getName();
                        if (name == null) return false;
                        
                        // 精确匹配天数
                        boolean matches = name.contains(intent.duration + "日") || 
                                         name.contains(intent.duration + "天") ||
                                         name.contains(getChineseNumber(intent.duration) + "日");
                        
                        log.info("产品 '{}' 天数匹配检查: {} (目标: {}天)", name, matches, intent.duration);
                        return matches;
                    })
                    .collect(Collectors.toList());
            log.info("过滤后产品数量: {}", tours.size());
        }
        
        // 如果精确匹配没有结果，放宽条件查询相近天数的产品
        if (tours.isEmpty() && intent.duration != null && intent.duration > 1) {
            log.info("精确匹配无结果，查询相近天数产品");
            params.remove("duration"); // 移除精确天数限制
            
            List<TourDTO> allTours = new ArrayList<>();
            if ("group".equals(intent.tourType) || "all".equals(intent.tourType)) {
                allTours.addAll(convertGroupToursToTourDTO(groupTourService.getAllGroupTours(params).getRecords()));
            }
            
            // 按相近天数排序 (±1天范围内优先)
            tours = allTours.stream()
                    .filter(tour -> {
                        String name = tour.getName();
                        if (name == null) return false;
                        
                        // 查找相近天数 (目标天数±1)
                        for (int days = intent.duration - 1; days <= intent.duration + 1; days++) {
                            if (days > 0 && (name.contains(days + "日") || 
                                           name.contains(days + "天") ||
                                           name.contains(getChineseNumber(days) + "日"))) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .sorted((a, b) -> {
                        // 优先显示精确匹配天数的产品
                        int scoreA = getTourDurationScore(a.getName(), intent.duration);
                        int scoreB = getTourDurationScore(b.getName(), intent.duration);
                        return Integer.compare(scoreB, scoreA); // 降序排列，分数高的在前
                    })
                    .collect(Collectors.toList());
                    
            log.info("相近天数查询结果: {} 个产品", tours.size());
        }
        
        // 如果还是没有找到匹配的产品，返回热门产品
        if (tours.isEmpty()) {
            log.info("未找到匹配产品，返回热门产品");
            tours = getHotTours(10, agentId);
        }
        
        return tours;
    }
    
    /**
     * 获取中文数字
     */
    private String getChineseNumber(Integer number) {
        if (number == null) return "";
        switch (number) {
            case 1: return "一";
            case 2: return "二";
            case 3: return "三";
            case 4: return "四";
            case 5: return "五";
            case 6: return "六";
            case 7: return "七";
            case 8: return "八";
            case 9: return "九";
            case 10: return "十";
            default: return number.toString();
        }
    }
    
    /**
     * 计算产品天数匹配分数
     */
    private int getTourDurationScore(String tourName, Integer targetDuration) {
        if (tourName == null || targetDuration == null) return 0;
        
        // 精确匹配得10分
        if (tourName.contains(targetDuration + "日") || 
            tourName.contains(targetDuration + "天") ||
            tourName.contains(getChineseNumber(targetDuration) + "日")) {
            return 10;
        }
        
        // 相近天数得分递减
        for (int diff = 1; diff <= 2; diff++) {
            // 检查 ±diff 天
            int lowerDays = targetDuration - diff;
            int higherDays = targetDuration + diff;
            
            if (lowerDays > 0 && (tourName.contains(lowerDays + "日") || 
                                 tourName.contains(lowerDays + "天") ||
                                 tourName.contains(getChineseNumber(lowerDays) + "日"))) {
                return 10 - diff * 2;
            }
            
            if (tourName.contains(higherDays + "日") || 
                tourName.contains(higherDays + "天") ||
                tourName.contains(getChineseNumber(higherDays) + "日")) {
                return 10 - diff * 2;
            }
        }
        
        return 0;
    }
    
    /**
     * 转换DayTourDTO到TourDTO
     */
    private List<TourDTO> convertDayToursToTourDTO(List<?> dayTours) {
        List<TourDTO> tours = new ArrayList<>();
        for (Object obj : dayTours) {
            if (obj instanceof DayTourDTO) {
                DayTourDTO dayTour = (DayTourDTO) obj;
                TourDTO tour = new TourDTO();
                BeanUtils.copyProperties(dayTour, tour);
                tour.setTourType("day");
                tours.add(tour);
            }
        }
        return tours;
    }
    
    /**
     * 转换GroupTourDTO到TourDTO
     */
    private List<TourDTO> convertGroupToursToTourDTO(List<?> groupTours) {
        List<TourDTO> tours = new ArrayList<>();
        for (Object obj : groupTours) {
            if (obj instanceof GroupTourDTO) {
                GroupTourDTO groupTour = (GroupTourDTO) obj;
                TourDTO tour = new TourDTO();
                BeanUtils.copyProperties(groupTour, tour);
                tour.setTourType("group");
                tours.add(tour);
            }
        }
        return tours;
    }
    
    /**
     * 应用代理商折扣
     */
    private void applyDiscountToTours(List<TourDTO> tours, Long agentId) {
        for (TourDTO tour : tours) {
            if (tour.getPrice() != null) {
                try {
                    tour.setDiscountedPrice(discountService.getDiscountedPrice(tour.getPrice(), agentId));
                } catch (Exception e) {
                    log.warn("应用折扣失败: {}", e.getMessage());
                    tour.setDiscountedPrice(tour.getPrice());
                }
            }
        }
    }
    
    /**
     * 生成推荐理由
     */
    private String generateRecommendationReason(QueryIntent intent, int resultCount) {
        StringBuilder reason = new StringBuilder();
        
        if (resultCount == 0) {
            reason.append("抱歉，没有找到完全匹配的产品。");
            return reason.toString();
        }
        
        reason.append("根据您的需求");
        
        if (intent.region != null) {
            reason.append("（").append(intent.region).append("地区");
        }
        
        if (intent.duration != null) {
            if (reason.length() > 10) reason.append("，");
            reason.append(intent.duration).append("天行程");
        }
        
        if (intent.keywords.size() > 2) {
            if (reason.length() > 10) reason.append("，");
            reason.append("包含").append(String.join("、", intent.keywords.subList(0, 2)));
        }
        
        if (reason.length() > 10) {
            reason.append("）");
        }
        
        reason.append("，为您推荐了").append(Math.min(resultCount, 8)).append("个精选产品。");
        
        return reason.toString();
    }
    
    /**
     * 生成搜索建议
     */
    private String generateSearchSuggestion(QueryIntent intent, int resultCount) {
        if (resultCount > 8) {
            return "显示了前8个推荐产品，如需查看更多选择，请提供更具体的需求。";
        } else if (resultCount == 0) {
            return "建议尝试其他关键词，如'一日游'、'跟团游'或具体地区名称。";
        } else {
            return "以上是为您精选的产品，如需了解详细信息或预订，请联系客服。";
        }
    }
    
    /**
     * 根据关键词搜索旅游知识
     * @param keyword 关键词
     * @return 搜索结果
     */
    @Override
    public String searchByKeyword(String keyword) {
        log.info("搜索旅游知识，关键词: {}", keyword);
        
        try {
            // 解析用户意图并获取产品推荐
            TourRecommendationResponse response = getProductRecommendations(keyword, null);
            
            if (response.getRecommendedTours() != null && !response.getRecommendedTours().isEmpty()) {
                StringBuilder result = new StringBuilder();
                result.append("为您找到以下相关产品：\n\n");
                
                for (int i = 0; i < Math.min(3, response.getRecommendedTours().size()); i++) {
                    TourDTO tour = response.getRecommendedTours().get(i);
                    result.append(String.format("%d. %s\n", i + 1, tour.getName()));
                    if (tour.getPrice() != null) {
                        result.append(String.format("   价格：$%.2f\n", tour.getPrice()));
                    }
                    result.append("\n");
                }
                
                result.append("如需了解更多产品或预订，请联系客服。");
                return result.toString();
            } else {
                return "抱歉，未找到相关的旅游产品。建议尝试其他关键词，如'霍巴特'、'一日游'等。";
            }
        } catch (Exception e) {
            log.error("搜索旅游知识失败: {}", e.getMessage(), e);
            return "搜索服务暂时不可用，请稍后再试。";
        }
    }
    
    /**
     * 查询意图内部类
     */
    private static class QueryIntent {
        String region;           // 地区
        Integer duration;        // 天数
        String tourType;         // 产品类型
        List<String> keywords;   // 关键词
        String originalMessage;  // 原始消息
        
        @Override
        public String toString() {
            return String.format("QueryIntent{region='%s', duration=%d, tourType='%s', keywords=%s}", 
                    region, duration, tourType, keywords);
        }
    }
} 