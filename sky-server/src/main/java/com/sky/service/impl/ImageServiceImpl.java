package com.sky.service.impl;

import com.sky.constant.ImageType;
import com.sky.dto.ImageDTO;
import com.sky.entity.DayTourImage;
import com.sky.entity.GroupTourImage;
import com.sky.exception.BusinessException;
import com.sky.mapper.DayTourImageMapper;
import com.sky.mapper.GroupTourImageMapper;
import com.sky.service.ImageService;
import com.sky.utils.AliOssUtil;
import com.sky.vo.ImageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片服务实现类
 */
@Service
@Slf4j
public class ImageServiceImpl implements ImageService {

    @Autowired
    private AliOssUtil aliOssUtil;
    
    @Autowired
    private DayTourImageMapper dayTourImageMapper;
    
    @Autowired
    private GroupTourImageMapper groupTourImageMapper;
    
    @Value("${sky.image.directory:images/}")
    private String imageDirectory;

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return 图片URL
     */
    @Override
    public String upload(MultipartFile file) {
        try {
            // 上传到OSS并返回访问URL
            return aliOssUtil.uploadFile(file, imageDirectory);
        } catch (Exception e) {
            log.error("图片上传失败：{}", e.getMessage());
            throw new BusinessException("图片上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传并保存图片信息
     *
     * @param file     图片文件
     * @param imageDTO 图片信息
     * @return 图片视图对象
     */
    @Override
    @Transactional
    public ImageVO saveImage(MultipartFile file, ImageDTO imageDTO) {
        // 1. 上传图片到OSS
        String imageUrl = upload(file);
        
        // 2. 根据类型保存图片信息
        ImageVO imageVO = new ImageVO();
        if (ImageType.DAY_TOUR.equals(imageDTO.getType())) {
            // 保存一日游图片
            DayTourImage dayTourImage = new DayTourImage();
            dayTourImage.setDayTourId(imageDTO.getRelatedId());
            dayTourImage.setImageUrl(imageUrl);
            dayTourImage.setDescription(imageDTO.getDescription());
            dayTourImage.setIsPrimary(imageDTO.getIsPrimary() != null ? imageDTO.getIsPrimary() : 0);
            dayTourImage.setPosition(imageDTO.getPosition() != null ? imageDTO.getPosition() : 0);
            dayTourImage.setCreatedAt(LocalDateTime.now());
            dayTourImage.setUpdatedAt(LocalDateTime.now());
            
            // 如果是主图，将其他图片设为非主图
            if (dayTourImage.getIsPrimary() == 1) {
                dayTourImageMapper.resetPrimaryImage(imageDTO.getRelatedId());
            }
            
            // 保存图片信息
            dayTourImageMapper.insert(dayTourImage);
            
            // 如果是主图，更新一日游的主图URL
            if (dayTourImage.getIsPrimary() == 1) {
                dayTourImageMapper.updateDayTourImageUrl(imageDTO.getRelatedId(), imageUrl);
            }
            
            // 转换为返回对象
            BeanUtils.copyProperties(dayTourImage, imageVO);
            imageVO.setType(ImageType.DAY_TOUR);
            imageVO.setRelatedId(dayTourImage.getDayTourId());
            
        } else if (ImageType.GROUP_TOUR.equals(imageDTO.getType())) {
            // 保存跟团游图片
            GroupTourImage groupTourImage = new GroupTourImage();
            groupTourImage.setGroupTourId(imageDTO.getRelatedId());
            groupTourImage.setImageUrl(imageUrl);
            groupTourImage.setDescription(imageDTO.getDescription());
            groupTourImage.setIsPrimary(imageDTO.getIsPrimary() != null ? imageDTO.getIsPrimary() : 0);
            groupTourImage.setPosition(imageDTO.getPosition() != null ? imageDTO.getPosition() : 0);
            groupTourImage.setCreatedAt(LocalDateTime.now());
            groupTourImage.setUpdatedAt(LocalDateTime.now());
            
            // 如果是主图，将其他图片设为非主图
            if (groupTourImage.getIsPrimary() == 1) {
                groupTourImageMapper.resetPrimaryImage(imageDTO.getRelatedId());
            }
            
            // 保存图片信息
            groupTourImageMapper.insert(groupTourImage);
            
            // 如果是主图，更新跟团游的主图URL
            if (groupTourImage.getIsPrimary() == 1) {
                groupTourImageMapper.updateGroupTourImageUrl(imageDTO.getRelatedId(), imageUrl);
            }
            
            // 转换为返回对象
            BeanUtils.copyProperties(groupTourImage, imageVO);
            imageVO.setType(ImageType.GROUP_TOUR);
            imageVO.setRelatedId(groupTourImage.getGroupTourId());
            
        } else {
            throw new BusinessException("不支持的图片类型：" + imageDTO.getType());
        }
        
        imageVO.setImageUrl(imageUrl);
        return imageVO;
    }

    /**
     * 根据类型和关联ID获取图片列表
     *
     * @param type      图片类型 (day_tour, group_tour)
     * @param relatedId 关联ID
     * @return 图片列表
     */
    @Override
    public List<ImageVO> getImagesByTypeAndId(String type, Integer relatedId) {
        List<ImageVO> imageVOList = new ArrayList<>();
        
        if (ImageType.DAY_TOUR.equals(type)) {
            // 获取一日游图片列表
            List<DayTourImage> dayTourImages = dayTourImageMapper.selectByDayTourId(relatedId);
            for (DayTourImage image : dayTourImages) {
                ImageVO imageVO = new ImageVO();
                BeanUtils.copyProperties(image, imageVO);
                imageVO.setType(ImageType.DAY_TOUR);
                imageVO.setRelatedId(image.getDayTourId());
                imageVOList.add(imageVO);
            }
        } else if (ImageType.GROUP_TOUR.equals(type)) {
            // 获取跟团游图片列表
            List<GroupTourImage> groupTourImages = groupTourImageMapper.selectByGroupTourId(relatedId);
            for (GroupTourImage image : groupTourImages) {
                ImageVO imageVO = new ImageVO();
                BeanUtils.copyProperties(image, imageVO);
                imageVO.setType(ImageType.GROUP_TOUR);
                imageVO.setRelatedId(image.getGroupTourId());
                imageVOList.add(imageVO);
            }
        } else {
            throw new BusinessException("不支持的图片类型：" + type);
        }
        
        return imageVOList;
    }

    /**
     * 设置主图
     *
     * @param id        图片ID
     * @param type      图片类型
     * @param relatedId 关联ID
     */
    @Override
    @Transactional
    public void setPrimaryImage(Integer id, String type, Integer relatedId) {
        if (ImageType.DAY_TOUR.equals(type)) {
            // 将其他图片设为非主图
            dayTourImageMapper.resetPrimaryImage(relatedId);
            // 设置当前图片为主图
            dayTourImageMapper.setPrimaryImage(id);
            // 获取图片URL
            DayTourImage image = dayTourImageMapper.selectById(id);
            if (image == null) {
                throw new BusinessException("图片不存在");
            }
            // 更新一日游的主图URL
            dayTourImageMapper.updateDayTourImageUrl(relatedId, image.getImageUrl());
        } else if (ImageType.GROUP_TOUR.equals(type)) {
            // 将其他图片设为非主图
            groupTourImageMapper.resetPrimaryImage(relatedId);
            // 设置当前图片为主图
            groupTourImageMapper.setPrimaryImage(id);
            // 获取图片URL
            GroupTourImage image = groupTourImageMapper.selectById(id);
            if (image == null) {
                throw new BusinessException("图片不存在");
            }
            // 更新跟团游的主图URL
            groupTourImageMapper.updateGroupTourImageUrl(relatedId, image.getImageUrl());
        } else {
            throw new BusinessException("不支持的图片类型：" + type);
        }
    }

    /**
     * 删除图片
     *
     * @param id 图片ID
     */
    @Override
    @Transactional
    public void deleteImage(Integer id) {
        // 1. 查询图片信息
        DayTourImage dayTourImage = dayTourImageMapper.selectById(id);
        GroupTourImage groupTourImage = null;
        
        if (dayTourImage != null) {
            // 获取图片URL
            String imageUrl = dayTourImage.getImageUrl();
            
            // 2. 删除OSS中的图片
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String objectName = aliOssUtil.getObjectNameFromUrl(imageUrl);
                if (objectName != null) {
                    aliOssUtil.deleteFile(objectName);
                }
            }
            
            // 3. 删除数据库记录
            dayTourImageMapper.deleteById(id);
            
            // 4. 如果是主图，则需要找一张新的图片设为主图
            if (dayTourImage.getIsPrimary() == 1) {
                // 查找第一张图片
                DayTourImage firstImage = dayTourImageMapper.selectFirstByDayTourId(dayTourImage.getDayTourId());
                if (firstImage != null) {
                    // 设置为主图
                    setPrimaryImage(firstImage.getId(), ImageType.DAY_TOUR, dayTourImage.getDayTourId());
                } else {
                    // 没有其他图片，清空主图
                    dayTourImageMapper.updateDayTourImageUrl(dayTourImage.getDayTourId(), null);
                }
            }
        } else {
            // 尝试从跟团游图片中查找
            groupTourImage = groupTourImageMapper.selectById(id);
            if (groupTourImage != null) {
                // 获取图片URL
                String imageUrl = groupTourImage.getImageUrl();
                
                // 2. 删除OSS中的图片
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    String objectName = aliOssUtil.getObjectNameFromUrl(imageUrl);
                    if (objectName != null) {
                        aliOssUtil.deleteFile(objectName);
                    }
                }
                
                // 3. 删除数据库记录
                groupTourImageMapper.deleteById(id);
                
                // 4. 如果是主图，则需要找一张新的图片设为主图
                if (groupTourImage.getIsPrimary() == 1) {
                    // 查找第一张图片
                    GroupTourImage firstImage = groupTourImageMapper.selectFirstByGroupTourId(groupTourImage.getGroupTourId());
                    if (firstImage != null) {
                        // 设置为主图
                        setPrimaryImage(firstImage.getId(), ImageType.GROUP_TOUR, groupTourImage.getGroupTourId());
                    } else {
                        // 没有其他图片，清空主图
                        groupTourImageMapper.updateGroupTourImageUrl(groupTourImage.getGroupTourId(), null);
                    }
                }
            } else {
                throw new BusinessException("图片不存在");
            }
        }
    }

    /**
     * 更新图片位置
     *
     * @param id       图片ID
     * @param position 位置
     */
    @Override
    public void updateImagePosition(Integer id, Integer position) {
        // 尝试更新一日游图片位置
        int rows = dayTourImageMapper.updatePosition(id, position);
        if (rows == 0) {
            // 尝试更新跟团游图片位置
            rows = groupTourImageMapper.updatePosition(id, position);
            if (rows == 0) {
                throw new BusinessException("图片不存在");
            }
        }
    }

    /**
     * 更新图片描述
     *
     * @param id          图片ID
     * @param description 描述
     */
    @Override
    public void updateImageDescription(Integer id, String description) {
        // 尝试更新一日游图片描述
        int rows = dayTourImageMapper.updateDescription(id, description);
        if (rows == 0) {
            // 尝试更新跟团游图片描述
            rows = groupTourImageMapper.updateDescription(id, description);
            if (rows == 0) {
                throw new BusinessException("图片不存在");
            }
        }
    }
} 