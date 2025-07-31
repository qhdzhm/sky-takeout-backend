package com.sky.controller.agent;

import com.sky.context.BaseContext;
import com.sky.result.Result;
import com.sky.service.AgentService;
import com.sky.service.ImageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 代理商头像相关接口
 */
@RestController
@RequestMapping("/agent/avatar")
@Api(tags = "代理商头像相关接口")
@Slf4j
public class AgentAvatarController {

    @Autowired
    private ImageService imageService;
    
    @Autowired
    private AgentService agentService;

    /**
     * 上传头像
     *
     * @param file 头像文件
     * @return 头像URL
     */
    @PostMapping("/upload")
    @ApiOperation("上传头像")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("代理商上传头像：{}", file.getOriginalFilename());
        
        // 验证文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || !isImageFile(fileName)) {
            return Result.error("只支持图片格式文件");
        }
        
        // 验证文件大小（限制2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("头像文件大小不能超过2MB");
        }
        
        try {
            // 1. 上传图片文件到服务器/云存储
            String url = imageService.upload(file);
            log.info("头像上传成功，URL: {}", url);
            
            // 2. 获取当前登录的代理商ID
            Long agentId = BaseContext.getCurrentId();
            if (agentId == null) {
                return Result.error("用户未登录");
            }
            
            // 3. 更新数据库中的头像URL
            agentService.updateAvatar(agentId, url);
            log.info("头像URL已保存到数据库：agentId={}, url={}", agentId, url);
            
            return Result.success(url);
        } catch (Exception e) {
            log.error("头像上传失败：{}", e.getMessage());
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 验证是否为图片文件
     *
     * @param fileName 文件名
     * @return 是否为图片文件
     */
    private boolean isImageFile(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(".jpg") || 
               lowerCaseFileName.endsWith(".jpeg") || 
               lowerCaseFileName.endsWith(".png") || 
               lowerCaseFileName.endsWith(".gif") || 
               lowerCaseFileName.endsWith(".webp");
    }
}