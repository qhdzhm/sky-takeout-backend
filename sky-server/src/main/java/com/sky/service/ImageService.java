package com.sky.service;

import com.sky.dto.ImageDTO;
import com.sky.vo.ImageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 图片服务接口
 */
public interface ImageService {

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return 图片URL
     */
    String upload(MultipartFile file);
    
    /**
     * 上传并保存图片信息
     *
     * @param file     图片文件
     * @param imageDTO 图片信息
     * @return 图片视图对象
     */
    ImageVO saveImage(MultipartFile file, ImageDTO imageDTO);
    
    /**
     * 根据类型和关联ID获取图片列表
     *
     * @param type      图片类型 (day_tour, group_tour)
     * @param relatedId 关联ID
     * @return 图片列表
     */
    List<ImageVO> getImagesByTypeAndId(String type, Integer relatedId);
    
    /**
     * 设置主图
     *
     * @param id        图片ID
     * @param type      图片类型
     * @param relatedId 关联ID
     */
    void setPrimaryImage(Integer id, String type, Integer relatedId);
    
    /**
     * 删除图片
     *
     * @param id 图片ID
     */
    void deleteImage(Integer id);
    
    /**
     * 更新图片位置
     *
     * @param id       图片ID
     * @param position 位置
     */
    void updateImagePosition(Integer id, Integer position);
    
    /**
     * 更新图片描述
     *
     * @param id          图片ID
     * @param description 描述
     */
    void updateImageDescription(Integer id, String description);
} 