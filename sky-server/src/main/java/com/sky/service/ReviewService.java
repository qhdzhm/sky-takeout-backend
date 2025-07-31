package com.sky.service;

import com.sky.dto.ReviewDTO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface ReviewService {

    /**
     * 创建评论
     * @param reviewDTO 评论信息
     * @return 评论ID
     */
    Integer createReview(ReviewDTO reviewDTO);

    /**
     * 获取旅游产品评论
     * @param tourType 旅游产品类型
     * @param tourId 旅游产品ID
     * @return 评论列表
     */
    List<ReviewDTO> getTourReviews(String tourType, Integer tourId);

    /**
     * 获取用户评论
     * @return 评论列表
     */
    List<ReviewDTO> getUserReviews();
} 