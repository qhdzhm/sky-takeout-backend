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
import com.sky.mapper.RegionMapper;
import com.sky.mapper.ReviewMapper;
import com.sky.mapper.GuideMapper;
import com.sky.mapper.VehicleMapper;
import com.sky.mapper.DayTourMapper;
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
import java.util.Arrays;

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
    
    // Qwen API配置
    @Value("${qwen.api.key:}")
    private String qwenApiKey;
    
    @Value("${qwen.api.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String qwenBaseUrl;
    
    @Value("${qwen.model:qwen-turbo}")
    private String qwenModel;
    
    @Value("${qwen.timeout:30000}")
    private int qwenTimeout;
    
    @Value("${qwen.max-tokens:2000}")
    private int qwenMaxTokens;
    
    @Value("${qwen.temperature:0.7}")
    private double qwenTemperature;
    
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
    
    // 新增：百度搜索API配置（用于获取外部信息）
    @Value("${baidu.search.api-key:}")
    private String baiduSearchApiKey;
    
    @Value("${baidu.search.base-url:https://aip.baidubce.com/rest/2.0}")
    private String baiduSearchBaseUrl;
    
    @Value("${baidu.search.enabled:false}")
    private boolean baiduSearchEnabled;
    
    // 新增：汇率API配置
    @Value("${exchange.api.key:}")
    private String exchangeApiKey;
    
    @Value("${exchange.api.base-url:https://api.exchangerate-api.com/v4}")
    private String exchangeApiBaseUrl;
    
    @Value("${exchange.api.enabled:false}")
    private boolean exchangeApiEnabled;
    
    // 新增：新闻API配置
    @Value("${news.api.key:}")
    private String newsApiKey;
    
    @Value("${news.api.base-url:https://newsapi.org/v2}")
    private String newsApiBaseUrl;
    
    @Value("${news.api.enabled:false}")
    private boolean newsApiEnabled;
    
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
    
    @Autowired
    private RegionMapper regionMapper;
    
    @Autowired
    private ReviewMapper reviewMapper;
    
    @Autowired
    private GuideMapper guideMapper;
    
    @Autowired
    private VehicleMapper vehicleMapper;
    
    @Autowired
    private DayTourMapper dayTourMapper;
    
    private OkHttpClient httpClient;
    
    @PostConstruct
    public void init() {
        // 初始化HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(qwenTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(qwenTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(qwenTimeout, TimeUnit.MILLISECONDS)
                .build();
                
        if (qwenApiKey != null && !qwenApiKey.isEmpty()) {
            log.info("Qwen AI服务初始化成功，模型: {}", qwenModel);
        } else {
            log.warn("Qwen API Key未配置，聊天功能将受限");
        }
        
        // 保留DeepSeek作为备用
        if (deepseekApiKey != null && !deepseekApiKey.isEmpty()) {
            log.info("DeepSeek AI服务作为备用，模型: {}", deepseekModel);
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
    public boolean checkRateLimit(String sessionId, String userId) {
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
     * 调用Qwen AI服务 (阿里云DashScope OpenAI兼容API)
     */
    private String callQwenAI(String prompt) {
        if (qwenApiKey == null || qwenApiKey.isEmpty()) {
            throw new RuntimeException("Qwen API Key未配置");
        }
        
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", qwenModel);
            
            // 使用OpenAI兼容格式的messages
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", qwenMaxTokens);
            requestBody.put("temperature", qwenTemperature);

            RequestBody body = RequestBody.create(
                requestBody.toString(), 
                MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                .url(qwenBaseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + qwenApiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    log.error("Qwen API调用失败: {} {}, 响应体: {}", response.code(), response.message(), errorBody);
                    throw new RuntimeException("Qwen API调用失败: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                log.debug("Qwen API响应: {}", responseBody);
                
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                
                String content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
                
                // 清理Qwen响应中的markdown代码块标记
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
            log.error("Qwen API调用异常", e);
            throw new RuntimeException("Qwen AI调用失败: " + e.getMessage());
        }
    }

    /**
     * 使用AI智能识别结构化订单数据（增强版）
     */
    private boolean isStructuredOrderDataWithAI(String message) {
        // 如果Qwen未配置，回退到传统方法
        if (qwenApiKey == null || qwenApiKey.isEmpty()) {
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
            
            String aiResponse = callQwenAI(aiPrompt);
            
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
     * 检查是否为产品选择回复（数字1-5）
     */
    private boolean isProductSelectionReply(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = message.trim();
        // 检查是否为纯数字且在1-5范围内
        return trimmed.matches("^[1-5]$");
    }
    
    /**
     * 检查最近聊天历史中是否有产品选择提示
     */
    private boolean hasRecentProductSelectionPrompt(String sessionId) {
        try {
            // 获取最近3条聊天记录
            List<ChatMessage> recentHistory = getRecentChatHistory(sessionId, 3);
            
            for (ChatMessage chatMessage : recentHistory) {
                String botResponse = chatMessage.getBotResponse();
                if (botResponse != null && botResponse.contains("请回复产品编号")) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("检查产品选择提示历史失败: {}", e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * 处理产品选择回复
     */
    private ChatResponse handleProductSelectionReply(ChatRequest request, String message) {
        try {
            int selectedIndex = Integer.parseInt(message.trim()) - 1; // 转换为0基索引
            
            // 从聊天历史中获取产品推荐信息和原始订单数据
            List<ChatMessage> recentHistory = getRecentChatHistory(request.getSessionId(), 10);
            
            // 寻找包含产品推荐的聊天记录
            ChatMessage productSelectionMessage = null;
            String originalOrderData = null;
            
            for (ChatMessage chatMessage : recentHistory) {
                String botResponse = chatMessage.getBotResponse();
                String extractedData = chatMessage.getExtractedData();
                
                if (botResponse != null && botResponse.contains("请回复产品编号")) {
                    productSelectionMessage = chatMessage;
                    originalOrderData = extractedData; // 获取原始订单数据
                    break;
                }
            }
            
            if (productSelectionMessage == null || originalOrderData == null) {
                return ChatResponse.error("抱歉，我无法找到您之前的产品选择信息，请重新发送订单信息。");
            }
            
            // 重新解析原始订单信息
            OrderInfo orderInfo;
            try {
                orderInfo = JSON.parseObject(originalOrderData, OrderInfo.class);
            } catch (Exception e) {
                log.error("解析原始订单数据失败: {}", e.getMessage(), e);
                return ChatResponse.error("抱歉，订单信息解析失败，请重新发送订单信息。");
            }
            
            // 重新获取相似产品列表
            List<GroupTourDTO> similarProducts = findSimilarProducts(orderInfo.getServiceType());
            
            if (selectedIndex < 0 || selectedIndex >= similarProducts.size()) {
                return ChatResponse.error(String.format("请选择有效的产品编号（1-%d）", similarProducts.size()));
            }
            
            // 获取用户选择的产品
            GroupTourDTO selectedProduct = similarProducts.get(selectedIndex);
            
            log.info("用户选择了产品: ID={}, 名称={}", selectedProduct.getId(), selectedProduct.getName());
            
            // 生成订单URL参数
            String orderParams = generateOrderParams(orderInfo, selectedProduct);
            
            // 根据用户类型决定跳转页面
            String redirectUrl;
            Integer userType = request.getUserType();
            boolean isAgent = (userType != null && (userType == 2 || userType == 3)); // 2=操作员, 3=中介主号(代理商)
            
            if (isAgent) {
                // 中介用户跳转到中介订单页面
                String agentOrderParams = "tourId=" + selectedProduct.getId() + "&" + orderParams;
                redirectUrl = "/agent-booking/group-tours/" + selectedProduct.getId() + "?" + agentOrderParams;
                String userTypeName = (userType == 2) ? "操作员" : (userType == 3) ? "中介主号" : "中介用户";
                log.info("{}，跳转到中介订单页面: {}", userTypeName, redirectUrl);
            } else {
                // 普通用户跳转到普通订单页面
                redirectUrl = "/booking?" + orderParams;
                log.info("普通用户，跳转到普通订单页面: {}", redirectUrl);
            }
            
            // 构建响应消息
            String responseMessage;
            if (isAgent) {
                String userTypeName = (userType == 2) ? "操作员" : (userType == 3) ? "中介主号" : "中介";
                responseMessage = String.format("✅ 已选择产品：**%s**\n\n📋 订单信息已为%s准备完成，正在跳转到订单页面...", 
                    selectedProduct.getName(), userTypeName);
            } else {
                responseMessage = String.format("✅ 已选择产品：**%s**\n\n📋 订单信息已准备完成，正在跳转到订单页面...", 
                    selectedProduct.getName());
            }
            
            // 保存聊天记录
            saveChatMessage(request, responseMessage, 2, JSON.toJSONString(orderInfo));
            
            return ChatResponse.orderSuccess(
                responseMessage,
                JSON.toJSONString(orderInfo),
                redirectUrl
            );
            
        } catch (NumberFormatException e) {
            return ChatResponse.error("请输入有效的数字（1-5）");
        } catch (Exception e) {
            log.error("处理产品选择回复失败: {}", e.getMessage(), e);
            return ChatResponse.error("处理您的选择时出现错误，请重试或联系客服。");
        }
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
                
                // 根据用户类型决定跳转页面
                String redirectUrl;
                Integer userType = request.getUserType();
                boolean isAgent = (userType != null && (userType == 2 || userType == 3)); // 2=操作员, 3=中介主号(代理商)
                
                if (isAgent) {
                    // 中介用户（操作员或中介主号）跳转到中介订单页面
                    // 确保URL参数中包含tourId
                    String agentOrderParams = "tourId=" + product.getId() + "&" + orderParams;
                    redirectUrl = "/agent-booking/group-tours/" + product.getId() + "?" + agentOrderParams;
                    String userTypeName = (userType == 2) ? "操作员" : (userType == 3) ? "中介主号" : "中介用户";
                    log.info("{}，跳转到中介订单页面: {}", userTypeName, redirectUrl);
                } else {
                    // 普通用户跳转到普通订单页面
                    redirectUrl = "/booking?" + orderParams;
                    log.info("普通用户，跳转到普通订单页面: {}", redirectUrl);
                }
                
                // 根据用户当前状态和用户类型优化响应消息
                String responseMessage;
                boolean isOnBookingPage = (request.getCurrentPage() != null && 
                                         request.getCurrentPage().contains("/booking"));
                
                if (isOnBookingPage) {
                    responseMessage = "订单信息已重新解析完成！找到匹配产品：" + product.getName() + 
                                    "。页面将自动更新以显示最新的订单信息和预填数据。";
                } else {
                    if (isAgent) {
                        String userTypeName = (userType == 2) ? "操作员" : (userType == 3) ? "中介主号" : "中介";
                        responseMessage = "订单信息已解析完成，找到产品：" + product.getName() + "，正在为" + userTypeName + "跳转到订单页面...";
                    } else {
                        responseMessage = "订单信息已解析完成，找到产品：" + product.getName() + "，正在跳转到订单页面...";
                    }
                }
                
                // 保存聊天记录
                saveChatMessage(request, responseMessage, 2, JSON.toJSONString(orderInfo));
                
                return ChatResponse.orderSuccess(
                    responseMessage,
                    JSON.toJSONString(orderInfo),
                    redirectUrl
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
        String message = request.getMessage().toLowerCase().trim();
        
        try {
            // 优先检查是否为产品选择回复（数字1-5）
            if (isProductSelectionReply(message)) {
                return handleProductSelectionReply(request, message);
            }
            
            // 检查是否为订单查询请求
            if (isOrderQueryRequest(message)) {
                return handleOrderQuery(request);
            }
            
            // 检查是否为产品查询请求
            if (isProductQueryRequest(message)) {
                return handleProductQuery(request);
            }
            
            // 1. 优先处理天气查询
            if (isWeatherQueryRequest(message)) {
                String weatherResponse = getWeatherInfo(request.getMessage());
                saveChatMessage(request, weatherResponse, 4, null); // 4代表天气查询
                return ChatResponse.success(weatherResponse);
            }
            
            // 2. 新增：汇率查询
            if (isExchangeRateQuery(message)) {
                String exchangeResponse = getExchangeRateInfo(request.getMessage());
                saveChatMessage(request, exchangeResponse, 5, null); // 5代表汇率查询
                return ChatResponse.success(exchangeResponse);
            }
            
            // 3. 新增：旅游相关新闻查询
            if (isTravelNewsQuery(message)) {
                String newsResponse = getTravelNewsInfo(request.getMessage());
                saveChatMessage(request, newsResponse, 6, null); // 6代表新闻查询
                return ChatResponse.success(newsResponse);
            }
            
            // 4. 新增：实时交通信息查询
            if (isTrafficQuery(message)) {
                String trafficResponse = getTrafficInfo(request.getMessage());
                saveChatMessage(request, trafficResponse, 7, null); // 7代表交通查询
                return ChatResponse.success(trafficResponse);
            }
            
            // 5. 新增：旅游攻略查询
            if (isTravelGuideQuery(message)) {
                String guideResponse = getTravelGuideInfo(request.getMessage());
                saveChatMessage(request, guideResponse, 8, null); // 8代表攻略查询
                return ChatResponse.success(guideResponse);
            }
            
            // 6. 智能问答（原有功能增强）
            String smartResponse = handleSmartQuestion(request, message);
            if (smartResponse != null && !smartResponse.isEmpty()) {
                saveChatMessage(request, smartResponse, 2, null);
                return ChatResponse.success(smartResponse);
            }
            
            // 7. FAQ查询（保留原有）
            String faqAnswer = searchFAQAnswer(request.getMessage());
            if (faqAnswer != null && !faqAnswer.isEmpty()) {
                saveChatMessage(request, faqAnswer, 2, null);
                return ChatResponse.success(faqAnswer);
            }
            
            // 8. 默认智能回复
            String defaultResponse = getEnhancedDefaultResponse(message);
            saveChatMessage(request, defaultResponse, 2, null);
            return ChatResponse.success(defaultResponse);
            
        } catch (Exception e) {
            log.error("处理一般问题失败: {}", e.getMessage(), e);
            String errorResponse = "抱歉，我现在遇到了一些问题，请稍后再试或者联系客服获取帮助。";
            saveChatMessage(request, errorResponse, 2, null);
            return ChatResponse.success(errorResponse);
        }
    }
    
    /**
     * 智能问题分类和处理
     */
    private String handleSmartQuestion(ChatRequest request, String message) {
        try {
            log.info("开始智能问题处理，消息: {}", message);
            
            // 获取聊天历史作为上下文
            List<ChatMessage> recentHistory = getRecentChatHistory(request.getSessionId(), 5);
            
            // 首先尝试AI驱动的意图分析（包含上下文）
            String aiResponse = analyzeUserIntentWithAI(request, message, recentHistory);
            if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                String result = processAIIntentResponse(request, message, aiResponse, recentHistory);
                if (result != null && !result.trim().isEmpty()) {
                    log.info("AI智能分析成功处理用户请求");
                    return result;
                }
            }
            
            // AI分析失败时，回退到基础意图识别
            log.info("AI分析未能处理，回退到基础意图识别");
            return handleBasicIntentRecognition(request, message);
            
        } catch (Exception e) {
            log.error("智能问题处理发生异常", e);
            return handleBasicIntentRecognition(request, message);
        }
    }
    
    /**
     * 让AI自主分析用户意图并决定查询什么
     */
    private String analyzeUserIntentWithAI(ChatRequest request, String message, List<ChatMessage> recentHistory) {
        try {
            String prompt = buildIntentAnalysisPrompt(request, message, recentHistory);
            String response = callQwenAI(prompt);
            log.info("AI意图分析响应: {}", response);
            return response;
        } catch (Exception e) {
            log.error("AI意图分析失败", e);
            return null;
        }
    }
    
    /**
     * 构建AI意图分析提示词
     */
    private String buildIntentAnalysisPrompt(ChatRequest request, String message, List<ChatMessage> recentHistory) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是Happy Tassie Travel(塔斯马尼亚快乐旅行)的智能助手。请分析用户意图并提供帮助。\n\n");
        
        // 添加对话历史上下文
        if (recentHistory != null && !recentHistory.isEmpty()) {
            prompt.append("=== 对话历史上下文 ===\n");
            for (ChatMessage msg : recentHistory) {
                if (msg.getUserMessage() != null && !msg.getUserMessage().trim().isEmpty()) {
                    prompt.append("用户: ").append(msg.getUserMessage()).append("\n");
                }
                if (msg.getBotResponse() != null && !msg.getBotResponse().trim().isEmpty()) {
                    prompt.append("助手: ").append(msg.getBotResponse().substring(0, Math.min(200, msg.getBotResponse().length()))).append("...\n");
                }
            }
            prompt.append("\n");
        }
        
        prompt.append("当前用户问题: ").append(message).append("\n\n");
        
        prompt.append("=== 重要：数据查询规则 ===\n");
        prompt.append("🚨 **绝对禁止编造任何信息！** 🚨\n");
        prompt.append("- 任何涉及具体客户姓名的询问，必须查询数据库\n");
        prompt.append("- 绝不能编造航班号、订单号、价格等具体信息\n");
        prompt.append("- 只能基于查询到的真实数据回答\n");
        prompt.append("- 如果数据库中没有相关信息，就说没有\n\n");
        
        prompt.append("=== 系统能力说明 ===\n");
        prompt.append("我可以帮助您:\n");
        prompt.append("1. 客户订单查询 - 通过姓名、电话、护照号查询客户的旅游订单信息\n");
        prompt.append("2. 航班信息查询 - 查询航班号、起降时间、航空公司等信息\n");
        prompt.append("3. 旅游产品推荐 - 根据需求推荐合适的旅游产品\n");
        prompt.append("4. 地区信息查询 - 塔斯马尼亚各地区景点、特色介绍\n");
        prompt.append("5. 客户评价查询 - 查看产品评价和反馈\n");
        prompt.append("6. 导游服务信息 - 导游配备、服务标准等\n");
        prompt.append("7. 车辆安排信息 - 不同团队规模的车辆配置\n");
        prompt.append("8. 天气信息查询 - 塔斯马尼亚天气状况和旅行建议\n\n");
        
        prompt.append("=== 权限说明 ===\n");
        prompt.append("当前用户类型: ");
        if (request.getUserType() == 1) {
            prompt.append("普通用户(只能查询自己的订单)\n");
        } else if (request.getUserType() == 2) {
            prompt.append("操作员(只能查询自己创建的订单)\n");
        } else if (request.getUserType() == 3) {
            prompt.append("中介主号(可查询代理商所有订单)\n");
        }
        prompt.append("当前页面: ").append(request.getCurrentPage() != null ? request.getCurrentPage() : "未知").append("\n\n");
        
        prompt.append("=== 智能助手指令 ===\n");
        prompt.append("请直接回答用户问题，不要显示分析过程或意图判断。如果是天气查询，直接查询并返回实时天气信息。\n\n");
        
        prompt.append("🔍 **客户信息查询（必须使用数据库查询）**\n");
        prompt.append("1. 如果用户询问具体客户的任何信息（如航班号、订单状态、价格等），回复：\n");
        prompt.append("   ACTION:QUERY_CUSTOMER:客户姓名\n");
        prompt.append("   例如：\n");
        prompt.append("   - 用户问：\"左静静航班号是什么\" → 回复：ACTION:QUERY_CUSTOMER:左静静\n");
        prompt.append("   - 用户问：\"张三的订单\" → 回复：ACTION:QUERY_CUSTOMER:张三\n");
        prompt.append("   - 用户问：\"王五的价格\" → 回复：ACTION:QUERY_CUSTOMER:王五\n\n");
        
        prompt.append("2. 如果在对话历史中已经查询过某客户，但用户询问更详细信息，仍然重新查询：\n");
        prompt.append("   ACTION:QUERY_CUSTOMER:客户姓名\n\n");
        
        prompt.append("📋 **通用服务问题（不涉及具体客户）**\n");
        prompt.append("3. 如果是导游服务咨询，回答：每个团配备1名导游，全程陪同，不固定分配景点\n\n");
        
        prompt.append("4. 如果是车辆安排咨询，回答：\n");
        prompt.append("   - 1-7人：7座商务车\n");
        prompt.append("   - 8-12人：12座中巴\n");
        prompt.append("   - 13人以上：大巴车\n\n");
        
        prompt.append("5. 如果用户说\"都要\"、\"全部\"等，请根据上下文理解用户想要什么信息，但仍需查询数据库获取真实数据\n\n");
        
        prompt.append("6. 其他情况请提供自然、有帮助的回答，但绝不编造具体数据\n\n");
        
        prompt.append("🚨 **再次强调：绝对不能编造航班号、订单号、价格等任何具体信息！必须查询数据库获取真实数据！**");
        
        return prompt.toString();
    }
    
    /**
     * 处理AI意图分析的响应
     */
    private String processAIIntentResponse(ChatRequest request, String message, String aiResponse, List<ChatMessage> recentHistory) {
        try {
            log.info("处理AI意图响应: {}", aiResponse);
            
            // 检查AI是否指示需要查询客户信息
            if (aiResponse.contains("ACTION:QUERY_CUSTOMER:")) {
                String customerName = extractCustomerNameFromAIResponse(aiResponse);
                if (customerName != null && !customerName.trim().isEmpty()) {
                    log.info("AI指示查询客户: {}", customerName);
                    return queryCustomerInfoByName(request, customerName.trim(), recentHistory);
                }
            }
            
            // 如果AI响应中包含具体的航班号、订单号等信息，但没有查询指令，说明AI可能在编造信息
            if (containsSpecificBusinessData(aiResponse) && !aiResponse.contains("ACTION:QUERY_CUSTOMER:")) {
                log.warn("AI响应包含具体业务数据但未执行数据库查询，可能是编造信息: {}", aiResponse);
                
                // 尝试从用户消息中提取客户姓名，强制执行查询
                String extractedName = extractCustomerNameFromMessage(message);
                if (extractedName != null && !extractedName.trim().isEmpty()) {
                    log.info("强制执行客户查询: {}", extractedName);
                    return queryCustomerInfoByName(request, extractedName.trim(), recentHistory);
                }
                
                // 如果无法提取客户姓名，返回提示需要查询数据库
                return "抱歉，我需要查询数据库来获取准确的信息。请告诉我您要查询的客户姓名，我会为您查找真实的数据。";
            }
            
            // 如果AI给出了通用性回答且不涉及具体数据，直接返回
            return aiResponse;
            
        } catch (Exception e) {
            log.error("处理AI意图响应失败", e);
            return "抱歉，处理您的请求时遇到了问题。请重新描述您的需求。";
        }
    }

    /**
     * 检查响应是否包含具体的业务数据（航班号、订单号等）
     */
    private boolean containsSpecificBusinessData(String response) {
        if (response == null) return false;
        
        // 检查是否包含航班号模式（字母+数字组合）
        if (response.matches(".*[A-Z]{2}\\d{3,4}.*")) {
            return true;
        }
        
        // 检查是否包含订单号模式
        if (response.matches(".*HT\\d+.*")) {
            return true;
        }
        
        // 检查是否包含具体价格信息
        if (response.matches(".*\\$\\d+.*") || response.matches(".*￥\\d+.*")) {
            return true;
        }
        
        // 检查是否包含具体日期信息
        if (response.matches(".*\\d{4}-\\d{2}-\\d{2}.*")) {
            return true;
        }
        
        return false;
    }

    /**
     * 从用户消息中提取客户姓名
     */
    private String extractCustomerNameFromMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        // 提取中文姓名
        List<String> chineseNames = extractChineseNames(message);
        if (!chineseNames.isEmpty()) {
            return chineseNames.get(0);
        }
        
        return null;
    }
    
    /**
     * 从AI响应中提取客户姓名
     */
    private String extractCustomerNameFromAIResponse(String aiResponse) {
        try {
            String[] parts = aiResponse.split("ACTION:QUERY_CUSTOMER:");
            if (parts.length > 1) {
                String namePart = parts[1].split("\n")[0].trim();
                return namePart;
            }
        } catch (Exception e) {
            log.error("从AI响应提取客户姓名失败: {}", e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 根据客户姓名查询客户信息
     */
    private String queryCustomerInfoByName(ChatRequest request, String customerName, List<ChatMessage> recentHistory) {
        try {
            log.info("AI指示查询客户信息: {}", customerName);
            
            // 查询客户订单
            List<TourBooking> bookings = tourBookingMapper.getByContactPersonLike(customerName);
            
            if (bookings == null || bookings.isEmpty()) {
                return String.format("没有找到客户 \"%s\" 的相关订单信息。请确认姓名是否正确，或联系客服获取帮助。", customerName);
            }
            
            // 权限过滤
            List<TourBooking> authorizedBookings = new ArrayList<>();
            String currentUserId = request.getUserId();
            Integer userType = request.getUserType();
            
            for (TourBooking booking : bookings) {
                if (hasPermissionToViewBooking(booking, currentUserId, userType)) {
                    authorizedBookings.add(booking);
                }
            }
            
            if (authorizedBookings.isEmpty()) {
                return String.format("找到客户 \"%s\" 的订单信息，但您没有权限查看。如需查询，请联系相关负责人。", customerName);
            }
            
            // 构建客户信息回复
            return buildCustomerInfoResponse(customerName, authorizedBookings, recentHistory);
            
        } catch (Exception e) {
            log.error("查询客户信息失败: {}", e.getMessage(), e);
            return String.format("查询客户 \"%s\" 信息时出现错误，请稍后重试或联系客服。", customerName);
        }
    }
    
    /**
     * 构建客户信息回复
     */
    private String buildCustomerInfoResponse(String customerName, List<TourBooking> bookings, List<ChatMessage> recentHistory) {
        StringBuilder response = new StringBuilder();
        
        if (bookings.isEmpty()) {
            response.append("抱歉，没有找到 \"").append(customerName).append("\" 的订单信息。\n");
            response.append("请确认客户姓名是否正确，或联系客服人员协助查询。");
            return response.toString();
        }
        
        response.append("📋 找到 \"").append(customerName).append("\" 的订单信息：\n\n");
        
        for (int i = 0; i < bookings.size(); i++) {
            TourBooking booking = bookings.get(i);
            response.append("🔸 **订单 ").append(i + 1).append("**\n");
            response.append("订单号：").append(booking.getOrderNumber()).append("\n");
            response.append("服务类型：").append(booking.getServiceType() != null ? booking.getServiceType() : "未指定").append("\n");
            response.append("出发日期：").append(booking.getTourStartDate() != null ? booking.getTourStartDate().toString() : "未设定").append("\n");
            response.append("结束日期：").append(booking.getTourEndDate() != null ? booking.getTourEndDate().toString() : "未设定").append("\n");
            response.append("订单状态：").append(getStatusText(booking.getStatus())).append("\n");
            response.append("支付状态：").append(getPaymentStatusText(booking.getPaymentStatus())).append("\n");
            
            // 航班信息
            if (booking.getFlightNumber() != null || booking.getReturnFlightNumber() != null) {
                response.append("\n✈️ **航班信息**\n");
                if (booking.getFlightNumber() != null) {
                    response.append("到达航班：").append(booking.getFlightNumber());
                    if (booking.getArrivalDepartureTime() != null) {
                        response.append("（起飞：").append(booking.getArrivalDepartureTime()).append("）");
                    }
                    if (booking.getArrivalLandingTime() != null) {
                        response.append("（降落：").append(booking.getArrivalLandingTime()).append("）");
                    }
                    response.append("\n");
                }
                if (booking.getReturnFlightNumber() != null) {
                    response.append("离开航班：").append(booking.getReturnFlightNumber());
                    if (booking.getDepartureDepartureTime() != null) {
                        response.append("（起飞：").append(booking.getDepartureDepartureTime()).append("）");
                    }
                    if (booking.getDepartureLandingTime() != null) {
                        response.append("（降落：").append(booking.getDepartureLandingTime()).append("）");
                    }
                    response.append("\n");
                }
            }
            
            // 住宿信息
            if (booking.getHotelLevel() != null && !booking.getHotelLevel().trim().isEmpty()) {
                response.append("\n🏨 住宿安排：").append(booking.getHotelLevel());
                if (booking.getRoomType() != null) {
                    response.append(" (").append(booking.getRoomType()).append(")");
                }
                response.append("\n");
            }
            
            // 费用信息
            if (booking.getTotalPrice() != null) {
                response.append("\n💰 订单金额：$").append(booking.getTotalPrice()).append("\n");
            }
            
            // 备注信息
            if (booking.getSpecialRequests() != null && !booking.getSpecialRequests().trim().isEmpty()) {
                response.append("\n📝 特殊要求：\n");
                // 处理特殊要求的格式化，如果包含数字序号，每行一个
                String specialRequests = booking.getSpecialRequests();
                if (specialRequests.matches(".*\\d+\\..*")) {
                    // 包含数字序号，按序号分行
                    String[] lines = specialRequests.split("(?=\\d+\\.)");
                    for (String line : lines) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            response.append(line).append("\n");
                        }
                    }
                } else {
                    // 普通文本，直接显示
                    response.append(specialRequests).append("\n");
                }
            }
            
            // 每个订单之间空一行
            if (i < bookings.size() - 1) {
                response.append("\n---\n\n");
            }
        }
        
        // 检查用户是否在询问特定信息
        String lastUserMessage = getLastUserMessage(recentHistory);
        if (lastUserMessage != null) {
            if (lastUserMessage.contains("航班号") || lastUserMessage.contains("航班")) {
                response.append("\n\n🔍 **航班号汇总：**\n");
                for (TourBooking booking : bookings) {
                    if (booking.getFlightNumber() != null || booking.getReturnFlightNumber() != null) {
                        response.append("订单 ").append(booking.getOrderNumber()).append("：");
                        if (booking.getFlightNumber() != null) {
                            response.append("到达 ").append(booking.getFlightNumber()).append(" ");
                        }
                        if (booking.getReturnFlightNumber() != null) {
                            response.append("离开 ").append(booking.getReturnFlightNumber());
                        }
                        response.append("\n");
                    }
                }
            }
        }
        
        return response.toString();
    }
    
    /**
     * 获取最后一条用户消息
     */
    private String getLastUserMessage(List<ChatMessage> recentHistory) {
        if (recentHistory == null || recentHistory.isEmpty()) {
            return null;
        }
        
        // 从最后开始查找最近的用户消息
        for (int i = recentHistory.size() - 1; i >= 0; i--) {
            ChatMessage msg = recentHistory.get(i);
            if (msg.getUserMessage() != null && !msg.getUserMessage().trim().isEmpty()) {
                return msg.getUserMessage();
            }
        }
        return null;
    }
    
    /**
     * 检查用户是否有权限查看订单
     */
    private boolean hasPermissionToViewBooking(TourBooking booking, String currentUserId, Integer userType) {
        if (currentUserId == null || userType == null || booking == null) {
            return false;
        }
        
        try {
            log.info("权限判断详情 - 订单agentId: {} (类型: {}), 订单operatorId: {} (类型: {}), 当前userId: {} (类型: {})", 
                    booking.getAgentId(), booking.getAgentId() != null ? booking.getAgentId().getClass().getSimpleName() : "null",
                    booking.getOperatorId(), booking.getOperatorId() != null ? booking.getOperatorId().getClass().getSimpleName() : "null", 
                    currentUserId, currentUserId.getClass().getSimpleName());
            
            if (userType == 1) {
                // 普通用户：只能查询自己的订单
                // 对于guest用户，直接比较字符串；对于数字用户ID，需要转换比较
                boolean hasPermission = false;
                if (currentUserId.startsWith("guest_")) {
                    // guest用户暂时不能查看订单（如需要可以调整逻辑）
                    hasPermission = false;
                } else {
                    try {
                        Long userIdLong = Long.parseLong(currentUserId);
                        hasPermission = booking.getUserId() != null && booking.getUserId().equals(userIdLong.intValue());
                    } catch (NumberFormatException e) {
                        hasPermission = false;
                    }
                }
                log.info(hasPermission ? "✅ 普通用户权限验证通过" : "❌ 普通用户权限验证失败：userId {} NOT equals currentUserId {}", booking.getUserId(), currentUserId);
                return hasPermission;
                
            } else if (userType == 2) {
                // userType=2 可能是操作员或者代理商主号，需要进一步判断
                
                // 首先检查是否是代理商主号 - 如果订单的agentId等于当前userId，说明是代理商主号
                try {
                    Long currentUserIdLong = Long.parseLong(currentUserId);
                    if (booking.getAgentId() != null && booking.getAgentId().equals(currentUserIdLong.intValue())) {
                        log.info("✅ 代理商主号权限验证通过：订单agentId {} equals currentUserId {}", booking.getAgentId(), currentUserId);
                        return true;
                    }
                    
                    // 然后检查是否是操作员 - 如果订单的operatorId等于当前userId，说明是操作员
                    if (booking.getOperatorId() != null && booking.getOperatorId().equals(currentUserIdLong)) {
                        log.info("✅ 操作员权限验证通过：订单operatorId {} equals currentUserId {}", booking.getOperatorId(), currentUserId);
                        return true;
                    }
                } catch (NumberFormatException e) {
                    // guest用户等非数字ID无法转换，不具备代理商或操作员权限
                    log.info("⚠️ 非数字userId无法验证代理商或操作员权限: {}", currentUserId);
                }
                
                log.info("❌ userType=2 权限验证失败：既不是代理商主号（订单agentId={}, 当前userId={}），也不是操作员（订单operatorId={}, 当前userId={})", 
                        booking.getAgentId(), currentUserId, booking.getOperatorId(), currentUserId);
                return false;
                
            } else if (userType == 3) {
                // 中介主号：只能查询自己代理商下的订单（agentId = currentUserId）
                try {
                    Long currentUserIdLong = Long.parseLong(currentUserId);
                    log.info("userType=3权限检查详情(hasPermissionToViewBooking) - 订单agentId: {} (类型: {}), 当前userId: {} (类型: {})", 
                        booking.getAgentId(), 
                        booking.getAgentId() != null ? booking.getAgentId().getClass().getSimpleName() : "null",
                        currentUserIdLong, 
                        currentUserIdLong.getClass().getSimpleName());
                    
                    if (booking.getAgentId() != null && booking.getAgentId().longValue() == currentUserIdLong.longValue()) {
                        log.info("✅ 中介主号权限：允许查看自己代理商的订单 (agentId={}, currentUserId={})", booking.getAgentId(), currentUserIdLong);
                        return true;
                    } else {
                        log.info("❌ 中介主号权限：无权限查看其他代理商订单 (订单agentId={}, 当前userId={})", booking.getAgentId(), currentUserIdLong);
                        return false;
                    }
                } catch (NumberFormatException e) {
                    log.error("中介主号userId转换失败: {}", currentUserId);
                    return false;
                }
            }
            
        } catch (Exception e) {
            log.error("权限检查失败: {}", e.getMessage(), e);
        }
        
        log.info("❌ 未知用户类型或权限验证失败：userType={}", userType);
        return false;
    }
    
    /**
     * 基础意图识别（作为AI分析的备选方案）
     */
    private String handleBasicIntentRecognition(ChatRequest request, String message) {
        // 1. 人员信息查询（姓名、联系人、航班等）
        if (isPersonInfoQuery(message)) {
            return handlePersonInfoQuery(request, message);
        }
        
        // 2. 地区相关查询
        if (isRegionQuery(message)) {
            return handleRegionQuery(request, message);
        }

        // 3. 产品详情查询
        if (isProductDetailQuery(message)) {
            return handleProductDetailQuery(request, message);
        }
        
        // 4. 导游相关查询
        if (isGuideQuery(message)) {
            return handleGuideQuery(request, message);
        }
        
        // 5. 车辆相关查询
        if (isVehicleQuery(message)) {
            return handleVehicleQuery(request, message);
        }
        
        // 6. 订单统计查询
        if (isOrderStatQuery(message)) {
            return handleOrderStatQuery(request, message);
        }
        
        // 7. 具体业务查询
        if (isSpecificBusinessQuery(message)) {
            return handleSpecificBusinessQuery(request, message);
        }
        
        // 8. 问候和感谢
        if (isGreetingOrThanks(message)) {
            return handleGreetingOrThanks(message);
        }
        
        // 9. 默认智能对话处理
        return handleGeneralSmartConversation(request, message);
    }
    
    /**
     * 判断是否为地区查询
     */
    private boolean isRegionQuery(String message) {
        String[] regionKeywords = {"地区", "区域", "塔斯马尼亚", "霍巴特", "朗塞斯顿", "德文港", "摇篮山", "威灵顿山", 
                                 "有什么地方", "哪些地区", "景点分布", "旅游区域", "地方推荐"};
        String lowerMessage = message.toLowerCase();
        return Arrays.stream(regionKeywords).anyMatch(keyword -> 
            lowerMessage.contains(keyword.toLowerCase()));
    }
    
    /**
     * 处理地区查询
     */
    private String handleRegionQuery(ChatRequest request, String message) {
        try {
            // 获取所有地区信息
            List<com.sky.dto.RegionDTO> regions = regionMapper.getAll();
            
            if (regions == null || regions.isEmpty()) {
                return "暂时没有找到地区信息，请联系客服获取更多帮助。";
            }
            
            StringBuilder response = new StringBuilder();
            response.append("📍 **塔斯马尼亚旅游地区介绍**\n\n");
            
            for (com.sky.dto.RegionDTO region : regions) {
                Integer dayTourCount = regionMapper.countDayTours(region.getId());
                Integer groupTourCount = regionMapper.countGroupTours(region.getId());
                
                response.append("🏞️ **").append(region.getName()).append("**\n");
                if (region.getDescription() != null) {
                    response.append("   ").append(region.getDescription()).append("\n");
                }
                response.append("   📊 一日游产品: ").append(dayTourCount != null ? dayTourCount : 0).append("个\n");
                response.append("   🚌 跟团游产品: ").append(groupTourCount != null ? groupTourCount : 0).append("个\n\n");
            }
            
            response.append("如需了解具体地区的旅游产品，请告诉我您感兴趣的地区名称！");
            
            return response.toString();
            
        } catch (Exception e) {
            log.error("处理地区查询失败: {}", e.getMessage(), e);
            return "查询地区信息时出现错误，请稍后重试或联系客服。";
        }
    }
    
    /**
     * 判断是否为评价查询
     */
    private boolean isReviewQuery(String message) {
        String[] reviewKeywords = {"评价", "评论", "评分", "怎么样", "好不好", "口碑", "体验", "满意度", "推荐吗"};
        return Arrays.stream(reviewKeywords).anyMatch(keyword -> message.contains(keyword));
    }
    

    

    
    /**
     * 判断是否为产品详情查询
     */
    private boolean isProductDetailQuery(String message) {
        String[] detailKeywords = {"详情", "介绍", "行程", "包含", "不包含", "亮点", "费用", "价格", "时间", "安排"};
        return Arrays.stream(detailKeywords).anyMatch(keyword -> message.contains(keyword));
    }
    
    /**
     * 处理产品详情查询
     */
    private String handleProductDetailQuery(ChatRequest request, String message) {
        try {
            String productName = extractProductNameFromMessage(message);
            
            if (productName != null) {
                GroupTourDTO groupTour = groupTourMapper.findByNameLike(productName);
                if (groupTour != null) {
                    return getProductDetailInfo(groupTour);
                }
            }
            
            return "请告诉我您想了解哪个具体产品的详情？比如'塔斯马尼亚南部4日游的详细行程'";
            
        } catch (Exception e) {
            log.error("处理产品详情查询失败: {}", e.getMessage(), e);
            return "查询产品详情时出现错误，请稍后重试。";
        }
    }
    
    /**
     * 获取产品详情信息
     */
    private String getProductDetailInfo(GroupTourDTO product) {
        try {
            StringBuilder response = new StringBuilder();
            response.append("🌟 **").append(product.getName()).append("**\n\n");
            
            // 基本信息
            response.append("📅 时长: ").append(product.getDuration() != null ? product.getDuration() : "待定").append("\n");
            response.append("💰 价格: $").append(product.getPrice());
            if (product.getDiscountedPrice() != null && product.getDiscountedPrice().compareTo(product.getPrice()) < 0) {
                response.append(" (优惠价: $").append(product.getDiscountedPrice()).append(")");
            }
            response.append("\n");
            
            if (product.getLocation() != null) {
                response.append("📍 地点: ").append(product.getLocation()).append("\n");
            }
            
            if (product.getRating() != null) {
                response.append("⭐ 评分: ").append(product.getRating()).append("/5.0\n");
            }
            
            response.append("\n");
            
            // 描述
            if (product.getDescription() != null) {
                response.append("📖 **产品描述**\n").append(product.getDescription()).append("\n\n");
            }
            
            // 获取亮点
            try {
                List<String> highlights = groupTourMapper.getHighlights(product.getId());
                if (highlights != null && !highlights.isEmpty()) {
                    response.append("✨ **产品亮点**\n");
                    for (String highlight : highlights) {
                        response.append("• ").append(highlight).append("\n");
                    }
                    response.append("\n");
                }
            } catch (Exception e) {
                log.warn("获取产品亮点失败: {}", e.getMessage());
            }
            
            // 获取包含项目
            try {
                List<String> inclusions = groupTourMapper.getInclusions(product.getId());
                if (inclusions != null && !inclusions.isEmpty()) {
                    response.append("✅ **费用包含**\n");
                    for (String inclusion : inclusions) {
                        response.append("• ").append(inclusion).append("\n");
                    }
                    response.append("\n");
                }
            } catch (Exception e) {
                log.warn("获取包含项目失败: {}", e.getMessage());
            }
            
            // 获取不包含项目
            try {
                List<String> exclusions = groupTourMapper.getExclusions(product.getId());
                if (exclusions != null && !exclusions.isEmpty()) {
                    response.append("❌ **费用不包含**\n");
                    for (String exclusion : exclusions) {
                        response.append("• ").append(exclusion).append("\n");
                    }
                    response.append("\n");
                }
            } catch (Exception e) {
                log.warn("获取不包含项目失败: {}", e.getMessage());
            }
            
            response.append("需要预订或了解更多详情，请点击: ");
            response.append("http://localhost:3000/booking?product=").append(product.getId());
            
            return response.toString();
            
        } catch (Exception e) {
            log.error("获取产品详情失败: {}", e.getMessage(), e);
            return "获取产品详情时出现错误，请稍后重试。";
        }
    }
    
    /**
     * 判断是否为导游查询
     */
    private boolean isGuideQuery(String message) {
        String[] guideKeywords = {"导游", "向导", "讲解员", "带队", "guide"};
        return Arrays.stream(guideKeywords).anyMatch(keyword -> 
            message.toLowerCase().contains(keyword.toLowerCase()));
    }
    
    /**
     * 处理导游查询
     */
    private String handleGuideQuery(ChatRequest request, String message) {
        // 这里可以根据实际需求实现导游相关查询
        return "关于导游服务，我们为每个团队都配备专业的中文导游。导游熟悉当地历史文化，会为您提供详细的景点讲解。如需了解特定产品的导游安排，请告诉我具体的旅游产品名称。";
    }
    
    /**
     * 判断是否为车辆查询
     */
    private boolean isVehicleQuery(String message) {
        String[] vehicleKeywords = {"车辆", "交通", "大巴", "小巴", "车子", "接送", "transportation"};
        return Arrays.stream(vehicleKeywords).anyMatch(keyword -> 
            message.toLowerCase().contains(keyword.toLowerCase()));
    }
    
    /**
     * 处理车辆查询
     */
    private String handleVehicleQuery(ChatRequest request, String message) {
        return "🚌 我们的交通安排：\n" +
               "• 小团(1-6人): 舒适SUV或商务车\n" +
               "• 中团(7-12人): 12座商务车\n" +
               "• 大团(13-20人): 豪华大巴\n" +
               "• 所有车辆都配备空调，确保舒适出行\n" +
               "• 专业司机，安全可靠\n\n" +
               "具体车辆安排会根据您的团队人数确定，如需了解特定产品的交通安排，请告诉我产品名称。";
    }
    
    /**
     * 判断是否为订单统计查询
     */
    private boolean isOrderStatQuery(String message) {
        String[] statKeywords = {"统计", "数量", "多少", "总共", "一共", "count"};
        String[] orderKeywords = {"订单", "预订", "booking"};
        
        return Arrays.stream(statKeywords).anyMatch(keyword -> message.contains(keyword)) &&
               Arrays.stream(orderKeywords).anyMatch(keyword -> message.contains(keyword));
    }
    
    /**
     * 处理订单统计查询
     */
    private String handleOrderStatQuery(ChatRequest request, String message) {
        // 基于安全考虑，不提供具体的统计数据，只给出一般性回复
        return "关于订单统计信息，出于数据安全考虑，我无法提供具体数字。如您需要查看订单相关信息，请：\n" +
               "1. 管理员请登录后台管理系统查看\n" +
               "2. 客户请在'我的订单'页面查看个人订单\n" +
               "3. 如有其他需求，请联系客服";
    }
    
    /**
     * 从消息中提取产品名称
     */
    private String extractProductNameFromMessage(String message) {
        // 简单的关键词匹配，实际可以更复杂
        String[] commonProducts = {"塔斯马尼亚南部4日游", "塔斯马尼亚北部3日游", "霍巴特一日游", "摇篮山一日游", 
                                  "威灵顿山", "朗塞斯顿", "德文港"};
        
        for (String product : commonProducts) {
            if (message.contains(product)) {
                return product;
            }
        }
        
        return null;
    }
    
    /**
     * 处理一般性智能对话
     */
    private String handleGeneralSmartConversation(ChatRequest request, String message) {
        // 构建更开放的对话上下文，允许AI更自由地回应
        String conversationContext = buildSmartConversationContext(request, message);
        
        try {
            String aiResponse = callQwenAI(conversationContext);
            if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                return aiResponse;
            }
        } catch (Exception e) {
            log.error("AI对话处理失败: {}", e.getMessage(), e);
        }
        
        // 如果AI回复失败，提供友好的默认回复
        return getDefaultResponse(message);
    }
    
    /**
     * 检测是否是人员信息查询
     */
    private boolean isPersonInfoQuery(String message) {
        // 检测包含人名的模式
        boolean hasPersonName = message.matches(".*[\\u4e00-\\u9fa5]{2,4}.*"); // 包含中文姓名
        
        // 检测查询类关键词
        String[] queryKeywords = {"航班", "电话", "联系方式", "信息", "是啥", "是什么", "多少", "几点"};
        boolean hasQueryKeyword = false;
        for (String keyword : queryKeywords) {
            if (message.contains(keyword)) {
                hasQueryKeyword = true;
                break;
            }
        }
        
        return hasPersonName && hasQueryKeyword;
    }
    
    /**
     * 处理人员信息查询
     */
    private String handlePersonInfoQuery(ChatRequest request, String message) {
        // 提取人名
        String[] words = message.split("[\\s，。！？、]");
        String personName = null;
        for (String word : words) {
            if (word.matches("[\\u4e00-\\u9fa5]{2,4}")) { // 中文姓名模式
                personName = word;
                break;
            }
        }
        
        if (personName == null) {
            return "抱歉，我没能识别出您询问的是哪位客户。请提供更具体的姓名信息，我来帮您查询。";
        }
        
        // 尝试从订单系统查询该人员信息
        try {
            List<TourBooking> bookings = tourBookingMapper.getByContactPersonLike(personName);
            if (bookings != null && !bookings.isEmpty()) {
                TourBooking booking = bookings.get(0); // 获取最新的订单
                
                StringBuilder response = new StringBuilder();
                response.append("📋 **找到客户信息：").append(personName).append("**\n\n");
                
                if (message.contains("航班")) {
                    if (booking.getReturnFlightNumber() != null || booking.getFlightNumber() != null) {
                        response.append("✈️ **航班信息**：\n");
                        if (booking.getFlightNumber() != null) {
                            response.append("• 抵达航班：").append(booking.getFlightNumber()).append("\n");
                        }
                        if (booking.getReturnFlightNumber() != null) {
                            response.append("• 离开航班：").append(booking.getReturnFlightNumber()).append("\n");
                        }
                    } else {
                        response.append("❌ 该客户的航班信息尚未完善，请查看订单详情或联系客户确认。\n");
                    }
                }
                
                if (message.contains("电话") || message.contains("联系")) {
                    response.append("\n📞 **联系方式**：\n");
                    response.append("• 联系人：").append(booking.getContactPerson()).append("\n");
                    response.append("• 电话：").append(booking.getContactPhone()).append("\n");
                }
                
                response.append("\n🔗 **订单详情**：[查看完整订单](/orders/").append(booking.getBookingId()).append(")");
                
                return response.toString();
            } else {
                return "🔍 **未找到客户：" + personName + "**\n\n" +
                       "可能的原因：\n" +
                       "• 姓名拼写不正确\n" +
                       "• 该客户尚未预订\n" +
                       "• 信息录入有误\n\n" +
                       "💡 **建议**：\n" +
                       "• 检查姓名拼写\n" +
                       "• 尝试搜索电话号码\n" +
                       "• 查看所有订单列表";
            }
        } catch (Exception e) {
            log.error("查询客户信息失败: {}", e.getMessage(), e);
            return "抱歉，查询客户信息时遇到问题。请稍后重试或联系技术支持。";
        }
    }
    
    /**
     * 检测是否是具体业务查询
     */
    private boolean isSpecificBusinessQuery(String message) {
        String[] businessKeywords = {
            "订单", "预订", "行程", "价格", "时间", "地点", "景点", "酒店", 
            "接送", "导游", "包含", "退款", "取消", "修改", "确认"
        };
        
        for (String keyword : businessKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理具体业务查询
     */
    private String handleSpecificBusinessQuery(ChatRequest request, String message) {
        // 这里可以添加更多具体的业务逻辑
        // 目前先返回null，让AI来处理
        return null;
    }
    
    /**
     * 检测是否是打招呼或感谢
     */
    private boolean isGreetingOrThanks(String message) {
        String[] greetings = {"你好", "您好", "嗨", "hi", "hello", "谢谢", "感谢", "再见", "bye"};
        String lowerMessage = message.toLowerCase();
        
        for (String greeting : greetings) {
            if (lowerMessage.contains(greeting)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理打招呼或感谢
     */
    private String handleGreetingOrThanks(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("谢谢") || lowerMessage.contains("感谢")) {
            return "😊 不客气！很高兴能帮助到您。如果还有其他问题，随时告诉我哦！";
        } else if (lowerMessage.contains("再见") || lowerMessage.contains("bye")) {
            return "👋 再见！祝您旅途愉快，期待为您提供更多服务！";
        } else {
            return "😊 您好！我是Happy Tassie Travel的AI客服助手，很高兴为您服务！有什么可以帮助您的吗？";
        }
    }
    
    /**
     * 构建智能对话上下文，让AI更自由发挥
     */
    private String buildSmartConversationContext(ChatRequest request, String message) {
        StringBuilder context = new StringBuilder();
        
        // 更灵活的系统提示
        context.append("你是Happy Tassie Travel的专业AI客服助手，具有以下特点：\n");
        context.append("1. 🎯 专业且人性化：既有专业知识，又能灵活应变\n");
        context.append("2. 🔍 智能理解：能理解用户的具体需求和上下文\n");
        context.append("3. 💡 主动帮助：不仅回答问题，还能主动提供有用建议\n");
        context.append("4. 🌟 友好亲切：保持温暖、耐心的服务态度\n\n");
        
        // 当前用户信息上下文
        if (request.getCurrentPage() != null) {
            context.append("用户当前页面：").append(request.getCurrentPage()).append("\n");
        }
        context.append("用户类型：").append(request.getUserType() == 2 ? "代理商操作员" : "普通客户").append("\n\n");
        
        // 获取最近的对话历史
        List<ChatMessage> history = getRecentChatHistoryByUserId(request.getUserId(), 3);
        if (history != null && !history.isEmpty()) {
            context.append("最近对话历史：\n");
            for (ChatMessage msg : history) {
                if (msg.getUserMessage() != null) {
                    context.append("用户：").append(msg.getUserMessage()).append("\n");
                }
                if (msg.getBotResponse() != null) {
                    context.append("助手：").append(msg.getBotResponse()).append("\n");
                }
            }
            context.append("\n");
        }
        
        // 当前用户问题
        context.append("用户当前问题：").append(message).append("\n\n");
        
        // 指导原则
        context.append("请根据用户问题智能回复，遵循以下原则：\n");
        context.append("- 如果是具体查询，尽力提供有用信息或指导\n");
        context.append("- 如果信息不足，礼貌询问更多详情\n");
        context.append("- 保持专业但不失人情味\n");
        context.append("- 适当推荐相关服务，但不要过度营销\n");
        context.append("- 回复要简洁明了，重点突出\n\n");
        
        return context.toString();
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
        // 如果Qwen未配置，回退到传统方法
        if (qwenApiKey == null || qwenApiKey.isEmpty()) {
            return parseOrderInfoTraditional(message);
        }
        
        try {
            // 构建智能AI提示，让AI像人一样理解订单信息
            String aiPrompt = "你是一个专业的旅游订单信息提取专家。请仔细分析以下订单文本，像人一样智能地理解和提取所有信息。\n\n" +
                    "## 📋 需要提取的信息字段：\n\n" +
                    "### 🎯 基础服务信息\n" +
                    "- **服务类型**: 旅游产品名称（如塔州南部四日游、一日游等）\n" +
                    "- **参团日期**: 开始和结束日期（**严格保持原始格式**：如6月22日、2024-06-22、Jun 22等，不要自动添加年份）\n" +
                    "- **跟团人数**: 参与旅游的总人数\n\n" +
                    "### ✈️ 航班信息\n" +
                    "- **抵达航班**: 到达塔斯马尼亚的航班号（对应去程/到达）\n" +
                    "- **返程航班**: 离开塔斯马尼亚的航班号（对应回程/离开）\n" +
                    "- **抵达时间**: 到达当地的具体时间\n" +
                    "- **出发地点**: 接机或集合地点\n\n" +
                    "### 👥 客户信息\n" +
                    "- **主要联系人**: 姓名和电话（支持中英文姓名）\n" +
                    "- **所有乘客**: 每个人的姓名、护照号、电话等\n" +
                    "- **特殊需求**: 年龄、饮食要求、身体状况等\n\n" +
                    "### 🏨 住宿信息\n" +
                    "- **房型**: 双床房、大床房、三人房、单人房等\n" +
                    "- **酒店级别**: 3星、4星、5星、经济型等\n" +
                    "- **特殊要求**: 指定酒店、楼层、景观等\n\n" +
                    "### 🧳 其他信息\n" +
                    "- **行李数量**: 托运行李件数\n" +
                    "- **行程安排**: 每天的详细安排\n" +
                    "- **备注信息**: 所有特殊说明、赠品、优惠等\n\n" +
                    "## 🤖 智能理解规则：\n\n" +
                    "1. **日期格式严格保持原样**: \n" +
                    "   - 如果原文是\"6月22日—6月25日\"，就输出\"6月22日\"和\"6月25日\"\n" +
                    "   - 如果原文是\"2024-06-22\"，就输出\"2024-06-22\"\n" +
                    "   - **绝对不要**自动添加、修改或转换年份格式\n" +
                    "2. **姓名电话智能分离**: 识别\"方靓 0473953844\"中的姓名和电话\n" +
                    "3. **航班号标准化**: 从\"返程航班:va1537\"中提取并转为大写\"VA1537\"\n" +
                    "4. **房型保持原文**: \"双床房\"就是\"双床房\"，不要改成\"双人房\"\n" +
                    "5. **数量提取**: \"跟团人数：2\"、\"2人\"、\"两个人\"都表示2人\n" +
                    "6. **行程按天解析**: 理解\"第一天：霍巴特接机\"等分天描述\n" +
                    "7. **备注完整保留**: 提取所有\"备注\"、\"注意\"、\"赠送\"等信息，不要遗漏\n\n" +
                    "## 📤 返回格式（严格JSON）：\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"serviceType\": \"完整的服务类型描述（保持原文）\",\n" +
                    "  \"startDate\": \"开始日期(严格保持原始格式，不要添加年份)\",\n" +
                    "  \"endDate\": \"结束日期(严格保持原始格式，不要添加年份)\",\n" +
                    "  \"groupSize\": 人数(数字),\n" +
                    "  \"customerInfo\": {\n" +
                    "    \"primaryContact\": {\n" +
                    "      \"name\": \"主要联系人姓名\",\n" +
                    "      \"phone\": \"电话号码（去除空格）\",\n" +
                    "      \"passport\": \"护照号(如有)\",\n" +
                    "      \"email\": \"邮箱(如有)\"\n" +
                    "    },\n" +
                    "    \"allPassengers\": [\n" +
                    "      {\n" +
                    "        \"name\": \"乘客姓名\",\n" +
                    "        \"phone\": \"电话\",\n" +
                    "        \"passport\": \"护照号\",\n" +
                    "        \"age\": \"年龄(如有)\",\n" +
                    "        \"specialNeeds\": \"特殊需求(如有)\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"flightInfo\": {\n" +
                    "    \"arrivalFlightNumber\": \"抵达航班号(到达塔斯马尼亚的航班,大写)\",\n" +
                    "    \"arrivalTime\": \"抵达时间(原格式)\",\n" +
                    "    \"departureFlightNumber\": \"返程航班号(离开塔斯马尼亚的航班,大写)\",\n" +
                    "    \"departureTime\": \"返程时间(原格式)\",\n" +
                    "    \"departureLocation\": \"出发地点(原文描述)\"\n" +
                    "  },\n" +
                    "  \"hotelInfo\": {\n" +
                    "    \"roomType\": \"房型(保持原文表述)\",\n" +
                    "    \"hotelLevel\": \"酒店级别(保持原文)\",\n" +
                    "    \"specialRequests\": \"特殊要求(原文)\"\n" +
                    "  },\n" +
                    "  \"luggageCount\": 行李数量(数字),\n" +
                    "  \"itinerary\": {\n" +
                    "    \"day1\": \"第一天行程(原文)\",\n" +
                    "    \"day2\": \"第二天行程(原文)\",\n" +
                    "    \"day3\": \"第三天行程(原文)\",\n" +
                    "    \"day4\": \"第四天行程(原文)\",\n" +
                    "    \"day5\": \"第五天行程(如有)\",\n" +
                    "    \"summary\": \"行程总结(如有)\"\n" +
                    "  },\n" +
                    "  \"notes\": [\n" +
                    "    \"备注信息1(原文)\",\n" +
                    "    \"备注信息2(原文)\",\n" +
                    "    \"赠品信息(原文)\",\n" +
                    "    \"特殊安排(原文)\"\n" +
                    "  ],\n" +
                    "  \"extractionQuality\": {\n" +
                    "    \"completeness\": 0.0-1.0,\n" +
                    "    \"confidence\": 0.0-1.0,\n" +
                    "    \"missingFields\": [\"缺失的字段列表\"],\n" +
                    "    \"extractedFields\": [\"成功提取的字段列表\"],\n" +
                    "    \"notes\": \"提取说明，如发现的问题或不确定的地方\"\n" +
                    "  }\n" +
                    "}\n" +
                    "```\n\n" +
                    "## 🎯 **核心原则（非常重要）**：\n" +
                    "1. **忠实原文**: 严格按照原文提取，不要自作主张修改格式\n" +
                    "2. **日期格式**: 绝对不要自动添加年份或转换日期格式\n" +
                    "3. **完整性**: 即使某个字段为空，也要在JSON中包含该字段（值为null或空字符串）\n" +
                    "4. **准确性**: 数量字段必须是数字类型，电话号码要去除空格\n" +
                    "5. **航班号**: 统一转换为大写格式\n" +
                    "6. **文本清理**: 所有文本要去除前后空格，但保持内容原样\n\n" +
                    "请仔细分析并严格按照原文提取所有可用信息：\n\n" +
                    "=== 订单信息开始 ===\n" + message + "\n=== 订单信息结束 ===";
            
            String aiResponse = callQwenAI(aiPrompt);
            
            log.info("AI订单解析响应: {}", aiResponse);
            
            try {
                JSONObject result = JSON.parseObject(aiResponse);
                
                OrderInfo.OrderInfoBuilder builder = OrderInfo.builder();
                
                // 提取基础服务信息
                if (result.containsKey("serviceType") && result.getString("serviceType") != null) {
                    builder.serviceType(result.getString("serviceType").trim());
                }
                
                if (result.containsKey("startDate") && result.getString("startDate") != null) {
                    builder.startDate(result.getString("startDate").trim());
                }
                
                if (result.containsKey("endDate") && result.getString("endDate") != null) {
                    builder.endDate(result.getString("endDate").trim());
                }
                
                if (result.containsKey("groupSize")) {
                    try {
                        builder.groupSize(result.getInteger("groupSize"));
                    } catch (Exception e) {
                        // 尝试从字符串解析数字
                        String groupSizeStr = result.getString("groupSize");
                        if (groupSizeStr != null && !groupSizeStr.trim().isEmpty()) {
                            try {
                                builder.groupSize(Integer.parseInt(groupSizeStr.replaceAll("[^0-9]", "")));
                            } catch (NumberFormatException nfe) {
                                log.warn("无法解析团队人数: {}", groupSizeStr);
                            }
                        }
                    }
                }
                
                // 提取客户信息
                if (result.containsKey("customerInfo")) {
                    JSONObject customerInfo = result.getJSONObject("customerInfo");
                    List<OrderInfo.CustomerInfo> customers = new ArrayList<>();
                    
                    // 主要联系人
                    if (customerInfo.containsKey("primaryContact") && customerInfo.getJSONObject("primaryContact") != null) {
                        JSONObject primaryContact = customerInfo.getJSONObject("primaryContact");
                        OrderInfo.CustomerInfo customer = new OrderInfo.CustomerInfo();
                        
                        if (primaryContact.containsKey("name") && primaryContact.getString("name") != null) {
                            customer.setName(primaryContact.getString("name").trim());
                        }
                        if (primaryContact.containsKey("phone") && primaryContact.getString("phone") != null) {
                            customer.setPhone(primaryContact.getString("phone").trim().replaceAll("\\s+", ""));
                        }
                        if (primaryContact.containsKey("passport") && primaryContact.getString("passport") != null) {
                            customer.setPassport(primaryContact.getString("passport").trim());
                        }
                        
                        if (customer.getName() != null || customer.getPhone() != null) {
                            customers.add(customer);
                        }
                    }
                    
                    // 所有乘客信息
                    if (customerInfo.containsKey("allPassengers")) {
                        JSONArray allPassengers = customerInfo.getJSONArray("allPassengers");
                        for (int i = 0; i < allPassengers.size(); i++) {
                            JSONObject passenger = allPassengers.getJSONObject(i);
                            OrderInfo.CustomerInfo customer = new OrderInfo.CustomerInfo();
                            
                            if (passenger.containsKey("name") && passenger.getString("name") != null) {
                                customer.setName(passenger.getString("name").trim());
                            }
                            if (passenger.containsKey("phone") && passenger.getString("phone") != null) {
                                customer.setPhone(passenger.getString("phone").trim().replaceAll("\\s+", ""));
                            }
                            if (passenger.containsKey("passport") && passenger.getString("passport") != null) {
                                customer.setPassport(passenger.getString("passport").trim());
                            }
                            
                            if (customer.getName() != null || customer.getPhone() != null) {
                                // 避免重复添加主联系人
                                boolean isDuplicate = customers.stream().anyMatch(existing -> 
                                    (existing.getName() != null && existing.getName().equals(customer.getName())) ||
                                    (existing.getPhone() != null && existing.getPhone().equals(customer.getPhone()))
                                );
                                if (!isDuplicate) {
                                    customers.add(customer);
                                }
                            }
                        }
                    }
                    
                    if (!customers.isEmpty()) {
                        builder.customers(customers);
                    }
                }
                
                // 提取航班信息
                if (result.containsKey("flightInfo")) {
                    JSONObject flightInfo = result.getJSONObject("flightInfo");
                    
                    // 处理新字段名格式
                    if (flightInfo.containsKey("arrivalFlightNumber") && flightInfo.getString("arrivalFlightNumber") != null) {
                        builder.arrivalFlight(flightInfo.getString("arrivalFlightNumber").trim().toUpperCase());
                        log.info("提取抵达航班（新格式）: {}", flightInfo.getString("arrivalFlightNumber"));
                    }
                    if (flightInfo.containsKey("departureFlightNumber") && flightInfo.getString("departureFlightNumber") != null 
                        && flightInfo.containsKey("arrivalFlightNumber")) {
                        // 新格式：departureFlightNumber = 返程航班
                        builder.departureFlight(flightInfo.getString("departureFlightNumber").trim().toUpperCase());
                        log.info("提取返程航班（新格式）: {}", flightInfo.getString("departureFlightNumber"));
                    }
                    
                    // 处理旧字段名格式：需要修正AI的错误映射
                    if (!flightInfo.containsKey("arrivalFlightNumber")) {
                        // AI在旧格式中的映射有错误，需要修正
                        if (flightInfo.containsKey("returnFlightNumber") && flightInfo.getString("returnFlightNumber") != null) {
                            // returnFlightNumber 在AI中对应抵达航班（正确）
                            builder.arrivalFlight(flightInfo.getString("returnFlightNumber").trim().toUpperCase());
                            log.info("提取抵达航班（旧格式修正）: {}", flightInfo.getString("returnFlightNumber"));
                        }
                        if (flightInfo.containsKey("departureFlightNumber") && flightInfo.getString("departureFlightNumber") != null) {
                            // departureFlightNumber 在AI中对应返程航班（正确）
                            builder.departureFlight(flightInfo.getString("departureFlightNumber").trim().toUpperCase());
                            log.info("提取返程航班（旧格式修正）: {}", flightInfo.getString("departureFlightNumber"));
                        }
                    }
                    
                    if (flightInfo.containsKey("arrivalTime") && flightInfo.getString("arrivalTime") != null) {
                        builder.arrivalTime(flightInfo.getString("arrivalTime").trim());
                    }
                    if (flightInfo.containsKey("departureLocation") && flightInfo.getString("departureLocation") != null) {
                        builder.departure(flightInfo.getString("departureLocation").trim());
                    }
                }
                
                // 提取住宿信息
                if (result.containsKey("hotelInfo")) {
                    JSONObject hotelInfo = result.getJSONObject("hotelInfo");
                    
                    if (hotelInfo.containsKey("roomType") && hotelInfo.getString("roomType") != null) {
                        builder.roomType(hotelInfo.getString("roomType").trim());
                    }
                    if (hotelInfo.containsKey("hotelLevel") && hotelInfo.getString("hotelLevel") != null) {
                        builder.hotelLevel(hotelInfo.getString("hotelLevel").trim());
                    }
                }
                
                // 提取行李数量
                if (result.containsKey("luggageCount")) {
                    try {
                        builder.luggage(result.getInteger("luggageCount"));
                    } catch (Exception e) {
                        String luggageStr = result.getString("luggageCount");
                        if (luggageStr != null && !luggageStr.trim().isEmpty()) {
                            try {
                                builder.luggage(Integer.parseInt(luggageStr.replaceAll("[^0-9]", "")));
                            } catch (NumberFormatException nfe) {
                                log.warn("无法解析行李数量: {}", luggageStr);
                            }
                        }
                    }
                }
                
                // 提取行程安排
                if (result.containsKey("itinerary")) {
                    JSONObject itinerary = result.getJSONObject("itinerary");
                    StringBuilder itineraryText = new StringBuilder();
                    
                    // 按天提取行程
                    for (int day = 1; day <= 10; day++) { // 最多支持10天
                        String dayKey = "day" + day;
                        if (itinerary.containsKey(dayKey) && itinerary.getString(dayKey) != null) {
                            String dayPlan = itinerary.getString(dayKey).trim();
                            if (!dayPlan.isEmpty()) {
                                if (itineraryText.length() > 0) {
                                    itineraryText.append("\n");
                                }
                                itineraryText.append("第").append(day).append("天：").append(dayPlan);
                            }
                        }
                    }
                    
                    // 如果有总结，也加入
                    if (itinerary.containsKey("summary") && itinerary.getString("summary") != null) {
                        String summary = itinerary.getString("summary").trim();
                        if (!summary.isEmpty()) {
                            if (itineraryText.length() > 0) {
                                itineraryText.append("\n");
                            }
                            itineraryText.append("行程总结：").append(summary);
                        }
                    }
                    
                    if (itineraryText.length() > 0) {
                        builder.itinerary(itineraryText.toString());
                    }
                }
                
                // 提取备注信息
                if (result.containsKey("notes")) {
                    JSONArray notes = result.getJSONArray("notes");
                    StringBuilder notesText = new StringBuilder();
                    
                    for (int i = 0; i < notes.size(); i++) {
                        String note = notes.getString(i);
                        if (note != null && !note.trim().isEmpty()) {
                            if (notesText.length() > 0) {
                                notesText.append("\n");
                            }
                            notesText.append(note.trim());
                        }
                    }
                    
                    if (notesText.length() > 0) {
                        builder.notes(notesText.toString());
                    }
                }
                
                OrderInfo orderInfo = builder.build();
                
                // 记录提取质量
                if (result.containsKey("extractionQuality")) {
                    JSONObject quality = result.getJSONObject("extractionQuality");
                    double completeness = quality.getDoubleValue("completeness");
                    double confidence = quality.getDoubleValue("confidence");
                    JSONArray missingFields = quality.getJSONArray("missingFields");
                    JSONArray extractedFields = quality.getJSONArray("extractedFields");
                    
                    log.info("AI提取质量评估 - 完整度: {}, 置信度: {}, 缺失字段: {}, 已提取字段: {}", 
                        completeness, confidence, missingFields, extractedFields);
                }
                
                log.info("AI智能解析成功，提取到的订单信息: {}", JSON.toJSONString(orderInfo));
                return orderInfo;
                
            } catch (Exception parseEx) {
                log.warn("解析AI响应JSON失败，尝试传统方法: {}", parseEx.getMessage());
                log.debug("原始AI响应: {}", aiResponse);
                
                // 如果AI解析失败，回退到传统方法
                OrderInfo traditionalResult = parseOrderInfoTraditional(message);
                return traditionalResult;
            }
            
        } catch (Exception e) {
            log.error("AI智能解析失败，回退到传统方法: {}", e.getMessage());
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
        // 支持多种服务类型字段名
        extractField(message, "服务类型：(.+?)\\n", builder::serviceType);
        extractField(message, "目的地：(.+?)\\n", builder::serviceType);
        extractField(message, "出发地点：(.+?)\\n", builder::departure);
        extractField(message, "服务车型：(.+?)\\n", builder::vehicleType);
        extractField(message, "房型：(.+?)\\n", builder::roomType);
        extractField(message, "酒店级别：(.+?)\\n", builder::hotelLevel);
        
        // 解析日期范围
        Pattern datePattern = Pattern.compile("(?:参团日期|预计到达日期).*?：(.+?)\\n");
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
        // 处理 "6月19日-6月22日" 或 "6月19日—6月22日" 格式
        if (dateRange.contains("-") || dateRange.contains("—")) {
            String[] parts = dateRange.split("[-—]");
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
                    String hotelLevel = orderInfo.getHotelLevel().trim();
                    // 处理酒店星级：3.5星向下取整为3星，其他保持原样
                    if (hotelLevel.equals("3.5星") || hotelLevel.equals("3.5")) {
                        hotelLevel = "3星";
                        log.info("酒店星级3.5向下取整为3星");
                    }
                    params.append("hotelLevel=").append(java.net.URLEncoder.encode(hotelLevel, "UTF-8")).append("&");
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
            
            // 添加showAIDialog参数，触发前端表单自动填充对话框
            params.append("showAIDialog=true&");
            
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
            fallbackParams.append("showAIDialog=true&");
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
        
        // 首先检查是否包含订单号（HT开头的14位数字）
        boolean hasOrderNumber = message.matches(".*\\bHT\\d{14}\\b.*");
        if (hasOrderNumber) {
            log.info("检测到订单号查询请求: {}", message);
            return true;
        }
        
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
            String currentUserIdStr = request.getUserId();
            Integer userType = request.getUserType();
            
            if (currentUserIdStr == null) {
                String response = "请先登录后再查询订单信息。";
                saveChatMessage(request, response, 2, null);
                return ChatResponse.success(response);
            }
            
            // 对于guest用户，不允许查询订单
            if (currentUserIdStr.startsWith("guest_")) {
                String response = "游客用户无法查询订单信息，请先注册登录。";
                saveChatMessage(request, response, 2, null);
                return ChatResponse.success(response);
            }
            
            // 转换用户ID为Long类型
            Long currentUserId;
            try {
                currentUserId = Long.parseLong(currentUserIdStr);
            } catch (NumberFormatException e) {
                String response = "用户ID格式错误，请重新登录。";
                saveChatMessage(request, response, 2, null);
                return ChatResponse.success(response);
            }
            
            log.info("当前用户信息: userId={}, userType={}", currentUserId, userType);
            log.info("用户类型解释: userType=1(普通用户-查询自己订单), userType=2(操作员-查询自己创建的订单), userType=3(中介主号-查询代理商所有订单)");
            
            // 提取查询关键词
            List<String> names = extractChineseNames(message);
            List<String> phones = extractPhoneNumbers(message);
            List<String> passports = extractPassportNumbers(message);
            List<String> orderNumbers = extractOrderNumbers(message);
            
            log.info("提取的查询信息 - 姓名: {}, 电话: {}, 护照: {}, 订单号: {}", names, phones, passports, orderNumbers);
            
            // 如果没有提取到有效的查询信息
            if (names.isEmpty() && phones.isEmpty() && passports.isEmpty() && orderNumbers.isEmpty()) {
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
            
            // 按订单号查询（优先级最高）
            for (String orderNumber : orderNumbers) {
                TourBooking booking = tourBookingMapper.getByOrderNumber(orderNumber);
                if (booking != null) {
                    log.info("通过订单号 '{}' 查询到订单: bookingId={}, contactPerson={}, userId={}, agentId={}, operatorId={}", 
                        orderNumber, booking.getBookingId(), booking.getContactPerson(),
                        booking.getUserId(), booking.getAgentId(), booking.getOperatorId());
                    
                    // 根据用户类型进行权限控制
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
                        // 首先检查是否是代理商主号 - 如果订单的agentId等于当前userId，说明是代理商主号
                        if (booking.getAgentId() != null && booking.getAgentId().equals(currentUserId.intValue())) {
                            hasPermission = true;
                            permissionReason = "代理商主号查询属下订单";
                        }
                        // 然后检查是否是操作员 - 如果订单的operatorId等于当前userId，说明是操作员
                        else if (booking.getOperatorId() != null && booking.getOperatorId().equals(currentUserId)) {
                            hasPermission = true;
                            permissionReason = "操作员查询自己创建的订单";
                        } else {
                            permissionReason = String.format("userType=2权限验证失败：既不是代理商主号（订单agentId=%s, 当前userId=%s），也不是操作员（订单operatorId=%s, 当前userId=%s)", 
                                booking.getAgentId(), currentUserId, booking.getOperatorId(), currentUserId);
                        }
                    } else if (userType == 3) {
                        // 中介主号：可以查询代理商下所有订单（agentId = currentUserId）
                        log.info("userType=3权限检查详情(按订单号) - 订单agentId: {} (类型: {}), 当前userId: {} (类型: {})", 
                            booking.getAgentId(), 
                            booking.getAgentId() != null ? booking.getAgentId().getClass().getSimpleName() : "null",
                            currentUserId, 
                            currentUserId.getClass().getSimpleName());
                        
                        // 修复：统一转换为Long类型进行比较
                        if (booking.getAgentId() != null && booking.getAgentId().longValue() == currentUserId.longValue()) {
                            hasPermission = true;
                            permissionReason = "中介主号查询所属代理商的所有订单";
                            log.info("✅ userType=3权限验证通过(按订单号)：订单agentId {} equals currentUserId {}", booking.getAgentId(), currentUserId);
                        } else {
                            permissionReason = String.format("中介主号无权限查询其他代理商订单 (订单agentId=%s, 当前agentId=%s)", 
                                booking.getAgentId(), currentUserId);
                            log.info("❌ userType=3权限验证失败(按订单号)：订单agentId {} not equals currentUserId {}", booking.getAgentId(), currentUserId);
                        }
                    }
                    
                    if (hasPermission) {
                        foundBookings.add(booking);
                        log.info("✅ 用户{}有权限访问订单{}: {}", currentUserId, booking.getBookingId(), permissionReason);
                    } else {
                        log.info("❌ 用户{}无权限访问订单{}: {}", currentUserId, booking.getBookingId(), permissionReason);
                    }
                } else {
                    log.info("订单号 '{}' 未找到对应订单", orderNumber);
                }
            }
            
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
                        
                        // 首先检查是否是代理商主号 - 如果订单的agentId等于当前userId，说明是代理商主号
                        if (booking.getAgentId() != null && booking.getAgentId().equals(currentUserId.intValue())) {
                            hasPermission = true;
                            permissionReason = "代理商主号查询属下订单";
                            log.info("✅ 代理商主号权限验证通过：订单agentId {} equals currentUserId {}", booking.getAgentId(), currentUserId);
                        }
                        // 然后检查是否是操作员 - 如果订单的operatorId等于当前userId，说明是操作员
                        else if (booking.getOperatorId() != null && booking.getOperatorId().equals(currentUserId)) {
                            hasPermission = true;
                            permissionReason = "操作员查询自己创建的订单";
                            log.info("✅ 操作员权限验证通过：订单operatorId {} equals currentUserId {}", booking.getOperatorId(), currentUserId);
                        } else {
                            permissionReason = String.format("userType=2权限验证失败：既不是代理商主号（订单agentId=%s, 当前userId=%s），也不是操作员（订单operatorId=%s, 当前userId=%s)", 
                                booking.getAgentId(), currentUserId, booking.getOperatorId(), currentUserId);
                            log.info("❌ userType=2权限验证失败：既不是代理商主号（订单agentId={}, 当前userId={}），也不是操作员（订单operatorId={}, 当前userId={})", 
                                booking.getAgentId(), currentUserId, booking.getOperatorId(), currentUserId);
                        }
                    } else if (userType == 3) {
                        // 中介主号：可以查询代理商下所有订单（agentId = currentUserId）
                        log.info("userType=3权限检查详情 - 订单agentId: {} (类型: {}), 当前userId: {} (类型: {})", 
                            booking.getAgentId(), 
                            booking.getAgentId() != null ? booking.getAgentId().getClass().getSimpleName() : "null",
                            currentUserId, 
                            currentUserId.getClass().getSimpleName());
                        
                        // 修复：统一转换为Long类型进行比较
                        if (booking.getAgentId() != null && booking.getAgentId().longValue() == currentUserId.longValue()) {
                            hasPermission = true;
                            permissionReason = "中介主号查询所属代理商的所有订单";
                            log.info("✅ userType=3权限验证通过：订单agentId {} equals currentUserId {}", booking.getAgentId(), currentUserId);
                        } else {
                            permissionReason = String.format("中介主号无权限查询其他代理商订单 (订单agentId=%s, 当前agentId=%s)", 
                                booking.getAgentId(), currentUserId);
                            log.info("❌ userType=3权限验证失败：订单agentId {} not equals currentUserId {}", booking.getAgentId(), currentUserId);
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
                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("没有找到您有权限查看的相关订单信息。\n\n");
                
                // 如果提供了订单号但没找到，给出更具体的提示
                if (!orderNumbers.isEmpty()) {
                    responseBuilder.append("🔍 **订单号查询结果：**\n");
                    for (String orderNumber : orderNumbers) {
                        TourBooking booking = tourBookingMapper.getByOrderNumber(orderNumber);
                        if (booking == null) {
                            responseBuilder.append(String.format("• 订单号 `%s`：订单不存在\n", orderNumber));
                        } else {
                            responseBuilder.append(String.format("• 订单号 `%s`：订单存在但您无权限查看\n", orderNumber));
                        }
                    }
                    responseBuilder.append("\n");
                }
                
                responseBuilder.append("可能的原因：\n");
                if (!orderNumbers.isEmpty()) {
                    responseBuilder.append("• 订单号不存在或输入错误\n");
                }
                if (!names.isEmpty()) {
                    responseBuilder.append("• 联系人姓名不匹配\n");
                }
                responseBuilder.append("• 订单不在您的权限范围内\n");
                responseBuilder.append("• 信息输入有误\n\n");
                
                responseBuilder.append("建议：\n");
                responseBuilder.append("• 确认订单号格式正确（如：HT2025061300145）\n");
                responseBuilder.append("• 确认联系人姓名拼写正确（支持中文和英文）\n");
                responseBuilder.append("• 联系客服协助查询\n\n");
                responseBuilder.append(String.format("调试信息：当前用户类型=%d, 用户ID=%d", userType, currentUserId));
                
                String response = responseBuilder.toString();
                
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
        
        // 排除订单号：HT开头的16位字符串
        if (name.matches("HT\\d{14}")) {
            log.info("'{}' 是订单号，跳过", name);
            return false;
        }
        
        // 排除订单号的部分：单独的HT
        if (name.equalsIgnoreCase("HT")) {
            log.info("'{}' 是订单号前缀，跳过", name);
            return false;
        }
        
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
            String passport = matcher1.group();
            // 排除订单号的部分（如T20250613）
            if (!passport.matches("T\\d{8}")) {
                passports.add(passport);
            }
        }
        
        // 其他国家护照格式：2个字母 + 7位数字
        Pattern pattern2 = Pattern.compile("[A-Z]{2}\\d{7}");
        Matcher matcher2 = pattern2.matcher(text);
        while (matcher2.find()) {
            String passport = matcher2.group();
            // 排除订单号的部分（如HT2025061）
            if (!passport.matches("HT\\d{7}")) {
                passports.add(passport);
            }
        }
        
        return passports;
    }

    /**
     * 提取订单号（HT开头的订单号）
     */
    private List<String> extractOrderNumbers(String text) {
        List<String> orderNumbers = new ArrayList<>();
        
        // 订单号格式：HT + 8位日期 + 4位序列号 + 2位随机数，例如：HT20250613000198
        // 总长度：HT(2) + 14位数字 = 16位
        Pattern orderPattern = Pattern.compile("HT\\d{14}");
        Matcher matcher = orderPattern.matcher(text.toUpperCase());
        
        while (matcher.find()) {
            String orderNumber = matcher.group();
            orderNumbers.add(orderNumber);
            log.info("提取到订单号: {}", orderNumber);
        }
        
        return orderNumbers;
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
                try {
                    agentId = Long.parseLong(request.getUserId());
                } catch (NumberFormatException e) {
                    log.warn("无法解析代理商ID: {}", request.getUserId());
                    agentId = null;
                }
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

    /**
     * 获取最近的聊天历史记录
     */
    private List<ChatMessage> getRecentChatHistory(String sessionId, int limit) {
        try {
            List<ChatMessage> allHistory = getChatHistory(sessionId);
            if (allHistory == null || allHistory.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 返回最近的几条记录，按时间倒序
            List<ChatMessage> recent = new ArrayList<>();
            int start = Math.max(0, allHistory.size() - limit);
            for (int i = start; i < allHistory.size(); i++) {
                recent.add(allHistory.get(i));
            }
            return recent;
        } catch (Exception e) {
            log.error("获取聊天历史失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取最近的聊天历史记录（按用户ID）
     */
    private List<ChatMessage> getRecentChatHistoryByUserId(String userId, int limit) {
        try {
            if (userId == null) {
                return new ArrayList<>();
            }
            
            // 对于guest用户，无法查询历史记录，返回空列表
            if (userId.startsWith("guest_")) {
                return new ArrayList<>();
            }
            
            // 对于数字用户ID，转换后查询
            try {
                Long userIdLong = Long.parseLong(userId);
                List<ChatMessage> history = chatMessageMapper.selectRecentByUserId(userIdLong, limit);
                return history != null ? history : new ArrayList<>();
            } catch (NumberFormatException e) {
                log.warn("无法解析用户ID为数字: {}", userId);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("获取用户聊天历史失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 判断是否为汇率查询
     */
    private boolean isExchangeRateQuery(String message) {
        String[] exchangeKeywords = {
            "汇率", "汇率查询", "exchange rate", "currency", "澳元", "人民币", "美元", "汇率换算",
            "澳币", "aud", "cny", "usd", "货币", "兑换", "换算"
        };
        
        for (String keyword : exchangeKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取汇率信息
     */
    private String getExchangeRateInfo(String message) {
        try {
            // 提取货币对
            String[] currencies = extractCurrencyPair(message);
            String fromCurrency = currencies[0];
            String toCurrency = currencies[1];
            
            // 优先使用API查询
            if (exchangeApiEnabled && exchangeApiKey != null && !exchangeApiKey.isEmpty()) {
                return getExchangeRateFromAPI(fromCurrency, toCurrency);
            }
            
            // 如果API不可用，返回基本汇率信息
            return getBasicExchangeRateInfo(fromCurrency, toCurrency);
            
        } catch (Exception e) {
            log.error("获取汇率信息失败: {}", e.getMessage(), e);
            return "抱歉，暂时无法获取汇率信息。不过，我可以告诉您，在计划塔斯马尼亚旅行时，" +
                   "建议您提前了解澳元汇率变化，这样可以更好地规划旅行预算。您还可以询问我们的旅游产品和价格信息！";
        }
    }
    
    /**
     * 从消息中提取货币对
     */
    private String[] extractCurrencyPair(String message) {
        // 默认查询澳元对人民币汇率
        String from = "AUD";  // 澳元
        String to = "CNY";    // 人民币
        
        // 根据消息内容智能识别货币对
        if (message.contains("美元") || message.contains("usd")) {
            if (message.contains("澳元") || message.contains("aud")) {
                from = "USD";
                to = "AUD";
            } else {
                from = "USD";
                to = "CNY";
            }
        } else if (message.contains("人民币") && message.contains("澳元")) {
            from = "CNY";
            to = "AUD";
        }
        
        return new String[]{from, to};
    }
    
    /**
     * 从API获取汇率
     */
    private String getExchangeRateFromAPI(String from, String to) {
        try {
            String url = exchangeApiBaseUrl + "/latest/" + from;
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "TravelBot/1.0")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return parseExchangeRateResponse(responseBody, from, to);
                }
            }
        } catch (Exception e) {
            log.error("API获取汇率失败: {}", e.getMessage(), e);
        }
        
        return getBasicExchangeRateInfo(from, to);
    }
    
    /**
     * 解析汇率API响应
     */
    private String parseExchangeRateResponse(String responseBody, String from, String to) {
        try {
            JSONObject json = JSON.parseObject(responseBody);
            JSONObject rates = json.getJSONObject("rates");
            
            if (rates != null && rates.containsKey(to)) {
                double rate = rates.getDoubleValue(to);
                return formatExchangeRateResponse(from, to, rate);
            }
        } catch (Exception e) {
            log.error("解析汇率响应失败: {}", e.getMessage(), e);
        }
        
        return getBasicExchangeRateInfo(from, to);
    }
    
    /**
     * 格式化汇率响应
     */
    private String formatExchangeRateResponse(String from, String to, double rate) {
        String fromName = getCurrencyName(from);
        String toName = getCurrencyName(to);
        
        StringBuilder response = new StringBuilder();
        response.append("💱 实时汇率信息：\n\n");
        response.append(String.format("1 %s = %.4f %s\n", fromName, rate, toName));
        response.append(String.format("1 %s = %.4f %s\n\n", toName, 1/rate, fromName));
        
        // 添加旅游相关建议
        if ("AUD".equals(from) || "AUD".equals(to)) {
            response.append("🏝️ 塔斯马尼亚旅游小贴士：\n");
            response.append("• 澳洲大部分地方都支持刷卡，建议携带少量现金\n");
            response.append("• 我们的旅游产品价格已包含GST，无隐形费用\n");
            response.append("• 想了解具体的旅游套餐价格吗？我可以为您推荐合适的产品！");
        }
        
        return response.toString();
    }
    
    /**
     * 获取货币名称
     */
    private String getCurrencyName(String code) {
        switch (code.toUpperCase()) {
            case "AUD": return "澳元";
            case "CNY": return "人民币";
            case "USD": return "美元";
            case "EUR": return "欧元";
            case "GBP": return "英镑";
            case "JPY": return "日元";
            default: return code;
        }
    }
    
    /**
     * 获取基本汇率信息
     */
    private String getBasicExchangeRateInfo(String from, String to) {
        return "💱 汇率信息：\n\n" +
               "抱歉，无法获取实时汇率数据。建议您通过银行或专业金融应用查询最新汇率。\n\n" +
               "🏝️ 塔斯马尼亚旅游支付小贴士：\n" +
               "• 我们接受多种支付方式，包括信用卡支付\n" +
               "• 澳洲旅游时建议携带少量现金备用\n" +
               "• 想了解我们的旅游产品价格吗？我可以为您详细介绍！";
    }
    
    /**
     * 判断是否为旅游新闻查询
     */
    private boolean isTravelNewsQuery(String message) {
        String[] newsKeywords = {
            "新闻", "资讯", "消息", "最新", "动态", "news", "塔斯马尼亚新闻",
            "旅游新闻", "景点新闻", "开放时间", "活动", "节庆", "festival"
        };
        
        for (String keyword : newsKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取旅游新闻信息
     */
    private String getTravelNewsInfo(String message) {
        try {
            // 如果启用了新闻API，尝试获取实时新闻
            if (newsApiEnabled && newsApiKey != null && !newsApiKey.isEmpty()) {
                return getTravelNewsFromAPI(message);
            }
            
            // 否则返回塔斯马尼亚旅游相关的固定信息
            return getTasmanianTravelNews();
            
        } catch (Exception e) {
            log.error("获取旅游新闻失败: {}", e.getMessage(), e);
            return getTasmanianTravelNews();
        }
    }
    
    /**
     * 从API获取旅游新闻
     */
    private String getTravelNewsFromAPI(String message) {
        try {
            String query = "Tasmania travel OR 塔斯马尼亚旅游";
            String url = newsApiBaseUrl + "/everything?q=" + java.net.URLEncoder.encode(query, "UTF-8") +
                        "&language=en&sortBy=publishedAt&pageSize=5";
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-API-Key", newsApiKey)
                    .addHeader("User-Agent", "TravelBot/1.0")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    return parseNewsResponse(responseBody);
                }
            }
        } catch (Exception e) {
            log.error("从API获取新闻失败: {}", e.getMessage(), e);
        }
        
        return getTasmanianTravelNews();
    }
    
    /**
     * 解析新闻API响应
     */
    private String parseNewsResponse(String responseBody) {
        try {
            JSONObject json = JSON.parseObject(responseBody);
            JSONArray articles = json.getJSONArray("articles");
            
            if (articles != null && articles.size() > 0) {
                StringBuilder news = new StringBuilder();
                news.append("📰 塔斯马尼亚旅游最新资讯：\n\n");
                
                for (int i = 0; i < Math.min(3, articles.size()); i++) {
                    JSONObject article = articles.getJSONObject(i);
                    String title = article.getString("title");
                    String description = article.getString("description");
                    
                    news.append(String.format("%d. %s\n", i + 1, title));
                    if (description != null && description.length() > 0) {
                        news.append(String.format("   %s\n\n", 
                            description.length() > 100 ? description.substring(0, 100) + "..." : description));
                    }
                }
                
                news.append("💡 想了解更多塔斯马尼亚的旅游信息吗？我可以为您推荐最适合的旅游路线！");
                return news.toString();
            }
        } catch (Exception e) {
            log.error("解析新闻响应失败: {}", e.getMessage(), e);
        }
        
        return getTasmanianTravelNews();
    }
    
    /**
     * 获取塔斯马尼亚旅游新闻
     */
    private String getTasmanianTravelNews() {
        return "📰 塔斯马尼亚旅游资讯：\n\n" +
               "🏝️ 塔斯马尼亚是澳洲的旅游瑰宝，四季皆宜旅游\n" +
               "🌺 夏季（12-2月）是薰衣草盛开的季节\n" +
               "🍁 秋季（3-5月）可以欣赏到美丽的秋叶\n" +
               "❄️ 冬季（6-8月）是观赏极光的最佳时期\n" +
               "🌸 春季（9-11月）万物复苏，气候宜人\n\n" +
               "🎯 我们提供全年的旅游服务，包括：\n" +
               "• 摇篮山-圣克莱尔湖国家公园\n" +
               "• 亚瑟港历史遗址\n" +
               "• 惠灵顿山\n" +
               "• 萨拉曼卡市场\n\n" +
               "想了解具体的行程安排吗？我可以为您定制专属的塔斯马尼亚之旅！";
    }
    
    /**
     * 判断是否为交通查询
     */
    private boolean isTrafficQuery(String message) {
        String[] trafficKeywords = {
            "交通", "路况", "堵车", "traffic", "道路", "高速", "路线", "怎么去",
            "开车", "自驾", "公交", "机场", "接送", "交通工具"
        };
        
        for (String keyword : trafficKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取交通信息
     */
    private String getTrafficInfo(String message) {
        return "🚗 塔斯马尼亚交通信息：\n\n" +
               "🛣️ 主要交通方式：\n" +
               "• 自驾游：最受欢迎的方式，可以自由探索\n" +
               "• 我们的旅游巴士：专业司机，安全舒适\n" +
               "• 机场接送：霍巴特机场往返市区\n\n" +
               "🚌 我们提供的交通服务：\n" +
               "• 全程旅游巴士接送\n" +
               "• 酒店接送服务\n" +
               "• 机场接送安排\n" +
               "• 专业中文导游陪同\n\n" +
               "📍 主要景点距离：\n" +
               "• 霍巴特 ↔ 摇篮山：约 2.5 小时车程\n" +
               "• 霍巴特 ↔ 亚瑟港：约 1.5 小时车程\n" +
               "• 霍巴特 ↔ 里奇蒙：约 30 分钟车程\n\n" +
               "想了解具体的交通安排吗？我们的旅游套餐都包含交通接送服务！";
    }
    
    /**
     * 判断是否为旅游攻略查询
     */
    private boolean isTravelGuideQuery(String message) {
        String[] guideKeywords = {
            "攻略", "指南", "怎么玩", "推荐", "路线", "行程", "景点", "玩法",
            "游记", "经验", "建议", "must visit", "best", "recommendation"
        };
        
        for (String keyword : guideKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取旅游攻略信息
     */
    private String getTravelGuideInfo(String message) {
        return "📖 塔斯马尼亚旅游攻略：\n\n" +
               "🏆 必游景点推荐：\n" +
               "1️⃣ 摇篮山-圣克莱尔湖国家公园\n" +
               "   - 徒步爱好者的天堂\n" +
               "   - 可看到袋熊、袋鼠等野生动物\n\n" +
               "2️⃣ 亚瑟港历史遗址\n" +
               "   - 了解澳洲监狱历史\n" +
               "   - 夜游活动别有一番风味\n\n" +
               "3️⃣ 惠灵顿山\n" +
               "   - 俯瞰霍巴特全景\n" +
               "   - 日出日落都很美\n\n" +
               "4️⃣ 萨拉曼卡市场\n" +
               "   - 每周六的集市\n" +
               "   - 当地手工艺品和美食\n\n" +
               "🎯 最佳旅游时间：\n" +
               "• 夏季（12-2月）：薰衣草季节\n" +
               "• 秋季（3-5月）：气候宜人，游客较少\n\n" +
               "🍽️ 必尝美食：\n" +
               "• 塔斯马尼亚三文鱼\n" +
               "• 生蚝和海鲜\n" +
               "• 当地葡萄酒\n\n" +
               "想要定制专属的塔斯马尼亚行程吗？我可以根据您的喜好推荐最合适的旅游套餐！";
    }
    
    /**
     * 获取增强的默认回复
     */
    private String getEnhancedDefaultResponse(String message) {
        // 分析消息内容，提供更智能的回复
        if (message.contains("谢谢") || message.contains("thank")) {
            return "不客气！很高兴能为您提供帮助。如果您对塔斯马尼亚旅游有任何其他问题，随时可以问我！🌟";
        }
        
        if (message.contains("你好") || message.contains("hello") || message.contains("hi")) {
            return "您好！我是塔斯马尼亚旅游AI助手，很高兴为您服务！\n\n" +
                   "我可以帮您：\n" +
                   "🏝️ 了解塔斯马尼亚景点信息\n" +
                   "📅 查询旅游行程安排\n" +
                   "🌤️ 获取当地天气信息\n" +
                   "💱 查询汇率信息\n" +
                   "📰 了解最新旅游资讯\n" +
                   "🎯 个性化行程推荐\n\n" +
                   "请告诉我您想了解什么，我会尽力为您提供帮助！";
        }
        
        if (message.contains("再见") || message.contains("bye")) {
            return "再见！期待下次为您服务。祝您塔斯马尼亚之旅愉快！🏝️✨";
        }
        
        // 默认智能回复
        return "我是塔斯马尼亚旅游AI助手，专门为您提供旅游咨询服务！\n\n" +
               "您可以问我：\n" +
               "• 塔斯马尼亚的景点介绍\n" +
               "• 天气情况查询\n" +
               "• 旅游行程推荐\n" +
               "• 汇率和实用信息\n" +
               "• 订单查询和管理\n\n" +
               "如果您有其他问题，也可以直接告诉我，我会尽力帮助您！😊";
    }
} 