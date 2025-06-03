package com.sky.service.impl;

import com.sky.dto.TourScheduleBatchSaveDTO;
import com.sky.dto.TourScheduleOrderDTO;
import com.sky.entity.TourScheduleOrder;
import com.sky.mapper.TourScheduleOrderMapper;
import com.sky.service.TourScheduleOrderService;
import com.sky.vo.TourScheduleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 行程排序业务实现类
 */
@Service
@Slf4j
public class TourScheduleOrderServiceImpl implements TourScheduleOrderService {

    @Autowired
    private TourScheduleOrderMapper tourScheduleOrderMapper;

    /**
     * 通过订单ID获取行程排序
     * @param bookingId 订单ID
     * @return 行程排序视图对象列表
     */
    @Override
    public List<TourScheduleVO> getSchedulesByBookingId(Integer bookingId) {
        log.info("通过订单ID获取行程排序: {}", bookingId);
        
        List<TourScheduleOrder> scheduleOrders = tourScheduleOrderMapper.getByBookingId(bookingId);
        
        return scheduleOrders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 通过日期范围获取行程排序
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 行程排序视图对象列表
     */
    @Override
    public List<TourScheduleVO> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("通过日期范围获取行程排序: {} - {}", startDate, endDate);
        
        List<TourScheduleOrder> scheduleOrders = tourScheduleOrderMapper.getByDateRange(startDate, endDate);
        
        return scheduleOrders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 保存单个行程排序
     * @param tourScheduleOrderDTO 行程排序DTO
     * @return 保存结果
     */
    @Override
    @Transactional
    public boolean saveSchedule(TourScheduleOrderDTO tourScheduleOrderDTO) {
        log.info("保存单个行程排序: {}", tourScheduleOrderDTO);
        
        try {
            TourScheduleOrder tourScheduleOrder = convertToEntity(tourScheduleOrderDTO);
            tourScheduleOrder.setCreatedAt(LocalDateTime.now());
            tourScheduleOrder.setUpdatedAt(LocalDateTime.now());
            
            tourScheduleOrderMapper.insert(tourScheduleOrder);
            return true;
        } catch (Exception e) {
            log.error("保存行程排序失败", e);
            return false;
        }
    }

    /**
     * 批量保存行程排序
     * @param batchSaveDTO 批量保存DTO
     * @return 保存结果
     */
    @Override
    @Transactional
    public boolean saveBatchSchedules(TourScheduleBatchSaveDTO batchSaveDTO) {
        log.info("批量保存行程排序: {}", batchSaveDTO);
        
        try {
            if (batchSaveDTO.getSchedules() == null || batchSaveDTO.getSchedules().isEmpty()) {
                log.warn("批量保存的行程排序列表为空");
                return false;
            }
            
            // 先删除该订单的所有行程排序
            if (batchSaveDTO.getBookingId() != null) {
                tourScheduleOrderMapper.deleteByBookingId(batchSaveDTO.getBookingId());
            }
            
            // 转换DTO为实体对象
            List<TourScheduleOrder> scheduleOrders = batchSaveDTO.getSchedules().stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());
            
            // 设置创建和更新时间
            LocalDateTime now = LocalDateTime.now();
            scheduleOrders.forEach(order -> {
                order.setCreatedAt(now);
                order.setUpdatedAt(now);
            });
            
            // 批量插入
            tourScheduleOrderMapper.insertBatch(scheduleOrders);
            return true;
        } catch (Exception e) {
            log.error("批量保存行程排序失败", e);
            return false;
        }
    }

    /**
     * 初始化订单的行程排序
     * @param bookingId 订单ID
     * @return 初始化结果
     */
    @Override
    @Transactional
    public boolean initOrderSchedules(Integer bookingId) {
        log.info("初始化订单的行程排序: {}", bookingId);
        
        try {
            // 先删除该订单的所有行程排序
            tourScheduleOrderMapper.deleteByBookingId(bookingId);
            
            // 这里可以根据业务需求添加初始化逻辑
            // 例如：根据订单信息自动生成默认的行程排序
            
            return true;
        } catch (Exception e) {
            log.error("初始化订单行程排序失败", e);
            return false;
        }
    }

    /**
     * 将实体对象转换为VO对象
     * @param entity 实体对象
     * @return VO对象
     */
    private TourScheduleVO convertToVO(TourScheduleOrder entity) {
        TourScheduleVO vo = new TourScheduleVO();
        BeanUtils.copyProperties(entity, vo);
        
        // 根据标题或地点名称生成颜色
        String locationName = entity.getTitle() != null ? entity.getTitle() : 
                             (entity.getTourLocation() != null ? entity.getTourLocation() : 
                              entity.getTourName() != null ? entity.getTourName() : "");
        vo.setColor(generateColorByLocation(locationName));
        
        return vo;
    }

    /**
     * 将DTO对象转换为实体对象
     * @param dto DTO对象
     * @return 实体对象
     */
    private TourScheduleOrder convertToEntity(TourScheduleOrderDTO dto) {
        TourScheduleOrder entity = new TourScheduleOrder();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    /**
     * 根据地点名称生成颜色（与前端保持一致）
     * @param locationName 地点名称
     * @return 颜色值
     */
    private String generateColorByLocation(String locationName) {
        if (locationName == null || locationName.isEmpty()) {
            return "#1890ff"; // 默认蓝色
        }
        
        // 与前端保持一致的颜色映射
        if (locationName.contains("霍巴特")) return "#13c2c2";
        if (locationName.contains("朗塞斯顿")) return "#722ed1";
        if (locationName.contains("摇篮山")) return "#7b68ee";
        if (locationName.contains("酒杯湾")) return "#ff9c6e";
        if (locationName.contains("亚瑟港")) return "#dc3545";
        if (locationName.contains("布鲁尼岛") || locationName.contains("布鲁尼")) return "#87d068";
        if (locationName.contains("惠灵顿山")) return "#f56a00";
        if (locationName.contains("塔斯马尼亚")) return "#1890ff";
        if (locationName.contains("菲欣纳")) return "#3f8600";
        if (locationName.contains("塔斯曼半岛") || locationName.contains("塔斯曼")) return "#ff4d4f";
        if (locationName.contains("玛丽亚岛") || locationName.contains("玛丽亚")) return "#ffaa00";
        if (locationName.contains("摩恩谷")) return "#9254de";
        if (locationName.contains("菲尔德山")) return "#237804";
        if (locationName.contains("非常湾")) return "#5cdbd3";
        if (locationName.contains("卡尔德")) return "#096dd9";
        
        // 根据旅游类型生成颜色作为备选
        if (locationName.contains("一日游")) return "#108ee9";
        if (locationName.contains("跟团游")) return "#fa8c16";
        if (locationName.contains("待安排")) return "#bfbfbf";
        
        // 如果没有匹配的固定颜色，使用哈希算法生成一致的颜色
        int hashCode = 0;
        for (char c : locationName.toCharArray()) {
            hashCode = c + ((hashCode << 5) - hashCode);
        }
        
        int h = Math.abs(hashCode) % 360;
        int s = 70 + Math.abs(hashCode % 20); // 70-90%饱和度
        int l = 55 + Math.abs((hashCode >> 4) % 15); // 55-70%亮度
        
        return String.format("hsl(%d, %d%%, %d%%)", h, s, l);
    }
} 