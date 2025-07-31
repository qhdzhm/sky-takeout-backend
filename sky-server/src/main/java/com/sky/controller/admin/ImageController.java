package com.sky.controller.admin;

import com.sky.dto.ImageDTO;
import com.sky.result.Result;
import com.sky.service.ImageService;
import com.sky.vo.ImageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 图片相关接口
 */
@RestController
@RequestMapping("/admin/images")
@Api(tags = "图片相关接口")
@Slf4j
public class ImageController {

    @Autowired
    private ImageService imageService;

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return 图片URL
     */
    @PostMapping("/upload")
    @ApiOperation("上传图片")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("上传图片：{}", file.getOriginalFilename());
        String url = imageService.upload(file);
        return Result.success(url);
    }
    
    /**
     * 上传并保存图片
     *
     * @param file     图片文件
     * @param type     图片类型
     * @param relatedId 关联ID
     * @param description 图片描述
     * @param isPrimary 是否主图
     * @param position 图片位置
     * @return 图片视图对象
     */
    @PostMapping("/save")
    @ApiOperation("上传并保存图片")
    public Result<ImageVO> save(@RequestParam("file") MultipartFile file, 
                              @RequestParam("type") String type,
                              @RequestParam("relatedId") Integer relatedId, 
                              @RequestParam(value = "description", required = false) String description,
                              @RequestParam(value = "isPrimary", required = false) Integer isPrimary,
                              @RequestParam(value = "position", required = false) Integer position) {
        log.info("上传并保存图片：{}, 类型：{}, 关联ID：{}", file.getOriginalFilename(), type, relatedId);
        
        ImageDTO imageDTO = ImageDTO.builder()
                .type(type)
                .relatedId(relatedId)
                .description(description)
                .isPrimary(isPrimary)
                .position(position)
                .build();
                
        ImageVO imageVO = imageService.saveImage(file, imageDTO);
        return Result.success(imageVO);
    }
    
    /**
     * 获取图片列表
     *
     * @param type      图片类型
     * @param relatedId 关联ID
     * @return 图片列表
     */
    @GetMapping("/list")
    @ApiOperation("获取图片列表")
    public Result<List<ImageVO>> list(@RequestParam("type") String type, @RequestParam("relatedId") Integer relatedId) {
        log.info("获取图片列表，类型：{}，关联ID：{}", type, relatedId);
        List<ImageVO> list = imageService.getImagesByTypeAndId(type, relatedId);
        return Result.success(list);
    }
    
    /**
     * 设置主图
     *
     * @param id 图片ID
     * @param requestBody 包含type和relatedId的请求体
     * @return 操作结果
     */
    @PutMapping("/primary/{id}")
    @ApiOperation("设置主图")
    public Result<String> setPrimary(@PathVariable Integer id, @RequestBody Map<String, Object> requestBody) {
        String type = (String) requestBody.get("type");
        Integer relatedId = Integer.valueOf(requestBody.get("relatedId").toString());
        log.info("设置主图，ID：{}，类型：{}，关联ID：{}", id, type, relatedId);
        imageService.setPrimaryImage(id, type, relatedId);
        return Result.success();
    }
    
    /**
     * 删除图片
     *
     * @param id 图片ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除图片")
    public Result<String> delete(@PathVariable Integer id) {
        log.info("删除图片，ID：{}", id);
        imageService.deleteImage(id);
        return Result.success();
    }
    
    /**
     * 调整图片顺序
     *
     * @param id 图片ID
     * @param requestBody 包含position的请求体
     * @return 操作结果
     */
    @PutMapping("/position/{id}")
    @ApiOperation("调整图片顺序")
    public Result<String> updatePosition(@PathVariable Integer id, @RequestBody Map<String, Object> requestBody) {
        Integer position = Integer.valueOf(requestBody.get("position").toString());
        log.info("调整图片顺序，ID：{}，位置：{}", id, position);
        imageService.updateImagePosition(id, position);
        return Result.success();
    }
    
    /**
     * 修改图片描述
     *
     * @param id 图片ID
     * @param requestBody 包含description的请求体
     * @return 操作结果
     */
    @PutMapping("/description/{id}")
    @ApiOperation("修改图片描述")
    public Result<String> updateDescription(@PathVariable Integer id, @RequestBody Map<String, Object> requestBody) {
        String description = (String) requestBody.get("description");
        log.info("修改图片描述，ID：{}，描述：{}", id, description);
        imageService.updateImageDescription(id, description);
        return Result.success();
    }
} 