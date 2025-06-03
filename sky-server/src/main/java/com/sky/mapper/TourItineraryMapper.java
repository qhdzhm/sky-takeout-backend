package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 旅游行程Mapper接口
 */
@Mapper
public interface TourItineraryMapper {

    /**
     * 根据团队游ID获取行程
     * @param tourId 团队游ID
     * @return 行程列表
     */
    @Select("SELECT * FROM tour_itinerary WHERE group_tour_id = #{tourId} ORDER BY day_number")
    List<Map<String, Object>> getGroupTourItinerary(Integer tourId);

    /**
     * 根据一日游ID获取行程
     * @param tourId 一日游ID
     * @return 行程列表
     */
    @Select("SELECT * FROM day_tour_itinerary WHERE day_tour_id = #{tourId} ORDER BY display_order")
    List<Map<String, Object>> getDayTourItinerary(Integer tourId);

    /**
     * 根据产品ID和类型获取行程
     * @param tourId 产品ID
     * @param tourType 产品类型
     * @return 行程列表
     */
    default List<Map<String, Object>> getItineraryByTourId(@Param("tourId") Integer tourId, @Param("tourType") String tourType) {
        if (tourType != null && tourType.equalsIgnoreCase("group_tour")) {
            return getGroupTourItinerary(tourId);
        } else {
            return getDayTourItinerary(tourId);
        }
    }
} 