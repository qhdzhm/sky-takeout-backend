package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信工具类
 */
@Component
@Slf4j
public class WeChatUtil {

    private static String appid;
    private static String secret;

    @Value("${sky.wechat.appid}")
    public void setAppid(String appid) {
        WeChatUtil.appid = appid;
    }

    @Value("${sky.wechat.secret}")
    public void setSecret(String secret) {
        WeChatUtil.secret = secret;
    }

    /**
     * 获取微信用户OpenID和访问令牌
     *
     * @param code 微信登录临时凭证
     * @return Map包含openid等信息
     */
    public static Map<String, String> getWxLoginInfo(String code) {
        // 使用OAuth 2.0授权码获取访问令牌
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appid +
                "&secret=" + secret + "&code=" + code + "&grant_type=authorization_code";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String responseBody = responseEntity.getBody();
        
        log.info("微信OAuth2.0访问令牌返回结果：{}", responseBody);
        
        Map<String, String> resultMap = new HashMap<>();
        
        if (responseBody != null) {
            JSONObject jsonObject = JSON.parseObject(responseBody);
            String openid = jsonObject.getString("openid");
            String accessToken = jsonObject.getString("access_token");
            String refreshToken = jsonObject.getString("refresh_token");
            String scope = jsonObject.getString("scope");
            String errcode = jsonObject.getString("errcode");
            String errmsg = jsonObject.getString("errmsg");
            
            if (openid != null) {
                resultMap.put("openid", openid);
                if (accessToken != null) {
                    resultMap.put("access_token", accessToken);
                }
                if (refreshToken != null) {
                    resultMap.put("refresh_token", refreshToken);
                }
                if (scope != null) {
                    resultMap.put("scope", scope);
                }
                
                // 如果scope包含snsapi_userinfo，则获取用户信息
                if (scope != null && scope.contains("snsapi_userinfo") && accessToken != null) {
                    try {
                        String userInfoJson = getUserInfo(accessToken, openid);
                        log.info("获取到微信用户信息：{}", userInfoJson);
                        
                        JSONObject userInfo = JSON.parseObject(userInfoJson);
                        String nickname = userInfo.getString("nickname");
                        String headimgurl = userInfo.getString("headimgurl");
                        String unionid = userInfo.getString("unionid");
                        
                        if (nickname != null) {
                            resultMap.put("nickname", nickname);
                        }
                        if (headimgurl != null) {
                            resultMap.put("headimgurl", headimgurl);
                        }
                        if (unionid != null) {
                            resultMap.put("unionid", unionid);
                        }
                    } catch (Exception e) {
                        log.error("获取微信用户信息失败", e);
                    }
                }
            } else {
                // 错误处理
                resultMap.put("errcode", errcode);
                resultMap.put("errmsg", errmsg);
            }
        }
        
        return resultMap;
    }
    
    /**
     * 获取微信用户信息
     *
     * @param accessToken 访问令牌
     * @param openid 用户的OpenID
     * @return JSON字符串，包含用户信息
     */
    public static String getUserInfo(String accessToken, String openid) {
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken +
                "&openid=" + openid + "&lang=zh_CN";
        
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }
    
    /**
     * 获取微信网页授权访问令牌
     *
     * @param code 授权码
     * @return 包含访问令牌的JSON字符串
     */
    public static String getWebAccessToken(String code) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appid +
                "&secret=" + secret + "&code=" + code + "&grant_type=authorization_code";
        
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }
} 