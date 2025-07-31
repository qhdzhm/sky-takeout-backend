package com.sky.service.impl;

import com.sky.dto.ItineraryOptionDTO;
import com.sky.dto.ItineraryOptionGroupDTO;
import com.sky.dto.SaveItineraryChoicesDTO;
import com.sky.entity.BookingItineraryChoice;
import com.sky.entity.GroupTourDayTourRelation;
import com.sky.mapper.BookingItineraryChoiceMapper;
import com.sky.mapper.GroupTourDayTourRelationMapper;
import com.sky.service.ItineraryOptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行程选项Service实现类
 */
@Service
@Slf4j
public class ItineraryOptionServiceImpl implements ItineraryOptionService {

    @Autowired
    private GroupTourDayTourRelationMapper groupTourDayTourRelationMapper;

    @Autowired
    private BookingItineraryChoiceMapper bookingItineraryChoiceMapper;

    @Override
    public List<ItineraryOptionGroupDTO> getItineraryOptionGroups(Integer groupTourId) {
        log.info("获取跟团游{}的所有行程选项组", groupTourId);
        
        List<GroupTourDayTourRelation> relations = groupTourDayTourRelationMapper.getByGroupTourId(groupTourId);
        
        // 按天数和选项组名称分组
        Map<String, List<GroupTourDayTourRelation>> groupedByDay = relations.stream()
                .collect(Collectors.groupingBy(r -> r.getDayNumber() + "_" + (r.getOptionGroupName() != null ? r.getOptionGroupName() : "default")));
        
        List<ItineraryOptionGroupDTO> result = new ArrayList<>();
        
        for (Map.Entry<String, List<GroupTourDayTourRelation>> entry : groupedByDay.entrySet()) {
            List<GroupTourDayTourRelation> groupRelations = entry.getValue();
            GroupTourDayTourRelation firstRelation = groupRelations.get(0);
            
            // 构建选项列表
            List<ItineraryOptionDTO> options = groupRelations.stream()
                    .map(this::convertToOptionDTO)
                    .collect(Collectors.toList());
            
            ItineraryOptionGroupDTO groupDTO = ItineraryOptionGroupDTO.builder()
                    .groupTourId(groupTourId)
                    .dayNumber(firstRelation.getDayNumber())
                    .optionGroupName(firstRelation.getOptionGroupName() != null ? firstRelation.getOptionGroupName() : "第" + firstRelation.getDayNumber() + "天行程")
                    .required(firstRelation.getIsOptional() == 0)
                    .options(options)
                    .build();
            
            result.add(groupDTO);
        }
        
        return result;
    }

    @Override
    public List<ItineraryOptionGroupDTO> getOptionalItineraryGroups(Integer groupTourId) {
        log.info("获取跟团游{}的可选行程选项组", groupTourId);
        
        List<GroupTourDayTourRelation> relations = groupTourDayTourRelationMapper.getOptionalByGroupTourId(groupTourId);
        
        // 按天数和选项组名称分组
        Map<String, List<GroupTourDayTourRelation>> groupedByOptionGroup = relations.stream()
                .collect(Collectors.groupingBy(r -> r.getDayNumber() + "_" + (r.getOptionGroupName() != null ? r.getOptionGroupName() : "default")));
        
        List<ItineraryOptionGroupDTO> result = new ArrayList<>();
        
        for (Map.Entry<String, List<GroupTourDayTourRelation>> entry : groupedByOptionGroup.entrySet()) {
            List<GroupTourDayTourRelation> groupRelations = entry.getValue();
            GroupTourDayTourRelation firstRelation = groupRelations.get(0);
            
            // 构建选项列表
            List<ItineraryOptionDTO> options = groupRelations.stream()
                    .map(this::convertToOptionDTO)
                    .collect(Collectors.toList());
            
            ItineraryOptionGroupDTO groupDTO = ItineraryOptionGroupDTO.builder()
                    .groupTourId(groupTourId)
                    .dayNumber(firstRelation.getDayNumber())
                    .optionGroupName(firstRelation.getOptionGroupName() != null ? firstRelation.getOptionGroupName() : "第" + firstRelation.getDayNumber() + "天选择")
                    .required(false)
                    .options(options)
                    .build();
            
            result.add(groupDTO);
        }
        
        return result;
    }

    @Override
    public List<GroupTourDayTourRelation> getOptionalByGroupTourIdAndDay(Integer groupTourId, Integer dayNumber) {
        return groupTourDayTourRelationMapper.getOptionalByGroupTourIdAndDay(groupTourId, dayNumber);
    }

    @Override
    @Transactional
    public boolean updateOptionalInfo(GroupTourDayTourRelation relation) {
        log.info("更新关联记录{}的可选项信息", relation.getId());
        return groupTourDayTourRelationMapper.updateOptionalInfo(relation) > 0;
    }

    @Override
    @Transactional
    public boolean batchUpdateOptionalInfo(List<GroupTourDayTourRelation> relations) {
        log.info("批量更新{}条关联记录的可选项信息", relations.size());
        return groupTourDayTourRelationMapper.batchUpdateOptionalInfo(relations) > 0;
    }

    @Override
    @Transactional
    public boolean saveItineraryChoices(SaveItineraryChoicesDTO saveDTO) {
        log.info("保存预订{}的行程选择", saveDTO.getBookingId());
        
        // 先删除已有的选择
        bookingItineraryChoiceMapper.deleteByBookingId(saveDTO.getBookingId());
        
        // 保存新的选择
        List<BookingItineraryChoice> choices = saveDTO.getChoices().stream()
                .map(choiceDTO -> BookingItineraryChoice.builder()
                        .bookingId(saveDTO.getBookingId())
                        .dayNumber(choiceDTO.getDayNumber())
                        .selectedDayTourId(choiceDTO.getSelectedDayTourId())
                        .optionGroupName(choiceDTO.getOptionGroupName())
                        .build())
                .collect(Collectors.toList());
        
        if (!choices.isEmpty()) {
            return bookingItineraryChoiceMapper.batchInsert(choices) > 0;
        }
        
        return true;
    }

    @Override
    public List<BookingItineraryChoice> getBookingItineraryChoices(Integer bookingId) {
        log.info("获取预订{}的行程选择", bookingId);
        return bookingItineraryChoiceMapper.getByBookingId(bookingId);
    }

    @Override
    @Transactional
    public boolean updateItineraryChoices(Integer bookingId, List<SaveItineraryChoicesDTO.ItineraryChoiceItemDTO> choices) {
        log.info("更新预订{}的行程选择", bookingId);
        
        // 先删除已有的选择
        bookingItineraryChoiceMapper.deleteByBookingId(bookingId);
        
        // 保存新的选择
        SaveItineraryChoicesDTO saveDTO = SaveItineraryChoicesDTO.builder()
                .bookingId(bookingId)
                .choices(choices)
                .build();
        
        return saveItineraryChoices(saveDTO);
    }

    @Override
    @Transactional
    public boolean deleteItineraryChoice(Integer bookingId, Integer dayNumber) {
        log.info("删除预订{}第{}天的行程选择", bookingId, dayNumber);
        return bookingItineraryChoiceMapper.deleteByBookingIdAndDay(bookingId, dayNumber) > 0;
    }

    @Override
    public BigDecimal calculateItineraryPriceImpact(Integer groupTourId, List<SaveItineraryChoicesDTO.ItineraryChoiceItemDTO> choices, BigDecimal basePrice) {
        log.info("计算跟团游{}的行程选择价格影响", groupTourId);
        
        BigDecimal totalPriceImpact = BigDecimal.ZERO;
        
        for (SaveItineraryChoicesDTO.ItineraryChoiceItemDTO choice : choices) {
            // 获取选择的一日游关联信息
            List<GroupTourDayTourRelation> relations = groupTourDayTourRelationMapper.getOptionalByGroupTourIdAndDay(groupTourId, choice.getDayNumber());
            
            for (GroupTourDayTourRelation relation : relations) {
                if (relation.getDayTourId().equals(choice.getSelectedDayTourId())) {
                    if (relation.getPriceDifference() != null) {
                        totalPriceImpact = totalPriceImpact.add(relation.getPriceDifference());
                    }
                    break;
                }
            }
        }
        
        return basePrice.add(totalPriceImpact);
    }

    @Override
    public List<GroupTourDayTourRelation> getDefaultItinerary(Integer groupTourId) {
        log.info("获取跟团游{}的默认行程", groupTourId);
        return groupTourDayTourRelationMapper.getDefaultItinerary(groupTourId);
    }

    @Override
    public List<GroupTourDayTourRelation> getRequiredItinerary(Integer groupTourId) {
        log.info("获取跟团游{}的必选行程", groupTourId);
        return groupTourDayTourRelationMapper.getRequiredByGroupTourId(groupTourId);
    }

    @Transactional
    public boolean saveItineraryOptions(Integer groupTourId, List<ItineraryOptionGroupDTO> optionGroups) {
        log.info("保存可选行程配置，groupTourId: {}, optionGroups: {}", groupTourId, optionGroups);
        
        try {
            for (ItineraryOptionGroupDTO group : optionGroups) {
                Integer dayNumber = group.getDayNumber();
                String optionGroupName = group.getOptionGroupName();
                
                // 先删除该天该组的现有配置
                groupTourDayTourRelationMapper.deleteOptionalByGroupTourIdAndDayAndGroup(
                    groupTourId, dayNumber, optionGroupName);
                
                // 保存新的选项
                for (ItineraryOptionDTO option : group.getOptions()) {
                    GroupTourDayTourRelation relation = new GroupTourDayTourRelation();
                    relation.setGroupTourId(groupTourId);
                    relation.setDayTourId(option.getDayTourId());
                    relation.setDayNumber(dayNumber);
                    relation.setIsOptional(1); // 标记为可选
                    relation.setOptionGroupName(optionGroupName);
                    relation.setPriceDifference(option.getPriceDifference());
                    relation.setIsDefault(option.getIsDefault() ? 1 : 0);
                    
                    groupTourDayTourRelationMapper.insert(relation);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("保存可选行程配置失败", e);
            return false;
        }
    }

    @Transactional
    public boolean deleteItineraryOptions(Integer groupTourId, Integer dayNumber, String optionGroupName) {
        log.info("删除可选行程配置，groupTourId: {}, dayNumber: {}, optionGroupName: {}", 
                groupTourId, dayNumber, optionGroupName);
        
        try {
            if (optionGroupName != null) {
                // 删除指定组的配置
                groupTourDayTourRelationMapper.deleteOptionalByGroupTourIdAndDayAndGroup(
                    groupTourId, dayNumber, optionGroupName);
            } else {
                // 删除该天所有可选配置
                groupTourDayTourRelationMapper.deleteOptionalByGroupTourIdAndDay(
                    groupTourId, dayNumber);
            }
            return true;
        } catch (Exception e) {
            log.error("删除可选行程配置失败", e);
            return false;
        }
    }

    /**
     * 将关联实体转换为选项DTO
     */
    private ItineraryOptionDTO convertToOptionDTO(GroupTourDayTourRelation relation) {
        return ItineraryOptionDTO.builder()
                .id(relation.getId())
                .dayTourId(relation.getDayTourId())
                .dayTourName(relation.getDayTourName())
                .dayTourDescription(relation.getDayTourDescription())
                .dayTourLocation(relation.getDayTourLocation())
                .dayTourDuration(relation.getDayTourDuration())
                .dayTourPrice(relation.getDayTourPrice())
                .priceDifference(relation.getPriceDifference())
                .isDefault(relation.getIsDefault() == 1)
                .dayTourImage(relation.getDayTourImage())
                .optionGroupName(relation.getOptionGroupName())
                .build();
    }
} 