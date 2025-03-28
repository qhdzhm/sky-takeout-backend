package com.sky.mapper;

import com.sky.dto.ReviewDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论数据访问层
 */
@Mapper
public interface ReviewMapper {

    /**
     * 创建评论
     * @param reviewDTO 评论信息
     * @return 影响行数
     */
    @Insert("INSERT INTO reviews(user_id, tour_type, tour_id, rating, comment, review_date, is_approved) " +
            "VALUES(#{userId}, #{tourType}, #{tourId}, #{rating}, #{comment}, NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "review_id")
    int insert(ReviewDTO reviewDTO);

    /**
     * 根据旅游产品类型和ID查询评论列表
     * @param tourType 旅游产品类型
     * @param tourId 旅游产品ID
     * @return 评论列表
     */
    @Select("SELECT r.*, u.username as userName FROM reviews r " +
            "JOIN users u ON r.user_id = u.user_id " +
            "WHERE r.tour_type = #{tourType} AND r.tour_id = #{tourId} AND r.is_approved = 1 " +
            "ORDER BY r.review_date DESC")
    List<ReviewDTO> getByTour(@Param("tourType") String tourType, @Param("tourId") Integer tourId);

    /**
     * 根据用户ID查询评论列表
     * @param userId 用户ID
     * @return 评论列表
     */
    @Select("SELECT r.*, " +
            "CASE WHEN r.tour_type = 'day_tour' THEN (SELECT name FROM day_tours WHERE day_tour_id = r.tour_id) " +
            "ELSE (SELECT title FROM group_tours WHERE group_tour_id = r.tour_id) END as tourName " +
            "FROM reviews r " +
            "WHERE r.user_id = #{userId} " +
            "ORDER BY r.review_date DESC")
    List<ReviewDTO> getByUserId(Integer userId);

    /**
     * 查询一日游平均评分
     * @param tourId 一日游ID
     * @return 平均评分
     */
    @Select("SELECT AVG(rating) FROM reviews WHERE tour_type = 'day_tour' AND tour_id = #{tourId} AND is_approved = 1")
    Double getDayTourAverageRating(Integer tourId);

    /**
     * 查询跟团游平均评分
     * @param tourId 跟团游ID
     * @return 平均评分
     */
    @Select("SELECT AVG(rating) FROM reviews WHERE tour_type = 'group_tour' AND tour_id = #{tourId} AND is_approved = 1")
    Double getGroupTourAverageRating(Integer tourId);

    /**
     * 查询一日游评论数量
     * @param tourId 一日游ID
     * @return 评论数量
     */
    @Select("SELECT COUNT(*) FROM reviews WHERE tour_type = 'day_tour' AND tour_id = #{tourId} AND is_approved = 1")
    Integer getDayTourReviewCount(Integer tourId);

    /**
     * 查询跟团游评论数量
     * @param tourId 跟团游ID
     * @return 评论数量
     */
    @Select("SELECT COUNT(*) FROM reviews WHERE tour_type = 'group_tour' AND tour_id = #{tourId} AND is_approved = 1")
    Integer getGroupTourReviewCount(Integer tourId);
} 