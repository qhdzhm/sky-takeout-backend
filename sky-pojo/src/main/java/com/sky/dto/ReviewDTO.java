package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论数据传输对象
 */
@Data
public class ReviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 评论ID
    private Integer id;

    // 用户ID
    private Integer userId;

    // 用户名
    private String username;

    // 用户头像
    private String userAvatar;

    // 旅游产品ID
    private Integer tourId;

    // 旅游产品类型（day_tour或group_tour）
    private String tourType;

    // 评分（1-5）
    private Integer rating;

    // 评论内容
    private String content;

    // 评论图片，多张图片以逗号分隔
    private String images;

    // 创建时间
    private LocalDateTime createTime;
} 