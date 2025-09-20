package com.sky.service.impl;

import com.sky.entity.TicketType;
import com.sky.mapper.TicketTypeMapper;
import com.sky.service.TicketTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 票务类型服务实现类 - 对标酒店房型服务实现类
 */
@Service
@Slf4j
public class TicketTypeServiceImpl implements TicketTypeService {

    @Autowired
    private TicketTypeMapper ticketTypeMapper;

    @Override
    public List<TicketType> getTicketTypesByAttractionId(Long attractionId) {
        log.info("根据景点ID获取票务类型列表：{}", attractionId);
        return ticketTypeMapper.getByAttractionId(attractionId);
    }

    @Override
    public List<TicketType> getActiveTicketTypesByAttractionId(Long attractionId) {
        log.info("根据景点ID获取活跃票务类型列表：{}", attractionId);
        return ticketTypeMapper.getActiveByAttractionId(attractionId);
    }

    @Override
    public List<TicketType> getAllActiveTicketTypes() {
        log.info("获取所有活跃票务类型");
        return ticketTypeMapper.getAllActive();
    }

    @Override
    public TicketType getTicketTypeById(Long id) {
        log.info("根据ID获取票务类型详情：{}", id);
        return ticketTypeMapper.getById(id);
    }

    @Override
    public TicketType getTicketTypeByCode(String ticketCode) {
        log.info("根据票务代码获取票务类型：{}", ticketCode);
        return ticketTypeMapper.getByTicketCode(ticketCode);
    }

    @Override
    public void createTicketType(TicketType ticketType) {
        log.info("创建票务类型：{}", ticketType);
        ticketType.setCreatedAt(LocalDateTime.now());
        ticketType.setUpdatedAt(LocalDateTime.now());
        
        // 如果status为空，设置为active
        if (ticketType.getStatus() == null || ticketType.getStatus().trim().isEmpty()) {
            ticketType.setStatus("active");
        }
        
        ticketTypeMapper.insert(ticketType);
    }

    @Override
    public void updateTicketType(TicketType ticketType) {
        log.info("更新票务类型：{}", ticketType);
        ticketType.setUpdatedAt(LocalDateTime.now());
        
        // 如果status为空，设置为active
        if (ticketType.getStatus() == null || ticketType.getStatus().trim().isEmpty()) {
            ticketType.setStatus("active");
        }
        
        ticketTypeMapper.update(ticketType);
    }

    @Override
    public void deleteTicketType(Long id) {
        log.info("删除票务类型：{}", id);
        ticketTypeMapper.deleteById(id);
    }

    @Override
    public void updateTicketTypeStatus(Long id, String status) {
        log.info("更新票务类型状态：id={}, status={}", id, status);
        ticketTypeMapper.updateStatus(id, status);
    }

    @Override
    public void deleteTicketTypesByAttractionId(Long attractionId) {
        log.info("根据景点ID删除所有票务类型：{}", attractionId);
        ticketTypeMapper.deleteByAttractionId(attractionId);
    }

    @Override
    public void batchCreateTicketTypes(List<TicketType> ticketTypes) {
        log.info("批量创建票务类型，数量：{}", ticketTypes.size());
        
        // 设置创建时间和状态
        LocalDateTime now = LocalDateTime.now();
        for (TicketType ticketType : ticketTypes) {
            ticketType.setCreatedAt(now);
            ticketType.setUpdatedAt(now);
            if (ticketType.getStatus() == null || ticketType.getStatus().trim().isEmpty()) {
                ticketType.setStatus("active");
            }
        }
        
        ticketTypeMapper.batchInsert(ticketTypes);
    }

    @Override
    public List<TicketType> searchTicketTypes(Long attractionId, String ticketType, String status) {
        log.info("根据条件搜索票务类型：attractionId={}, ticketType={}, status={}", 
                attractionId, ticketType, status);
        return ticketTypeMapper.searchTicketTypes(attractionId, ticketType, status);
    }
}

