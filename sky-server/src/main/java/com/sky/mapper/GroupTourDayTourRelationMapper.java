package com.sky.mapper;

import com.sky.dto.ItineraryOptionGroupDTO;
import com.sky.entity.GroupTourDayTourRelation;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 跟团游一日游关联Mapper
 */
@Mapper
public interface GroupTourDayTourRelationMapper {

    /**
     * 根据跟团游ID获取所有关联的一日游
     */
    @Select("SELECT gdr.*, dt.name as day_tour_name, dt.description as day_tour_description, " +
            "dt.price as day_tour_price, dt.location as day_tour_location, " +
            "dt.duration as day_tour_duration, dt.image_url as day_tour_image " +
            "FROM group_tour_day_tour_relation gdr " +
            "LEFT JOIN day_tours dt ON gdr.day_tour_id = dt.day_tour_id " +
            "WHERE gdr.group_tour_id = #{groupTourId} " +
            "ORDER BY gdr.day_number, gdr.is_optional, gdr.is_default DESC")
    List<GroupTourDayTourRelation> getByGroupTourId(Integer groupTourId);

    /**
     * 根据跟团游ID获取可选行程选项组
     */
    @Select("SELECT gdr.*, dt.name as day_tour_name, dt.description as day_tour_description, " +
            "dt.price as day_tour_price, dt.location as day_tour_location, " +
            "dt.duration as day_tour_duration, dt.image_url as day_tour_image " +
            "FROM group_tour_day_tour_relation gdr " +
            "LEFT JOIN day_tours dt ON gdr.day_tour_id = dt.day_tour_id " +
            "WHERE gdr.group_tour_id = #{groupTourId} AND gdr.is_optional = 1 " +
            "ORDER BY gdr.day_number, gdr.option_group_name, gdr.is_default DESC")
    List<GroupTourDayTourRelation> getOptionalByGroupTourId(Integer groupTourId);

    /**
     * 根据跟团游ID和天数获取可选项
     */
    @Select("SELECT gdr.*, dt.name as day_tour_name, dt.description as day_tour_description, " +
            "dt.price as day_tour_price, dt.location as day_tour_location, " +
            "dt.duration as day_tour_duration, dt.image_url as day_tour_image " +
            "FROM group_tour_day_tour_relation gdr " +
            "LEFT JOIN day_tours dt ON gdr.day_tour_id = dt.day_tour_id " +
            "WHERE gdr.group_tour_id = #{groupTourId} AND gdr.day_number = #{dayNumber} AND gdr.is_optional = 1 " +
            "ORDER BY gdr.is_default DESC")
    List<GroupTourDayTourRelation> getOptionalByGroupTourIdAndDay(Integer groupTourId, Integer dayNumber);

    /**
     * 更新关联记录的可选项信息
     */
    @Update("UPDATE group_tour_day_tour_relation SET " +
            "option_group_name = #{optionGroupName}, " +
            "price_difference = #{priceDifference}, " +
            "is_default = #{isDefault}, " +
            "update_time = NOW() " +
            "WHERE id = #{id}")
    int updateOptionalInfo(GroupTourDayTourRelation relation);

    /**
     * 批量更新关联记录的可选项信息
     */
    int batchUpdateOptionalInfo(List<GroupTourDayTourRelation> relations);

    /**
     * 根据ID获取关联记录
     */
    @Select("SELECT gdr.*, dt.name as day_tour_name, dt.description as day_tour_description, " +
            "dt.price as day_tour_price, dt.location as day_tour_location, " +
            "dt.duration as day_tour_duration, dt.image_url as day_tour_image " +
            "FROM group_tour_day_tour_relation gdr " +
            "LEFT JOIN day_tours dt ON gdr.day_tour_id = dt.day_tour_id " +
            "WHERE gdr.id = #{id}")
    GroupTourDayTourRelation getById(Integer id);

    /**
     * 根据跟团游ID获取必选行程
     */
    @Select("SELECT gdr.*, dt.name as day_tour_name, dt.description as day_tour_description, " +
            "dt.price as day_tour_price, dt.location as day_tour_location, " +
            "dt.duration as day_tour_duration, dt.image_url as day_tour_image " +
            "FROM group_tour_day_tour_relation gdr " +
            "LEFT JOIN day_tours dt ON gdr.day_tour_id = dt.day_tour_id " +
            "WHERE gdr.group_tour_id = #{groupTourId} AND gdr.is_optional = 0 " +
            "ORDER BY gdr.day_number")
    List<GroupTourDayTourRelation> getRequiredByGroupTourId(Integer groupTourId);

    /**
     * 根据跟团游ID获取默认行程（必选 + 可选的默认项）
     */
    @Select("SELECT gdr.*, dt.name as day_tour_name, dt.description as day_tour_description, " +
            "dt.price as day_tour_price, dt.location as day_tour_location, " +
            "dt.duration as day_tour_duration, dt.image_url as day_tour_image " +
            "FROM group_tour_day_tour_relation gdr " +
            "LEFT JOIN day_tours dt ON gdr.day_tour_id = dt.day_tour_id " +
            "WHERE gdr.group_tour_id = #{groupTourId} " +
            "AND (gdr.is_optional = 0 OR (gdr.is_optional = 1 AND gdr.is_default = 1)) " +
            "ORDER BY gdr.day_number")
    List<GroupTourDayTourRelation> getDefaultItinerary(Integer groupTourId);

    /**
     * 插入新的关联记录
     */
    @Insert("INSERT INTO group_tour_day_tour_relation " +
            "(group_tour_id, day_tour_id, day_number, is_optional, option_group_name, price_difference, is_default, create_time, update_time) " +
            "VALUES (#{groupTourId}, #{dayTourId}, #{dayNumber}, #{isOptional}, #{optionGroupName}, #{priceDifference}, #{isDefault}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GroupTourDayTourRelation relation);

    /**
     * 删除指定跟团游、天数和选项组的可选配置
     */
    @Delete("DELETE FROM group_tour_day_tour_relation " +
            "WHERE group_tour_id = #{groupTourId} AND day_number = #{dayNumber} " +
            "AND is_optional = 1 AND option_group_name = #{optionGroupName}")
    int deleteOptionalByGroupTourIdAndDayAndGroup(@Param("groupTourId") Integer groupTourId, 
                                                  @Param("dayNumber") Integer dayNumber, 
                                                  @Param("optionGroupName") String optionGroupName);

    /**
     * 删除指定跟团游和天数的所有可选配置
     */
    @Delete("DELETE FROM group_tour_day_tour_relation " +
            "WHERE group_tour_id = #{groupTourId} AND day_number = #{dayNumber} AND is_optional = 1")
    int deleteOptionalByGroupTourIdAndDay(@Param("groupTourId") Integer groupTourId, 
                                          @Param("dayNumber") Integer dayNumber);

    /**
     * 根据跟团游ID、一日游ID和天数获取价格差异
     */
    @Select("SELECT price_difference FROM group_tour_day_tour_relation " +
            "WHERE group_tour_id = #{groupTourId} AND day_tour_id = #{dayTourId} AND day_number = #{dayNumber}")
    BigDecimal getPriceDifferenceByTourAndDay(@Param("groupTourId") Integer groupTourId, 
                                              @Param("dayTourId") Integer dayTourId, 
                                              @Param("dayNumber") Integer dayNumber);
} 