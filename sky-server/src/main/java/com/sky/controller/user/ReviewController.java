package com.sky.controller.user;

import com.sky.dto.ReviewDTO;
import com.sky.result.Result;
import com.sky.service.ReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/user/reviews")
@Api(tags = "评论相关接口")
@Slf4j
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 创建评论
     * @param reviewDTO 评论信息
     * @return 评论ID
     */
    @PostMapping
    @ApiOperation("创建评论")
    public Result<Integer> createReview(@RequestBody ReviewDTO reviewDTO) {
        log.info("创建评论：{}", reviewDTO);
        Integer reviewId = reviewService.createReview(reviewDTO);
        return Result.success(reviewId);
    }

    /**
     * 获取旅游产品评论
     * @param tourType 旅游产品类型
     * @param tourId 旅游产品ID
     * @return 评论列表
     */
    @GetMapping("/tour")
    @ApiOperation("获取旅游产品评论")
    public Result<List<ReviewDTO>> getTourReviews(
            @RequestParam String tourType,
            @RequestParam Integer tourId) {
        log.info("获取旅游产品评论，类型：{}，ID：{}", tourType, tourId);
        List<ReviewDTO> reviews = reviewService.getTourReviews(tourType, tourId);
        return Result.success(reviews);
    }

    /**
     * 获取用户评论列表
     * @return 评论列表
     */
    @GetMapping
    @ApiOperation("获取用户评论列表")
    public Result<List<ReviewDTO>> getUserReviews() {
        log.info("获取用户评论列表");
        List<ReviewDTO> reviews = reviewService.getUserReviews();
        return Result.success(reviews);
    }
} 