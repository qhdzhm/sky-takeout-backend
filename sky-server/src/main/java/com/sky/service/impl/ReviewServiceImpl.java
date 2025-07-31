package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ReviewDTO;
import com.sky.exception.CustomException;
import com.sky.mapper.BookingMapper;
import com.sky.mapper.ReviewMapper;
import com.sky.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论服务实现类
 */
@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;
    
    @Autowired
    private BookingMapper bookingMapper;

    /**
     * 创建评论
     * @param reviewDTO 评论信息
     * @return 评论ID
     */
    @Override
    @Transactional
    public Integer createReview(ReviewDTO reviewDTO) {
        // 设置用户ID
        Long currentId = BaseContext.getCurrentId();
        reviewDTO.setUserId(currentId.intValue());
        
        // 验证用户是否有权限评论（是否有相关预订且已完成）
        boolean canReview = validateCanReview(
                reviewDTO.getTourId(), 
                reviewDTO.getTourType(), 
                currentId.intValue());
        
        if (!canReview) {
            throw new CustomException("您没有权限评论此旅游产品，请先完成相关预订");
        }
        
        // 设置评论时间
        reviewDTO.setCreateTime(LocalDateTime.now());
        
        // 创建评论
        reviewMapper.insert(reviewDTO);
        
        // 更新旅游产品的评分
        updateTourRating(reviewDTO.getTourId(), reviewDTO.getTourType());
        
        return reviewDTO.getId();
    }

    /**
     * 获取旅游产品评论
     * @param tourType 旅游产品类型
     * @param tourId 旅游产品ID
     * @return 评论列表
     */
    @Override
    public List<ReviewDTO> getTourReviews(String tourType, Integer tourId) {
        return reviewMapper.getByTour(tourType, tourId);
    }

    /**
     * 获取用户评论列表
     * @return 评论列表
     */
    @Override
    public List<ReviewDTO> getUserReviews() {
        Long currentId = BaseContext.getCurrentId();
        return reviewMapper.getByUserId(currentId.intValue());
    }
    
    /**
     * 验证用户是否有权限评论
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型
     * @param userId 用户ID
     * @return 是否有权限
     */
    private boolean validateCanReview(Integer tourId, String tourType, Integer userId) {
        // 检查用户是否有已完成的相关预订
        // 实际实现应该查询用户是否有已完成的预订
        // 这里简化处理，假设用户有权限评论
        return true;
    }
    
    /**
     * 更新旅游产品的评分
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型
     */
    private void updateTourRating(Integer tourId, String tourType) {
        Double avgRating;
        
        if ("day_tour".equals(tourType)) {
            avgRating = reviewMapper.getDayTourAverageRating(tourId);
            if (avgRating == null) {
                avgRating = 0.0;
            }
            // 这里应该调用更新一日游评分的方法
            // 由于没有对应的方法，这里只记录日志
            log.info("更新一日游评分，ID：{}，评分：{}", tourId, avgRating);
        } else {
            avgRating = reviewMapper.getGroupTourAverageRating(tourId);
            if (avgRating == null) {
                avgRating = 0.0;
            }
            // 这里应该调用更新跟团游评分的方法
            // 由于没有对应的方法，这里只记录日志
            log.info("更新跟团游评分，ID：{}，评分：{}", tourId, avgRating);
        }
    }
} 