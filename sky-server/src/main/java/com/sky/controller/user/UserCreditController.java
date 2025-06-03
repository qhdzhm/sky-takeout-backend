package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.UserCreditService;
import com.sky.vo.UserCreditVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户积分控制器
 */
@RestController
@RequestMapping("/user/credit")
@Api(tags = "用户积分接口")
@Slf4j
public class UserCreditController {
    
    @Autowired
    private UserCreditService userCreditService;
    
    /**
     * 获取当前用户的积分信息
     * @return 用户积分信息
     */
    @GetMapping("/info")
    @ApiOperation("获取用户积分信息")
    public Result<UserCreditVO> getCreditInfo() {
        // 从线程上下文中获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        log.info("获取用户积分信息: userId={}", userId);
        
        UserCreditVO creditInfo = userCreditService.getUserCreditInfo(userId);
        return Result.success(creditInfo);
    }
} 