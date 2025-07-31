package com.sky.controller.user;

import com.sky.properties.WeChatProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

/**
 * 微信登录页面控制器
 */
@Controller
@RequestMapping("/wechat-login")
@Slf4j
public class WechatLoginViewController {

    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 微信登录页面
     */
    @GetMapping("")
    public String loginPage(Model model) {
        // 设置跳转的回调地址
        String redirectUri = "https://yourdomain.com/api/user/wechat/callback";
        
        // 创建微信授权URL
        String authUrl = "https://open.weixin.qq.com/connect/qrconnect?" +
                "appid=" + weChatProperties.getAppid() +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=snsapi_login" + // 网页应用使用snsapi_login
                "&state=STATE#wechat_redirect";
        
        model.addAttribute("authUrl", authUrl);
        return "wechat-login";
    }
    
    /**
     * 微信登录回调
     */
    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code, HttpSession session) {
        log.info("接收到微信授权回调，授权码：{}", code);
        
        // 将授权码存入会话，前端JS可以使用此code调用后端登录接口
        session.setAttribute("wx_auth_code", code);
        
        // 重定向到前端首页或登录成功页面
        return "redirect:/";
    }
} 