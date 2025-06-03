package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.dto.ChatRequest;
import com.sky.dto.GroupTourDTO;
import com.sky.dto.OrderInfo;
import com.sky.entity.ChatMessage;
import com.sky.entity.TourBooking;
import com.sky.entity.Passenger;
import com.sky.mapper.ChatMessageMapper;
import com.sky.mapper.GroupTourMapper;
import com.sky.mapper.TourBookingMapper;
import com.sky.mapper.PassengerMapper;
import com.sky.mapper.DayTourFaqMapper;
import com.sky.service.ChatBotService;
import com.sky.service.TourKnowledgeService;
import com.sky.vo.ChatResponse;
import com.sky.vo.TourRecommendationResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 聊天机器人服务实现类
 */
@Service
@Slf4j
public class ChatBotServiceImpl implements ChatBotService {
    
    @Value("${deepseek.api.key:}")
    private String deepseekApiKey;
    
    @Value("${deepseek.api.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;
    
    @Value("${deepseek.model:deepseek-chat}")
    private String deepseekModel;
    
    @Value("${deepseek.timeout:30000}")
    private int deepseekTimeout;
    
    @Value("${deepseek.max-tokens:150}")
    private int deepseekMaxTokens;
    
    @Value("${deepseek.temperature:0.7}")
    private double deepseekTemperature;
    
    @Value("${flight.api.aviationstack.api-key:}")
    private String aviationStackApiKey;
    
    @Value("${flight.api.aviationstack.base-url:http://api.aviationstack.com/v1}")
    private String aviationStackBaseUrl;
    
    @Value("${flight.api.aviationstack.enabled:false}")
    private boolean aviationStackEnabled;
    
    @Value("${weather.openweathermap.api-key:}")
    private String weatherApiKey;
    
    @Value("${weather.openweathermap.base-url:http://api.openweathermap.org/data/2.5}")
    private String weatherApiBaseUrl;
    
    @Value("${weather.openweathermap.enabled:false}")
    private boolean weatherApiEnabled;
    
    @Value("${weather.openweathermap.cache-duration:600}")
    private int weatherCacheDuration;
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private GroupTourMapper groupTourMapper;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private TourBookingMapper tourBookingMapper;
    
    @Autowired
    private PassengerMapper passengerMapper;
    
    @Autowired
    private DayTourFaqMapper dayTourFaqMapper;
    
    @Autowired
    private TourKnowledgeService tourKnowledgeService;
    
    private OkHttpClient httpClient;
    
    @PostConstruct
    public void init() {
        // 初始化HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(deepseekTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(deepseekTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(deepseekTimeout, TimeUnit.MILLISECONDS)
                .build();
                
        if (deepseekApiKey != null && !deepseekApiKey.isEmpty()) {
            log.info("DeepSeek AI服务初始化成功，模型: {}", deepseekModel);
        } else {
            log.warn("DeepSeek API Key未配置，聊天功能将受限");
        }
    }
    
    @Override
    public ChatResponse processMessage(ChatRequest request) {
        try {
            // 1. 检查限流
            if (!checkRateLimit(request.getSessionId(), request.getUserId())) {
                return ChatResponse.error("请求过于频繁，请稍后再试", "RATE_LIMIT");
            }
            
            // 2. 检查是否为结构化订单信息
            if (isStructuredOrderData(request.getMessage())) {
                return handleOrderData(request);
            }
            
            // 3. 普通问答处理
            return handleGeneralQuestion(request);
            
        } catch (Exception e) {
            log.error("处理消息失败: {}", e.getMessage(), e);
            return ChatResponse.error("抱歉，我现在无法回答您的问题，请稍后重试。");
        }
    }
    
    @Override
    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatMessageMapper.selectBySessionId(sessionId);
    }
    
    @Override
    public boolean checkRateLimit(String sessionId, Long userId) {
        try {
            String key = "chatbot:rate:" + (userId != null ? userId : sessionId);
            String count = redisTemplate.opsForValue().get(key);
            
            if (count == null) {
                redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
                return true;
            }
            
            int currentCount = Integer.parseInt(count);
            if (currentCount >= 10) { // 每分钟最多10次
                return false;
            }
            
            redisTemplate.opsForValue().increment(key);
            return true;
        } catch (Exception e) {
            // Redis连接失败时跳过限流检查，记录警告日志
            log.warn("Redis连接失败，跳过限流检查: {}", e.getMessage());
            return true; // 允许请求通过
        }
    }
    
    /**
     * 调用DeepSeek AI服务
     */
    private String callDeepSeekAI(String prompt) {
        if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
            throw new RuntimeException("DeepSeek API Key未配置");
        }
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", deepseekModel);
            
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", deepseekMaxTokens);
            requestBody.put("temperature", deepseekTemperature);

            RequestBody body = RequestBody.create(
                requestBody.toString(), 
                MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                .url(deepseekBaseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + deepseekApiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("DeepSeek API调用失败: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                
                String content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
                
                // 清理DeepSeek响应中的markdown代码块标记
                if (content.startsWith("```json")) {
                    content = content.substring(7); // 移除 "```json"
                }
                if (content.endsWith("```")) {
                    content = content.substring(0, content.length() - 3); // 移除结尾的 "```"
                }
                content = content.trim(); // 去除首尾空白
                
                return content;
            }
        } catch (IOException e) {
            log.error("DeepSeek API调用异常", e);
            throw new RuntimeException("DeepSeek AI调用失败: " + e.getMessage());
        }
    }

    /**
     * 使用AI智能识别结构化订单数据（增强版）
     */
    private boolean isStructuredOrderDataWithAI(String message) {
        // 如果DeepSeek未配置，回退到传统方法
        if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
            return isStructuredOrderDataTraditional(message);
        }
        
        try {
            // 构建更强大的AI提示，能识别多种格式
            String aiPrompt = "请分析以下文本，判断是否为旅游订单信息。我需要你识别各种可能的格式：\n\n" +
                    "## 常见订单格式示例：\n" +
                    "1. 结构化格式：包含\"服务类型：\"、\"参团日期：\"等标签\n" +
                    "2. 自然语言格式：如\"我要预订塔州4日游，5月29日出发，3个人\"\n" +
                    "3. 表格格式：包含客户姓名、护照号、电话等信息\n" +
                    "4. 混合格式：部分结构化，部分自然语言\n" +
                    "5. 列表格式：用换行或分隔符分隔的信息\n" +
                    "6. 对话格式：客户与客服的对话记录\n\n" +
                    "## 关键识别要素（至少包含2-3个）：\n" +
                    "- 旅游服务类型（跟团游、一日游、包车、塔州游等）\n" +
                    "- 日期信息（出行日期、参团日期、X月X日格式）\n" +
                    "- 人员信息（姓名、护照号、电话、人数）\n" +
                    "- 住宿信息（酒店星级、房型、三人房等）\n" +
                    "- 航班信息（航班号如JQ719、起降时间）\n" +
                    "- 行程安排（天数、景点、活动）\n" +
                    "- 地理位置（塔斯马尼亚、霍巴特、朗塞斯顿等）\n\n" +
                    "## 判断标准：\n" +
                    "- 高置信度(0.8+)：包含明确的服务类型+日期+客户信息\n" +
                    "- 中置信度(0.6-0.8)：包含服务类型+日期或客户信息\n" +
                    "- 低置信度(0.4-0.6)：只包含部分旅游相关信息\n" +
                    "- 非订单(0.0-0.4)：主要是问询、闲聊或其他内容\n\n" +
                    "请返回JSON格式：\n" +
                    "{\n" +
                    "  \"isOrderData\": true/false,\n" +
                    "  \"confidence\": 0.0-1.0,\n" +
                    "  \"detected_elements\": [\n" +
                    "    \"service_type\", \"travel_date\", \"customer_info\",\n" +
                    "    \"flight_info\", \"hotel_info\", \"itinerary\", \"notes\"\n" +
                    "  ],\n" +
                    "  \"format_type\": \"structured|natural|table|mixed|list|conversation\",\n" +
                    "  \"reasoning\": \"详细的判断理由，说明识别到的关键信息\",\n" +
                    "  \"key_indicators\": [\"识别到的关键指标列表\"]\n" +
                    "}\n\n" +
                    "分析文本：\n" + message;
            
            String aiResponse = callDeepSeekAI(aiPrompt);
            
            log.info("AI智能识别响应: {}", aiResponse);
            
            try {
                JSONObject result = JSON.parseObject(aiResponse);
                boolean isOrderData = result.getBooleanValue("isOrderData");
                double confidence = result.getDoubleValue("confidence");
                
                // 置信度阈值设为0.6，避免误判
                boolean finalResult = isOrderData && confidence >= 0.6;
                
                log.info("AI智能识别结果: isOrderData={}, confidence={}, finalResult={}", 
                    isOrderData, confidence, finalResult);
                
                // 记录识别的元素和格式类型，用于优化
                JSONArray detectedElements = result.getJSONArray("detected_elements");
                String formatType = result.getString("format_type");
                String reasoning = result.getString("reasoning");
                
                log.debug("AI识别详情 - 检测到的元素: {}, 格式类型: {}, 推理: {}", 
                    detectedElements, formatType, reasoning);
                
                return finalResult;
                
            } catch (Exception parseEx) {
                log.warn("解析AI响应JSON失败，回退到传统方法: {}", parseEx.getMessage());
                return isStructuredOrderDataTraditional(message);
            }
            
        } catch (Exception e) {
            log.error("AI智能识别失败，回退到传统方法: {}", e.getMessage());
            return isStructuredOrderDataTraditional(message);
        }
    }
    
    /**
     * 传统的基于关键词的订单数据识别（重命名原方法）
     */
    private boolean isStructuredOrderDataTraditional(String message) {
        // 检查基本的结构化订单字段
        boolean hasServiceType = message.contains("服务类型：");
        boolean hasDate = message.contains("参团日期") || message.contains("出行日期") || message.contains("日期");
        boolean hasCustomerInfo = message.contains("客户信息：") || message.contains("乘客信息:") || message.contains("乘客信息：");
        
        // 如果有基础的三个字段，认为是结构化订单数据
        if (hasServiceType && hasDate && hasCustomerInfo) {
            return true;
        }
        
        // 更宽松的检查：只要有服务类型和日期信息，且消息较长（说明包含详细信息）
        if (hasServiceType && hasDate && message.length() > 100) {
            log.info("检测到可能的结构化订单数据（传统宽松模式）");
            return true;
        }
        
        // 检查是否包含多个订单关键字段
        int orderFieldCount = 0;
        if (message.contains("服务类型：")) orderFieldCount++;
        if (message.contains("参团日期") || message.contains("出行日期")) orderFieldCount++;
        if (message.contains("航班") || message.contains("出发时间")) orderFieldCount++;
        if (message.contains("乘客信息") || message.contains("客户信息")) orderFieldCount++;
        if (message.contains("房型") || message.contains("酒店")) orderFieldCount++;
        if (message.contains("行程安排") || message.contains("备注")) orderFieldCount++;
        
        // 如果包含3个或更多订单字段，认为是结构化数据
        if (orderFieldCount >= 3) {
            log.info("传统方法检测到结构化订单数据，包含{}个订单字段", orderFieldCount);
            return true;
        }
        
        return false;
    }

    /**
     * 检查是否为结构化订单数据（主入口方法）
     */
    private boolean isStructuredOrderData(String message) {
        // 优先使用AI智能识别
        return isStructuredOrderDataWithAI(message);
    }
    
    /**
     * 处理订单数据
     */
    private ChatResponse handleOrderData(ChatRequest request) {
        try {
            // 解析订单信息
            OrderInfo orderInfo = parseOrderInfo(request.getMessage());
            
            // 智能产品匹配
            GroupTourDTO product = findMatchingProduct(orderInfo.getServiceType());
            
            if (product != null) {
                log.info("找到匹配的产品: ID={}, 名称={}", product.getId(), product.getName());
                
                // 生成订单URL参数
                String orderParams = generateOrderParams(orderInfo, product);
                
                // 保存聊天记录
                String responseMessage = "订单信息已解析完成，找到产品：" + product.getName() + "，正在跳转到订单页面...";
                saveChatMessage(request, responseMessage, 2, JSON.toJSONString(orderInfo));
                
                return ChatResponse.orderSuccess(
                    responseMessage,
                    JSON.toJSONString(orderInfo),
                    "/booking?" + orderParams
                );
            } else {
                // 未找到匹配产品，提供相似产品选择
                List<GroupTourDTO> similarProducts = findSimilarProducts(orderInfo.getServiceType());
                String responseMessage = buildProductSelectionMessage(orderInfo.getServiceType(), similarProducts);
                
                saveChatMessage(request, responseMessage, 1, JSON.toJSONString(orderInfo));
                return ChatResponse.success(responseMessage);
            }
            
        } catch (Exception e) {
            log.error("解析订单信息失败: {}", e.getMessage(), e);
            return ChatResponse.error("订单信息格式不正确，请检查后重新发送");
        }
    }
    
    /**
     * 智能产品匹配
     */
    private GroupTourDTO findMatchingProduct(String serviceType) {
        if (serviceType == null || serviceType.isEmpty()) {
            return null;
        }
        
        // 1. 先尝试完全匹配
        GroupTourDTO product = groupTourMapper.findByNameLike(serviceType);
        if (product != null) {
            return product;
        }
        
        // 2. 提取关键词进行智能匹配
        String[] keywords = extractKeywords(serviceType);
        if (keywords.length > 0) {
            List<GroupTourDTO> products = groupTourMapper.findByKeywords(keywords);
            if (!products.isEmpty()) {
                return products.get(0); // 返回最匹配的产品
            }
        }
        
        return null;
    }
    
    /**
     * 查找相似产品
     */
    private List<GroupTourDTO> findSimilarProducts(String serviceType) {
        if (serviceType == null || serviceType.isEmpty()) {
            return groupTourMapper.findAllActive();
        }
        
        // 使用关键词搜索相似产品
        String[] keywords = extractKeywords(serviceType);
        if (keywords.length > 0) {
            List<GroupTourDTO> products = groupTourMapper.findByKeywords(keywords);
            if (!products.isEmpty()) {
                return products;
            }
        }
        
        // 如果关键词搜索没有结果，返回所有活跃产品
        return groupTourMapper.findAllActive();
    }
    
    /**
     * 提取关键词（改进版）
     */
    private String[] extractKeywords(String serviceType) {
        List<String> keywords = new ArrayList<>();
        
        // 先进行文本标准化
        String normalizedText = normalizeText(serviceType);
        
        // 地区关键词（同义词映射）
        if (normalizedText.contains("塔州") || normalizedText.contains("塔斯马尼亚") || normalizedText.contains("tasmania")) {
            keywords.add("塔斯马尼亚");
        }
        
        // 方位关键词
        if (normalizedText.contains("南部") || normalizedText.contains("南")) {
            keywords.add("南部");
        } else if (normalizedText.contains("北部") || normalizedText.contains("北")) {
            keywords.add("北部");
        } else if (normalizedText.contains("东部") || normalizedText.contains("东")) {
            keywords.add("东部");
        } else if (normalizedText.contains("西部") || normalizedText.contains("西")) {
            keywords.add("西部");
        }
        
        // 天数关键词（支持中文数字和阿拉伯数字）
        if (normalizedText.contains("1日") || normalizedText.contains("一日") || normalizedText.contains("1天")) {
            keywords.add("1日");
        } else if (normalizedText.contains("2日") || normalizedText.contains("二日") || normalizedText.contains("2天") || normalizedText.contains("两日")) {
            keywords.add("2日");
        } else if (normalizedText.contains("3日") || normalizedText.contains("三日") || normalizedText.contains("3天")) {
            keywords.add("3日");
        } else if (normalizedText.contains("4日") || normalizedText.contains("四日") || normalizedText.contains("4天")) {
            keywords.add("4日");
        } else if (normalizedText.contains("5日") || normalizedText.contains("五日") || normalizedText.contains("5天")) {
            keywords.add("5日");
        } else if (normalizedText.contains("6日") || normalizedText.contains("六日") || normalizedText.contains("6天")) {
            keywords.add("6日");
        } else if (normalizedText.contains("7日") || normalizedText.contains("七日") || normalizedText.contains("7天")) {
            keywords.add("7日");
        }
        
        // 特色关键词
        if (normalizedText.contains("环岛")) {
            keywords.add("环岛");
        }
        if (normalizedText.contains("精华")) {
            keywords.add("精华");
        }
        if (normalizedText.contains("全景")) {
            keywords.add("全景");
        }
        
        log.info("原始服务类型: {}, 提取的关键词: {}", serviceType, keywords);
        return keywords.toArray(new String[0]);
    }
    
    /**
     * 文本标准化
     */
    private String normalizeText(String text) {
        if (text == null) return "";
        
        // 转换为小写并去除空格
        String normalized = text.toLowerCase().replaceAll("\\s+", "");
        
        // 同义词替换
        normalized = normalized
            .replace("塔州", "塔斯马尼亚")
            .replace("tasmania", "塔斯马尼亚")
            .replace("跟团游", "")
            .replace("跟团", "")
            .replace("团游", "")
            .replace("游", "");
            
        return normalized;
    }
    
    /**
     * 构建产品选择消息
     */
    private String buildProductSelectionMessage(String originalServiceType, List<GroupTourDTO> products) {
        StringBuilder message = new StringBuilder();
        message.append("抱歉，没有找到完全匹配\"").append(originalServiceType).append("\"的产品。\n\n");
        message.append("以下是为您推荐的相似产品：\n\n");
        
        for (int i = 0; i < Math.min(products.size(), 5); i++) {
            GroupTourDTO product = products.get(i);
            message.append(String.format("%d. %s\n", i + 1, product.getName()));
            if (product.getPrice() != null) {
                message.append(String.format("   价格：$%.0f\n", product.getPrice()));
            }
            if (product.getDuration() != null) {
                message.append(String.format("   时长：%s\n", product.getDuration()));
            }
            message.append("\n");
        }
        
        message.append("请回复产品编号(1-").append(Math.min(products.size(), 5)).append(")，我将为您生成对应的订单信息。");
        return message.toString();
    }
    
    /**
     * 处理普通问答
     */
    private ChatResponse handleGeneralQuestion(ChatRequest request) {
        try {
            String message = request.getMessage().trim();
            
            // 检查是否为订单查询请求
            if (isOrderQueryRequest(message)) {
                return handleOrderQuery(request);
            }
            
            // 检查是否为产品查询请求
            if (isProductQueryRequest(message)) {
                return handleProductQuery(request);
            }
            
            // 检查是否为天气查询请求
            if (isWeatherQueryRequest(message)) {
                String weatherResponse = getWeatherInfo(message);
                if (weatherResponse != null) {
                    saveChatMessage(request, weatherResponse, 2, null);
                    return ChatResponse.success(weatherResponse);
                }
            }
            
            // 首先尝试从FAQ中查找答案
            String faqAnswer = searchFAQAnswer(message);
            if (faqAnswer != null) {
                log.info("从FAQ中找到匹配答案");
                saveChatMessage(request, faqAnswer, 2, null);
                return ChatResponse.success(faqAnswer);
            }
            
            // 如果DeepSeek服务不可用，返回默认回复
            if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
                return ChatResponse.success(getDefaultResponse(message));
            }
            
            // 构建对话上下文
            String conversationContext = buildConversationContextForDeepSeek(request);
            
            // 调用DeepSeek API
            String response = callDeepSeekAI(conversationContext);
            
            // 保存对话记录
            saveChatMessage(request, response, 2, null);
            
            return ChatResponse.success(response);
            
        } catch (Exception e) {
            log.error("处理普通问答失败: {}", e.getMessage(), e);
            return ChatResponse.success(getDefaultResponse(request.getMessage()));
        }
    }
    
    /**
     * 从FAQ数据库中搜索匹配的答案
     */
    private String searchFAQAnswer(String question) {
        try {
            // 从数据库获取所有FAQ
            List<com.sky.entity.DayTourFaq> faqs = dayTourFaqMapper.findAll();
            
            if (faqs == null || faqs.isEmpty()) {
                return null;
            }
            
            String lowerQuestion = question.toLowerCase();
            
            // 简单的关键词匹配算法
            for (com.sky.entity.DayTourFaq faq : faqs) {
                if (faq.getQuestion() != null && faq.getAnswer() != null) {
                    String lowerFaqQuestion = faq.getQuestion().toLowerCase();
                    
                    // 检查是否包含关键词匹配
                    if (isQuestionMatch(lowerQuestion, lowerFaqQuestion)) {
                        log.info("FAQ匹配成功: {} -> {}", question, faq.getQuestion());
                        return formatFAQAnswer(faq);
                    }
                }
            }
            
            // 如果没有直接匹配，尝试关键词匹配
            for (com.sky.entity.DayTourFaq faq : faqs) {
                if (faq.getQuestion() != null && faq.getAnswer() != null) {
                    if (hasCommonKeywords(lowerQuestion, faq.getQuestion().toLowerCase())) {
                        log.info("FAQ关键词匹配成功: {} -> {}", question, faq.getQuestion());
                        return formatFAQAnswer(faq);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("搜索FAQ失败: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * 检查问题是否匹配
     */
    private boolean isQuestionMatch(String userQuestion, String faqQuestion) {
        // 去除标点符号和多余空格
        String cleanUserQuestion = userQuestion.replaceAll("[\\p{Punct}\\s]+", "");
        String cleanFaqQuestion = faqQuestion.replaceAll("[\\p{Punct}\\s]+", "");
        
        // 完全匹配或包含匹配
        if (cleanUserQuestion.equals(cleanFaqQuestion) || 
            cleanUserQuestion.contains(cleanFaqQuestion) ||
            cleanFaqQuestion.contains(cleanUserQuestion)) {
            return true;
        }
        
        // 计算相似度（简单的字符包含比例）
        int matchingChars = 0;
        int minLength = Math.min(cleanUserQuestion.length(), cleanFaqQuestion.length());
        
        for (int i = 0; i < minLength; i++) {
            if (i < cleanUserQuestion.length() && i < cleanFaqQuestion.length() &&
                cleanUserQuestion.charAt(i) == cleanFaqQuestion.charAt(i)) {
                matchingChars++;
            }
        }
        
        // 如果相似度大于60%，认为匹配
        return (double) matchingChars / minLength > 0.6;
    }
    
    /**
     * 检查是否有共同关键词
     */
    private boolean hasCommonKeywords(String userQuestion, String faqQuestion) {
        // 定义关键词列表
        String[] keywords = {"价格", "费用", "多少钱", "时间", "几点", "什么时候", "天气", "气候", 
                           "景点", "地方", "推荐", "好玩", "美食", "吃", "住宿", "酒店", "交通", 
                           "接送", "航班", "行程", "几天", "一日游", "跟团", "预订", "取消", 
                           "退款", "优惠", "折扣", "包含", "不含", "儿童", "老人", "团费"};
        
        int commonCount = 0;
        for (String keyword : keywords) {
            if (userQuestion.contains(keyword) && faqQuestion.contains(keyword)) {
                commonCount++;
            }
        }
        
        // 如果有2个或以上共同关键词，认为相关
        return commonCount >= 2;
    }
    
    /**
     * 格式化FAQ答案
     */
    private String formatFAQAnswer(com.sky.entity.DayTourFaq faq) {
        StringBuilder answer = new StringBuilder();
        
        answer.append("💡 **常见问题解答**\n\n");
        answer.append("❓ **").append(faq.getQuestion()).append("**\n\n");
        answer.append("✅ ").append(faq.getAnswer());
        
        // 添加相关推荐
        answer.append("\n\n🔗 **相关信息**：\n");
        answer.append("• 更多详情请咨询客服\n");
        answer.append("• 预订请访问我们的官网\n");
        answer.append("• 实时优惠请关注我们的公告");
        
        return answer.toString();
    }
    
    /**
     * 构建DeepSeek对话上下文
     */
    private String buildConversationContextForDeepSeek(ChatRequest request) {
        StringBuilder context = new StringBuilder();
        
        // 系统提示 - 专业的塔斯马尼亚旅游AI助手
        String systemPrompt = buildTasmanianTravelSystemPrompt();
        context.append("系统指令：").append(systemPrompt).append("\n\n");
        
        // 获取最近的对话历史
        List<ChatMessage> history = chatMessageMapper.selectRecentByUserId(request.getUserId(), 5);
        for (ChatMessage msg : history) {
            if (msg.getUserMessage() != null) {
                context.append("用户：").append(msg.getUserMessage()).append("\n");
            }
            if (msg.getBotResponse() != null) {
                context.append("助手：").append(msg.getBotResponse()).append("\n");
            }
        }
        
        // 当前用户消息
        context.append("用户：").append(request.getMessage()).append("\n");
        context.append("助手：");
        
        return context.toString();
    }
    
    /**
     * 构建对话上下文 (已废弃，保留兼容性)
     */
    @Deprecated
    private List<String> buildConversationContext(ChatRequest request) {
        // 保留空方法，避免编译错误
        return new ArrayList<>();
    }
    
    /**
     * 构建塔斯马尼亚旅游专业系统提示
     */
    private String buildTasmanianTravelSystemPrompt() {
        try {
            // 使用产品知识服务生成系统提示
            return tourKnowledgeService.generateAISystemPrompt();
        } catch (Exception e) {
            log.warn("获取产品知识系统提示失败，使用默认提示: {}", e.getMessage());
            
            // 降级到默认系统提示
            return "你是Happy Tassie Travel（塔斯马尼亚快乐旅游）的专业AI客服助手。你精通塔斯马尼亚旅游，能够为客户提供专业的旅游咨询和建议。\n\n" +
                   
                   "## 🌟 网站介绍\n" +
                   "Happy Tassie Travel是专业的塔斯马尼亚旅游平台，提供跟团游、一日游等优质旅游服务。\n\n" +
                   
                   "## 🚌 主要产品线\n" +
                   "### 跟团游产品：\n" +
                   "- **6日塔斯马尼亚环岛游** ($1180 → $1038, 优惠12%)\n" +
                   "- **5日塔州南部经典游** ($880 → $748, 优惠15%)\n" +
                   "- **4日塔州北部文化游** ($680 → $578, 优惠15%)\n" +
                   "- **3日塔州精华游** ($480 → $408, 优惠15%)\n\n" +
                   
                   "### 一日游产品：\n" +
                   "- **酒杯湾一日游** ($180 → $153, 优惠15%)\n" +
                   "- **摇篮山一日游** ($160 → $136, 优惠15%)\n" +
                   "- **布鲁尼岛一日游** ($150 → $128, 优惠15%)\n" +
                   "- **MONA博物馆一日游** ($120 → $102, 优惠15%)\n\n" +
                   
                   "## 🎯 服务特色\n" +
                   "- **专业导游**：中文服务，深度讲解\n" +
                   "- **小团出行**：8-12人精品小团\n" +
                   "- **灵活定制**：可根据需求调整行程\n" +
                   "- **品质保证**：精选住宿和餐厅\n\n" +
                   
                   "## 🏞️ 热门景点\n" +
                   "### 南部地区：\n" +
                   "- **酒杯湾（Wineglass Bay）**：世界十大海湾之一\n" +
                   "- **萨拉曼卡市场**：周六集市，手工艺品和美食\n" +
                   "- **MONA博物馆**：当代艺术殿堂\n" +
                   "- **惠灵顿山**：俯瞰霍巴特全景\n" +
                   "- **布鲁尼岛**：野生动物和新鲜生蚝\n\n" +
                   
                   "### 北部地区：\n" +
                   "- **摇篮山（Cradle Mountain）**：世界自然遗产\n" +
                   "- **朗塞斯顿**：历史名城\n" +
                   "- **薰衣草农场**：紫色浪漫（12月-1月）\n" +
                   "- **塔玛河谷**：葡萄酒产区\n\n" +
                   
                   "## 💰 预订须知\n" +
                   "- **预订方式**：网站在线预订或联系客服\n" +
                   "- **付款方式**：支持信用卡、PayPal、银行转账\n" +
                   "- **取消政策**：出发前72小时免费取消\n" +
                   "- **儿童政策**：12岁以下儿童享受优惠价格\n\n" +
                   
                   "## 🎯 AI助手使命\n" +
                   "你的任务是：\n" +
                   "1. **产品咨询**：详细介绍旅游产品特色和行程\n" +
                   "2. **行程规划**：根据客户需求推荐合适的产品\n" +
                   "3. **实用建议**：提供天气、交通、美食等实用信息\n" +
                   "4. **预订引导**：指导客户完成预订流程\n" +
                   "5. **问题解答**：回答关于塔斯马尼亚旅游的各种问题\n\n" +
                   
                   "## 📝 回复风格要求\n" +
                   "- **热情友好**：保持温暖亲切的语调\n" +
                   "- **专业详细**：提供准确的产品信息\n" +
                   "- **个性化**：根据客户需求定制推荐\n" +
                   "- **实用性**：关注客户的实际需求\n" +
                   "- **引导行动**：适时引导客户预订或咨询\n\n" +
                   
                   "请始终记住，你代表Happy Tassie Travel，要展现专业性和热情，帮助每位客户获得最佳的塔斯马尼亚旅游体验。";
        }
    }
    
    /**
     * 获取默认回复 (当OpenAI不可用时)
     */
    private String getDefaultResponse(String message) {
        String lowerMessage = message.toLowerCase();
        
        // 天气查询处理
        if (lowerMessage.contains("天气") || lowerMessage.contains("气温") || lowerMessage.contains("下雨") || 
            lowerMessage.contains("晴天") || lowerMessage.contains("多云") || lowerMessage.contains("weather")) {
            return getWeatherInfo(message);
        }
        
        // 智能产品推荐
        if (lowerMessage.contains("推荐") || lowerMessage.contains("什么好玩") || lowerMessage.contains("去哪里")) {
            return getProductRecommendation(message);
        }
        
        if (message.contains("价格") || message.contains("费用") || message.contains("多少钱")) {
            return "💰 **我们的热门产品价格**：\n\n" +
                   "🚌 **跟团游**:\n" +
                   "• 6日塔斯马尼亚环岛游：$1038 (原价$1180，优惠12%)\n\n" +
                   "🏞️ **一日游**:\n" +
                   "• 霍巴特市游：$120/8小时\n" +
                   "• 布鲁尼岛一日游：$140/9小时\n" +
                   "• 酒杯湾一日游：$130/10小时\n\n" +
                   "价格包含中文导游、交通接送，具体详情请联系客服获取最新报价！";
        } else if (lowerMessage.contains("时间") || lowerMessage.contains("几点") || lowerMessage.contains("什么时候")) {
            return "⏰ **出团时间安排**：\n\n" +
                   "🚌 **跟团游**: 每周三、六发团\n" +
                   "🏞️ **一日游**: 通常8:00-9:00出发\n" +
                   "✈️ **接机服务**: 根据您的航班时间安排\n\n" +
                   "具体时间会根据季节和天气调整，建议您联系我们的旅游顾问确认详细时间安排。";
        } else if (lowerMessage.contains("天气") || lowerMessage.contains("气候")) {
            return "🌤️ **塔斯马尼亚气候指南**：\n\n" +
                   "🌞 **夏季(12-2月)**: 15-25°C，最佳旅游季节\n" +
                   "🍂 **秋季(3-5月)**: 10-20°C，色彩斑斓\n" +
                   "❄️ **冬季(6-8月)**: 5-15°C，清爽宜人\n" +
                   "🌸 **春季(9-11月)**: 8-18°C，万物复苏\n\n" +
                   "塔斯马尼亚气候多变，建议准备分层衣物。我们会根据天气情况调整行程，确保您的旅行体验！";
        } else if (lowerMessage.contains("景点") || lowerMessage.contains("地方")) {
            return "🏞️ **塔斯马尼亚必游景点**：\n\n" +
                   "🏆 **酒杯湾**: 世界十大海滩之一，绝美海岸线\n" +
                   "🏔️ **摇篮山**: 世界遗产，徒步天堂\n" +
                   "🏙️ **霍巴特**: 艺术文化之都，萨拉曼卡市场\n" +
                   "🦪 **布鲁尼岛**: 美食天堂，生蚝芝士威士忌\n" +
                   "🌉 **里奇蒙**: 澳洲最古老石桥，历史小镇\n\n" +
                   "我们的行程覆盖所有热门景点，让您一次玩遍塔斯马尼亚精华！";
        } else if (lowerMessage.contains("美食") || lowerMessage.contains("吃什么")) {
            return "🍽️ **塔斯马尼亚美食天堂**：\n\n" +
                   "🦪 **布鲁尼岛生蚝**: 世界顶级，现开现吃\n" +
                   "🐟 **塔斯马尼亚三文鱼**: 肉质鲜美，营养丰富\n" +
                   "🧀 **手工芝士**: 口感丰富，品种多样\n" +
                   "🍯 **革木蜂蜜**: 纯天然无污染，甜而不腻\n" +
                   "🥃 **塔斯马尼亚威士忌**: 屡获国际大奖\n\n" +
                   "我们的行程包含美食体验，让您的味蕾享受塔斯马尼亚的美味！";
        } else {
            return "🌟 **欢迎来到Happy Tassie Travel！**\n\n" +
                   "我是您的专属旅游顾问，很高兴为您介绍塔斯马尼亚的美丽风光！\n\n" +
                   "💡 **您可以问我**：\n" +
                   "• 推荐行程和景点\n" +
                   "• 产品价格和优惠\n" +
                   "• 最佳旅游时间\n" +
                   "• 特色美食介绍\n" +
                   "• 订单查询和管理\n\n" +
                   "如需详细咨询，请随时联系我们的专业客服团队！";
        }
    }
    
    /**
     * 智能产品推荐
     */
    private String getProductRecommendation(String message) {
        String lowerMessage = message.toLowerCase();
        
        // 根据关键词推荐产品
        if (lowerMessage.contains("几天") || lowerMessage.contains("深度") || lowerMessage.contains("全面")) {
            return "🚌 **推荐：6日塔斯马尼亚环岛游**\n\n" +
                   "✨ **为什么选择环岛游**：\n" +
                   "• 🎯 一次游遍所有精华景点\n" +
                   "• 🏨 5晚优质住宿，舒适体验\n" +
                   "• 👥 最多16人精品小团\n" +
                   "• 🗣️ 专业中文导游全程陪同\n" +
                   "• 💰 特价$1038 (原价$1180，省$142)\n\n" +
                   "📍 **行程亮点**：霍巴特 → 酒杯湾 → 萨拉曼卡 → 摇篮山\n" +
                   "📅 **发团时间**：每周三、六\n\n" +
                   "想了解详细行程？联系我们客服吧！";
        } else if (lowerMessage.contains("一天") || lowerMessage.contains("一日") || lowerMessage.contains("短途")) {
            return "🏞️ **精选一日游推荐**\n\n" +
                   "🥇 **酒杯湾一日游** - $130/10小时\n" +
                   "🏆 世界十大海滩，菲欣纳国家公园必游\n\n" +
                   "🥈 **布鲁尼岛一日游** - $140/9小时  \n" +
                   "🦪 美食天堂：生蚝、芝士、蜂蜜一网打尽\n\n" +
                   "🥉 **霍巴特市游** - $120/8小时\n" +
                   "🏛️ 文化历史：萨拉曼卡、惠灵顿山、里奇蒙\n\n" +
                   "💡 **选择建议**：\n" +
                   "• 喜欢自然风光 → 酒杯湾\n" +
                   "• 热爱美食体验 → 布鲁尼岛  \n" +
                   "• 偏爱人文历史 → 霍巴特市游";
        } else if (lowerMessage.contains("第一次") || lowerMessage.contains("新手") || lowerMessage.contains("初次")) {
            return "🌟 **首次塔斯马尼亚完美行程**\n\n" +
                   "👑 **强烈推荐：6日环岛游**\n" +
                   "✅ 一次玩遍所有必游景点\n" +
                   "✅ 专业导游介绍当地文化\n" +
                   "✅ 精品小团，深度体验\n\n" +
                   "🎯 **核心亮点**：\n" +
                   "• 🏆 酒杯湾：世界级海滩\n" +
                   "• 🏔️ 摇篮山：世界遗产徒步\n" +
                   "• 🦪 布鲁尼岛：顶级美食体验\n" +
                   "• 🏛️ 霍巴特：艺术文化之旅\n\n" +
                   "💰 **特惠价格**：$1038 (省$142)\n" +
                   "📞 **咨询预订**：联系客服获取详细行程单！";
        } else {
            return "🎯 **为您量身推荐**\n\n" +
                   "💫 **热门选择**：\n" +
                   "🚌 **6日环岛游** - $1038：深度游遍塔斯马尼亚\n" +
                   "🏞️ **酒杯湾一日游** - $130：世界十大海滩\n" +
                   "🦪 **布鲁尼岛一日游** - $140：美食天堂体验\n\n" +
                   "🤔 **如何选择**：\n" +
                   "• 时间充足 → 选择6日环岛游\n" +
                   "• 时间有限 → 选择一日游精华\n" +
                   "• 美食爱好者 → 布鲁尼岛必去\n" +
                   "• 摄影爱好者 → 酒杯湾绝佳\n\n" +
                   "💬 告诉我您的具体需求，我来为您定制最佳行程！";
        }
    }
    
    /**
     * 使用AI智能解析订单信息（优化版）
     */
    private OrderInfo parseOrderInfoWithAI(String message) {
        // 如果DeepSeek未配置，回退到传统方法
        if (deepseekApiKey == null || deepseekApiKey.isEmpty()) {
            return parseOrderInfoTraditional(message);
        }
        
        try {
            // 构建专业的订单解析提示
            String aiPrompt = "你是一个专业的旅游订单数据提取专家。请从以下文本中提取旅游订单信息。\n\n" +
                    "## 提取任务：\n" +
                    "1. **服务类型识别**：准确识别旅游产品类型\n" +
                    "   - 跟团游：'X日游'、'环岛游'、'跟团'等\n" +
                    "   - 一日游：'一日游'、'Day Tour'等\n" +
                    "   - 包车服务：'包车'、'接送'等\n\n" +
                    "2. **日期提取**：识别各种日期格式\n" +
                    "   - '5月29日'、'2024年5月29日'、'05/29'、'5-29'\n" +
                    "   - '参团日期'、'出行日期'、'开始日期'\n\n" +
                    "3. **客户信息提取**：\n" +
                    "   - 姓名：中英文姓名（如：张三、John Smith）\n" +
                    "   - 电话：手机号码各种格式\n" +
                    "   - 护照：字母+数字组合，通常8-9位\n\n" +
                    "4. **航班信息识别**：\n" +
                    "   - 航班号：JQ719、VA123、QF456等格式\n" +
                    "   - 时间：24小时制或12小时制（AM/PM）\n\n" +
                    "5. **住宿信息提取**：\n" +
                    "   - 星级：'3星'、'3.5星'、'4星'、'4.5星'、'5星'、'三星'、'四星'、'五星'等（保持原始格式）\n" +
                    "   - 房型：从文本中智能识别房间类型\n\n" +
                    "6. **人数信息**：\n" +
                    "   - 从'3个人'、'2位客人'、'成人2儿童1'等格式中提取\n" +
                    "   - 区分成人和儿童数量\n\n" +
                    "## 返回格式：\n" +
                    "{\n" +
                    "  \"serviceType\": \"服务类型或产品名称（保持原文描述）\",\n" +
                    "  \"startDate\": \"开始日期(统一格式：X月X日)\",\n" +
                    "  \"endDate\": \"结束日期(统一格式：X月X日)\",\n" +
                    "  \"departure\": \"出发地点\",\n" +
                    "  \"groupSize\": 总人数(数字),\n" +
                    "  \"adultCount\": 成人数(数字),\n" +
                    "  \"childCount\": 儿童数(数字),\n" +
                    "  \"luggage\": 行李数(数字),\n" +
                    "  \"roomType\": \"房间类型（标准化：单人房/双人房/三人房）\",\n" +
                    "  \"hotelLevel\": \"酒店星级（保持原始格式：3星/3.5星/4星/4.5星/5星等）\",\n" +
                    "  \"arrivalFlight\": \"抵达航班号\",\n" +
                    "  \"departureFlight\": \"返程航班号\",\n" +
                    "  \"arrivalTime\": \"抵达时间（24小时制：HH:MM）\",\n" +
                    "  \"departureTime\": \"返程时间（24小时制：HH:MM）\",\n" +
                    "  \"customers\": [\n" +
                    "    {\n" +
                    "      \"name\": \"姓名（保持原格式）\",\n" +
                    "      \"phone\": \"电话号码（保持原格式）\",\n" +
                    "      \"passport\": \"护照号（保持原格式）\",\n" +
                    "      \"isChild\": false,\n" +
                    "      \"age\": 年龄(如果是儿童)\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"itinerary\": \"行程安排详情\",\n" +
                    "  \"notes\": \"备注信息（包括特殊要求和其他说明）\",\n" +
                    "  \"extractionQuality\": \"high|medium|low\",\n" +
                    "  \"extractionDetails\": {\n" +
                    "    \"confidence\": 0.0-1.0,\n" +
                    "    \"extractedFields\": [\"成功提取的字段列表\"],\n" +
                    "    \"missingFields\": [\"缺失的重要字段列表\"],\n" +
                    "    \"ambiguousFields\": [\"存在歧义的字段列表\"]\n" +
                    "  }\n" +
                    "}\n\n" +
                    "## 特别注意：\n" +
                    "- 对于无法确定的字段请返回null\n" +
                    "- 保持原文的重要信息，不要过度解释\n" +
                    "- 如果存在多种可能的解释，选择最合理的一种\n" +
                    "- 提取质量评估要客观准确\n\n" +
                    "## 订单文本：\n" + message + "\n\n" +
                    "请仔细分析并提取所有可用信息：";
            
            String aiResponse = callDeepSeekAI(aiPrompt);
            
            log.info("AI订单解析响应: {}", aiResponse);
            
            // 解析AI响应并构建OrderInfo
            try {
                com.alibaba.fastjson.JSONObject jsonResponse = com.alibaba.fastjson.JSON.parseObject(aiResponse);
                OrderInfo.OrderInfoBuilder builder = OrderInfo.builder();
                
                // 提取基本信息，增加空值检查和数据清理
                if (jsonResponse.containsKey("serviceType") && jsonResponse.getString("serviceType") != null && 
                    !jsonResponse.getString("serviceType").trim().isEmpty()) {
                    builder.serviceType(jsonResponse.getString("serviceType").trim());
                }
                if (jsonResponse.containsKey("startDate") && jsonResponse.getString("startDate") != null && 
                    !jsonResponse.getString("startDate").trim().isEmpty()) {
                    builder.startDate(jsonResponse.getString("startDate").trim());
                }
                if (jsonResponse.containsKey("endDate") && jsonResponse.getString("endDate") != null && 
                    !jsonResponse.getString("endDate").trim().isEmpty()) {
                    builder.endDate(jsonResponse.getString("endDate").trim());
                }
                if (jsonResponse.containsKey("departure") && jsonResponse.getString("departure") != null && 
                    !jsonResponse.getString("departure").trim().isEmpty()) {
                    builder.departure(jsonResponse.getString("departure").trim());
                }
                
                // 人数信息处理（优先使用具体的成人/儿童数，其次使用总人数）
                Integer adultCount = jsonResponse.getInteger("adultCount");
                Integer childCount = jsonResponse.getInteger("childCount");
                Integer groupSize = jsonResponse.getInteger("groupSize");
                
                if (adultCount != null && adultCount > 0) {
                    // 如果有具体的成人数，使用它
                    builder.groupSize(adultCount + (childCount != null ? childCount : 0));
                } else if (groupSize != null && groupSize > 0) {
                    // 否则使用总人数
                    builder.groupSize(groupSize);
                }
                
                if (jsonResponse.containsKey("luggage") && jsonResponse.getInteger("luggage") != null) {
                    builder.luggage(jsonResponse.getInteger("luggage"));
                }
                if (jsonResponse.containsKey("roomType") && jsonResponse.getString("roomType") != null && 
                    !jsonResponse.getString("roomType").trim().isEmpty()) {
                    builder.roomType(jsonResponse.getString("roomType").trim());
                }
                if (jsonResponse.containsKey("hotelLevel") && jsonResponse.getString("hotelLevel") != null && 
                    !jsonResponse.getString("hotelLevel").trim().isEmpty()) {
                    String hotelLevel = jsonResponse.getString("hotelLevel").trim();
                    // 特殊处理：3.5星标准化为3星
                    if ("3.5星".equals(hotelLevel)) {
                        hotelLevel = "3星";
                        log.info("将酒店星级3.5星标准化为3星");
                    }
                    builder.hotelLevel(hotelLevel);
                }
                if (jsonResponse.containsKey("arrivalFlight") && jsonResponse.getString("arrivalFlight") != null && 
                    !jsonResponse.getString("arrivalFlight").trim().isEmpty()) {
                    builder.arrivalFlight(jsonResponse.getString("arrivalFlight").trim());
                }
                if (jsonResponse.containsKey("departureFlight") && jsonResponse.getString("departureFlight") != null && 
                    !jsonResponse.getString("departureFlight").trim().isEmpty()) {
                    builder.departureFlight(jsonResponse.getString("departureFlight").trim());
                }
                if (jsonResponse.containsKey("arrivalTime") && jsonResponse.getString("arrivalTime") != null && 
                    !jsonResponse.getString("arrivalTime").trim().isEmpty()) {
                    builder.arrivalTime(jsonResponse.getString("arrivalTime").trim());
                }
                
                // 行程和备注信息
                if (jsonResponse.containsKey("itinerary") && jsonResponse.getString("itinerary") != null && 
                    !jsonResponse.getString("itinerary").trim().isEmpty()) {
                    builder.itinerary(jsonResponse.getString("itinerary").trim());
                }
                if (jsonResponse.containsKey("notes") && jsonResponse.getString("notes") != null && 
                    !jsonResponse.getString("notes").trim().isEmpty()) {
                    builder.notes(jsonResponse.getString("notes").trim());
                }
                
                // 提取客户信息
                if (jsonResponse.containsKey("customers") && jsonResponse.getJSONArray("customers") != null) {
                    com.alibaba.fastjson.JSONArray customersArray = jsonResponse.getJSONArray("customers");
                    List<OrderInfo.CustomerInfo> customers = new ArrayList<>();
                    
                    for (int i = 0; i < customersArray.size(); i++) {
                        com.alibaba.fastjson.JSONObject customerJson = customersArray.getJSONObject(i);
                        OrderInfo.CustomerInfo.CustomerInfoBuilder customerBuilder = OrderInfo.CustomerInfo.builder();
                        
                        if (customerJson.containsKey("name") && customerJson.getString("name") != null && 
                            !customerJson.getString("name").trim().isEmpty()) {
                            customerBuilder.name(customerJson.getString("name").trim());
                        }
                        if (customerJson.containsKey("phone") && customerJson.getString("phone") != null && 
                            !customerJson.getString("phone").trim().isEmpty()) {
                            customerBuilder.phone(customerJson.getString("phone").trim());
                        }
                        if (customerJson.containsKey("passport") && customerJson.getString("passport") != null && 
                            !customerJson.getString("passport").trim().isEmpty()) {
                            customerBuilder.passport(customerJson.getString("passport").trim());
                        }
                        
                        OrderInfo.CustomerInfo customer = customerBuilder.build();
                        if (customer.getName() != null || customer.getPhone() != null || customer.getPassport() != null) {
                            customers.add(customer);
                        }
                    }
                    
                    if (!customers.isEmpty()) {
                        builder.customers(customers);
                    }
                }
                
                OrderInfo orderInfo = builder.build();
                
                // 检查提取质量和详细信息
                String extractionQuality = jsonResponse.getString("extractionQuality");
                com.alibaba.fastjson.JSONObject extractionDetails = jsonResponse.getJSONObject("extractionDetails");
                
                if (extractionDetails != null) {
                    Double confidence = extractionDetails.getDouble("confidence");
                    com.alibaba.fastjson.JSONArray extractedFields = extractionDetails.getJSONArray("extractedFields");
                    com.alibaba.fastjson.JSONArray missingFields = extractionDetails.getJSONArray("missingFields");
                    
                    log.info("AI订单解析完成: 服务类型={}, 开始日期={}, 客户数量={}, 提取质量={}, 置信度={}, 提取字段={}, 缺失字段={}", 
                    orderInfo.getServiceType(), orderInfo.getStartDate(), 
                        orderInfo.getCustomers() != null ? orderInfo.getCustomers().size() : 0,
                        extractionQuality, confidence, extractedFields, missingFields);
                } else {
                    log.info("AI订单解析完成: 服务类型={}, 开始日期={}, 客户数量={}, 提取质量={}", 
                        orderInfo.getServiceType(), orderInfo.getStartDate(), 
                        orderInfo.getCustomers() != null ? orderInfo.getCustomers().size() : 0,
                        extractionQuality);
                }
                
                // 如果提取质量低，尝试与传统方法合并结果
                if ("low".equals(extractionQuality)) {
                    log.info("AI提取质量较低，尝试与传统方法合并结果");
                    OrderInfo traditionalResult = parseOrderInfoTraditional(message);
                    return mergeOrderInfo(orderInfo, traditionalResult);
                }
                
                return orderInfo;
                
            } catch (Exception e) {
                log.warn("解析AI订单响应失败，回退到传统方法: {}", e.getMessage());
                return parseOrderInfoTraditional(message);
            }
            
        } catch (Exception e) {
            log.warn("AI订单解析失败，回退到传统方法: {}", e.getMessage());
            return parseOrderInfoTraditional(message);
        }
    }
    
    /**
     * 合并AI和传统方法的解析结果
     */
    private OrderInfo mergeOrderInfo(OrderInfo aiResult, OrderInfo traditionalResult) {
        if (aiResult == null) return traditionalResult;
        if (traditionalResult == null) return aiResult;
        
        OrderInfo.OrderInfoBuilder builder = OrderInfo.builder();
        
        // 优先使用AI结果，如果为空则使用传统方法结果
        builder.serviceType(aiResult.getServiceType() != null ? aiResult.getServiceType() : traditionalResult.getServiceType());
        builder.startDate(aiResult.getStartDate() != null ? aiResult.getStartDate() : traditionalResult.getStartDate());
        builder.endDate(aiResult.getEndDate() != null ? aiResult.getEndDate() : traditionalResult.getEndDate());
        builder.departure(aiResult.getDeparture() != null ? aiResult.getDeparture() : traditionalResult.getDeparture());
        builder.groupSize(aiResult.getGroupSize() != null ? aiResult.getGroupSize() : traditionalResult.getGroupSize());
        builder.luggage(aiResult.getLuggage() != null ? aiResult.getLuggage() : traditionalResult.getLuggage());
        builder.roomType(aiResult.getRoomType() != null ? aiResult.getRoomType() : traditionalResult.getRoomType());
        builder.hotelLevel(aiResult.getHotelLevel() != null ? aiResult.getHotelLevel() : traditionalResult.getHotelLevel());
        builder.arrivalFlight(aiResult.getArrivalFlight() != null ? aiResult.getArrivalFlight() : traditionalResult.getArrivalFlight());
        builder.departureFlight(aiResult.getDepartureFlight() != null ? aiResult.getDepartureFlight() : traditionalResult.getDepartureFlight());
        builder.arrivalTime(aiResult.getArrivalTime() != null ? aiResult.getArrivalTime() : traditionalResult.getArrivalTime());
        builder.notes(aiResult.getNotes() != null ? aiResult.getNotes() : traditionalResult.getNotes());
        builder.vehicleType(aiResult.getVehicleType() != null ? aiResult.getVehicleType() : traditionalResult.getVehicleType());
        builder.itinerary(aiResult.getItinerary() != null ? aiResult.getItinerary() : traditionalResult.getItinerary());
        
        // 合并客户信息（优先使用数量更多的结果）
        if (aiResult.getCustomers() != null && traditionalResult.getCustomers() != null) {
            if (aiResult.getCustomers().size() >= traditionalResult.getCustomers().size()) {
                builder.customers(aiResult.getCustomers());
            } else {
                builder.customers(traditionalResult.getCustomers());
            }
        } else if (aiResult.getCustomers() != null) {
            builder.customers(aiResult.getCustomers());
        } else {
            builder.customers(traditionalResult.getCustomers());
        }
        
        OrderInfo mergedResult = builder.build();
        log.info("AI和传统方法结果合并完成");
        return mergedResult;
    }

    /**
     * 传统方式解析订单信息（重命名原方法）
     */
    private OrderInfo parseOrderInfoTraditional(String message) {
        OrderInfo.OrderInfoBuilder builder = OrderInfo.builder();
        
        // 使用正则表达式提取信息
        extractField(message, "服务类型：(.+?)\\n", builder::serviceType);
        extractField(message, "出发地点：(.+?)\\n", builder::departure);
        extractField(message, "服务车型：(.+?)\\n", builder::vehicleType);
        extractField(message, "房型：(.+?)\\n", builder::roomType);
        extractField(message, "酒店级别：(.+?)\\n", builder::hotelLevel);
        
        // 解析日期范围
        Pattern datePattern = Pattern.compile("参团日期.*?：(.+?)\\n");
        Matcher dateMatcher = datePattern.matcher(message);
        if (dateMatcher.find()) {
            String dateRange = dateMatcher.group(1).trim();
            String[] dates = parseDateRange(dateRange);
            if (dates.length >= 2) {
                builder.startDate(dates[0]).endDate(dates[1]);
            }
        }
        
        // 解析航班信息
        extractFlightInfo(message, builder);
        
        // 解析人数
        Pattern groupPattern = Pattern.compile("跟团人数：(\\d+)");
        Matcher groupMatcher = groupPattern.matcher(message);
        if (groupMatcher.find()) {
            builder.groupSize(Integer.parseInt(groupMatcher.group(1)));
        }
        
        // 解析行李数
        Pattern luggagePattern = Pattern.compile("行李数：(\\d+)");
        Matcher luggageMatcher = luggagePattern.matcher(message);
        if (luggageMatcher.find()) {
            builder.luggage(Integer.parseInt(luggageMatcher.group(1)));
        }
        
        // 解析客户信息
        List<OrderInfo.CustomerInfo> customers = parseCustomerInfo(message);
        builder.customers(customers);
        
        // 解析行程
        extractItinerary(message, builder);
        
        // 解析备注
        extractNotes(message, builder);
        
        return builder.build();
    }

    /**
     * 解析订单信息（主入口方法）
     */
    private OrderInfo parseOrderInfo(String message) {
        // 优先使用AI智能解析
        return parseOrderInfoWithAI(message);
    }
    
    /**
     * 提取字段
     */
    private void extractField(String message, String regex, java.util.function.Consumer<String> setter) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            setter.accept(matcher.group(1).trim());
        }
    }
    
    /**
     * 解析日期范围
     */
    private String[] parseDateRange(String dateRange) {
        // 处理 "6月19日-6月22日" 格式
        if (dateRange.contains("-")) {
            String[] parts = dateRange.split("-");
            if (parts.length >= 2) {
                return new String[]{parts[0].trim(), parts[1].trim()};
            }
        }
        return new String[]{dateRange, dateRange};
    }
    
    /**
     * 解析航班信息
     */
    private void extractFlightInfo(String message, OrderInfo.OrderInfoBuilder builder) {
        // 解析抵达时间
        Pattern arrivalTimePattern = Pattern.compile("抵达时间\\s*:(.+?)\\n");
        Matcher arrivalTimeMatcher = arrivalTimePattern.matcher(message);
        if (arrivalTimeMatcher.find()) {
            String arrivalTime = arrivalTimeMatcher.group(1).trim();
            builder.arrivalTime(arrivalTime);
            log.info("解析到抵达时间: {}", arrivalTime);
        }
        
        // 解析抵达航班（支持多种格式）
        // 支持的格式：
        // - "抵达航班: VA1528 09:15AM抵达"
        // - "到达航班:JQ719   08:35"
        // - "抵达航班: VA1528"
        Pattern arrivalFlightPattern = Pattern.compile("(?:抵达航班|到达航班)\\s*[：:](.+?)(?=\\n|离开航班|回程航班|出发地点|$)");
        Matcher arrivalFlightMatcher = arrivalFlightPattern.matcher(message);
        if (arrivalFlightMatcher.find()) {
            String flightInfo = arrivalFlightMatcher.group(1).trim();
            
            // 提取航班号（通常是字母+数字的组合）
            Pattern flightNumberPattern = Pattern.compile("([A-Z]{1,3}\\d{1,4})");
            Matcher flightNumberMatcher = flightNumberPattern.matcher(flightInfo);
            
            if (flightNumberMatcher.find()) {
                String flightNumber = flightNumberMatcher.group(1);
                builder.arrivalFlight(flightNumber);
                log.info("解析到抵达航班号: {}", flightNumber);
                
                // 提取时间信息（支持多种时间格式）
                // 格式1: 09:15AM
                // 格式2: 08:35
                Pattern timePattern = Pattern.compile("(\\d{1,2}:\\d{2}\\s*(?:[AP]M)?|\\d{1,2}:\\d{2})");
                Matcher timeMatcher = timePattern.matcher(flightInfo);
                if (timeMatcher.find()) {
                    String timeInfo = timeMatcher.group(1);
                    // 如果还没有设置抵达时间，使用航班信息中的时间
                    if (builder.build().getArrivalTime() == null) {
                        builder.arrivalTime(timeInfo);
                        log.info("从航班信息中解析到抵达时间: {}", timeInfo);
                    }
                } else {
                    // 如果没有时间信息，尝试自动查询航班时间
                    log.info("航班{}未提供时间信息，尝试自动查询", flightNumber);
                    try {
                        OrderInfo.FlightInfo autoFlightInfo = queryFlightInfo(flightNumber);
                        if (autoFlightInfo != null) {
                            if (autoFlightInfo.getDepartureTime() != null) {
                                // 设置起飞时间作为参考
                                log.info("自动查询到航班{}起飞时间: {}", flightNumber, autoFlightInfo.getDepartureTime());
                            }
                            if (autoFlightInfo.getArrivalTime() != null) {
                                // 如果还没有设置抵达时间，使用查询到的时间
                                if (builder.build().getArrivalTime() == null) {
                                    builder.arrivalTime(autoFlightInfo.getArrivalTime());
                                    log.info("自动查询到航班{}抵达时间: {}", flightNumber, autoFlightInfo.getArrivalTime());
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("自动查询航班{}信息失败: {}", flightNumber, e.getMessage());
                    }
                }
            }
        }
        
        // 解析离开/回程航班（支持多种格式）
        // 支持的格式：
        // - "离开航班: XX123"
        // - "回程航班：JQ712  21:00"
        // - "返程航班: XX456"
        Pattern departureFlightPattern = Pattern.compile("(?:离开航班|回程航班|返程航班)\\s*[：:](.+?)(?=\\n|出发地点|服务车型|$)");
        Matcher departureFlightMatcher = departureFlightPattern.matcher(message);
        if (departureFlightMatcher.find()) {
            String departureInfo = departureFlightMatcher.group(1).trim();
            
            // 检查是否是"行程结束送回酒店"等描述
            if (!departureInfo.contains("行程结束") && !departureInfo.contains("送回")) {
                // 提取离开航班号
                Pattern flightNumberPattern = Pattern.compile("([A-Z]{1,3}\\d{1,4})");
                Matcher flightNumberMatcher = flightNumberPattern.matcher(departureInfo);
                
                if (flightNumberMatcher.find()) {
                    String flightNumber = flightNumberMatcher.group(1);
                    builder.departureFlight(flightNumber);
                    log.info("解析到离开航班号: {}", flightNumber);
                    
                    // 提取回程航班时间
                    Pattern timePattern = Pattern.compile("(\\d{1,2}:\\d{2}\\s*(?:[AP]M)?|\\d{1,2}:\\d{2})");
                    Matcher timeMatcher = timePattern.matcher(departureInfo);
                    if (timeMatcher.find()) {
                        String timeInfo = timeMatcher.group(1);
                        // 可以设置离开时间字段（如果OrderInfo有这个字段的话）
                        log.info("从回程航班信息中解析到时间: {}", timeInfo);
                    }
                    
                    // 尝试自动查询离开航班时间
                    try {
                        OrderInfo.FlightInfo autoFlightInfo = queryFlightInfo(flightNumber);
                        if (autoFlightInfo != null) {
                            log.info("自动查询到离开航班{}信息", flightNumber);
                            // 可以将查询到的信息存储到builder中，如果需要的话
                        }
                    } catch (Exception e) {
                        log.warn("自动查询离开航班{}信息失败: {}", flightNumber, e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * 解析客户信息
     */
    private List<OrderInfo.CustomerInfo> parseCustomerInfo(String message) {
        List<OrderInfo.CustomerInfo> customers = new ArrayList<>();
        
        try {
            // 提取乘客信息部分
            Pattern passengerSectionPattern = Pattern.compile("乘客信息[：:]([\\s\\S]*?)(?=房型[：:]|酒店|行程|备注|$)", Pattern.CASE_INSENSITIVE);
            Matcher sectionMatcher = passengerSectionPattern.matcher(message);
            
            String passengerSection = "";
            if (sectionMatcher.find()) {
                passengerSection = sectionMatcher.group(1).trim();
                log.info("原始客户信息: {}", passengerSection);
            } else {
                log.warn("未找到乘客信息部分");
                return customers;
            }
            
            // 尝试键值对格式解析
            List<OrderInfo.CustomerInfo> keyValueCustomers = parseKeyValueCustomerInfo(passengerSection);
            if (!keyValueCustomers.isEmpty()) {
                log.info("键值对格式解析成功，客户数量: {}", keyValueCustomers.size());
                return keyValueCustomers;
            }
            
            // 按姓名分割客户信息
            String[] customerBlocks = passengerSection.split("姓名[：:]");
            
            for (String block : customerBlocks) {
                if (block.trim().isEmpty()) continue;
                
                // 为每个块添加回"姓名："前缀（除了第一个空块）
                String customerLine = "姓名：" + block.trim();
                log.info("正在解析客户信息行: {}", customerLine);
                
                parseCustomerLine(customerLine, customers);
            }
            
            log.info("解析出的客户信息: {}", customers);
            
        } catch (Exception e) {
            log.error("解析客户信息失败: {}", e.getMessage(), e);
        }
        
        return customers;
    }
    
    /**
     * 解析键值对格式的客户信息
     * 支持格式：
     * 姓名：张三
     * 护照号：ED1234567
     * 电话：1234567890
     * [空行]
     * 姓名：李四
     * ...
     */
    private List<OrderInfo.CustomerInfo> parseKeyValueCustomerInfo(String customerInfo) {
        List<OrderInfo.CustomerInfo> customers = new ArrayList<>();
        
        // 按空行分割不同的客户
        String[] customerBlocks = customerInfo.split("\\n\\s*\\n");
        
        for (String block : customerBlocks) {
            if (block.trim().isEmpty()) continue;
            
            String name = null;
            String passport = null;
            String phone = null;
            String domesticPhone = null;
            String internationalPhone = null;
            
            String[] lines = block.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                log.info("正在解析客户信息行: {}", line);
                
                // 匹配各种字段格式 - 修复正则表达式
                if (line.contains("姓名")) {
                    // 提取姓名：支持 "姓名：xxx" 或 "姓名: xxx" 格式
                    String[] parts = line.split("[:：]", 2);
                    if (parts.length >= 2) {
                        name = parts[1].trim();
                        log.info("提取到姓名: {}", name);
                    }
                } else if (line.contains("护照号")) {
                    // 提取护照号：支持 "护照号：xxx" 或 "护照号: xxx" 格式
                    String[] parts = line.split("[:：]", 2);
                    if (parts.length >= 2) {
                        passport = parts[1].trim();
                        log.info("提取到护照号: {}", passport);
                    }
                } else if (line.contains("国内电话")) {
                    // 提取国内电话
                    String[] parts = line.split("[:：]", 2);
                    if (parts.length >= 2) {
                        domesticPhone = parts[1].trim().replaceAll("[\\s-]", "");
                        log.info("提取到国内电话: {}", domesticPhone);
                    }
                } else if (line.contains("澳洲电话")) {
                    // 提取澳洲电话
                    String[] parts = line.split("[:：]", 2);
                    if (parts.length >= 2) {
                        internationalPhone = parts[1].trim().replaceAll("[\\s-]", "");
                        log.info("提取到澳洲电话: {}", internationalPhone);
                    }
                } else if (line.matches(".*(?:电话|联系电话|手机).*[:：].*")) {
                    // 通用电话字段
                    String[] parts = line.split("[:：]", 2);
                    if (parts.length >= 2) {
                        phone = parts[1].trim().replaceAll("[\\s-]", "");
                        log.info("提取到电话: {}", phone);
                    }
                }
            }
            
            // 电话号码优先级：专门的电话字段 > 国内电话 > 澳洲电话
            if (phone == null) {
                if (domesticPhone != null) {
                    phone = domesticPhone;
                } else if (internationalPhone != null) {
                    phone = internationalPhone;
                }
            }
            
            // 验证并创建客户信息
            if (name != null && (passport != null || phone != null)) {
                OrderInfo.CustomerInfo.CustomerInfoBuilder builder = OrderInfo.CustomerInfo.builder()
                        .name(name);
                
                if (passport != null && !passport.isEmpty()) {
                    builder.passport(passport);
                }
                
                if (phone != null && !phone.isEmpty() && isValidPhone(phone)) {
                    builder.phone(phone);
                }
                
                OrderInfo.CustomerInfo customer = builder.build();
                customers.add(customer);
                
                log.info("键值对格式解析出客户: 姓名={}, 护照={}, 电话={}", name, passport, phone);
            } else {
                log.warn("客户信息不完整，跳过: 姓名={}, 护照={}, 电话={}", name, passport, phone);
            }
        }
        
        return customers;
    }
    
    /**
     * 解析单行客户信息
     */
    private void parseCustomerLine(String line, List<OrderInfo.CustomerInfo> customers) {
        // 匹配格式：姓名+护照号+电话号码
        // 例如：蔡洁Cai Jie EJ9015743 0448394950
        
        log.info("正在解析客户信息行: {}", line);
        
        // 改进的正则表达式，匹配 姓名(可能包含中英文) + 护照号(字母数字组合) + 电话号码
        // 护照号通常是字母开头+数字的组合，长度6-12位
        Pattern namePassportPhonePattern = Pattern.compile("([\\u4e00-\\u9fa5a-zA-Z\\s]+?)\\s+([A-Z]{1,3}\\d{6,12})\\s+([+\\d\\s-]+)");
        Matcher matcher = namePassportPhonePattern.matcher(line);
        
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            String passport = matcher.group(2).trim();
            String phone = matcher.group(3).trim().replaceAll("[\\s-]", "");
            
            if (!name.isEmpty() && !passport.isEmpty() && !phone.isEmpty()) {
                customers.add(OrderInfo.CustomerInfo.builder()
                        .name(name)
                        .passport(passport)
                        .phone(phone)
                        .build());
                log.info("解析出客户: 姓名={}, 护照={}, 电话={}", name, passport, phone);
                return; // 成功解析，直接返回
            }
        }
        
        // 如果上面的方法没有匹配到，尝试按空格分割进行解析
        // 处理格式：姓名 护照号 电话号码 (用空格分隔)
        String[] parts = line.split("\\s+");
        if (parts.length >= 3) {
            // 找到可能的护照号位置（包含字母和数字的组合）
            for (int i = 1; i < parts.length - 1; i++) {
                String possiblePassport = parts[i];
                if (isValidPassport(possiblePassport)) {
                    // 前面的部分组合为姓名
                    StringBuilder nameBuilder = new StringBuilder();
                    for (int j = 0; j < i; j++) {
                        if (j > 0) nameBuilder.append(" ");
                        nameBuilder.append(parts[j]);
                    }
                    String name = nameBuilder.toString().trim();
                    
                    // 后面的部分为电话号码
                    StringBuilder phoneBuilder = new StringBuilder();
                    for (int k = i + 1; k < parts.length; k++) {
                        phoneBuilder.append(parts[k]);
                    }
                    String phone = phoneBuilder.toString().replaceAll("[\\s-]", "");
                    
                    if (isValidName(name) && isValidPhone(phone)) {
                        customers.add(OrderInfo.CustomerInfo.builder()
                                .name(name)
                                .passport(possiblePassport)
                                .phone(phone)
                                .build());
                        log.info("备用方法解析出客户: 姓名={}, 护照={}, 电话={}", name, possiblePassport, phone);
                        return;
                    }
                }
            }
        }
        
        // 最后的备用方案：尝试原有的姓名+电话格式（不包含护照号）
        Pattern namePhonePattern = Pattern.compile("([\\u4e00-\\u9fa5a-zA-Z\\s]+)([+\\d\\s-]+)");
        Matcher namePhoneMatcher = namePhonePattern.matcher(line);
        
        if (namePhoneMatcher.find()) {
            String name = namePhoneMatcher.group(1).trim();
            String phone = namePhoneMatcher.group(2).trim().replaceAll("[\\s-]", "");
            
            if (isValidName(name) && isValidPhone(phone)) {
                customers.add(OrderInfo.CustomerInfo.builder()
                        .name(name)
                        .phone(phone)
                        .build());
                log.info("简单格式解析出客户: 姓名={}, 电话={}", name, phone);
            }
        }
    }
    
    /**
     * 验证是否为有效姓名
     */
    private boolean isValidName(String name) {
        return name != null && name.length() >= 2 && name.length() <= 10 && 
               name.matches("[\\u4e00-\\u9fa5a-zA-Z]+");
    }
    
    /**
     * 验证是否为有效电话
     */
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("[\\+\\d]+") && phone.length() >= 8;
    }
    
    /**
     * 验证是否为有效护照
     */
    private boolean isValidPassport(String passport) {
        return passport != null && passport.matches("[A-Z]{1,3}\\d{6,12}") && passport.length() >= 6 && passport.length() <= 12;
    }
    
    /**
     * 提取行程信息
     */
    private void extractItinerary(String message, OrderInfo.OrderInfoBuilder builder) {
        StringBuilder itinerary = new StringBuilder();
        
        Pattern dayPattern = Pattern.compile("Day\\d+:(.+?)(?=Day\\d+:|备注|$)");
        Matcher dayMatcher = dayPattern.matcher(message);
        
        while (dayMatcher.find()) {
            itinerary.append(dayMatcher.group(0).trim()).append("\n");
        }
        
        if (itinerary.length() > 0) {
            builder.itinerary(itinerary.toString().trim());
        }
    }
    
    /**
     * 提取备注信息
     */
    private void extractNotes(String message, OrderInfo.OrderInfoBuilder builder) {
        // 改进的正则表达式，支持多行备注信息
        // 匹配"备注"或"备注："后面的所有内容，直到消息结束
        Pattern notesPattern = Pattern.compile("备注\\s*[:：]?\\s*\\n?(.+?)$", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher notesMatcher = notesPattern.matcher(message);
        
        if (notesMatcher.find()) {
            String notes = notesMatcher.group(1).trim();
            // 清理多余的空行，但保留结构
            notes = notes.replaceAll("\\n\\s*\\n", "\n").trim();
            builder.notes(notes);
            log.info("解析到备注信息: {}", notes);
        } else {
            // 备用方案：寻找包含"备注"的行及其后面的内容
            String[] lines = message.split("\\n");
            boolean foundNotesSection = false;
            StringBuilder notesBuilder = new StringBuilder();
            
            for (String line : lines) {
                if (line.trim().matches(".*备注\\s*[:：]?.*")) {
                    foundNotesSection = true;
                    // 如果这行除了"备注："还有其他内容，添加它
                    String content = line.replaceAll(".*备注\\s*[:：]?\\s*", "").trim();
                    if (!content.isEmpty()) {
                        notesBuilder.append(content).append("\n");
                    }
                } else if (foundNotesSection) {
                    // 已经找到备注部分，添加后续行
                    notesBuilder.append(line.trim()).append("\n");
                }
            }
            
            if (notesBuilder.length() > 0) {
                String notes = notesBuilder.toString().trim();
                builder.notes(notes);
                log.info("备用方法解析到备注信息: {}", notes);
            }
        }
    }
    
    /**
     * 生成订单URL参数（优化版）
     */
    private String generateOrderParams(OrderInfo orderInfo, GroupTourDTO product) {
        StringBuilder params = new StringBuilder();
        
        try {
        // 添加产品ID（最重要的参数）
        if (product != null && product.getId() != null) {
            params.append("productId=").append(product.getId()).append("&");
            params.append("productType=group&"); // 标识为跟团游
            log.info("添加产品参数: productId={}, productType=group", product.getId());
        }
        
            // 基本订单信息
            if (orderInfo.getServiceType() != null && !orderInfo.getServiceType().trim().isEmpty()) {
                try {
                    params.append("serviceType=").append(java.net.URLEncoder.encode(orderInfo.getServiceType().trim(), "UTF-8")).append("&");
                } catch (java.io.UnsupportedEncodingException e) {
                    params.append("serviceType=").append(orderInfo.getServiceType().trim()).append("&");
                }
            }
            
            // 日期信息
            if (orderInfo.getStartDate() != null && !orderInfo.getStartDate().trim().isEmpty()) {
                try {
                    params.append("startDate=").append(java.net.URLEncoder.encode(orderInfo.getStartDate().trim(), "UTF-8")).append("&");
                } catch (java.io.UnsupportedEncodingException e) {
                    params.append("startDate=").append(orderInfo.getStartDate().trim()).append("&");
                }
            }
            
            if (orderInfo.getEndDate() != null && !orderInfo.getEndDate().trim().isEmpty()) {
                try {
                    params.append("endDate=").append(java.net.URLEncoder.encode(orderInfo.getEndDate().trim(), "UTF-8")).append("&");
                } catch (java.io.UnsupportedEncodingException e) {
                    params.append("endDate=").append(orderInfo.getEndDate().trim()).append("&");
                }
            }
            
            // 人数和行李信息
            if (orderInfo.getGroupSize() != null && orderInfo.getGroupSize() > 0) {
                params.append("groupSize=").append(orderInfo.getGroupSize()).append("&");
                log.info("添加团队人数参数: {}", orderInfo.getGroupSize());
            }
            
            if (orderInfo.getLuggage() != null && orderInfo.getLuggage() > 0) {
                params.append("luggageCount=").append(orderInfo.getLuggage()).append("&");
                log.info("添加行李数参数: {}", orderInfo.getLuggage());
            }
            
            // 地点信息
            if (orderInfo.getDeparture() != null && !orderInfo.getDeparture().trim().isEmpty()) {
                try {
                    params.append("departure=").append(java.net.URLEncoder.encode(orderInfo.getDeparture().trim(), "UTF-8")).append("&");
                } catch (java.io.UnsupportedEncodingException e) {
                    params.append("departure=").append(orderInfo.getDeparture().trim()).append("&");
                }
            }
            
            // 住宿信息
            if (orderInfo.getRoomType() != null && !orderInfo.getRoomType().trim().isEmpty()) {
                try {
                    params.append("roomType=").append(java.net.URLEncoder.encode(orderInfo.getRoomType().trim(), "UTF-8")).append("&");
                } catch (java.io.UnsupportedEncodingException e) {
                    params.append("roomType=").append(orderInfo.getRoomType().trim()).append("&");
                }
            }
            
            if (orderInfo.getHotelLevel() != null && !orderInfo.getHotelLevel().trim().isEmpty()) {
                try {
                    params.append("hotelLevel=").append(java.net.URLEncoder.encode(orderInfo.getHotelLevel().trim(), "UTF-8")).append("&");
                } catch (java.io.UnsupportedEncodingException e) {
                    params.append("hotelLevel=").append(orderInfo.getHotelLevel().trim()).append("&");
                }
            }
            
            // 航班信息
        if (orderInfo.getArrivalFlight() != null && !orderInfo.getArrivalFlight().trim().isEmpty()) {
            params.append("arrivalFlight=").append(orderInfo.getArrivalFlight().trim()).append("&");
            log.info("添加抵达航班参数: {}", orderInfo.getArrivalFlight());
        }
        
        if (orderInfo.getDepartureFlight() != null && !orderInfo.getDepartureFlight().trim().isEmpty()) {
            params.append("departureFlight=").append(orderInfo.getDepartureFlight().trim()).append("&");
            log.info("添加离开航班参数: {}", orderInfo.getDepartureFlight());
        }
        
        if (orderInfo.getArrivalTime() != null && !orderInfo.getArrivalTime().trim().isEmpty()) {
                try {
                    params.append("arrivalTime=").append(java.net.URLEncoder.encode(orderInfo.getArrivalTime().trim(), "UTF-8")).append("&");
            log.info("添加抵达时间参数: {}", orderInfo.getArrivalTime());
                } catch (java.io.UnsupportedEncodingException e) {
                    params.append("arrivalTime=").append(orderInfo.getArrivalTime().trim()).append("&");
                }
            }
            
            // 客户信息参数（支持最多5个客户）
            if (orderInfo.getCustomers() != null && !orderInfo.getCustomers().isEmpty()) {
                int customerCount = Math.min(orderInfo.getCustomers().size(), 5); // 最多传递5个客户
                for (int i = 0; i < customerCount; i++) {
                    OrderInfo.CustomerInfo customer = orderInfo.getCustomers().get(i);
                    int customerIndex = i + 1;
                    
                    if (customer.getName() != null && !customer.getName().trim().isEmpty()) {
                        try {
                            params.append("customerName").append(customerIndex).append("=")
                                  .append(java.net.URLEncoder.encode(customer.getName().trim(), "UTF-8")).append("&");
                        } catch (java.io.UnsupportedEncodingException e) {
                            params.append("customerName").append(customerIndex).append("=")
                                  .append(customer.getName().trim()).append("&");
                        }
                    }
                    
                    if (customer.getPhone() != null && !customer.getPhone().trim().isEmpty()) {
                        params.append("customerPhone").append(customerIndex).append("=")
                              .append(customer.getPhone().trim()).append("&");
                    }
                    
                    if (customer.getPassport() != null && !customer.getPassport().trim().isEmpty()) {
                        params.append("customerPassport").append(customerIndex).append("=")
                              .append(customer.getPassport().trim()).append("&");
                    }
                }
                log.info("添加客户信息参数: 客户数量={}", customerCount);
            }
            
            // 航班详细时间信息（如果通过API查询获得）
        if (orderInfo.getArrivalFlight() != null) {
            try {
                OrderInfo.FlightInfo flightInfo = queryFlightInfo(orderInfo.getArrivalFlight());
                if (flightInfo != null) {
                    if (flightInfo.getDepartureTime() != null) {
                            try {
                                params.append("arrivalFlightDepartureTime=")
                                      .append(java.net.URLEncoder.encode(flightInfo.getDepartureTime(), "UTF-8")).append("&");
                        log.info("添加抵达航班起飞时间参数: {}", flightInfo.getDepartureTime());
                            } catch (java.io.UnsupportedEncodingException e) {
                                params.append("arrivalFlightDepartureTime=").append(flightInfo.getDepartureTime()).append("&");
                            }
                    }
                    if (flightInfo.getArrivalTime() != null) {
                            try {
                                params.append("arrivalFlightLandingTime=")
                                      .append(java.net.URLEncoder.encode(flightInfo.getArrivalTime(), "UTF-8")).append("&");
                        log.info("添加抵达航班降落时间参数: {}", flightInfo.getArrivalTime());
                            } catch (java.io.UnsupportedEncodingException e) {
                                params.append("arrivalFlightLandingTime=").append(flightInfo.getArrivalTime()).append("&");
                            }
                    }
                } else {
                    log.info("航班{}详细信息查询失败，跳过航班时间参数", orderInfo.getArrivalFlight());
                }
            } catch (Exception e) {
                log.warn("查询抵达航班{}详细信息失败: {}", orderInfo.getArrivalFlight(), e.getMessage());
            }
        }
        
        // 查询返程航班详细信息
        if (orderInfo.getDepartureFlight() != null) {
            try {
                OrderInfo.FlightInfo flightInfo = queryFlightInfo(orderInfo.getDepartureFlight());
                if (flightInfo != null) {
                    if (flightInfo.getDepartureTime() != null) {
                            try {
                                params.append("departureFlightDepartureTime=")
                                      .append(java.net.URLEncoder.encode(flightInfo.getDepartureTime(), "UTF-8")).append("&");
                        log.info("添加返程航班起飞时间参数: {}", flightInfo.getDepartureTime());
                            } catch (java.io.UnsupportedEncodingException e) {
                                params.append("departureFlightDepartureTime=").append(flightInfo.getDepartureTime()).append("&");
                            }
                    }
                    if (flightInfo.getArrivalTime() != null) {
                            try {
                                params.append("departureFlightLandingTime=")
                                      .append(java.net.URLEncoder.encode(flightInfo.getArrivalTime(), "UTF-8")).append("&");
                        log.info("添加返程航班降落时间参数: {}", flightInfo.getArrivalTime());
                            } catch (java.io.UnsupportedEncodingException e) {
                                params.append("departureFlightLandingTime=").append(flightInfo.getArrivalTime()).append("&");
                            }
                    }
                } else {
                    log.info("航班{}详细信息查询失败，跳过航班时间参数", orderInfo.getDepartureFlight());
                }
            } catch (Exception e) {
                log.warn("查询返程航班{}详细信息失败: {}", orderInfo.getDepartureFlight(), e.getMessage());
            }
        }
        
            // 特殊要求/备注参数
        if (orderInfo.getNotes() != null && !orderInfo.getNotes().trim().isEmpty()) {
            try {
                params.append("specialRequests=").append(java.net.URLEncoder.encode(orderInfo.getNotes().trim(), "UTF-8")).append("&");
                log.info("添加特殊要求参数: {}", orderInfo.getNotes());
            } catch (java.io.UnsupportedEncodingException e) {
                log.warn("URL编码失败，跳过特殊要求参数: {}", e.getMessage());
            }
        }
        
            // 行程信息
            if (orderInfo.getItinerary() != null && !orderInfo.getItinerary().trim().isEmpty()) {
                try {
                    params.append("itinerary=").append(java.net.URLEncoder.encode(orderInfo.getItinerary().trim(), "UTF-8")).append("&");
                    log.info("添加行程信息参数");
                } catch (java.io.UnsupportedEncodingException e) {
                    log.warn("URL编码行程信息失败: {}", e.getMessage());
                }
            }
            
            // 车辆类型信息
            if (orderInfo.getVehicleType() != null && !orderInfo.getVehicleType().trim().isEmpty()) {
                try {
                    params.append("vehicleType=").append(java.net.URLEncoder.encode(orderInfo.getVehicleType().trim(), "UTF-8")).append("&");
                    log.info("添加车辆类型参数: {}", orderInfo.getVehicleType());
                } catch (java.io.UnsupportedEncodingException e) {
                    params.append("vehicleType=").append(orderInfo.getVehicleType().trim()).append("&");
                }
            }
            
            // 添加AI处理标识，让前端知道这是AI处理的订单
            params.append("aiProcessed=true&");
            
            // 添加处理时间戳，用于调试和跟踪
            params.append("aiProcessedTime=").append(System.currentTimeMillis()).append("&");
        
        // 移除最后的&
            if (params.length() > 0 && params.charAt(params.length() - 1) == '&') {
            params.setLength(params.length() - 1);
        }
        
        String finalParams = params.toString();
        log.info("生成的完整URL参数: {}", finalParams);
        log.info("完整跳转URL: /booking?{}", finalParams);
        
        return finalParams;
            
        } catch (Exception e) {
            log.error("生成订单URL参数时发生错误: {}", e.getMessage(), e);
            // 返回基本参数，确保系统仍能正常工作
            StringBuilder fallbackParams = new StringBuilder();
            if (product != null && product.getId() != null) {
                fallbackParams.append("productId=").append(product.getId()).append("&");
                fallbackParams.append("productType=group&");
            }
            fallbackParams.append("aiProcessed=true&");
            fallbackParams.append("error=paramGeneration");
            
            String fallback = fallbackParams.toString();
            log.warn("使用回退参数: {}", fallback);
            return fallback;
        }
    }
    
    /**
     * 保存聊天记录
     */
    private void saveChatMessage(ChatRequest request, String response, Integer messageType, String extractedData) {
        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .userMessage(request.getMessage())
                .botResponse(response)
                .messageType(messageType)
                .extractedData(extractedData)
                .userType(request.getUserType())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        try {
            chatMessageMapper.insert(chatMessage);
        } catch (Exception e) {
            log.error("保存聊天记录失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 查询航班信息（自动查询功能）
     * 这里提供一个框架，可以接入第三方航班API
     */
    private OrderInfo.FlightInfo queryFlightInfo(String flightNumber) {
        try {
            log.info("开始查询航班信息: {}", flightNumber);
            
            // 优先尝试真实API查询（AviationStack免费API）
            OrderInfo.FlightInfo realFlightInfo = queryFlightFromRealAPI(flightNumber);
            if (realFlightInfo != null) {
                log.info("真实API成功查询到航班{}信息", flightNumber);
                return realFlightInfo;
            }
            
            // 如果真实API失败，不使用模拟数据，直接返回null
            log.info("真实API查询失败，不填入航班详细信息");
            
        } catch (Exception e) {
            log.error("查询航班{}信息时出错: {}", flightNumber, e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * 模拟航班查询（用于演示，实际使用时替换为真实API）
     */
    private OrderInfo.FlightInfo simulateFlightQuery(String flightNumber) {
        // 这里可以添加一些常见航班的模拟数据
        // 实际部署时应该替换为真实的API调用
        
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            return null;
        }
        
        // 根据航班号前缀判断航空公司并提供模拟数据
        String airline = getAirlineByFlightNumber(flightNumber);
        
        // 模拟一些常见的时间
        String departureTime = "09:00";
        String arrivalTime = "12:30";
        
        // 根据航班号特征调整时间（简单的启发式规则）
        if (flightNumber.toLowerCase().contains("va")) {
            // Virgin Australia 的一些常见时间
            departureTime = "09:15";
            arrivalTime = "12:45";
        } else if (flightNumber.toLowerCase().contains("jq")) {
            // Jetstar 的一些常见时间
            departureTime = "10:30";
            arrivalTime = "14:00";
        }
        
        log.info("模拟查询航班{}: {}航空公司, 起飞={}, 抵达={}", 
            flightNumber, airline, departureTime, arrivalTime);
        
        return OrderInfo.FlightInfo.builder()
                .flightNumber(flightNumber)
                .airline(airline)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .departureAirport("SYD") // 默认悉尼
                .arrivalAirport("HBA") // 默认霍巴特
                .status("Scheduled")
                .build();
    }
    
    /**
     * 根据航班号获取航空公司
     */
    private String getAirlineByFlightNumber(String flightNumber) {
        if (flightNumber == null) return "Unknown";
        
        String prefix = flightNumber.replaceAll("\\d+", "").toUpperCase();
        
        switch (prefix) {
            case "VA": return "Virgin Australia";
            case "JQ": return "Jetstar";
            case "QF": return "Qantas";
            case "TT": return "Tigerair";
            case "AN": return "Ansett Australia";
            default: return "Unknown Airline";
        }
    }
    
    /**
     * 真实API查询方法 - AviationStack免费API
     * 文档: https://aviationstack.com/documentation
     */
    @SuppressWarnings("unused")
    private OrderInfo.FlightInfo queryFlightFromRealAPI(String flightNumber) {
        // 检查是否启用真实API
        if (!aviationStackEnabled) {
            log.debug("AviationStack API未启用，跳过真实API查询");
            return null;
        }
        
        // 检查API密钥是否配置
        if (aviationStackApiKey == null || aviationStackApiKey.isEmpty() || 
            "YOUR_AVIATIONSTACK_API_KEY".equals(aviationStackApiKey)) {
            log.warn("AviationStack API Key未配置，跳过真实API查询");
            return null;
        }
        
        try {
            // 构建请求URL - 查询特定航班号
            String url = String.format("%s/flights?access_key=%s&flight_iata=%s&limit=1", 
                aviationStackBaseUrl, aviationStackApiKey, flightNumber);
            
            log.info("调用AviationStack API查询航班: {}", flightNumber);
            
            // 使用Spring的RestTemplate发送HTTP请求
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析JSON响应
                com.alibaba.fastjson.JSONObject jsonResponse = com.alibaba.fastjson.JSON.parseObject(response.getBody());
                
                // 检查API调用是否成功
                if (jsonResponse.getBooleanValue("success")) {
                    com.alibaba.fastjson.JSONArray data = jsonResponse.getJSONArray("data");
                    
                    if (data != null && data.size() > 0) {
                        com.alibaba.fastjson.JSONObject flightData = data.getJSONObject(0);
                        
                        // 提取航班信息
                        com.alibaba.fastjson.JSONObject departure = flightData.getJSONObject("departure");
                        com.alibaba.fastjson.JSONObject arrival = flightData.getJSONObject("arrival");
                        com.alibaba.fastjson.JSONObject airline = flightData.getJSONObject("airline");
                        
                        String depTime = null;
                        String arrTime = null;
                        String depAirport = null;
                        String arrAirport = null;
                        String airlineName = null;
                        
                        // 解析出发信息
                        if (departure != null) {
                            depTime = parseDateTime(departure.getString("scheduled"));
                            depAirport = departure.getString("iata");
                        }
                        
                        // 解析到达信息
                        if (arrival != null) {
                            arrTime = parseDateTime(arrival.getString("scheduled"));
                            arrAirport = arrival.getString("iata");
                        }
                        
                        // 解析航空公司信息
                        if (airline != null) {
                            airlineName = airline.getString("name");
                        }
                        
                        OrderInfo.FlightInfo flightInfo = OrderInfo.FlightInfo.builder()
                                .flightNumber(flightNumber)
                                .departureTime(depTime)
                                .arrivalTime(arrTime)
                                .departureAirport(depAirport)
                                .arrivalAirport(arrAirport)
                                .airline(airlineName)
                                .status("Scheduled")
                                .build();
                        
                        log.info("AviationStack API成功返回航班{}信息: 起飞时间={}, 抵达时间={}, 航空公司={}", 
                            flightNumber, depTime, arrTime, airlineName);
                        
                        return flightInfo;
                    } else {
                        log.warn("AviationStack API未找到航班{}的信息", flightNumber);
                    }
                } else {
                    log.warn("AviationStack API调用失败: {}", jsonResponse.getString("error"));
                }
            } else {
                log.warn("AviationStack API响应异常: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("调用AviationStack API失败: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * 解析AviationStack返回的日期时间格式
     */
    private String parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        
        try {
            // AviationStack返回格式: "2025-05-31T09:15:00+00:00"
            // 我们只需要时间部分: "09:15"
            if (dateTimeStr.contains("T")) {
                String timePart = dateTimeStr.split("T")[1];
                if (timePart.contains(":")) {
                    String[] timeComponents = timePart.split(":");
                    if (timeComponents.length >= 2) {
                        return timeComponents[0] + ":" + timeComponents[1];
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析日期时间失败: {}", dateTimeStr, e);
        }
        
        return dateTimeStr; // 如果解析失败，返回原始字符串
    }

    /**
     * 检查是否为订单查询请求
     */
    private boolean isOrderQueryRequest(String message) {
        String lowerMessage = message.toLowerCase();
        
        // 检查是否包含订单查询关键词
        boolean hasOrderKeyword = lowerMessage.contains("订单") || 
                                 lowerMessage.contains("预订") || 
                                 lowerMessage.contains("查询") ||
                                 lowerMessage.contains("修改") ||
                                 lowerMessage.contains("更改") ||
                                 lowerMessage.contains("编辑");
        
        // 检查是否包含人名（中文姓名模式）
        boolean hasChineseName = message.matches(".*[\\u4e00-\\u9fa5]{2,4}.*");
        
        // 检查是否包含电话号码模式
        boolean hasPhoneNumber = message.matches(".*1[3-9]\\d{9}.*");
        
        // 检查是否包含护照号模式
        boolean hasPassportNumber = message.matches(".*[A-Z]\\d{8}.*") || 
                                   message.matches(".*[A-Z]{2}\\d{7}.*");
        
        // 如果包含订单关键词且包含人名、电话或护照号，则认为是订单查询
        if (hasOrderKeyword && (hasChineseName || hasPhoneNumber || hasPassportNumber)) {
            log.info("检测到订单查询请求: {}", message);
            return true;
        }
        
        // 检查特定的查询模式
        if (lowerMessage.matches(".*(查找|找到|搜索).*(订单|预订).*") ||
            lowerMessage.matches(".*(修改|更改|编辑).*(订单|预订).*") ||
            lowerMessage.matches(".*订单.*[\\u4e00-\\u9fa5]{2,4}.*") ||
            lowerMessage.matches(".*[\\u4e00-\\u9fa5]{2,4}.*订单.*")) {
            log.info("检测到订单查询请求（模式匹配）: {}", message);
            return true;
        }
        
        return false;
    }
    
    /**
     * 处理订单查询请求
     */
    private ChatResponse handleOrderQuery(ChatRequest request) {
        try {
            String message = request.getMessage();
            log.info("处理订单查询请求: {}", message);
            
            // 获取当前用户信息进行权限控制
            Long currentUserId = request.getUserId();
            Integer userType = request.getUserType();
            
            if (currentUserId == null) {
                String response = "请先登录后再查询订单信息。";
                saveChatMessage(request, response, 2, null);
                return ChatResponse.success(response);
            }
            
            log.info("当前用户信息: userId={}, userType={}", currentUserId, userType);
            log.info("用户类型解释: userType=1(普通用户-查询自己订单), userType=2(操作员-查询自己创建的订单), userType=3(中介主号-查询代理商所有订单)");
            
            // 提取查询关键词
            List<String> names = extractChineseNames(message);
            List<String> phones = extractPhoneNumbers(message);
            List<String> passports = extractPassportNumbers(message);
            
            log.info("提取的查询信息 - 姓名: {}, 电话: {}, 护照: {}", names, phones, passports);
            
            // 如果没有提取到有效的查询信息
            if (names.isEmpty() && phones.isEmpty() && passports.isEmpty()) {
                String response = "请提供更具体的查询信息，比如：\n" +
                               "• 联系人姓名（如：张三、李小明、Liu）\n" +
                               "• 联系电话（如：13800138000）\n" +
                               "• 护照号码\n" +
                               "• 订单号\n\n" +
                               "示例：\"查询张三的订单\" 或 \"修改李小明的预订信息\"";
                saveChatMessage(request, response, 2, null);
                return ChatResponse.success(response);
            }
            
            // 查询订单 - 根据用户类型进行权限控制
            List<TourBooking> foundBookings = new ArrayList<>();
            
            // 按姓名查询
            for (String name : names) {
                // 使用新的查询方法，支持中文和英文姓名
                List<TourBooking> allBookings = tourBookingMapper.getByPassengerName(name);
                log.info("通过姓名 '{}' 查询到 {} 个订单", name, allBookings.size());
                
                // 打印查询到的所有订单信息，用于调试
                for (TourBooking booking : allBookings) {
                    log.info("查询到订单: bookingId={}, orderNumber={}, contactPerson={}, userId={}, agentId={}, operatorId={}", 
                        booking.getBookingId(), booking.getOrderNumber(), booking.getContactPerson(),
                        booking.getUserId(), booking.getAgentId(), booking.getOperatorId());
                }
                
                // 根据用户类型过滤订单
                for (TourBooking booking : allBookings) {
                    boolean hasPermission = false;
                    String permissionReason = "";
                    
                    if (userType == 1) {
                        // 普通用户：只能查询自己的订单
                        if (booking.getUserId() != null && booking.getUserId().equals(currentUserId.intValue())) {
                            hasPermission = true;
                            permissionReason = "普通用户查询自己的订单";
                        } else {
                            permissionReason = String.format("普通用户无权限查询他人订单 (订单userId=%s, 当前userId=%s)", 
                                booking.getUserId(), currentUserId);
                        }
                    } else if (userType == 2) {
                        // 操作员：只能查询自己创建的订单（operatorId = currentUserId）
                        log.info("权限判断详情 - 订单operatorId: {} (类型: {}), 当前userId: {} (类型: {})", 
                            booking.getOperatorId(), 
                            booking.getOperatorId() != null ? booking.getOperatorId().getClass().getSimpleName() : "null",
                            currentUserId, 
                            currentUserId.getClass().getSimpleName());
                        
                        // 修复：使用Long类型进行比较，不转换为Integer
                        if (booking.getOperatorId() != null && booking.getOperatorId().equals(currentUserId)) {
                            hasPermission = true;
                            permissionReason = "操作员查询自己创建的订单";
                            log.info("✅ 权限验证通过：operatorId {} equals currentUserId {}", booking.getOperatorId(), currentUserId);
                        } else {
                            permissionReason = String.format("操作员只能查询自己创建的订单 (订单operatorId=%s, 当前operatorId=%s)", 
                                booking.getOperatorId(), currentUserId);
                            log.info("❌ 权限验证失败：operatorId {} NOT equals currentUserId {}", booking.getOperatorId(), currentUserId);
                            if (booking.getOperatorId() != null) {
                                log.info("具体比较结果：booking.getOperatorId().equals(currentUserId) = {}", 
                                    booking.getOperatorId().equals(currentUserId));
                            }
                        }
                    } else if (userType == 3) {
                        // 中介主号：可以查询整个代理商的所有订单（agentId = currentUserId）
                        // 修复：使用Long类型进行比较，不转换为Integer
                        if (booking.getAgentId() != null && booking.getAgentId().equals(currentUserId)) {
                            hasPermission = true;
                            permissionReason = "中介主号查询所属代理商的所有订单";
                        } else {
                            permissionReason = String.format("中介主号无权限查询其他代理商订单 (订单agentId=%s, 当前agentId=%s)", 
                                booking.getAgentId(), currentUserId);
                        }
                    }
                    
                    if (hasPermission) {
                        foundBookings.add(booking);
                        log.info("✅ 用户{}有权限访问订单{}: {}", currentUserId, booking.getBookingId(), permissionReason);
                    } else {
                        log.info("❌ 用户{}无权限访问订单{}: {}", currentUserId, booking.getBookingId(), permissionReason);
                    }
                }
            }
            
            // 按电话查询 - 需要添加相应的查询方法
            // 暂时跳过，可以后续添加
            
            // 去重
            foundBookings = foundBookings.stream()
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            
            log.info("最终找到 {} 个有权限的订单", foundBookings.size());
            
            if (foundBookings.isEmpty()) {
                String response = "没有找到您有权限查看的相关订单信息。\n\n" +
                               "可能的原因：\n" +
                               "• 联系人姓名不匹配\n" +
                               "• 订单不在您的权限范围内\n" +
                               "• 信息输入有误\n\n" +
                               "建议：\n" +
                               "• 确认联系人姓名拼写正确（支持中文和英文）\n" +
                               "• 提供订单号进行精确查询\n" +
                               "• 联系客服协助查询\n\n" +
                               String.format("调试信息：当前用户类型=%d, 用户ID=%d", userType, currentUserId);
                
                saveChatMessage(request, response, 2, null);
                return ChatResponse.success(response);
            }
            
            // 生成查询结果和跳转链接
            StringBuilder responseBuilder = new StringBuilder();
            responseBuilder.append("✅ 找到以下订单信息：\n\n");
            
            // 用于存储所有订单的跳转信息
            List<java.util.Map<String, Object>> orderActions = new ArrayList<>();
            
            for (int i = 0; i < foundBookings.size() && i < 5; i++) { // 最多显示5个结果
                TourBooking booking = foundBookings.get(i);
                responseBuilder.append(String.format("📋 **订单 %d**\n", i + 1));
                responseBuilder.append(String.format("🏷️ 订单号：`%s`\n", booking.getOrderNumber()));
                responseBuilder.append(String.format("👤 联系人：%s\n", booking.getContactPerson()));
                responseBuilder.append(String.format("📞 联系电话：%s\n", booking.getContactPhone()));
                responseBuilder.append(String.format("📅 行程日期：%s 至 %s\n", 
                    booking.getTourStartDate(), booking.getTourEndDate()));
                responseBuilder.append(String.format("📊 订单状态：%s\n", getStatusText(booking.getStatus())));
                responseBuilder.append(String.format("💰 支付状态：%s\n", getPaymentStatusText(booking.getPaymentStatus())));
                
                // 生成正确的编辑链接
                String editUrl = generateEditOrderUrl(booking);
                responseBuilder.append(String.format("🔗 订单详情链接：%s\n\n", editUrl));
                
                // 添加到订单操作列表（供前端渲染按钮）
                java.util.Map<String, Object> orderAction = new java.util.HashMap<>();
                orderAction.put("bookingId", booking.getBookingId());
                orderAction.put("orderNumber", booking.getOrderNumber());
                orderAction.put("contactPerson", booking.getContactPerson());
                orderAction.put("editUrl", editUrl);
                orderAction.put("status", booking.getStatus());
                orderActions.add(orderAction);
            }
            
            if (foundBookings.size() > 5) {
                responseBuilder.append(String.format("📝 还有 %d 个相关订单，请提供更具体的信息以缩小查询范围。", 
                    foundBookings.size() - 5));
            }
            
            String response = responseBuilder.toString();
            
            // 构建结构化的返回数据，包含订单操作信息
            java.util.Map<String, Object> structuredData = new java.util.HashMap<>();
            structuredData.put("orderActions", orderActions);
            structuredData.put("totalFound", foundBookings.size());
            structuredData.put("displayed", Math.min(foundBookings.size(), 5));
            
            saveChatMessage(request, response, 2, JSON.toJSONString(structuredData));
            
            // 返回带有订单操作信息的ChatResponse
            ChatResponse chatResponse = ChatResponse.success(response);
            // 添加订单操作信息到响应中
            chatResponse.setOrderData(JSON.toJSONString(structuredData));
            chatResponse.setMessageType(2); // 设置为订单查询消息类型
            
            return chatResponse;
            
        } catch (Exception e) {
            log.error("处理订单查询失败: {}", e.getMessage(), e);
            String errorResponse = "查询订单时出现错误，请稍后重试或联系客服。\n\n" +
                                 "如果问题持续存在，请联系客服热线或在线客服。";
            saveChatMessage(request, errorResponse, 2, null);
            return ChatResponse.success(errorResponse);
        }
    }
    
    /**
     * 提取中文姓名和英文拼音姓名（简化版本）
     */
    private List<String> extractChineseNames(String text) {
        List<String> names = new ArrayList<>();
        log.info("开始提取姓名，原始输入文本: '{}'", text);
        
        // 预处理文本：移除换行符和多余空格
        String cleanText = text.replaceAll("\\s+", " ").trim();
        log.info("预处理后的文本: '{}'", cleanText);
        
        // 简单的分词方式：按空格、"的"、"订单"、"预订"等分割
        String[] words = cleanText.split("[\\s的订单预订查询修改编辑]+");
        
        for (String word : words) {
            word = word.trim();
            if (word.isEmpty()) continue;
            
            log.info("检查候选词: '{}'", word);
            
            // 检查是否为有效姓名（中文或英文）
            if (isValidNameCandidate(word)) {
                names.add(word);
                log.info("提取到姓名: {}", word);
            }
        }
        
        // 如果还没有找到，尝试从原文中直接匹配常见的中文姓名
        if (names.isEmpty()) {
            log.info("直接分词未找到姓名，尝试模式匹配...");
            // 中文姓名（2-4个汉字）
            Pattern chinesePattern = Pattern.compile("[\\u4e00-\\u9fa5]{2,4}");
            Matcher chineseMatcher = chinesePattern.matcher(cleanText);
            while (chineseMatcher.find()) {
                String name = chineseMatcher.group();
                if (!isCommonWord(name)) {
                    names.add(name);
                    log.info("模式匹配提取到中文姓名: {}", name);
                }
            }
            
            // 英文姓名（字母组合，长度2-15）
            Pattern englishPattern = Pattern.compile("[a-zA-Z]{2,15}");
            Matcher englishMatcher = englishPattern.matcher(cleanText);
            while (englishMatcher.find()) {
                String name = englishMatcher.group();
                if (isValidNameCandidate(name)) {
                    names.add(name);
                    log.info("模式匹配提取到英文姓名: {}", name);
                }
            }
        }
        
        log.info("最终提取的所有姓名: {}", names);
        return names;
    }
    
    /**
     * 检查是否为有效的姓名候选（简化版本）
     */
    private boolean isValidNameCandidate(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        name = name.trim();
        
        // 长度检查
        if (name.length() < 2 || name.length() > 15) {
            log.info("'{}' 长度不符合要求，跳过", name);
            return false;
        }
        
        // 检查是否包含数字或特殊字符
        if (name.matches(".*[\\d\\s\\p{Punct}].*")) {
            log.info("'{}' 包含数字或特殊字符，跳过", name);
            return false;
        }
        
        // 检查是否为常见词汇
        if (isCommonWord(name) || isCommonEnglishWord(name)) {
            log.info("'{}' 是常见词汇，跳过", name);
            return false;
        }
        
        // 检查是否为明显的非姓名词汇
        String[] invalidWords = {"订单", "预订", "查询", "修改", "编辑", "信息", "详情", 
                                "order", "booking", "query", "search", "find", "edit", "info"};
        for (String invalid : invalidWords) {
            if (name.equalsIgnoreCase(invalid)) {
                log.info("'{}' 是无效词汇，跳过", name);
                return false;
            }
        }
        
        // 中文姓名：2-4个汉字
        if (name.matches("[\\u4e00-\\u9fa5]{2,4}")) {
            log.info("'{}' 是有效的中文姓名候选", name);
            return true;
        }
        
        // 英文姓名：只包含字母
        if (name.matches("[a-zA-Z]{2,15}")) {
            log.info("'{}' 是有效的英文姓名候选", name);
            return true;
        }
        
        log.info("'{}' 不符合姓名格式，跳过", name);
        return false;
    }
    
    /**
     * 提取电话号码
     */
    private List<String> extractPhoneNumbers(String text) {
        List<String> phones = new ArrayList<>();
        Pattern pattern = Pattern.compile("1[3-9]\\d{9}");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            phones.add(matcher.group());
        }
        
        return phones;
    }
    
    /**
     * 提取护照号
     */
    private List<String> extractPassportNumbers(String text) {
        List<String> passports = new ArrayList<>();
        
        // 中国护照格式：E + 8位数字
        Pattern pattern1 = Pattern.compile("[A-Z]\\d{8}");
        Matcher matcher1 = pattern1.matcher(text);
        while (matcher1.find()) {
            passports.add(matcher1.group());
        }
        
        // 其他国家护照格式：2个字母 + 7位数字
        Pattern pattern2 = Pattern.compile("[A-Z]{2}\\d{7}");
        Matcher matcher2 = pattern2.matcher(text);
        while (matcher2.find()) {
            passports.add(matcher2.group());
        }
        
        return passports;
    }
    
    /**
     * 检查是否为常见词汇（非姓名）
     */
    private boolean isCommonWord(String word) {
        String[] commonWords = {"订单", "预订", "查询", "修改", "更改", "编辑", "客户", "联系", 
                               "电话", "手机", "护照", "信息", "详情", "状态", "日期", "时间",
                               "的订单", "的预订", "的信息", "的详情", "的状态", "预订信息",
                               "订单信息", "客户信息", "联系信息", "航班信息", "酒店信息"};
        
        for (String common : commonWords) {
            if (word.equals(common)) {
                log.info("'{}' 是常见词汇，跳过", word);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否为有效的中文姓名
     */
    private boolean isValidChineseName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否包含数字
        if (name.matches(".*\\d.*")) {
            return false;
        }
        
        // 检查是否包含特殊字符
        if (name.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return false;
        }
        
        // 检查长度（中文姓名通常2-4个字符）
        if (name.length() < 2 || name.length() > 4) {
            return false;
        }
        
        // 检查是否全部为中文字符
        if (!name.matches("[\\u4e00-\\u9fa5]+")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "待处理";
            case "confirmed": return "已确认";
            case "cancelled": return "已取消";
            case "completed": return "已完成";
            default: return status;
        }
    }
    
    /**
     * 获取支付状态文本
     */
    private String getPaymentStatusText(String paymentStatus) {
        if (paymentStatus == null) return "未知";
        switch (paymentStatus) {
            case "unpaid": return "未支付";
            case "partial": return "部分支付";
            case "paid": return "已支付";
            case "refunded": return "已退款";
            default: return paymentStatus;
        }
    }
    
    /**
     * 生成编辑订单链接
     */
    private String generateEditOrderUrl(TourBooking booking) {
        // 根据前端路由结构生成正确的链接
        // 使用订单详情页面：/orders/:orderId
        return String.format("http://localhost:3000/orders/%d", booking.getBookingId());
    }
    
    /**
     * 检查是否为有效的英文全名（姓名格式）
     */
    private boolean isValidEnglishName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否包含数字
        if (name.matches(".*\\d.*")) {
            return false;
        }
        
        // 检查格式：首字母大写的两个单词，中间用空格分隔
        if (!name.matches("^[A-Z][a-z]+\\s+[A-Z][a-z]+$")) {
            return false;
        }
        
        // 过滤掉一些常见的非姓名词汇
        String[] parts = name.split("\\s+");
        for (String part : parts) {
            if (isCommonEnglishWord(part)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查是否为有效的单个英文姓名
     */
    private boolean isValidSingleEnglishName(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.info("验证英文单名 '{}': 为空，跳过", name);
            return false;
        }
        
        // 检查是否包含数字
        if (name.matches(".*\\d.*")) {
            log.info("验证英文单名 '{}': 包含数字，跳过", name);
            return false;
        }
        
        // 检查格式：首字母大写，后续小写，长度2-15（修改最小长度从3改为2）
        if (!name.matches("^[A-Z][a-z]{1,14}$")) {
            log.info("验证英文单名 '{}': 格式不匹配 ^[A-Z][a-z]{{1,14}}$，跳过", name);
            return false;
        }
        
        // 检查是否为常见英文姓名
        boolean isCommonName = isCommonEnglishName(name);
        log.info("验证英文单名 '{}': 是否常见姓名 = {}", name, isCommonName);
        
        // 检查是否为常见英文词汇
        boolean isCommonWord = isCommonEnglishWord(name);
        log.info("验证英文单名 '{}': 是否常见词汇 = {}", name, isCommonWord);
        
        // 过滤掉常见的非姓名英文词汇，但允许常见的英文姓名
        if (isCommonWord && !isCommonName) {
            log.info("验证英文单名 '{}': 是常见词汇但不是常见姓名，跳过", name);
            return false;
        }
        
        log.info("验证英文单名 '{}': 通过验证", name);
        return true;
    }
    
    /**
     * 检查是否为常见的英文姓名
     */
    private boolean isCommonEnglishName(String name) {
        String[] commonNames = {"Liu", "Li", "Wang", "Zhang", "Chen", "Yang", "Wu", "Huang", "Zhou", "Xu", 
                               "Sun", "Ma", "Zhu", "Hu", "Guo", "He", "Lin", "Gao", "Luo", "Zheng",
                               "Lei", "Deng", "Feng", "Song", "Tang", "Bai", "Han", "Cao", "Peng", "Zeng",
                               "Xie", "Dong", "Yu", "Shi", "Lu", "Chang", "Jiang", "Pan", "Ye", "Xu",
                               "John", "Jane", "Smith", "Brown", "Johnson", "Williams", "Jones", "Miller",
                               "Davis", "Garcia", "Rodriguez", "Wilson", "Martinez", "Anderson", "Taylor",
                               // 添加常见的中文拼音姓名变体
                               "huamiao", "xiaoming", "xiaoli", "xiaohua", "xiaoyan", "xiaofang", "xiaojun",
                               "wei", "ming", "jun", "hong", "ping", "lei", "tao", "jing", "hua", "yan", 
                               "fang", "ling", "bin", "hao", "qiang", "peng", "gang", "bo", "chao", "kai",
                               "xinyi", "yuting", "mengqi", "zihan", "yuxin", "ruoxi", "hanyu", "siqi"};
        
        for (String commonName : commonNames) {
            if (name.equalsIgnoreCase(commonName)) {
                log.info("'{}' 是常见英文姓名", name);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否为常见的英文词汇（非姓名）
     */
    private boolean isCommonEnglishWord(String word) {
        String[] commonWords = {"Order", "Booking", "Hotel", "Flight", "Date", "Time", "Service", 
                               "Tour", "Travel", "Guest", "Customer", "Phone", "Contact", "Email",
                               "Check", "Room", "Night", "Day", "Adult", "Child", "Person", "Group"};
        
        for (String common : commonWords) {
            if (word.equalsIgnoreCase(common)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否包含动作词汇
     */
    private boolean containsActionWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String[] actionWords = {"查看", "查询", "修改", "编辑", "更改", "删除", "创建", "添加", "搜索", "找到"};
        
        for (String action : actionWords) {
            if (text.contains(action)) {
                log.info("'{}' 包含动作词汇 '{}', 过滤掉", text, action);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否为有效的小写英文姓名
     */
    private boolean isValidLowercaseEnglishName(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.info("验证小写英文名 '{}': 为空，跳过", name);
            return false;
        }
        
        // 检查是否包含数字
        if (name.matches(".*\\d.*")) {
            log.info("验证小写英文名 '{}': 包含数字，跳过", name);
            return false;
        }
        
        // 检查格式：全小写字母，长度2-15
        if (!name.matches("^[a-z]{2,15}$")) {
            log.info("验证小写英文名 '{}': 格式不匹配 ^[a-z]{{2,15}}$，跳过", name);
            return false;
        }
        
        // 检查是否为常见英文姓名（忽略大小写）
        boolean isCommonName = isCommonEnglishName(name);
        log.info("验证小写英文名 '{}': 是否常见姓名 = {}", name, isCommonName);
        
        // 检查是否为常见英文词汇（忽略大小写）
        boolean isCommonWord = isCommonEnglishWord(name);
        log.info("验证小写英文名 '{}': 是否常见词汇 = {}", name, isCommonWord);
        
        // 过滤掉一些明显的非姓名词汇
        String[] invalidWords = {"the", "and", "but", "for", "with", "from", "this", "that", "what", 
                                "when", "where", "how", "why", "can", "will", "would", "could", 
                                "order", "booking", "hotel", "flight", "date", "time", "service", 
                                "tour", "travel", "guest", "customer", "phone", "contact", "email"};
        
        for (String invalid : invalidWords) {
            if (name.equalsIgnoreCase(invalid)) {
                log.info("验证小写英文名 '{}': 是无效词汇，跳过", name);
                return false;
            }
        }
        
        // 如果是常见词汇但不是常见姓名，则过滤掉
        if (isCommonWord && !isCommonName) {
            log.info("验证小写英文名 '{}': 是常见词汇但不是常见姓名，跳过", name);
            return false;
        }
        
        // 长度过短的词汇需要更严格验证（必须是常见姓名）
        if (name.length() <= 3 && !isCommonName) {
            log.info("验证小写英文名 '{}': 长度过短且不是常见姓名，跳过", name);
            return false;
        }
        
        log.info("验证小写英文名 '{}': 通过验证", name);
        return true;
    }
    
    /**
     * 获取天气信息
     */
    private String getWeatherInfo(String message) {
        try {
            // 提取城市名（塔斯马尼亚相关城市）
            String cityName = extractCityName(message);
            if (cityName == null) {
                cityName = "Hobart"; // 默认霍巴特
            }
            
            // 尝试从真实API获取天气信息
            WeatherInfo weatherInfo = getWeatherFromAPI(cityName);
            
            if (weatherInfo != null) {
                return formatWeatherResponse(weatherInfo, cityName);
            } else {
                // API失败时，返回通用天气建议
                return getGeneralWeatherAdvice(cityName);
            }
            
        } catch (Exception e) {
            log.error("获取天气信息失败: {}", e.getMessage(), e);
            return getGeneralWeatherAdvice("塔斯马尼亚");
        }
    }
    
    /**
     * 从消息中提取城市名
     */
    private String extractCityName(String message) {
        String lowerMessage = message.toLowerCase();
        
        // 塔斯马尼亚主要城市和景点
        String[][] cityMappings = {
            {"霍巴特", "hobart"},
            {"朗塞斯顿", "launceston"}, 
            {"德文港", "devonport"},
            {"伯尼", "burnie"},
            {"酒杯湾", "freycinet"},
            {"摇篮山", "cradle mountain"},
            {"布鲁尼岛", "bruny island"},
            {"惠灵顿山", "mount wellington"},
            {"里奇蒙", "richmond"},
            {"斯特拉恩", "strahan"},
            {"塔斯马尼亚", "hobart"}  // 默认用霍巴特代表塔斯马尼亚
        };
        
        for (String[] mapping : cityMappings) {
            if (lowerMessage.contains(mapping[0])) {
                return mapping[1];
            }
        }
        
        return null;
    }
    
    /**
     * 从OpenWeatherMap API获取天气信息
     */
    private WeatherInfo getWeatherFromAPI(String cityName) {
        if (!weatherApiEnabled || weatherApiKey == null || weatherApiKey.isEmpty() || 
            "YOUR_OPENWEATHERMAP_API_KEY".equals(weatherApiKey)) {
            log.warn("OpenWeatherMap API未配置或未启用");
            return null;
        }
        
        try {
            // 先检查缓存
            String cacheKey = "weather:" + cityName.toLowerCase();
            String cachedData = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedData != null) {
                log.info("从缓存获取天气信息: {}", cityName);
                return JSON.parseObject(cachedData, WeatherInfo.class);
            }
            
            // 构建API请求URL
            String url = String.format("%s/weather?q=%s,AU&appid=%s&units=metric&lang=zh_cn", 
                weatherApiBaseUrl, cityName, weatherApiKey);
            
            log.info("请求OpenWeatherMap天气API: {}", cityName);
            
            // 发送HTTP请求
            RestTemplate restTemplate = new RestTemplate();
            org.springframework.http.ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析天气数据
                WeatherInfo weatherInfo = parseWeatherResponse(response.getBody());
                
                if (weatherInfo != null) {
                    // 缓存结果
                    redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(weatherInfo), 
                        Duration.ofSeconds(weatherCacheDuration));
                    
                    log.info("成功获取{}天气信息: {}°C, {}", cityName, weatherInfo.getTemperature(), weatherInfo.getDescription());
                    return weatherInfo;
                }
            } else {
                log.warn("OpenWeatherMap API响应异常: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("调用OpenWeatherMap API失败: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * 解析OpenWeatherMap API响应
     */
    private WeatherInfo parseWeatherResponse(String responseBody) {
        try {
            com.alibaba.fastjson.JSONObject jsonResponse = com.alibaba.fastjson.JSON.parseObject(responseBody);
            
            // 检查响应是否成功
            if (jsonResponse.getIntValue("cod") != 200) {
                log.warn("OpenWeatherMap API返回错误: {}", jsonResponse.getString("message"));
                return null;
            }
            
            // 提取天气信息
            com.alibaba.fastjson.JSONObject main = jsonResponse.getJSONObject("main");
            com.alibaba.fastjson.JSONArray weather = jsonResponse.getJSONArray("weather");
            com.alibaba.fastjson.JSONObject wind = jsonResponse.getJSONObject("wind");
            
            WeatherInfo weatherInfo = new WeatherInfo();
            
            if (main != null) {
                weatherInfo.setTemperature(main.getDoubleValue("temp"));
                weatherInfo.setFeelsLike(main.getDoubleValue("feels_like"));
                weatherInfo.setMinTemperature(main.getDoubleValue("temp_min"));
                weatherInfo.setMaxTemperature(main.getDoubleValue("temp_max"));
                weatherInfo.setHumidity(main.getIntValue("humidity"));
                weatherInfo.setPressure(main.getIntValue("pressure"));
            }
            
            if (weather != null && weather.size() > 0) {
                com.alibaba.fastjson.JSONObject weatherObj = weather.getJSONObject(0);
                weatherInfo.setDescription(weatherObj.getString("description"));
                weatherInfo.setIcon(weatherObj.getString("icon"));
                weatherInfo.setMain(weatherObj.getString("main"));
            }
            
            if (wind != null) {
                weatherInfo.setWindSpeed(wind.getDoubleValue("speed"));
                weatherInfo.setWindDirection(wind.getIntValue("deg"));
            }
            
            // 设置时间戳
            weatherInfo.setTimestamp(System.currentTimeMillis() / 1000);
            
            return weatherInfo;
            
        } catch (Exception e) {
            log.error("解析天气API响应失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 格式化天气响应信息
     */
    private String formatWeatherResponse(WeatherInfo weather, String cityName) {
        StringBuilder response = new StringBuilder();
        
        // 获取中文城市名
        String chineseCityName = getChineseCityName(cityName);
        
        response.append("🌤️ **").append(chineseCityName).append("实时天气**\n\n");
        
        // 基本天气信息
        response.append("🌡️ **当前气温**: ").append(Math.round(weather.getTemperature())).append("°C\n");
        response.append("🌈 **天气状况**: ").append(weather.getDescription()).append("\n");
        
        if (weather.getFeelsLike() != null && weather.getFeelsLike() != 0) {
            response.append("👤 **体感温度**: ").append(Math.round(weather.getFeelsLike())).append("°C\n");
        }
        
        if (weather.getMinTemperature() != null && weather.getMaxTemperature() != null) {
            response.append("📊 **温度范围**: ").append(Math.round(weather.getMinTemperature()))
                     .append("°C ~ ").append(Math.round(weather.getMaxTemperature())).append("°C\n");
        }
        
        if (weather.getHumidity() != null && weather.getHumidity() > 0) {
            response.append("💧 **湿度**: ").append(weather.getHumidity()).append("%\n");
        }
        
        if (weather.getWindSpeed() != null && weather.getWindSpeed() > 0) {
            response.append("🌬️ **风速**: ").append(String.format("%.1f", weather.getWindSpeed())).append(" m/s\n");
        }
        
        response.append("\n");
        
        // 旅游建议
        response.append("🎒 **旅游建议**:\n");
        response.append(getTravelAdvice(weather)).append("\n\n");
        
        // 数据来源
        response.append("📡 *数据来源: OpenWeatherMap*");
        
        return response.toString();
    }
    
    /**
     * 获取中文城市名
     */
    private String getChineseCityName(String englishName) {
        switch (englishName.toLowerCase()) {
            case "hobart": return "霍巴特";
            case "launceston": return "朗塞斯顿";
            case "devonport": return "德文港";
            case "burnie": return "伯尼";
            case "freycinet": return "酒杯湾";
            case "cradle mountain": return "摇篮山";
            case "bruny island": return "布鲁尼岛";
            case "mount wellington": return "惠灵顿山";
            case "richmond": return "里奇蒙";
            case "strahan": return "斯特拉恩";
            default: return englishName;
        }
    }
    
    /**
     * 根据天气状况提供旅游建议
     */
    private String getTravelAdvice(WeatherInfo weather) {
        double temp = weather.getTemperature();
        String description = weather.getDescription();
        String main = weather.getMain();
        
        StringBuilder advice = new StringBuilder();
        
        // 温度建议
        if (temp < 5) {
            advice.append("• 🧥 气温较低，建议穿厚外套、毛衣等保暖衣物\n");
        } else if (temp < 15) {
            advice.append("• 👕 气温适中偏凉，建议穿长袖+外套，方便增减\n");
        } else if (temp < 25) {
            advice.append("• 🌞 气温宜人，适合户外活动，建议穿轻便舒适衣物\n");
        } else {
            advice.append("• ☀️ 气温较高，建议穿轻薄透气衣物，注意防晒\n");
        }
        
        // 天气状况建议
        if (main != null) {
            switch (main.toLowerCase()) {
                case "rain":
                case "drizzle":
                    advice.append("• 🌧️ 有降雨，建议携带雨具，选择室内活动或有遮挡的景点\n");
                    break;
                case "snow":
                    advice.append("• ❄️ 有降雪，路面可能湿滑，注意安全，适合观赏雪景\n");
                    break;
                case "clear":
                    advice.append("• ☀️ 天气晴朗，是户外游览的绝佳时机\n");
                    break;
                case "clouds":
                    advice.append("• ☁️ 多云天气，适合拍照，光线柔和\n");
                    break;
            }
        }
        
        // 湿度建议
        if (weather.getHumidity() != null) {
            if (weather.getHumidity() > 80) {
                advice.append("• 💧 湿度较高，体感可能较闷，注意适当补水\n");
            } else if (weather.getHumidity() < 40) {
                advice.append("• 🏜️ 湿度较低，注意保湿，多喝水\n");
            }
        }
        
        // 风速建议
        if (weather.getWindSpeed() != null && weather.getWindSpeed() > 5) {
            advice.append("• 🌬️ 风力较大，户外活动注意防风保暖\n");
        }
        
        return advice.length() > 0 ? advice.toString().trim() : "• 🌟 当前天气适宜旅游，祝您玩得愉快！";
    }
    
    /**
     * 获取通用天气建议（API不可用时的后备方案）
     */
    private String getGeneralWeatherAdvice(String cityName) {
        String chineseCityName = getChineseCityName(cityName);
        
        return "🌤️ **" + chineseCityName + "天气提醒**\n\n" +
               "抱歉，暂时无法获取实时天气信息。以下是塔斯马尼亚的一般天气建议：\n\n" +
               "❄️ **冬季 (6-8月)**:\n" +
               "• 气温: 5-15°C，早晚较冷\n" +
               "• 建议: 多层穿衣，防风外套必备\n" +
               "• 优点: 人少景美，空气清新\n\n" +
               "🌸 **春季 (9-11月)**:\n" +
               "• 气温: 8-18°C，变化较大\n" +
               "• 建议: 准备增减衣物\n" +
               "• 优点: 野花盛开，风景如画\n\n" +
               "☀️ **夏季 (12-2月)**:\n" +
               "• 气温: 15-25°C，舒适宜人\n" +
               "• 建议: 轻便衣物+防晒用品\n" +
               "• 优点: 户外活动最佳时节\n\n" +
               "🍂 **秋季 (3-5月)**:\n" +
               "• 气温: 10-20°C，色彩斑斓\n" +
               "• 建议: 长袖+轻薄外套\n" +
               "• 优点: 摄影绝佳，避开人流\n\n" +
               "💡 **小提示**: 塔斯马尼亚天气变化快，建议随时关注天气预报并准备多层衣物！\n\n" +
               "想获取实时天气？请稍后重试或查看官方天气预报。";
    }
    
    /**
     * 天气信息实体类
     */
    public static class WeatherInfo {
        private Double temperature;        // 当前温度
        private Double feelsLike;         // 体感温度
        private Double minTemperature;    // 最低温度
        private Double maxTemperature;    // 最高温度
        private Integer humidity;         // 湿度
        private Integer pressure;         // 气压
        private String description;       // 天气描述
        private String icon;             // 天气图标
        private String main;             // 主要天气状况
        private Double windSpeed;        // 风速
        private Integer windDirection;   // 风向
        private Long timestamp;          // 时间戳
        
        // Getters and Setters
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        
        public Double getFeelsLike() { return feelsLike; }
        public void setFeelsLike(Double feelsLike) { this.feelsLike = feelsLike; }
        
        public Double getMinTemperature() { return minTemperature; }
        public void setMinTemperature(Double minTemperature) { this.minTemperature = minTemperature; }
        
        public Double getMaxTemperature() { return maxTemperature; }
        public void setMaxTemperature(Double maxTemperature) { this.maxTemperature = maxTemperature; }
        
        public Integer getHumidity() { return humidity; }
        public void setHumidity(Integer humidity) { this.humidity = humidity; }
        
        public Integer getPressure() { return pressure; }
        public void setPressure(Integer pressure) { this.pressure = pressure; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public String getMain() { return main; }
        public void setMain(String main) { this.main = main; }
        
        public Double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }
        
        public Integer getWindDirection() { return windDirection; }
        public void setWindDirection(Integer windDirection) { this.windDirection = windDirection; }
        
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * 检查是否为产品查询请求
     */
    private boolean isProductQueryRequest(String message) {
        String lowerMessage = message.toLowerCase();
        
        // 产品查询关键词
        String[] productKeywords = {
            "推荐", "有什么", "哪些产品", "一日游", "跟团游", "多日游", "旅游", "景点",
            "南部", "北部", "东部", "西部", "霍巴特", "朗塞斯顿", 
            "酒杯湾", "摇篮山", "布鲁尼岛", "费林德斯岛",
            "几天", "天数", "价格", "费用", "多少钱", "热门", "好玩"
        };
        
        for (String keyword : productKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 处理产品查询请求
     */
    private ChatResponse handleProductQuery(ChatRequest request) {
        try {
            log.info("处理产品查询请求: {}", request.getMessage());
            
            // 获取代理商ID（只有中介主号才能享受代理商价格）
            Long agentId = null;
            if (request.getUserType() != null && request.getUserType() == 3) {
                agentId = request.getUserId();
            }
            
            // 调用产品知识服务获取推荐
            TourRecommendationResponse recommendation = tourKnowledgeService.getProductRecommendations(
                    request.getMessage(), agentId);
            
            // 生成友好的回复文本 - 传递用户类型信息
            String response = formatProductRecommendationResponse(recommendation, request.getUserType());
            
            // 构建产品操作信息用于前端按钮
            java.util.Map<String, Object> productData = buildProductActionData(recommendation);
            
            // 保存对话记录
            saveChatMessage(request, response, 2, JSON.toJSONString(productData));
            
            // 返回带有产品操作信息的ChatResponse
            ChatResponse chatResponse = ChatResponse.success(response);
            chatResponse.setOrderData(JSON.toJSONString(productData));
            chatResponse.setMessageType(3); // 设置为产品推荐消息类型
            
            return chatResponse;
            
        } catch (Exception e) {
            log.error("处理产品查询失败: {}", e.getMessage(), e);
            
            // 降级到基础产品推荐
            String fallbackResponse = getBasicProductRecommendation(request.getMessage());
            saveChatMessage(request, fallbackResponse, 2, null);
            return ChatResponse.success(fallbackResponse);
        }
    }
    
    /**
     * 格式化产品推荐响应
     */
    private String formatProductRecommendationResponse(TourRecommendationResponse recommendation, Integer userType) {
        StringBuilder response = new StringBuilder();
        
        // 添加推荐理由
        response.append("🌟 ").append(recommendation.getRecommendationReason()).append("\n\n");
        
        if (recommendation.getRecommendedTours() != null && !recommendation.getRecommendedTours().isEmpty()) {
            response.append("📋 **为您推荐以下产品：**\n\n");
            
            int count = 1;
            for (com.sky.dto.TourDTO tour : recommendation.getRecommendedTours()) {
                response.append(String.format("**%d. %s**\n", count++, tour.getName()));
                
                // 价格显示逻辑：只有中介主号（userType=3）才显示优惠价
                if (userType != null && userType == 3) {
                    // 中介主号显示优惠价
                    if (tour.getDiscountedPrice() != null && tour.getPrice() != null && 
                        !tour.getDiscountedPrice().equals(tour.getPrice())) {
                        response.append(String.format("💰 价格：~~¥%.0f~~ **¥%.0f** (中介主号优惠价)\n", 
                                tour.getPrice().doubleValue(), tour.getDiscountedPrice().doubleValue()));
                    } else if (tour.getPrice() != null) {
                        response.append(String.format("💰 中介主号价格：¥%.0f\n", tour.getPrice().doubleValue()));
                    }
                } else {
                    // 普通用户和中介操作员只显示原价
                    if (tour.getPrice() != null) {
                        response.append(String.format("💰 价格：¥%.0f\n", tour.getPrice().doubleValue()));
                    }
                }
                
                // 添加产品类型
                if ("day".equals(tour.getTourType())) {
                    response.append("⏰ 类型：一日游\n");
                } else if ("group".equals(tour.getTourType())) {
                    response.append("⏰ 类型：跟团游\n");
                }
                
                // 添加简短描述
                if (tour.getDescription() != null && tour.getDescription().length() > 0) {
                    String shortDesc = tour.getDescription().length() > 50 ? 
                            tour.getDescription().substring(0, 50) + "..." : tour.getDescription();
                    response.append("📝 ").append(shortDesc).append("\n");
                }
                
                // 不在文本中添加链接，由前端通过orderData渲染按钮
                response.append("\n");
            }
        } else {
            response.append("😊 暂时没有找到完全匹配的产品，但我们有其他精彩选择！\n\n");
        }
        
        // 添加搜索建议
        if (recommendation.getSearchSuggestion() != null) {
            response.append("💡 **温馨提示：** ").append(recommendation.getSearchSuggestion()).append("\n\n");
        }
        
        // 根据用户类型显示不同的价格说明
        if (userType != null && userType == 3) {
            response.append("💎 **中介主号专享：** 以上价格为您的专属优惠价格\n\n");
        } else if (userType != null && userType == 2) {
            response.append("👔 **中介操作员：** 以上为标准零售价格，如需优惠价请联系主号\n\n");
        } else {
            response.append("💰 **价格说明：** 以上为标准零售价格\n\n");
        }
        
        // 添加联系信息
        response.append("📞 **需要更多信息或预订？**\n");
        response.append("• 点击下方按钮查看详细行程和预订\n");
        response.append("• 如需咨询请联系客服\n");
        response.append("• 实时优惠请关注我们的最新公告");
        
        return response.toString();
    }
    
    /**
     * 构建产品操作数据用于前端按钮渲染
     */
    private java.util.Map<String, Object> buildProductActionData(TourRecommendationResponse recommendation) {
        java.util.Map<String, Object> productData = new java.util.HashMap<>();
        
        if (recommendation.getRecommendedTours() != null && !recommendation.getRecommendedTours().isEmpty()) {
            List<java.util.Map<String, Object>> productActions = new ArrayList<>();
            
            for (com.sky.dto.TourDTO tour : recommendation.getRecommendedTours()) {
                java.util.Map<String, Object> productAction = new java.util.HashMap<>();
                productAction.put("productId", tour.getId());
                productAction.put("productName", tour.getName());
                productAction.put("productType", tour.getTourType());
                productAction.put("price", tour.getPrice());
                productAction.put("discountedPrice", tour.getDiscountedPrice());
                
                // 生成产品详情链接
                String detailsUrl = generateProductDetailsUrl(tour);
                productAction.put("detailsUrl", detailsUrl);
                
                // 生成预订链接
                String bookingUrl = generateProductBookingUrl(tour);
                productAction.put("bookingUrl", bookingUrl);
                
                productActions.add(productAction);
            }
            
            productData.put("productActions", productActions);
        }
        
        productData.put("totalRecommendations", recommendation.getTotalCount());
        productData.put("queryType", recommendation.getQueryType());
        productData.put("hasMore", recommendation.getHasMore());
        
        return productData;
    }
    
    /**
     * 生成产品详情页链接
     */
    private String generateProductDetailsUrl(com.sky.dto.TourDTO tour) {
        if ("day".equals(tour.getTourType())) {
            return String.format("/tours/day-tour/%d", tour.getId());
        } else if ("group".equals(tour.getTourType())) {
            return String.format("/tours/group-tour/%d", tour.getId());
        } else {
            return String.format("/tours/%d", tour.getId());
        }
    }
    
    /**
     * 生成产品预订页链接
     */
    private String generateProductBookingUrl(com.sky.dto.TourDTO tour) {
        StringBuilder bookingUrl = new StringBuilder();
        bookingUrl.append("/booking?");
        
        // 添加产品信息
        bookingUrl.append("productId=").append(tour.getId()).append("&");
        bookingUrl.append("productType=").append(tour.getTourType()).append("&");
        
        if (tour.getName() != null) {
            try {
                bookingUrl.append("productName=").append(java.net.URLEncoder.encode(tour.getName(), "UTF-8")).append("&");
            } catch (java.io.UnsupportedEncodingException e) {
                log.warn("URL编码失败: {}", e.getMessage());
            }
        }
        
        if (tour.getPrice() != null) {
            bookingUrl.append("price=").append(tour.getPrice()).append("&");
        }
        
        // 移除最后的&
        if (bookingUrl.length() > 0 && bookingUrl.charAt(bookingUrl.length() - 1) == '&') {
            bookingUrl.setLength(bookingUrl.length() - 1);
        }
        
        return bookingUrl.toString();
    }
    
    /**
     * 检查是否为天气查询请求
     */
    private boolean isWeatherQueryRequest(String message) {
        String lowerMessage = message.toLowerCase();
        
        String[] weatherKeywords = {
            "天气", "气温", "温度", "下雨", "阳光", "风", "气候", 
            "weather", "temperature", "rain", "sunny", "cloudy"
        };
        
        for (String keyword : weatherKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取基础产品推荐（降级方案）
     */
    private String getBasicProductRecommendation(String message) {
        StringBuilder response = new StringBuilder();
        
        response.append("🌟 **塔斯马尼亚热门推荐**\n\n");
        
        if (message.contains("一日游") || message.contains("1日") || message.contains("1天")) {
            response.append("🚌 **热门一日游：**\n");
            response.append("• 酒杯湾一日游 - 欣赏绝美海湾风光\n");
            response.append("• 摇篮山一日游 - 探索原始森林\n");
            response.append("• 布鲁尼岛一日游 - 品尝新鲜海鲜\n");
        } else if (message.contains("跟团") || message.contains("多日") || message.contains("几天")) {
            response.append("🏕️ **精选跟团游：**\n");
            response.append("• 塔州南部3日游 - 深度体验南部风光\n");
            response.append("• 塔州环岛5日游 - 全面领略塔斯马尼亚\n");
            response.append("• 塔州北部4日游 - 探索历史文化\n");
        } else {
            response.append("🎯 **推荐产品：**\n");
            response.append("• 一日游：适合时间紧张的游客\n");
            response.append("• 跟团游：深度体验塔斯马尼亚文化\n");
            response.append("• 主题游：摄影、美食、冒险等特色体验\n");
        }
        
        response.append("\n📞 **了解更多详情请联系客服！**");
        
        return response.toString();
    }
} 