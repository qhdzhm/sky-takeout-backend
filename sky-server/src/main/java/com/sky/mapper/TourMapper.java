package com.sky.mapper;

import com.sky.dto.TourDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 通用旅游数据访问层
 */
@Mapper
public interface TourMapper {

    /**
     * 分页查询旅游产品
     * @param keyword 关键词
     * @param location 地点
     * @param category 类别
     * @param regionId 地区ID
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param tourType 旅游类型
     * @return 旅游产品列表
     */
    List<TourDTO> pageQuery(@Param("keyword") String keyword,
                           @Param("location") String location,
                           @Param("category") String category,
                           @Param("regionId") Integer regionId,
                           @Param("minPrice") Double minPrice,
                           @Param("maxPrice") Double maxPrice,
                           @Param("tourType") String tourType);

    /**
     * 根据ID查询旅游产品
     * @param id 旅游产品ID
     * @param tourType 旅游类型
     * @return 旅游产品信息
     */
    TourDTO getById(@Param("id") Integer id, @Param("tourType") String tourType);

    /**
     * 搜索旅游产品
     * @param keyword 关键词
     * @param location 地点
     * @param category 类别
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param tourType 旅游类型
     * @return 旅游产品列表
     */
    List<TourDTO> search(@Param("keyword") String keyword,
                        @Param("location") String location,
                        @Param("category") String category,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        @Param("tourType") String tourType);

    /**
     * 查询热门一日游
     * @param limit 限制数量
     * @return 热门一日游列表
     */
    List<TourDTO> getHotDayTours(Integer limit);

    /**
     * 查询热门跟团游
     * @param limit 限制数量
     * @return 热门跟团游列表
     */
    List<TourDTO> getHotGroupTours(Integer limit);

    /**
     * 查询推荐一日游
     * @param limit 限制数量
     * @return 推荐一日游列表
     */
    List<TourDTO> getRecommendedDayTours(Integer limit);

    /**
     * 查询推荐跟团游
     * @param limit 限制数量
     * @return 推荐跟团游列表
     */
    List<TourDTO> getRecommendedGroupTours(Integer limit);

    /**
     * 获取适合人群选项
     * @return 适合人群选项列表
     */
    @Select("SELECT suitable_id as id, name FROM suitable_for ORDER BY suitable_id")
    List<Map<String, Object>> getSuitableOptions();

    /**
     * 获取旅游产品适合人群
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型（day_tour/group_tour）
     * @return 适合人群列表
     */
    @Select({
        "<script>",
        "SELECT sf.suitable_id as id, sf.name FROM ",
        "<if test='tourType == \"day_tour\"'>",
        "  day_tour_suitable_relation dsr JOIN suitable_for sf ON dsr.suitable_id = sf.suitable_id ",
        "  WHERE dsr.day_tour_id = #{tourId}",
        "</if>",
        "<if test='tourType == \"group_tour\"'>",
        "  group_tour_suitable_relation gsr JOIN suitable_for sf ON gsr.suitable_id = sf.suitable_id ",
        "  WHERE gsr.group_tour_id = #{tourId}",
        "</if>",
        "</script>"
    })
    List<Map<String, Object>> getSuitableForByTourId(@Param("tourId") Integer tourId, @Param("tourType") String tourType);

    /**
     * 删除旅游产品适合人群关联
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型（day_tour/group_tour）
     */
    @Delete({
        "<script>",
        "<if test='tourType == \"day_tour\"'>",
        "  DELETE FROM day_tour_suitable_relation WHERE day_tour_id = #{tourId}",
        "</if>",
        "<if test='tourType == \"group_tour\"'>",
        "  DELETE FROM group_tour_suitable_relation WHERE group_tour_id = #{tourId}",
        "</if>",
        "</script>"
    })
    void deleteSuitableByTourId(@Param("tourId") Integer tourId, @Param("tourType") String tourType);

    /**
     * 添加旅游产品适合人群关联
     * @param tourId 旅游产品ID
     * @param suitableId 适合人群ID
     * @param tourType 旅游产品类型（day_tour/group_tour）
     */
    @Insert({
        "<script>",
        "<if test='tourType == \"day_tour\"'>",
        "  INSERT INTO day_tour_suitable_relation(day_tour_id, suitable_id) VALUES(#{tourId}, #{suitableId})",
        "</if>",
        "<if test='tourType == \"group_tour\"'>",
        "  INSERT INTO group_tour_suitable_relation(group_tour_id, suitable_id) VALUES(#{tourId}, #{suitableId})",
        "</if>",
        "</script>"
    })
    void insertTourSuitable(@Param("tourId") Integer tourId, @Param("suitableId") Integer suitableId, @Param("tourType") String tourType);

    /**
     * 获取基于订单统计的热门产品（近N天）
     * @param days 统计天数
     * @param limit 限制数量
     * @return 热门产品列表（包含一日游和多日游）
     */
    List<Map<String, Object>> getPopularToursByOrders(@Param("days") Integer days, @Param("limit") Integer limit);
} 