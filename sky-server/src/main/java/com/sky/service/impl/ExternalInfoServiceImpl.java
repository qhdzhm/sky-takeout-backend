package com.sky.service.impl;

import com.sky.service.ExternalInfoService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ExternalInfoServiceImpl implements ExternalInfoService {

    @Value("${weather.openweathermap.api-key:}")
    private String weatherApiKey;

    @Value("${weather.openweathermap.base-url:http://api.openweathermap.org/data/2.5}")
    private String weatherApiBaseUrl;

    @Value("${weather.openweathermap.enabled:false}")
    private boolean weatherApiEnabled;

    @Value("${exchange.api.key:}")
    private String exchangeApiKey;

    @Value("${exchange.api.base-url:https://api.exchangerate-api.com/v4}")
    private String exchangeApiBaseUrl;

    @Value("${exchange.api.enabled:false}")
    private boolean exchangeApiEnabled;

    @Value("${news.api.key:}")
    private String newsApiKey;

    @Value("${news.api.base-url:https://newsapi.org/v2}")
    private String newsApiBaseUrl;

    @Value("${news.api.enabled:false}")
    private boolean newsApiEnabled;

    @Value("${baidu.search.api-key:}")
    private String baiduSearchApiKey;

    @Value("${baidu.search.base-url:https://aip.baidubce.com/rest/2.0}")
    private String baiduSearchBaseUrl;

    @Value("${baidu.search.enabled:false}")
    private boolean baiduSearchEnabled;

    private final RedisTemplate<String, String> redisTemplate;
    private OkHttpClient httpClient;

    public ExternalInfoServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String getWeatherInfo(String cityName) {
        if (!weatherApiEnabled || weatherApiKey.isEmpty()) {
            return getBasicWeatherInfo(cityName);
        }

        try {
            // 检查缓存
            String cacheKey = "weather:" + cityName;
            String cachedResult = redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }

            // 调用天气API
            String url = String.format("%s/weather?q=%s&appid=%s&units=metric&lang=zh_cn", 
                    weatherApiBaseUrl, cityName, weatherApiKey);
            
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    String weatherInfo = parseWeatherInfo(responseBody, cityName);
                    
                    // 缓存结果10分钟
                    redisTemplate.opsForValue().set(cacheKey, weatherInfo, 600, TimeUnit.SECONDS);
                    return weatherInfo;
                }
            }
        } catch (Exception e) {
            log.error("获取天气信息失败: {}", e.getMessage());
        }

        return getBasicWeatherInfo(cityName);
    }

    @Override
    public String getExchangeRate(String fromCurrency, String toCurrency) {
        if (!exchangeApiEnabled || exchangeApiKey.isEmpty()) {
            return getBasicExchangeRateInfo(fromCurrency, toCurrency);
        }

        try {
            // 检查缓存
            String cacheKey = String.format("exchange:%s:%s", fromCurrency, toCurrency);
            String cachedResult = redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }

            // 调用汇率API
            String url = String.format("%s/latest/%s", exchangeApiBaseUrl, fromCurrency);
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + exchangeApiKey)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    String rateInfo = parseExchangeRateInfo(responseBody, fromCurrency, toCurrency);
                    
                    // 缓存结果5分钟
                    redisTemplate.opsForValue().set(cacheKey, rateInfo, 300, TimeUnit.SECONDS);
                    return rateInfo;
                }
            }
        } catch (Exception e) {
            log.error("获取汇率信息失败: {}", e.getMessage());
        }

        return getBasicExchangeRateInfo(fromCurrency, toCurrency);
    }

    @Override
    public String getTravelNews(String keyword) {
        if (!newsApiEnabled || newsApiKey.isEmpty()) {
            return getBasicTravelNews(keyword);
        }

        try {
            // 检查缓存
            String cacheKey = "news:" + keyword;
            String cachedResult = redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }

            // 调用新闻API
            String url = String.format("%s/everything?q=%s+travel&language=zh&sortBy=publishedAt&pageSize=5", 
                    newsApiBaseUrl, keyword);
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-API-Key", newsApiKey)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    String newsInfo = parseNewsInfo(responseBody, keyword);
                    
                    // 缓存结果30分钟
                    redisTemplate.opsForValue().set(cacheKey, newsInfo, 1800, TimeUnit.SECONDS);
                    return newsInfo;
                }
            }
        } catch (Exception e) {
            log.error("获取新闻信息失败: {}", e.getMessage());
        }

        return getBasicTravelNews(keyword);
    }

    @Override
    public String getTrafficInfo(String location) {
        // 由于交通信息API通常需要特殊权限，这里提供基本信息
        return getBasicTrafficInfo(location);
    }

    @Override
    public String smartSearch(String query, String context) {
        if (!baiduSearchEnabled || baiduSearchApiKey.isEmpty()) {
            return getBasicSearchInfo(query, context);
        }

        try {
            // 检查缓存
            String cacheKey = "search:" + query.hashCode();
            String cachedResult = redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }

            // 这里可以集成百度搜索API或其他搜索服务
            String searchResult = performSearch(query, context);
            
            // 缓存结果15分钟
            redisTemplate.opsForValue().set(cacheKey, searchResult, 900, TimeUnit.SECONDS);
            return searchResult;
        } catch (Exception e) {
            log.error("智能搜索失败: {}", e.getMessage());
        }

        return getBasicSearchInfo(query, context);
    }

    @Override
    public String getTravelGuide(String destination) {
        try {
            // 检查缓存
            String cacheKey = "guide:" + destination;
            String cachedResult = redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }

            String guideInfo = getBasicTravelGuide(destination);
            
            // 缓存结果1小时
            redisTemplate.opsForValue().set(cacheKey, guideInfo, 3600, TimeUnit.SECONDS);
            return guideInfo;
        } catch (Exception e) {
            log.error("获取旅游攻略失败: {}", e.getMessage());
        }

        return "抱歉，暂时无法获取" + destination + "的旅游攻略信息。";
    }

    private String parseWeatherInfo(String responseBody, String cityName) {
        try {
            // 简化的JSON解析，实际应使用Jackson或其他JSON库
            if (responseBody.contains("\"temp\"")) {
                String temp = extractJsonValue(responseBody, "temp");
                String description = extractJsonValue(responseBody, "description");
                String humidity = extractJsonValue(responseBody, "humidity");
                
                return String.format("📍 %s 当前天气：\n🌡️ 温度：%s°C\n☁️ 天气：%s\n💧 湿度：%s%%\n\n适合出行，请根据天气情况准备合适的衣物。", 
                        cityName, temp, description, humidity);
            }
        } catch (Exception e) {
            log.error("解析天气信息失败: {}", e.getMessage());
        }
        return getBasicWeatherInfo(cityName);
    }

    private String parseExchangeRateInfo(String responseBody, String fromCurrency, String toCurrency) {
        try {
            if (responseBody.contains("\"rates\"")) {
                String rate = extractJsonValue(responseBody, toCurrency);
                return String.format("💱 汇率查询结果：\n1 %s = %s %s\n\n*汇率仅供参考，实际兑换请以银行汇率为准", 
                        fromCurrency, rate, toCurrency);
            }
        } catch (Exception e) {
            log.error("解析汇率信息失败: {}", e.getMessage());
        }
        return getBasicExchangeRateInfo(fromCurrency, toCurrency);
    }

    private String parseNewsInfo(String responseBody, String keyword) {
        try {
            if (responseBody.contains("\"articles\"")) {
                StringBuilder newsInfo = new StringBuilder();
                newsInfo.append("📰 ").append(keyword).append(" 相关旅游新闻：\n\n");
                
                // 简化解析，实际应使用proper JSON解析
                String[] articles = responseBody.split("\"title\":");
                int count = 0;
                for (int i = 1; i < articles.length && count < 3; i++) {
                    String title = extractJsonValue(articles[i], null);
                    if (title != null && title.length() > 10) {
                        newsInfo.append("• ").append(title).append("\n");
                        count++;
                    }
                }
                
                return newsInfo.toString();
            }
        } catch (Exception e) {
            log.error("解析新闻信息失败: {}", e.getMessage());
        }
        return getBasicTravelNews(keyword);
    }

    private String extractJsonValue(String json, String key) {
        try {
            if (key == null) {
                // 提取第一个引号内的内容
                int start = json.indexOf("\"") + 1;
                int end = json.indexOf("\"", start);
                if (start > 0 && end > start) {
                    return json.substring(start, end);
                }
            } else {
                String searchKey = "\"" + key + "\":";
                int start = json.indexOf(searchKey);
                if (start >= 0) {
                    start += searchKey.length();
                    if (json.charAt(start) == '"') {
                        start++;
                        int end = json.indexOf("\"", start);
                        if (end > start) {
                            return json.substring(start, end);
                        }
                    } else {
                        int end = json.indexOf(",", start);
                        if (end == -1) end = json.indexOf("}", start);
                        if (end > start) {
                            return json.substring(start, end).trim();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("提取JSON值失败: {}", e.getMessage());
        }
        return null;
    }

    private String getBasicWeatherInfo(String cityName) {
        return String.format("📍 %s 天气信息：\n由于天气API未配置，无法获取实时天气数据。\n建议您查询当地天气预报，合理安排出行计划。", cityName);
    }

    private String getBasicExchangeRateInfo(String fromCurrency, String toCurrency) {
        return String.format("💱 %s 到 %s 汇率查询：\n由于汇率API未配置，无法获取实时汇率。\n建议您查询银行汇率或使用专业汇率app。", 
                fromCurrency, toCurrency);
    }

    private String getBasicTravelNews(String keyword) {
        return String.format("📰 %s 旅游资讯：\n由于新闻API未配置，无法获取最新资讯。\n建议您关注相关旅游网站或新闻平台获取最新信息。", keyword);
    }

    private String getBasicTrafficInfo(String location) {
        return String.format("🚗 %s 交通信息：\n建议使用高德地图、百度地图等导航软件查询实时交通状况。\n出行前请提前规划路线，避开拥堵路段。", location);
    }

    private String performSearch(String query, String context) {
        // 这里可以集成实际的搜索API
        return String.format("🔍 关于 \"%s\" 的搜索结果：\n由于搜索API未完全配置，建议您：\n• 访问官方旅游网站\n• 查询专业旅游论坛\n• 咨询当地旅游信息中心", query);
    }

    private String getBasicSearchInfo(String query, String context) {
        return String.format("🔍 搜索 \"%s\"：\n建议您通过官方渠道或专业平台获取更详细的信息。", query);
    }

    private String getBasicTravelGuide(String destination) {
        return String.format("🗺️ %s 旅游攻略：\n\n基本信息：\n• 建议提前了解当地天气和文化\n• 准备必要的旅行证件\n• 规划合理的行程安排\n• 了解当地交通方式\n\n更详细的攻略信息，建议咨询我们的旅游顾问或查看专业旅游网站。", destination);
    }
} 