package com.sky.service;

import com.sky.dto.ItineraryOptionGroupDTO;
import com.sky.dto.SaveItineraryChoicesDTO;
import com.sky.entity.BookingItineraryChoice;
import com.sky.entity.GroupTourDayTourRelation;

import java.math.BigDecimal;
import java.util.List;

/**
 * 行程选项Service接口
 */
public interface ItineraryOptionService {

    /**
     * 根据跟团游ID获取所有行程选项组
     */
    List<ItineraryOptionGroupDTO> getItineraryOptionGroups(Integer groupTourId);

    /**
     * 根据跟团游ID获取可选行程选项组
     */
    List<ItineraryOptionGroupDTO> getOptionalItineraryGroups(Integer groupTourId);

    /**
     * 根据跟团游ID和天数获取可选项
     */
    List<GroupTourDayTourRelation> getOptionalByGroupTourIdAndDay(Integer groupTourId, Integer dayNumber);

    /**
     * 更新关联记录的可选项信息
     */
    boolean updateOptionalInfo(GroupTourDayTourRelation relation);

    /**
     * 批量更新关联记录的可选项信息
     */
    boolean batchUpdateOptionalInfo(List<GroupTourDayTourRelation> relations);

    /**
     * 保存用户的行程选择
     */
    boolean saveItineraryChoices(SaveItineraryChoicesDTO saveDTO);

    /**
     * 获取预订的行程选择
     */
    List<BookingItineraryChoice> getBookingItineraryChoices(Integer bookingId);

    /**
     * 更新行程选择
     */
    boolean updateItineraryChoices(Integer bookingId, List<SaveItineraryChoicesDTO.ItineraryChoiceItemDTO> choices);

    /**
     * 删除行程选择
     */
    boolean deleteItineraryChoice(Integer bookingId, Integer dayNumber);

    /**
     * 计算行程选择的价格影响
     */
    BigDecimal calculateItineraryPriceImpact(Integer groupTourId, List<SaveItineraryChoicesDTO.ItineraryChoiceItemDTO> choices, BigDecimal basePrice);

    /**
     * 获取跟团游的默认行程（必选 + 可选的默认项）
     */
    List<GroupTourDayTourRelation> getDefaultItinerary(Integer groupTourId);

    /**
     * 获取跟团游的必选行程
     */
    List<GroupTourDayTourRelation> getRequiredItinerary(Integer groupTourId);
}