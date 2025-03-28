package com.sky.mapper;

import com.sky.dto.GroupTourDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 跟团游数据访问层
 */
@Mapper
public interface GroupTourMapper {

    /**
     * 分页查询跟团游
     * @param title 标题
     * @param location 地点
     * @param category 类别
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param minDays 最少天数
     * @param maxDays 最多天数
     * @return 跟团游列表
     */
    List<GroupTourDTO> pageQuery(@Param("title") String title,
                                @Param("location") String location,
                                @Param("category") String category,
                                @Param("minPrice") Double minPrice,
                                @Param("maxPrice") Double maxPrice,
                                @Param("minDays") Integer minDays,
                                @Param("maxDays") Integer maxDays);

    /**
     * 根据ID查询跟团游
     * @param id 跟团游ID
     * @return 跟团游信息
     */
    @Select("SELECT group_tour_id AS id, title AS name, description, price, discounted_price AS discountedPrice, " +
            "duration, days, nights, rating, reviews_count AS reviewsCount, tour_code AS tourCode, " +
            "departure_info AS departureInfo, group_size AS groupSize, language, " +
            "image_url AS coverImage, is_active AS isActive, location, category, " +
            "departure_address AS departureAddress, guide_fee AS guideFee, guide_id AS guideId " +
            "FROM group_tours WHERE group_tour_id = #{id}")
    GroupTourDTO getById(Integer id);

    /**
     * 查询跟团游行程安排
     * @param tourId 跟团游ID
     * @return 行程安排列表
     */
    @Select("SELECT * FROM tour_itinerary WHERE group_tour_id = #{tourId} ORDER BY day_number")
    List<Map<String, Object>> getItinerary(Integer tourId);

    /**
     * 查询跟团游可用日期
     * @param tourId 跟团游ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 可用日期列表
     */
    List<Map<String, Object>> getAvailableDates(@Param("tourId") Integer tourId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    /**
     * 查询跟团游主题
     * @return 主题列表
     */
    @Select("SELECT gt.theme_id as id, gt.name FROM group_tour_themes gt")
    List<Map<String, Object>> getThemes();

    /**
     * 查询跟团游主题关联，并返回主题名称
     * @param tourId 跟团游ID
     * @return 主题信息列表
     */
    @Select("SELECT gt.theme_id as id, gt.name FROM group_tour_theme_relation gtr " +
            "JOIN group_tour_themes gt ON gtr.theme_id = gt.theme_id " +
            "WHERE gtr.group_tour_id = #{tourId}")
    List<Map<String, Object>> getThemesByTourId(Integer tourId);

    /**
     * 查询跟团游亮点
     * @param tourId 跟团游ID
     * @return 亮点列表
     */
    @Select("SELECT description FROM tour_highlights WHERE group_tour_id = #{tourId}")
    List<String> getHighlights(Integer tourId);

    /**
     * 查询跟团游包含项目
     * @param tourId 跟团游ID
     * @return 包含项目列表
     */
    @Select("SELECT description FROM tour_inclusions WHERE group_tour_id = #{tourId}")
    List<String> getInclusions(Integer tourId);

    /**
     * 查询跟团游不包含项目
     * @param tourId 跟团游ID
     * @return 不包含项目列表
     */
    @Select("SELECT description FROM tour_exclusions WHERE group_tour_id = #{tourId}")
    List<String> getExclusions(Integer tourId);

    /**
     * 查询跟团游常见问题
     * @param tourId 跟团游ID
     * @return 常见问题列表
     */
    @Select("SELECT question, answer FROM tour_faqs WHERE group_tour_id = #{tourId}")
    List<Map<String, Object>> getFaqs(Integer tourId);

    /**
     * 查询跟团游贴士
     * @param tourId 跟团游ID
     * @return 贴士列表
     */
    @Select("SELECT description FROM tour_tips WHERE group_tour_id = #{tourId}")
    List<String> getTips(Integer tourId);

    /**
     * 查询跟团游图片
     * @param tourId 跟团游ID
     * @return 图片列表
     */
    @Select("SELECT image_url, thumbnail_url, description FROM group_tour_images WHERE group_tour_id = #{tourId}")
    List<Map<String, Object>> getImages(Integer tourId);

    /**
     * 查询跟团游主题关联
     * @param tourId 跟团游ID
     * @return 主题ID列表
     */
    @Select("SELECT theme_id FROM group_tour_theme_relation WHERE group_tour_id = #{tourId}")
    List<Integer> getThemeIds(Integer tourId);

    /**
     * 查询跟团游适合人群关联
     * @param tourId 跟团游ID
     * @return 适合人群ID列表
     */
    @Select("SELECT suitable_id FROM group_tour_suitable_relation WHERE group_tour_id = #{tourId}")
    List<Integer> getSuitableIds(Integer tourId);

    /**
     * 查询团队游关联的一日游
     * @param groupTourId 团队游ID
     * @return 关联的一日游列表
     */
    @Select("SELECT r.id, r.day_tour_id, r.day_number, r.is_optional, " +
            "dt.name as day_tour_name, dt.location, dt.price, dt.duration " +
            "FROM group_tour_day_tour_relation r " +
            "JOIN day_tours dt ON r.day_tour_id = dt.day_tour_id " +
            "WHERE r.group_tour_id = #{groupTourId} " +
            "ORDER BY r.day_number, r.id")
    List<Map<String, Object>> getGroupTourDayTours(Integer groupTourId);

    /**
     * 删除团队游关联的一日游
     * @param groupTourId 团队游ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM group_tour_day_tour_relation WHERE group_tour_id = #{groupTourId}")
    int deleteGroupTourDayTours(Integer groupTourId);

    /**
     * 保存团队游关联的一日游
     * @param groupTourId 团队游ID
     * @param dayTourId 一日游ID
     * @param dayNumber 天数
     * @param isOptional 是否可选
     * @return 影响的行数
     */
    @Insert("INSERT INTO group_tour_day_tour_relation (group_tour_id, day_tour_id, day_number, is_optional) " +
            "VALUES (#{groupTourId}, #{dayTourId}, #{dayNumber}, #{isOptional})")
    int saveGroupTourDayTour(@Param("groupTourId") Integer groupTourId, 
                            @Param("dayTourId") Integer dayTourId,
                            @Param("dayNumber") Integer dayNumber,
                            @Param("isOptional") Integer isOptional);

    /**
     * 更新跟团游信息
     * @param groupTourDTO 跟团游信息
     */
    void update(GroupTourDTO groupTourDTO);

    /**
     * 删除跟团游关联的主题
     * @param tourId 跟团游ID
     */
    @Delete("DELETE FROM group_tour_theme_relation WHERE group_tour_id = #{tourId}")
    void deleteThemesByTourId(Integer tourId);

    /**
     * 添加跟团游主题关联
     * @param tourId 跟团游ID
     * @param themeId 主题ID
     */
    @Insert("INSERT INTO group_tour_theme_relation (group_tour_id, theme_id) VALUES (#{tourId}, #{themeId})")
    void insertTourTheme(@Param("tourId") Integer tourId, @Param("themeId") Integer themeId);

    /**
     * 删除跟团游亮点
     * @param tourId 跟团游ID
     */
    @Delete("DELETE FROM tour_highlights WHERE group_tour_id = #{tourId}")
    void deleteHighlights(Integer tourId);

    /**
     * 添加跟团游亮点
     * @param tourId 跟团游ID
     * @param highlight 亮点描述
     */
    @Insert("INSERT INTO tour_highlights (group_tour_id, description) VALUES (#{tourId}, #{highlight})")
    void insertHighlight(@Param("tourId") Integer tourId, @Param("highlight") String highlight);

    /**
     * 删除跟团游包含项目
     * @param tourId 跟团游ID
     */
    @Delete("DELETE FROM tour_inclusions WHERE group_tour_id = #{tourId}")
    void deleteInclusions(Integer tourId);

    /**
     * 添加跟团游包含项目
     * @param tourId 跟团游ID
     * @param inclusion 包含项目描述
     */
    @Insert("INSERT INTO tour_inclusions (group_tour_id, description) VALUES (#{tourId}, #{inclusion})")
    void insertInclusion(@Param("tourId") Integer tourId, @Param("inclusion") String inclusion);

    /**
     * 删除跟团游不包含项目
     * @param tourId 跟团游ID
     */
    @Delete("DELETE FROM tour_exclusions WHERE group_tour_id = #{tourId}")
    void deleteExclusions(Integer tourId);

    /**
     * 添加跟团游不包含项目
     * @param tourId 跟团游ID
     * @param exclusion 不包含项目描述
     */
    @Insert("INSERT INTO tour_exclusions (group_tour_id, description) VALUES (#{tourId}, #{exclusion})")
    void insertExclusion(@Param("tourId") Integer tourId, @Param("exclusion") String exclusion);

    /**
     * 删除跟团游贴士
     * @param tourId 跟团游ID
     */
    @Delete("DELETE FROM tour_tips WHERE group_tour_id = #{tourId}")
    void deleteTips(Integer tourId);

    /**
     * 添加跟团游贴士
     * @param tourId 跟团游ID
     * @param tip 贴士描述
     */
    @Insert("INSERT INTO tour_tips (group_tour_id, description) VALUES (#{tourId}, #{tip})")
    void insertTip(@Param("tourId") Integer tourId, @Param("tip") String tip);

    /**
     * 删除跟团游常见问题
     * @param tourId 跟团游ID
     */
    @Delete("DELETE FROM tour_faqs WHERE group_tour_id = #{tourId}")
    void deleteFaqs(Integer tourId);

    /**
     * 添加跟团游常见问题
     * @param tourId 跟团游ID
     * @param question 问题
     * @param answer 答案
     */
    @Insert("INSERT INTO tour_faqs (group_tour_id, question, answer) VALUES (#{tourId}, #{question}, #{answer})")
    void insertFaq(@Param("tourId") Integer tourId, 
                  @Param("question") String question, 
                  @Param("answer") String answer);

    /**
     * 删除跟团游行程安排
     * @param tourId 跟团游ID
     */
    @Delete("DELETE FROM tour_itinerary WHERE group_tour_id = #{tourId}")
    void deleteItinerary(Integer tourId);

    /**
     * 添加跟团游行程安排
     * @param tourId 跟团游ID
     * @param dayNumber 天数
     * @param title 标题
     * @param description 描述
     * @param meals 餐食
     * @param accommodation 住宿
     */
    @Insert("INSERT INTO tour_itinerary (group_tour_id, day_number, title, description, meals, accommodation) " +
            "VALUES (#{tourId}, #{dayNumber}, #{title}, #{description}, #{meals}, #{accommodation})")
    void insertItinerary(@Param("tourId") Integer tourId, 
                        @Param("dayNumber") Integer dayNumber,
                        @Param("title") String title, 
                        @Param("description") String description,
                        @Param("meals") String meals, 
                        @Param("accommodation") String accommodation);

    /**
     * 插入新的团队游基本信息
     * @param groupTourDTO 团队游信息
     * @return 插入后的团队游ID
     */
    @Insert("INSERT INTO group_tours (title, short_title, description, price, discounted_price, " +
            "duration, days, nights, image_url, is_active, location, category, departure_address) " +
            "VALUES (#{name}, #{shortTitle}, #{description}, #{price}, #{discountedPrice}, " +
            "#{duration}, #{days}, #{nights}, #{coverImage}, 1, #{location}, #{category}, #{departureAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "group_tour_id")
    Integer insert(GroupTourDTO groupTourDTO);

    /**
     * 更新团队游状态（上架/下架）
     * @param id 团队游ID
     * @param status 状态（0-下架，1-上架）
     */
    @Update("UPDATE group_tours SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    
    /**
     * 根据ID删除团队游
     * @param id 团队游ID
     */
    @Delete("DELETE FROM group_tours WHERE id = #{id}")
    void deleteById(Integer id);
    
    /**
     * 删除团队游相关的图片
     * @param tourId 团队游ID
     */
    @Delete("DELETE FROM group_tour_images WHERE group_tour_id = #{tourId}")
    void deleteImages(Integer tourId);
} 