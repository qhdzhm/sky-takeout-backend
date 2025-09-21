package com.sky.service.impl;

import com.sky.dto.DayTourDTO;
import com.sky.dto.DayTourPageQueryDTO;
import com.sky.dto.GroupTourDTO;
import com.sky.dto.TourScheduleBatchSaveDTO;
import com.sky.dto.TourScheduleOrderDTO;
import com.sky.dto.UpdateTourLocationDTO;
import com.github.pagehelper.Page;
import com.sky.entity.DayTour;
import com.sky.entity.TourBooking;
import com.sky.entity.TourScheduleOrder;
import com.sky.mapper.DayTourMapper;
import com.sky.mapper.GroupTourMapper;
import com.sky.mapper.TourBookingMapper;
import com.sky.mapper.TourScheduleOrderMapper;
import com.sky.mapper.TourGuideVehicleAssignmentMapper;
import com.sky.service.TourScheduleOrderService;
import com.sky.vo.TourScheduleVO;
import com.sky.vo.HotelCustomerStatisticsVO;

import com.sky.context.BaseContext;
// import com.sky.vo.TourGuideVehicleAssignmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行程排序业务实现类
 */
@Service
@Slf4j
public class TourScheduleOrderServiceImpl implements TourScheduleOrderService {

    @Autowired
    private TourScheduleOrderMapper tourScheduleOrderMapper;

    @Autowired
    private TourBookingMapper tourBookingMapper;

    @Autowired
    private TourGuideVehicleAssignmentMapper tourGuideVehicleAssignmentMapper;
    
    @Autowired
    private DayTourMapper dayTourMapper;
    
    @Autowired
    private GroupTourMapper groupTourMapper;

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
            tourScheduleOrder.setUpdatedAt(LocalDateTime.now());
            
            // 判断是插入还是更新
            if (tourScheduleOrderDTO.getId() != null && tourScheduleOrderDTO.getId() > 0) {
                // 更新操作
                log.info("执行更新操作 - ID: {}", tourScheduleOrderDTO.getId());
                tourScheduleOrderMapper.update(tourScheduleOrder);
            } else {
                // 插入操作
                log.info("执行插入操作");
                tourScheduleOrder.setCreatedAt(LocalDateTime.now());
                tourScheduleOrderMapper.insert(tourScheduleOrder);
            }
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
            
            // ====== 关键修改：从订单表获取完整信息 ======
            TourBooking originalBooking = null;
            if (batchSaveDTO.getBookingId() != null) {
                originalBooking = tourBookingMapper.getById(batchSaveDTO.getBookingId());
                log.info("获取到原始订单信息: {}", originalBooking);
            }
            
            // 转换DTO为实体对象，并补充完整的订单信息
            List<TourScheduleOrder> scheduleOrders = new ArrayList<>();
            for (TourScheduleOrderDTO dto : batchSaveDTO.getSchedules()) {
                TourScheduleOrder entity = convertToEntityWithBookingInfo(dto, originalBooking);
                scheduleOrders.add(entity);
            }
            
            // 设置创建和更新时间
            LocalDateTime now = LocalDateTime.now();
            scheduleOrders.forEach(order -> {
                order.setCreatedAt(now);
                order.setUpdatedAt(now);
            });
            
            // 批量插入
            tourScheduleOrderMapper.insertBatch(scheduleOrders);
            log.info("成功批量保存行程排序，共 {} 条记录", scheduleOrders.size());
            return true;
        } catch (Exception e) {
            log.error("批量保存行程排序失败", e);
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
     * 将DTO对象转换为实体对象，并补充完整的订单信息
     * @param dto DTO对象
     * @param originalBooking 原始订单信息
     * @return 实体对象
     */
    private TourScheduleOrder convertToEntityWithBookingInfo(TourScheduleOrderDTO dto, TourBooking originalBooking) {
        TourScheduleOrder entity = new TourScheduleOrder();
        
        // 首先复制DTO中的字段
        BeanUtils.copyProperties(dto, entity);
        
        // 如果有原始订单信息，补充缺失的字段（强制覆盖null值）
        if (originalBooking != null) {
            // ============ 必填字段优先设置 ============
            // 确保必填字段不为null
            if (entity.getTourId() == null) {
                entity.setTourId(originalBooking.getTourId());
            }
            if (entity.getTourType() == null || entity.getTourType().isEmpty()) {
                entity.setTourType(originalBooking.getTourType());
            }
            log.info("开始补充订单 {} 的完整信息到排团表", originalBooking.getOrderNumber());
            
            // 记录补充前的状态
            log.info("补充前DTO状态 - 联系人:{}, 电话:{}, 航班号:{}, 返程航班:{}, 酒店房数:{}, 房间详情:{}", 
                    entity.getContactPerson(), entity.getContactPhone(), 
                    entity.getFlightNumber(), entity.getReturnFlightNumber(),
                    entity.getHotelRoomCount(), entity.getRoomDetails());
            
            // 记录原始订单的可用数据
            log.info("原始订单可用数据 - 联系人:{}, 电话:{}, 航班号:{}, 返程航班:{}, 酒店房数:{}, 房间详情:{}", 
                    originalBooking.getContactPerson(), originalBooking.getContactPhone(), 
                    originalBooking.getFlightNumber(), originalBooking.getReturnFlightNumber(),
                    originalBooking.getHotelRoomCount(), originalBooking.getRoomDetails());
            
            // ============ 基本订单信息 ============
            // 强制补充所有null或空值字段
            if (entity.getOrderNumber() == null || entity.getOrderNumber().isEmpty()) {
                entity.setOrderNumber(originalBooking.getOrderNumber());
            }
            if (entity.getAdultCount() == null) {
                entity.setAdultCount(originalBooking.getAdultCount());
            }
            if (entity.getChildCount() == null) {
                entity.setChildCount(originalBooking.getChildCount());
            }
            if (entity.getContactPerson() == null || entity.getContactPerson().isEmpty()) {
                entity.setContactPerson(originalBooking.getContactPerson());
            }
            if (entity.getContactPhone() == null || entity.getContactPhone().isEmpty()) {
                entity.setContactPhone(originalBooking.getContactPhone());
            }
            // ============ 智能设置接送地点 ============
            // 如果前端已经提供了接送地点（通过多酒店逻辑计算），则优先使用前端数据
            // 只有在前端未提供时，才使用后端的智能逻辑
            boolean frontendProvidedPickup = entity.getPickupLocation() != null && !entity.getPickupLocation().trim().isEmpty();
            boolean frontendProvidedDropoff = entity.getDropoffLocation() != null && !entity.getDropoffLocation().trim().isEmpty();
            
            if (frontendProvidedPickup && frontendProvidedDropoff) {
                // 前端已经通过多酒店逻辑计算好了接送地点，直接使用
                log.info("✅ 使用前端多酒店智能计算的接送地点 - 订单{} 第{}天: 接客=\"{}\", 送客=\"{}\"", 
                        originalBooking.getOrderNumber(), entity.getDayNumber(), 
                        entity.getPickupLocation(), entity.getDropoffLocation());
            } else {
                // 前端未提供完整接送信息，使用后端的智能逻辑（兼容性处理）
            Integer currentDayNumber = entity.getDayNumber();
            LocalDate startDate = originalBooking.getTourStartDate();
            LocalDate endDate = originalBooking.getTourEndDate();
            
            if (currentDayNumber != null && startDate != null && endDate != null) {
                long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
                boolean isFirstDay = currentDayNumber == 1;
                boolean isLastDay = currentDayNumber == totalDays;
                
                    if (isFirstDay && !frontendProvidedPickup) {
                    entity.setPickupLocation(originalBooking.getPickupLocation() != null ? originalBooking.getPickupLocation() : "");
                        log.info("📍 后端智能设置第一天接客地点 - 订单{} 第{}天: 接客地点=\"{}\"", 
                            originalBooking.getOrderNumber(), currentDayNumber, entity.getPickupLocation());
                    }
                    if (isFirstDay && !frontendProvidedDropoff) {
                        entity.setDropoffLocation(""); // 等酒店系统开发完成
                        log.info("📍 后端智能设置第一天送客地点 - 订单{} 第{}天: 送客地点=酒店(未开发)", 
                                originalBooking.getOrderNumber(), currentDayNumber);
                    }
                    if (isLastDay && !frontendProvidedPickup) {
                        entity.setPickupLocation(""); // 等酒店系统开发完成
                        log.info("📍 后端智能设置最后一天接客地点 - 订单{} 第{}天: 接客地点=酒店(未开发)", 
                                originalBooking.getOrderNumber(), currentDayNumber);
                    }
                    if (isLastDay && !frontendProvidedDropoff) {
                    entity.setDropoffLocation(originalBooking.getDropoffLocation() != null ? originalBooking.getDropoffLocation() : "");
                        log.info("📍 后端智能设置最后一天送客地点 - 订单{} 第{}天: 送客地点=\"{}\"", 
                            originalBooking.getOrderNumber(), currentDayNumber, entity.getDropoffLocation());
                    }
                    if (!isFirstDay && !isLastDay) {
                        if (!frontendProvidedPickup) {
                    entity.setPickupLocation(""); // 等酒店系统开发
                        }
                        if (!frontendProvidedDropoff) {
                    entity.setDropoffLocation(""); // 等酒店系统开发
                        }
                        log.info("📍 后端智能设置中间天数接送地点 - 订单{} 第{}天: 等酒店系统开发", 
                            originalBooking.getOrderNumber(), currentDayNumber);
                }
            } else {
                // 如果无法确定天数，使用原始数据
                log.warn("无法确定行程天数，使用原始接送地点 - 订单{}, dayNumber={}", 
                        originalBooking.getOrderNumber(), currentDayNumber);
                    if (!frontendProvidedPickup && entity.getPickupLocation() == null) {
                    entity.setPickupLocation(originalBooking.getPickupLocation());
                }
                    if (!frontendProvidedDropoff && entity.getDropoffLocation() == null) {
                    entity.setDropoffLocation(originalBooking.getDropoffLocation());
                    }
                }
            }
            if (entity.getSpecialRequests() == null || entity.getSpecialRequests().isEmpty()) {
                entity.setSpecialRequests(originalBooking.getSpecialRequests());
            }
            if (entity.getLuggageCount() == null) {
                entity.setLuggageCount(originalBooking.getLuggageCount());
            }
            if (entity.getPassengerContact() == null || entity.getPassengerContact().isEmpty()) {
                entity.setPassengerContact(originalBooking.getPassengerContact());
            }
            
            // ============ 酒店信息 ============
            // 强制补充所有null值，不管前端是否传递
            if (entity.getHotelLevel() == null || entity.getHotelLevel().isEmpty()) {
                entity.setHotelLevel(originalBooking.getHotelLevel());
            }
            if (entity.getRoomType() == null || entity.getRoomType().isEmpty()) {
                entity.setRoomType(originalBooking.getRoomType());
            }
            if (entity.getHotelRoomCount() == null) {
                entity.setHotelRoomCount(originalBooking.getHotelRoomCount());
            }
            if (entity.getHotelCheckInDate() == null) {
                entity.setHotelCheckInDate(originalBooking.getHotelCheckInDate());
            }
            if (entity.getHotelCheckOutDate() == null) {
                entity.setHotelCheckOutDate(originalBooking.getHotelCheckOutDate());
            }
            if (entity.getRoomDetails() == null || entity.getRoomDetails().isEmpty()) {
                entity.setRoomDetails(originalBooking.getRoomDetails());
            }
            
            // ============ 航班信息智能分配 ============
            // 根据行程天数智能分配航班信息：
            // - 第一天：使用到达航班信息
            // - 最后一天：使用返程航班信息  
            // - 中间天数：不需要航班信息
            Integer dayNumber = entity.getDayNumber();
            LocalDate tourStartDate = originalBooking.getTourStartDate();
            LocalDate tourEndDate = originalBooking.getTourEndDate();
            
            if (dayNumber != null && tourStartDate != null && tourEndDate != null) {
                long totalDays = java.time.temporal.ChronoUnit.DAYS.between(tourStartDate, tourEndDate) + 1;
                boolean isFirstDay = dayNumber == 1;
                boolean isLastDay = dayNumber == totalDays;
                
                if (isFirstDay) {
                    // 第一天：设置到达航班信息
                    if (entity.getFlightNumber() == null || entity.getFlightNumber().isEmpty()) {
                        entity.setFlightNumber(originalBooking.getFlightNumber());
                    }
                    if (entity.getArrivalDepartureTime() == null) {
                        entity.setArrivalDepartureTime(originalBooking.getArrivalDepartureTime());
                    }
                    if (entity.getArrivalLandingTime() == null) {
                        entity.setArrivalLandingTime(originalBooking.getArrivalLandingTime());
                    }
                    // 清空返程航班信息（第一天不需要）
                    entity.setReturnFlightNumber("");
                    entity.setDepartureDepartureTime(null);
                    entity.setDepartureLandingTime(null);
                    
                    log.info("🛫 第一天航班信息设置 - 订单{} 第{}天: 到达航班={}, 到达时间={}", 
                            originalBooking.getOrderNumber(), dayNumber, 
                            entity.getFlightNumber(), entity.getArrivalLandingTime());
                            
                } else if (isLastDay) {
                    // 最后一天：设置返程航班信息
                    if (entity.getReturnFlightNumber() == null || entity.getReturnFlightNumber().isEmpty()) {
                        entity.setReturnFlightNumber(originalBooking.getReturnFlightNumber());
                    }
                    if (entity.getDepartureDepartureTime() == null) {
                        entity.setDepartureDepartureTime(originalBooking.getDepartureDepartureTime());
                    }
                    if (entity.getDepartureLandingTime() == null) {
                        entity.setDepartureLandingTime(originalBooking.getDepartureLandingTime());
                    }
                    // 清空到达航班信息（最后一天不需要）
                    entity.setFlightNumber("");
                    entity.setArrivalDepartureTime(null);
                    entity.setArrivalLandingTime(null);
                    
                    log.info("🛫 最后一天航班信息设置 - 订单{} 第{}天: 返程航班={}, 起飞时间={}", 
                            originalBooking.getOrderNumber(), dayNumber, 
                            entity.getReturnFlightNumber(), entity.getDepartureDepartureTime());
                            
                } else {
                    // 中间天数：清空所有航班信息
                    entity.setFlightNumber("");
                    entity.setArrivalDepartureTime(null);
                    entity.setArrivalLandingTime(null);
                    entity.setReturnFlightNumber("");
                    entity.setDepartureDepartureTime(null);
                    entity.setDepartureLandingTime(null);
                    
                    log.info("🛫 中间天数航班信息设置 - 订单{} 第{}天: 无航班信息需求", 
                            originalBooking.getOrderNumber(), dayNumber);
                }
            } else {
                // 如果无法确定行程天数，使用原有逻辑
                log.warn("无法确定行程天数，使用原有航班信息逻辑 - 订单{}, dayNumber={}", 
                        originalBooking.getOrderNumber(), dayNumber);
                        
                if (entity.getFlightNumber() == null || entity.getFlightNumber().isEmpty()) {
                    entity.setFlightNumber(originalBooking.getFlightNumber());
                }
                if (entity.getArrivalDepartureTime() == null) {
                    entity.setArrivalDepartureTime(originalBooking.getArrivalDepartureTime());
                }
                if (entity.getArrivalLandingTime() == null) {
                    entity.setArrivalLandingTime(originalBooking.getArrivalLandingTime());
                }
                if (entity.getReturnFlightNumber() == null || entity.getReturnFlightNumber().isEmpty()) {
                    entity.setReturnFlightNumber(originalBooking.getReturnFlightNumber());
                }
                if (entity.getDepartureDepartureTime() == null) {
                    entity.setDepartureDepartureTime(originalBooking.getDepartureDepartureTime());
                }
                if (entity.getDepartureLandingTime() == null) {
                    entity.setDepartureLandingTime(originalBooking.getDepartureLandingTime());
                }
            }
            
            // ============ 日期信息 ============
            if (entity.getTourStartDate() == null) {
                entity.setTourStartDate(originalBooking.getTourStartDate());
            }
            if (entity.getTourEndDate() == null) {
                entity.setTourEndDate(originalBooking.getTourEndDate());
            }
            if (entity.getPickupDate() == null) {
                entity.setPickupDate(originalBooking.getPickupDate());
            }
            if (entity.getDropoffDate() == null) {
                entity.setDropoffDate(originalBooking.getDropoffDate());
            }
            if (entity.getBookingDate() == null) {
                entity.setBookingDate(originalBooking.getBookingDate());
            }
            
            // ============ 联系和行程信息 ============
            // 注意：passengerContact 已在基本信息部分处理过，此处不重复
            if (entity.getItineraryDetails() == null || entity.getItineraryDetails().isEmpty()) {
                entity.setItineraryDetails(originalBooking.getItineraryDetails());
            }
            
            // ============ 标识字段 ============
            if (entity.getIsFirstOrder() == null) {
                entity.setIsFirstOrder(originalBooking.getIsFirstOrder() != null && originalBooking.getIsFirstOrder() == 1);
            }
            if (entity.getFromReferral() == null) {
                entity.setFromReferral(originalBooking.getFromReferral() != null && originalBooking.getFromReferral() == 1);
            }
            if (entity.getReferralCode() == null || entity.getReferralCode().isEmpty()) {
                entity.setReferralCode(originalBooking.getReferralCode());
            }
            
            // ============ 业务信息 ============
            // 强制补充业务字段，确保完整性
            if (entity.getServiceType() == null || entity.getServiceType().isEmpty()) {
                entity.setServiceType(originalBooking.getServiceType());
            }
            if (entity.getPaymentStatus() == null || entity.getPaymentStatus().isEmpty()) {
                entity.setPaymentStatus(originalBooking.getPaymentStatus());
            }
            if (entity.getTotalPrice() == null) {
                entity.setTotalPrice(originalBooking.getTotalPrice());
            }
            if (entity.getUserId() == null) {
                entity.setUserId(originalBooking.getUserId());
            }
            if (entity.getAgentId() == null) {
                entity.setAgentId(originalBooking.getAgentId());
            }
            if (entity.getOperatorId() == null) {
                entity.setOperatorId(originalBooking.getOperatorId());
            }
            if (entity.getGroupSize() == null) {
                entity.setGroupSize(originalBooking.getGroupSize());
            }
            if (entity.getStatus() == null || entity.getStatus().isEmpty()) {
                entity.setStatus(originalBooking.getStatus());
            }
            if (entity.getBookingDate() == null) {
                entity.setBookingDate(originalBooking.getBookingDate());
            }
            
            // ============ 团型信息同步 ============
            // 🎯 同步团型字段到排团表
            if (entity.getGroupType() == null || entity.getGroupType().isEmpty()) {
                entity.setGroupType(originalBooking.getGroupType());
            }
            if (entity.getGroupSizeLimit() == null) {
                entity.setGroupSizeLimit(originalBooking.getGroupSizeLimit());
            }
            
            log.info("🎯 团型信息同步 - 订单{}: 团型={}, 人数限制={}", 
                    originalBooking.getOrderNumber(), entity.getGroupType(), entity.getGroupSizeLimit());
            
            // ============ 产品名称设置 ============
            // 从原始订单中获取产品名称
            if (entity.getTourName() == null || entity.getTourName().isEmpty()) {
                // 根据产品类型获取产品名称
                try {
                                         String tourName = getTourName(originalBooking.getTourId(), originalBooking.getTourType());
                    entity.setTourName(tourName);
                } catch (Exception e) {
                    log.warn("获取产品名称失败，使用默认值: {}", e.getMessage());
                    entity.setTourName("未知产品");
                }
            }
            
            log.info("✅ 已补充订单 {} 第{}天的完整信息", originalBooking.getOrderNumber(), entity.getDayNumber());
            log.info("  基本信息 - 联系人={}, 电话={}, 成人数={}, 儿童数={}, 总价={}", 
                    entity.getContactPerson(), entity.getContactPhone(), 
                    entity.getAdultCount(), entity.getChildCount(), entity.getTotalPrice());
            log.info("  酒店信息 - 等级={}, 房型={}, 房间数={}, 入住={}, 退房={}, 详情={}", 
                    entity.getHotelLevel(), entity.getRoomType(), entity.getHotelRoomCount(),
                    entity.getHotelCheckInDate(), entity.getHotelCheckOutDate(), entity.getRoomDetails());
            log.info("  智能航班信息 - 到达航班={}, 到达时间={}, 返程航班={}, 返程起飞={}", 
                    entity.getFlightNumber(), entity.getArrivalLandingTime(),
                    entity.getReturnFlightNumber(), entity.getDepartureDepartureTime());
            log.info("  其他信息 - 服务类型={}, 支付状态={}, 代理商ID={}, 团队规模={}, 状态={}", 
                    entity.getServiceType(), entity.getPaymentStatus(), 
                    entity.getAgentId(), entity.getGroupSize(), entity.getStatus());
        }
        
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

    /**
     * 根据产品ID和类型获取产品名称
     * @param tourId 产品ID
     * @param tourType 产品类型
     * @return 产品名称
     */
    private String getTourName(Integer tourId, String tourType) {
        try {
            if ("day_tour".equals(tourType)) {
                DayTour dayTour = dayTourMapper.getById(tourId);
                return dayTour != null ? dayTour.getName() : "一日游";
            } else if ("group_tour".equals(tourType)) {
                GroupTourDTO groupTour = groupTourMapper.getById(tourId);
                return groupTour != null ? groupTour.getName() : "团队游";
            }
        } catch (Exception e) {
            log.warn("获取产品名称失败: {}", e.getMessage());
        }
        return "旅游产品";
    }

    /**
     * 根据日期和地点获取导游车辆分配信息
     * @param date 日期
     * @param location 地点
     * @return 分配信息列表
     */
    @Override
    public List<Object> getAssignmentByDateAndLocation(LocalDate date, String location) {
        log.info("根据日期和地点获取导游车辆分配信息: 日期={}, 地点={}", date, location);
        
        // 调用导游车辆分配服务获取数据
        try {
            List<Object> assignments = tourGuideVehicleAssignmentMapper.getByDestinationWithFuzzyMatch(location, date);
            log.info("找到{}条分配记录", assignments.size());
            return assignments;
        } catch (Exception e) {
            log.error("获取导游车辆分配信息失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据订单号搜索行程排序
     * @param orderNumber 订单号
     * @return 行程排序视图对象列表
     */
    @Override
    public List<TourScheduleVO> getSchedulesByOrderNumber(String orderNumber) {
        log.info("根据订单号搜索行程排序: {}", orderNumber);
        
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            log.warn("订单号为空，返回空列表");
            return new ArrayList<>();
        }
        
        List<TourScheduleOrder> scheduleOrders = tourScheduleOrderMapper.getByOrderNumber(orderNumber.trim());
        
        return scheduleOrders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 根据联系人姓名搜索行程排序
     * @param contactPerson 联系人姓名
     * @return 行程排序视图对象列表
     */
    @Override
    public List<TourScheduleVO> getSchedulesByContactPerson(String contactPerson) {
        log.info("根据联系人姓名搜索行程排序: {}", contactPerson);
        
        if (contactPerson == null || contactPerson.trim().isEmpty()) {
            log.warn("联系人姓名为空，返回空列表");
            return new ArrayList<>();
        }
        
        List<TourScheduleOrder> scheduleOrders = tourScheduleOrderMapper.getByContactPerson(contactPerson.trim());
        
        return scheduleOrders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAvailableDayTours(Map<String, Object> params) {
        log.info("获取可选的一日游产品列表: {}", params);
        
        try {
            // 使用分页查询获取所有激活状态的一日游
            DayTourPageQueryDTO queryDTO = new DayTourPageQueryDTO();
            queryDTO.setIsActive(1); // 只获取激活的一日游
            queryDTO.setPageSize(1000); // 设置足够大的页面大小
            queryDTO.setPage(1);
            
            Page<DayTourDTO> page = dayTourMapper.pageQuery(queryDTO);
            List<DayTourDTO> dayTours = page.getResult();
            
            // 转换为Map格式，便于前端使用
            List<Map<String, Object>> result = new ArrayList<>();
            for (DayTourDTO dayTour : dayTours) {
                Map<String, Object> tourMap = new HashMap<>();
                tourMap.put("id", dayTour.getId());
                tourMap.put("name", dayTour.getName());
                tourMap.put("description", dayTour.getDescription());
                tourMap.put("price", dayTour.getPrice());
                tourMap.put("duration", dayTour.getDuration());
                tourMap.put("location", dayTour.getLocation());
                tourMap.put("departureAddress", dayTour.getDepartureAddress());
                tourMap.put("category", dayTour.getCategory());
                tourMap.put("rating", dayTour.getRating());
                tourMap.put("regionName", dayTour.getRegionName());
                result.add(tourMap);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取一日游产品列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean deleteSchedule(Integer scheduleId) {
        log.info("删除行程排序，ID：{}", scheduleId);
        
        try {
            // 检查行程是否存在
            if (scheduleId == null) {
                log.warn("行程ID不能为空");
                return false;
            }
            
            // 执行删除操作
            int deletedRows = tourScheduleOrderMapper.deleteById(scheduleId);
            
            if (deletedRows > 0) {
                log.info("行程排序删除成功，ID：{}，影响行数：{}", scheduleId, deletedRows);
                return true;
            } else {
                log.warn("行程排序删除失败，ID：{}，可能不存在", scheduleId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("删除行程排序时发生异常，ID：{}，错误：{}", scheduleId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateGuideRemarks(Integer scheduleId, String guideRemarks) {
        log.info("开始更新导游备注，行程ID：{}，备注：{}", scheduleId, guideRemarks);
        
        try {
            int updatedRows = tourScheduleOrderMapper.updateGuideRemarksById(scheduleId, guideRemarks);
            
            if (updatedRows > 0) {
                log.info("导游备注更新成功，行程ID：{}，影响行数：{}", scheduleId, updatedRows);
                return true;
            } else {
                log.warn("导游备注更新失败，行程ID：{}，可能不存在", scheduleId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("更新导游备注时发生异常，行程ID：{}，错误：{}", scheduleId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public HotelCustomerStatisticsVO getHotelCustomerStatistics(String hotelName, LocalDate tourDate) {
        log.info("开始统计酒店客人信息，酒店名称：{}，日期：{}", hotelName, tourDate);
        
        try {
            // 查询住在该酒店的所有客人（包含导游信息）
            List<TourScheduleOrder> allCustomers = tourScheduleOrderMapper.getCustomersByHotelDateAndGuide(hotelName, tourDate, null);
            
            if (allCustomers.isEmpty()) {
                log.info("未找到住在酒店 {} 在日期 {} 的客人", hotelName, tourDate);
                return HotelCustomerStatisticsVO.builder()
                    .hotelName(hotelName)
                    .tourDate(tourDate)
                    .totalCustomers(0)
                    .guideGroups(new ArrayList<>())
                    .build();
            }
            
            // 按导游分组
            Map<String, List<TourScheduleOrder>> customersByGuide = allCustomers.stream()
                .collect(Collectors.groupingBy(customer -> {
                    // 处理没有导游分配的情况
                    String guideName = customer.getGuideName();
                    return (guideName != null && !guideName.trim().isEmpty()) ? guideName : "未分配导游";
                }));
            
            // 构建导游分组信息
            List<HotelCustomerStatisticsVO.GuideCustomerGroup> guideGroups = new ArrayList<>();
            
            for (Map.Entry<String, List<TourScheduleOrder>> entry : customersByGuide.entrySet()) {
                String guideName = entry.getKey();
                List<TourScheduleOrder> guideCustomers = entry.getValue();
                
                // 获取车辆信息（同一导游的客人使用相同车辆）
                String vehicleInfo = guideCustomers.stream()
                    .map(TourScheduleOrder::getVehicleInfo)
                    .filter(info -> info != null && !info.trim().isEmpty())
                    .findFirst()
                    .orElse("未分配车辆");
                
                // 构建客人详细信息列表
                List<HotelCustomerStatisticsVO.CustomerDetail> customerDetails = guideCustomers.stream()
                    .map(customer -> HotelCustomerStatisticsVO.CustomerDetail.builder()
                        .orderNumber(customer.getOrderNumber())
                        .contactPerson(customer.getContactPerson())
                        .contactPhone(customer.getContactPhone())
                        .adultCount(customer.getAdultCount())
                        .childCount(customer.getChildCount())
                        .pickupLocation(customer.getPickupLocation())
                        .dropoffLocation(customer.getDropoffLocation())
                        .specialRequests(customer.getSpecialRequests())
                        .bookingId(customer.getBookingId())
                        .build())
                    .collect(Collectors.toList());
                
                // 构建导游分组
                HotelCustomerStatisticsVO.GuideCustomerGroup guideGroup = HotelCustomerStatisticsVO.GuideCustomerGroup.builder()
                    .guideName(guideName)
                    .vehicleInfo(vehicleInfo)
                    .customerCount(guideCustomers.size())
                    .customers(customerDetails)
                    .build();
                
                guideGroups.add(guideGroup);
            }
            
            // 按导游姓名排序
            guideGroups.sort((g1, g2) -> {
                // "未分配导游"排在最后
                if ("未分配导游".equals(g1.getGuideName()) && !"未分配导游".equals(g2.getGuideName())) {
                    return 1;
                }
                if (!"未分配导游".equals(g1.getGuideName()) && "未分配导游".equals(g2.getGuideName())) {
                    return -1;
                }
                return g1.getGuideName().compareTo(g2.getGuideName());
            });
            
            // 构建最终结果
            HotelCustomerStatisticsVO result = HotelCustomerStatisticsVO.builder()
                .hotelName(hotelName)
                .tourDate(tourDate)
                .totalCustomers(allCustomers.size())
                .guideGroups(guideGroups)
                .build();
            
            log.info("酒店客人统计完成，酒店：{}，日期：{}，总客人数：{}，导游分组数：{}", 
                    hotelName, tourDate, result.getTotalCustomers(), guideGroups.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("统计酒店客人信息时发生异常，酒店：{}，日期：{}，错误：{}", hotelName, tourDate, e.getMessage(), e);
            throw new RuntimeException("统计酒店客人信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新订单游玩地点 - 用于同车订票拖拽功能
     */
    @Override
    @Transactional
    public boolean updateTourLocation(UpdateTourLocationDTO updateLocationDTO) {
        log.info("开始更新订单游玩地点，订单ID：{}，新地点：{}，日期：{}", 
                updateLocationDTO.getOrderId(), updateLocationDTO.getNewLocation(), updateLocationDTO.getTourDate());
        
        try {
            // 检查参数
            if (updateLocationDTO.getOrderId() == null || 
                updateLocationDTO.getNewLocation() == null || 
                updateLocationDTO.getTourDate() == null) {
                log.warn("更新订单游玩地点参数不完整：{}", updateLocationDTO);
                return false;
            }

            // 更新排团表中该订单在指定日期的游玩地点
            int updatedRows = tourScheduleOrderMapper.updateTourLocationByBookingIdAndDate(
                    updateLocationDTO.getOrderId(),
                    updateLocationDTO.getNewLocation(),
                    updateLocationDTO.getTourDate()
            );

            if (updatedRows > 0) {
                log.info("订单游玩地点更新成功，订单ID：{}，更新记录数：{}", updateLocationDTO.getOrderId(), updatedRows);
                return true;
            } else {
                log.warn("订单游玩地点更新失败，没有找到匹配的记录，订单ID：{}，日期：{}", 
                        updateLocationDTO.getOrderId(), updateLocationDTO.getTourDate());
                return false;
            }

        } catch (Exception e) {
            log.error("更新订单游玩地点时发生异常，订单ID：{}，异常：{}", updateLocationDTO.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("更新订单游玩地点失败：" + e.getMessage());
        }
    }
} 

