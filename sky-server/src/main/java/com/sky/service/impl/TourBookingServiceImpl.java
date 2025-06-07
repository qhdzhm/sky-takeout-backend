package com.sky.service.impl;

import com.sky.dto.PassengerDTO;
import com.sky.dto.PaymentDTO;
import com.sky.dto.TourBookingDTO;
import com.sky.dto.TourBookingUpdateDTO;
import com.sky.entity.TourBooking;
import com.sky.entity.BookingPassengerRelation;
import com.sky.entity.Passenger;
import com.sky.exception.BusinessException;
import com.sky.mapper.TourBookingMapper;
import com.sky.mapper.PassengerMapper;
import com.sky.mapper.DayTourMapper;
import com.sky.mapper.GroupTourMapper;
import com.sky.mapper.AgentMapper;
import com.sky.entity.Agent;
import com.sky.dto.GroupTourDTO;
import com.sky.entity.DayTour;
import com.sky.service.PassengerService;
import com.sky.service.TourBookingService;
import com.sky.service.HotelPriceService;
import com.sky.service.NotificationService;
import com.sky.utils.OrderNumberGenerator;
import com.sky.vo.TourBookingVO;
import com.sky.vo.PriceDetailVO;
import com.sky.vo.PassengerVO;
import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sky.mapper.UserMapper;
import com.sky.mapper.AgentOperatorMapper;
import com.sky.mapper.TourScheduleOrderMapper;
import com.sky.entity.TourScheduleOrder;
import com.sky.mapper.TourItineraryMapper;

/**
 * 旅游订单服务实现类
 */
@Service
@Slf4j
public class TourBookingServiceImpl implements TourBookingService {

    @Autowired
    private TourBookingMapper tourBookingMapper;
    
    @Autowired
    private PassengerService passengerService;
    
    @Autowired
    private PassengerMapper passengerMapper;
    
    @Autowired
    private DayTourMapper dayTourMapper;
    
    @Autowired
    private GroupTourMapper groupTourMapper;
    
    @Autowired
    private AgentMapper agentMapper;
    
    @Autowired
    private HotelPriceService hotelPriceService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AgentOperatorMapper agentOperatorMapper;
    
    @Autowired
    private TourScheduleOrderMapper tourScheduleOrderMapper;
    
    @Autowired
    private DayTourServiceImpl dayTourService;
    
    @Autowired
    private TourItineraryMapper tourItineraryMapper;

    /**
     * 根据ID查询旅游订单
     * 
     * @param bookingId 订单ID
     * @return 订单详细信息
     */
    @Override
    public TourBookingVO getById(Integer bookingId) {
        // 查询订单基本信息
        TourBooking tourBooking = tourBookingMapper.getById(bookingId);
        if (tourBooking == null) {
            return null;
        }
        
        // 转换为VO
        TourBookingVO tourBookingVO = new TourBookingVO();
        BeanUtils.copyProperties(tourBooking, tourBookingVO);
        
        // 查询订单关联的乘客信息
        tourBookingVO.setPassengers(passengerService.getByBookingId(bookingId));
        
        return tourBookingVO;
    }

    /**
     * 根据订单号查询订单
     * 
     * @param orderNumber 订单号
     * @return 订单详细信息
     */
    @Override
    public TourBookingVO getByOrderNumber(String orderNumber) {
        // 查询订单基本信息
        TourBooking tourBooking = tourBookingMapper.getByOrderNumber(orderNumber);
        if (tourBooking == null) {
            return null;
        }
        
        // 转换为VO
        TourBookingVO tourBookingVO = new TourBookingVO();
        BeanUtils.copyProperties(tourBooking, tourBookingVO);
        
        // 查询订单关联的乘客信息
        tourBookingVO.setPassengers(passengerService.getByBookingId(tourBooking.getBookingId()));
        
        return tourBookingVO;
    }

    /**
     * 保存旅游订单
     * 
     * @param tourBookingDTO 订单信息
     * @return 订单ID
     */
    @Override
    @Transactional
    public Integer save(TourBookingDTO tourBookingDTO) {
        // 生成订单号
        String orderNumber = OrderNumberGenerator.generate();
        tourBookingDTO.setOrderNumber(orderNumber);
        System.out.println(tourBookingDTO);
        // 将DTO转换为实体
        TourBooking tourBooking = new TourBooking();
        
        // 首先打印整个DTO查看所有字段
        log.info("DTO原始数据: {}", tourBookingDTO);
        
        // 将DTO的属性复制到实体中
        BeanUtils.copyProperties(tourBookingDTO, tourBooking);
        
        // 从拦截器中获取userId和agentId
        try {
            // 获取用户类型
            String userType = BaseContext.getCurrentUserType();
            Long currentId = BaseContext.getCurrentId();
            
            if (userType != null && "agent".equals(userType)) {
                // 代理商主账号登录，只设置agentId，不设置userId
                Long agentId = BaseContext.getCurrentAgentId();
                if (agentId != null) {
                    tourBooking.setAgentId(agentId.intValue());
                    log.info("设置代理商ID: {}", agentId);
                } else if (currentId != null) {
                    // 如果没有单独的代理商ID，则使用当前ID作为代理商ID
                    tourBooking.setAgentId(currentId.intValue());
                    log.info("使用当前ID作为代理商ID: {}", currentId);
                }
                // 代理商登录不设置userId
                tourBooking.setUserId(null);
            } else if (userType != null && "agent_operator".equals(userType)) {
                // 操作员登录，设置agentId为所属代理商ID，同时记录操作员ID
                Long agentId = BaseContext.getCurrentAgentId();
                Long operatorId = BaseContext.getCurrentOperatorId();
                
                if (agentId != null) {
                    tourBooking.setAgentId(agentId.intValue());
                    log.info("操作员下单，设置代理商ID: {}", agentId);
                }
                
                // 记录操作员ID到数据库
                if (operatorId != null) {
                    tourBooking.setOperatorId(operatorId);
                    log.info("操作员下单，设置操作员ID: {}", operatorId);
                }
                
                // 操作员登录不设置userId
                tourBooking.setUserId(null);
            } else {
                // 普通用户登录，只设置userId，不设置agentId
                if (currentId != null) {
                    tourBooking.setUserId(currentId.intValue());
                    log.info("设置普通用户ID: {}", currentId);
                }
                // 普通用户登录不设置agentId
                tourBooking.setAgentId(null);
            }
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
        }
        
        // 确保groupSize被设置
        if (tourBookingDTO.getPassengers() != null) {
            int groupSize = tourBookingDTO.getPassengers().size();
            tourBooking.setGroupSize(groupSize);
            log.info("根据乘客数量设置groupSize: {}", groupSize);
            
            // 统计成人和儿童数量
            int adultCount = 0;
            int childCount = 0;
            
            for (PassengerDTO passenger : tourBookingDTO.getPassengers()) {
                if (passenger != null && passenger.getFullName() != null && !passenger.getFullName().trim().isEmpty()) {
                    if (Boolean.TRUE.equals(passenger.getIsChild())) {
                        childCount++;
                    } else {
                        adultCount++;
                    }
                }
            }
            
            // 设置成人和儿童数量
            tourBooking.setAdultCount(adultCount);
            tourBooking.setChildCount(childCount);
            log.info("设置成人数量: {}, 儿童数量: {}", adultCount, childCount);
        }
        
        // 如果前端已经传入了这些值，则优先使用前端传入的值
        if (tourBookingDTO.getAdultCount() != null) {
            tourBooking.setAdultCount(tourBookingDTO.getAdultCount());
            log.info("使用前端传入的成人数量: {}", tourBookingDTO.getAdultCount());
        }
        if (tourBookingDTO.getChildCount() != null) {
            tourBooking.setChildCount(tourBookingDTO.getChildCount());
            log.info("使用前端传入的儿童数量: {}", tourBookingDTO.getChildCount());
        }
        
        // 设置默认状态
        tourBooking.setStatus("PENDING");
        
        // 设置默认支付状态
        tourBooking.setPaymentStatus("UNPAID");
        
        // 设置创建时间
        tourBooking.setCreatedAt(java.time.LocalDateTime.now());
        tourBooking.setUpdatedAt(java.time.LocalDateTime.now());
        
        // 打印所有字段的值进行调试
        log.info("订单详细信息: {}", tourBooking);
        
        // 计算订单价格（使用详细的价格计算逻辑，与支付时保持一致）
        PriceDetailVO priceDetail = calculatePriceDetail(
            tourBooking.getTourId(), 
            tourBooking.getTourType(), 
            tourBooking.getAgentId() != null ? Long.valueOf(tourBooking.getAgentId()) : null, 
            tourBooking.getAdultCount() != null ? tourBooking.getAdultCount() : 1,  // 成人数量
            tourBooking.getChildCount() != null ? tourBooking.getChildCount() : 0,  // 儿童数量
            tourBooking.getHotelLevel() != null ? tourBooking.getHotelLevel() : "4星",  // 酒店等级
            tourBooking.getHotelRoomCount() != null ? tourBooking.getHotelRoomCount() : 1,  // 房间数量
            null  // userId参数
        );
        
        // 使用实际支付价格（与支付时逻辑一致）
        BigDecimal totalPrice = priceDetail.getActualPaymentPrice() != null ? 
                                priceDetail.getActualPaymentPrice() : priceDetail.getTotalPrice();
        
        tourBooking.setTotalPrice(totalPrice);
        
        log.info("下单价格计算: tourId={}, tourType={}, agentId={}, adultCount={}, childCount={}, 使用详细计算逻辑, totalPrice={}",
                tourBooking.getTourId(), tourBooking.getTourType(), tourBooking.getAgentId(), 
                tourBooking.getAdultCount(), tourBooking.getChildCount(), totalPrice);
        
        // 保存订单基本信息 - 添加try-catch并检查结果
        try {
            tourBookingMapper.insert(tourBooking);
            
            // 检查插入是否成功获取到ID
            if (tourBooking.getBookingId() == null) {
                log.error("订单保存失败，未能获取到订单ID");
                return null;
            }
            
            log.info("订单保存成功，订单ID: {}", tourBooking.getBookingId());
        } catch (Exception e) {
            log.error("订单保存失败", e);
            return null;
        }
        
        // 处理乘客信息
        List<PassengerDTO> passengers = tourBookingDTO.getPassengers();
        if (passengers != null && !passengers.isEmpty()) {
            for (PassengerDTO passengerDTO : passengers) {
                // 检查是否是有效的乘客信息，至少需要姓名或者(isChild=true且有childAge)
                boolean isValidPassenger = false;
                
                // 如果有姓名，视为有效
                if (passengerDTO.getFullName() != null && !passengerDTO.getFullName().trim().isEmpty()) {
                    isValidPassenger = true;
                }
                
                // 如果是儿童且有年龄，则视为有效
                if (Boolean.TRUE.equals(passengerDTO.getIsChild()) && passengerDTO.getChildAge() != null && !passengerDTO.getChildAge().trim().isEmpty()) {
                    isValidPassenger = true;
                }
                
                if (!isValidPassenger) {
                    log.info("跳过无效乘客记录");
                    continue;
                }
                
                // 确保乘客信息完整
                log.info("保存乘客信息: {}", passengerDTO);
                passengerService.addPassengerToBooking(tourBooking.getBookingId(), passengerDTO);
            }
        }
        
        // 🔔 发送新订单通知
        try {
            Double amount = tourBooking.getTotalPrice() != null ? 
                          tourBooking.getTotalPrice().doubleValue() : 0.0;
            String actionDetail = String.format("新订单创建，金额: $%.2f", amount);
            
            sendDetailedOrderNotification(tourBooking, "create", actionDetail);
        } catch (Exception e) {
            log.error("❌ 发送新订单通知失败: {}", e.getMessage(), e);
        }

        // 🆕 自动同步订单数据到排团表（用户下单后立即同步）
        try {
            autoSyncOrderToScheduleTable(tourBooking.getBookingId());
            log.info("✅ 订单数据已自动同步到排团表，订单ID: {}", tourBooking.getBookingId());
        } catch (Exception e) {
            log.error("❌ 自动同步订单到排团表失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响订单创建的主流程
        }
        
        log.info("✅ 订单创建完成，已自动同步到排团表，订单ID: {}", tourBooking.getBookingId());
        
        return tourBooking.getBookingId();
    }

    /**
     * 更新旅游订单
     * 
     * @param tourBookingDTO 订单信息
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean update(TourBookingDTO tourBookingDTO) {
        if (tourBookingDTO.getBookingId() == null) {
            log.error("更新订单时订单ID不能为空");
            return false;
        }
        
        // 获取必要的字段值
        Integer tourId = tourBookingDTO.getTourId();
        String tourType = tourBookingDTO.getTourType();
        
        // 根据乘客列表获取团队规模
        Integer groupSize = null;
        if (tourBookingDTO.getPassengers() != null) {
            groupSize = tourBookingDTO.getPassengers().size();
        }
        
        // 如果修改了旅游类型、旅游ID或人数，重新计算价格
        if (tourId != null || tourType != null || groupSize != null) {
            
            // 获取当前订单信息，确保有必要的数据用于价格计算
            TourBooking currentBooking = tourBookingMapper.getById(tourBookingDTO.getBookingId());
            
            tourId = tourId != null ? tourId : currentBooking.getTourId();
            tourType = tourType != null ? tourType : currentBooking.getTourType();
            groupSize = groupSize != null ? groupSize : currentBooking.getGroupSize();
            
            Long agentId = currentBooking.getAgentId() != null ? 
                Long.valueOf(currentBooking.getAgentId()) : null;
                
            BigDecimal totalPrice = calculateTotalPrice(
                tourId, tourType, agentId, groupSize
            );
            tourBookingDTO.setTotalPrice(totalPrice);
        }
        
        // 将DTO转换为实体
        TourBooking tourBooking = new TourBooking();
        BeanUtils.copyProperties(tourBookingDTO, tourBooking);
        
        // 更新订单基本信息
        tourBookingMapper.update(tourBooking);
        
        return true;
    }

    /**
     * 取消订单
     * 
     * @param bookingId 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean cancel(Integer bookingId) {
        log.info("取消订单, 订单ID: {}", bookingId);
        
        try {
            TourBooking tourBooking = tourBookingMapper.getById(bookingId);
            if (tourBooking == null) {
                log.error("订单不存在: {}", bookingId);
                return false;
            }
            
            // 只有待处理、已确认的订单且未支付的订单可以取消
            String status = tourBooking.getStatus();
            String paymentStatus = tourBooking.getPaymentStatus();
            
            if (!"pending".equals(status) && !"confirmed".equals(status)) {
                log.error("订单状态不允许取消: {}", status);
                return false;
            }
            
            if (!"unpaid".equals(paymentStatus)) {
                log.error("已支付的订单不允许直接取消: {}", paymentStatus);
                return false;
            }
            
            // 更新订单状态为已取消
            tourBooking.setStatus("cancelled");
            tourBooking.setUpdatedAt(LocalDateTime.now());
            
            // 添加取消原因到special_requests字段
            String cancelReason = "用户取消 - " + LocalDateTime.now();
            String specialRequests = tourBooking.getSpecialRequests();
            if (specialRequests != null && !specialRequests.isEmpty()) {
                specialRequests += "\n取消原因: " + cancelReason;
            } else {
                specialRequests = "取消原因: " + cancelReason;
            }
            tourBooking.setSpecialRequests(specialRequests);
            
            // 更新订单
            tourBookingMapper.update(tourBooking);
            
            // 🔔 发送订单取消通知
            try {
                sendDetailedOrderNotification(tourBooking, "cancel", "用户主动取消订单");
            } catch (Exception e) {
                log.error("❌ 发送订单取消通知失败: {}", e.getMessage(), e);
            }
            
            log.info("订单取消完成, 订单ID: {}", bookingId);
            return true;
        } catch (Exception e) {
            log.error("取消订单出错, 订单ID: {}, 错误: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("取消订单出错: " + e.getMessage(), e);
        }
    }

    /**
     * 确认订单
     * 
     * @param bookingId 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean confirm(Integer bookingId) {
        TourBooking tourBooking = tourBookingMapper.getById(bookingId);
        if (tourBooking == null) {
            log.error("订单不存在: {}", bookingId);
            return false;
        }
        
        // 只有待处理的订单可以确认
        if (!"pending".equals(tourBooking.getStatus())) {
            log.error("订单状态不允许确认: {}", tourBooking.getStatus());
            return false;
        }
        
        // 更新订单状态为已确认
        tourBooking.setStatus("confirmed");
        tourBookingMapper.update(tourBooking);
        
        // 🔔 发送订单确认通知
        try {
            sendDetailedOrderNotification(tourBooking, "confirm", "管理员确认订单");
        } catch (Exception e) {
            log.error("❌ 发送订单确认通知失败: {}", e.getMessage(), e);
        }
        
        return true;
    }

    /**
     * 完成订单
     * 
     * @param bookingId 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean complete(Integer bookingId) {
        TourBooking tourBooking = tourBookingMapper.getById(bookingId);
        if (tourBooking == null) {
            log.error("订单不存在: {}", bookingId);
            return false;
        }
        
        // 只有已确认的订单可以完成
        if (!"confirmed".equals(tourBooking.getStatus())) {
            log.error("订单状态不允许完成: {}", tourBooking.getStatus());
            return false;
        }
        
        // 更新订单状态为已完成
        tourBooking.setStatus("completed");
        tourBookingMapper.update(tourBooking);
        
        // 🔔 发送订单完成通知
        try {
            sendDetailedOrderNotification(tourBooking, "complete", "管理员标记订单完成");
        } catch (Exception e) {
            log.error("❌ 发送订单完成通知失败: {}", e.getMessage(), e);
        }
        
        return true;
    }
    
    /**
     * 计算订单总价
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param groupSize 团队人数
     * @return 计算得到的总价
     */
    @Override
    public BigDecimal calculateTotalPrice(Integer tourId, String tourType, Long agentId, Integer groupSize) {
        // 调用带酒店等级参数的方法，默认使用4星酒店
        return calculateTotalPrice(tourId, tourType, agentId, groupSize, "4星");
    }
    
    /**
     * 计算订单总价（带酒店等级参数）
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param groupSize 团队人数
     * @param hotelLevel 酒店等级
     * @return 计算得到的总价
     */
    @Override
    public BigDecimal calculateTotalPrice(Integer tourId, String tourType, Long agentId, Integer groupSize, String hotelLevel) {
        if (tourId == null || tourType == null || groupSize == null || groupSize <= 0) {
            log.error("计算价格参数不完整: tourId={}, tourType={}, groupSize={}", tourId, tourType, groupSize);
            return BigDecimal.ZERO; // 参数不完整，返回0
        }
        
        BigDecimal unitPrice = BigDecimal.ZERO;
        int nights = 0; // 夜数，用于计算酒店价格差异
        
        // 根据旅游类型获取单价
        if ("day_tour".equals(tourType)) {
            DayTour dayTour = dayTourMapper.getById(tourId);
            if (dayTour == null) {
                log.error("一日游不存在: {}", tourId);
                return BigDecimal.ZERO;
            }
            unitPrice = dayTour.getPrice();
        } else if ("group_tour".equals(tourType)) {
            GroupTourDTO groupTour = groupTourMapper.getById(tourId);
            if (groupTour == null) {
                log.error("跟团游不存在: {}", tourId);
                return BigDecimal.ZERO;
            }
            // 从duration字段解析天数，然后计算夜数
            try {
                String duration = groupTour.getDuration();
                if (duration != null && duration.contains("天")) {
                    // 例如："5天4晚" -> 解析出天数5
                    String daysStr = duration.substring(0, duration.indexOf("天"));
                    int days = Integer.parseInt(daysStr);
                    nights = days > 1 ? days - 1 : 0; // 夜数 = 天数 - 1
                }
            } catch (Exception e) {
                log.warn("解析行程天数失败: {}", e.getMessage());
                // 默认至少一晚
                nights = 1;
            }
            
            // 如果有折扣价，使用折扣价
            if (groupTour.getDiscountedPrice() != null && groupTour.getDiscountedPrice().compareTo(BigDecimal.ZERO) > 0) {
                unitPrice = groupTour.getDiscountedPrice();
            } else {
                unitPrice = groupTour.getPrice();
            }
        } else {
            log.error("无效的旅游类型: {}", tourType);
            return BigDecimal.ZERO;
        }
        
        // 计算基础总价 (单价 * 人数)
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(groupSize));
        
        // 如果是跟团游，并且指定了酒店等级，计算酒店差价
        if ("group_tour".equals(tourType) && nights > 0 && hotelLevel != null) {
            try {
                // 获取酒店价格差异
                BigDecimal hotelPriceDiff = hotelPriceService.getPriceDifferenceByLevel(hotelLevel);
                
                // 计算酒店差价总额: 差价 * 夜数 * 人数 (由于房间共享，这里可能需要调整逻辑，视业务需求而定)
                BigDecimal totalHotelPriceDiff = hotelPriceDiff.multiply(BigDecimal.valueOf(nights));
                totalHotelPriceDiff = totalHotelPriceDiff.multiply(BigDecimal.valueOf(groupSize));
                
                // 添加酒店价格差异到总价
                totalPrice = totalPrice.add(totalHotelPriceDiff);
                
                log.info("计算酒店差价: 酒店等级={}, 每晚差价={}, 住宿晚数={}, 总差价={}",
                        hotelLevel, hotelPriceDiff, nights, totalHotelPriceDiff);
            } catch (Exception e) {
                log.error("计算酒店差价失败: {}", e.getMessage(), e);
                // 发生错误时不加酒店差价，使用原价继续
            }
        }
        
        // 如果是代理商订单，应用折扣率
        if (agentId != null) {
            try {
                // AgentMapper.getById方法接受Long类型参数
                Agent agent = agentMapper.getById(agentId);
                if (agent != null && agent.getDiscountRate() != null) {
                    // 折扣率为0到1之间的小数
                    totalPrice = totalPrice.multiply(agent.getDiscountRate());
                }
            } catch (Exception e) {
                log.error("获取代理商信息失败: {}", e.getMessage());
                // 当获取代理商信息失败时，继续使用原价计算
            }
        }
        
        // 保留两位小数
        totalPrice = totalPrice.setScale(2, RoundingMode.HALF_UP);
        
        log.info("计算订单价格: tourId={}, tourType={}, agentId={}, groupSize={}, hotelLevel={}, unitPrice={}, totalPrice={}",
                tourId, tourType, agentId, groupSize, hotelLevel, unitPrice, totalPrice);
                
        return totalPrice;
    }

    /**
     * 计算订单价格详情（带成人数、儿童数、酒店等级、房间数量和用户ID参数）
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param adultCount 成人数量
     * @param childCount 儿童数量
     * @param hotelLevel 酒店等级
     * @param roomCount 房间数量
     * @param userId 用户ID
     * @return 价格详情，包含总价、基础价格和额外房费
     */
    @Override
    public PriceDetailVO calculatePriceDetail(Integer tourId, String tourType, Long agentId, Integer adultCount, 
                                         Integer childCount, String hotelLevel, Integer roomCount, Long userId) {
        return calculatePriceDetail(tourId, tourType, agentId, adultCount, childCount, hotelLevel, roomCount, userId, null);
    }

    /**
     * 计算价格明细（带房型参数）
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param adultCount 成人数量
     * @param childCount 儿童数量
     * @param hotelLevel 酒店等级
     * @param roomCount 房间数量
     * @param userId 用户ID
     * @param roomType 房间类型
     * @return 价格明细
     */
    public PriceDetailVO calculatePriceDetail(Integer tourId, String tourType, Long agentId, Integer adultCount, 
                                         Integer childCount, String hotelLevel, Integer roomCount, Long userId, String roomType) {
        if (tourId == null || tourType == null || adultCount == null || adultCount < 0) {
            log.error("计算价格参数不完整: tourId={}, tourType={}, adultCount={}", tourId, tourType, adultCount);
            return PriceDetailVO.builder()
                    .totalPrice(BigDecimal.ZERO)
                    .basePrice(BigDecimal.ZERO)
                    .extraRoomFee(BigDecimal.ZERO)
                    .nonAgentPrice(BigDecimal.ZERO)
                    .build();
        }
        
        // 确保儿童数量不为null且不小于0
        if (childCount == null || childCount < 0) {
            childCount = 0;
        }
        
        // 房间数量默认为1
        if (roomCount == null || roomCount <= 0) {
            roomCount = 1;
        }
        
        BigDecimal originalPrice = BigDecimal.ZERO; // 原价
        BigDecimal discountedPrice = BigDecimal.ZERO; // 折扣价
        int nights = 0; // 夜数，用于计算酒店价格差异
        
        // 根据旅游类型获取原价
        if ("day_tour".equals(tourType)) {
            DayTour dayTour = dayTourMapper.getById(tourId);
            if (dayTour == null) {
                log.error("一日游不存在: {}", tourId);
                return PriceDetailVO.builder()
                        .totalPrice(BigDecimal.ZERO)
                        .basePrice(BigDecimal.ZERO)
                        .extraRoomFee(BigDecimal.ZERO)
                        .nonAgentPrice(BigDecimal.ZERO)
                        .build();
            }
            originalPrice = dayTour.getPrice();
            
        } else if ("group_tour".equals(tourType)) {
            GroupTourDTO groupTour = groupTourMapper.getById(tourId);
            if (groupTour == null) {
                log.error("跟团游不存在: {}", tourId);
                return PriceDetailVO.builder()
                        .totalPrice(BigDecimal.ZERO)
                        .basePrice(BigDecimal.ZERO)
                        .extraRoomFee(BigDecimal.ZERO)
                        .nonAgentPrice(BigDecimal.ZERO)
                        .build();
            }
            
            // 获取原价
            originalPrice = groupTour.getPrice();
            
            // 从duration字段解析天数，然后计算夜数
            try {
                String duration = groupTour.getDuration();
                if (duration != null && duration.contains("天")) {
                    // 例如："5天4晚" -> 解析出天数5
                    String daysStr = duration.substring(0, duration.indexOf("天"));
                    int days = Integer.parseInt(daysStr);
                    nights = days > 1 ? days - 1 : 0; // 夜数 = 天数 - 1
                }
            } catch (Exception e) {
                log.warn("解析行程天数失败: {}", e.getMessage());
                // 默认至少一晚
                nights = 1;
            }
        } else {
            log.error("无效的旅游类型: {}", tourType);
            return PriceDetailVO.builder()
                    .totalPrice(BigDecimal.ZERO)
                    .basePrice(BigDecimal.ZERO)
                    .extraRoomFee(BigDecimal.ZERO)
                    .nonAgentPrice(BigDecimal.ZERO)
                    .build();
        }
        
        // 应用折扣 - 如果是代理商订单
        BigDecimal discountRate = BigDecimal.ONE; // 默认折扣率为1（不打折）
        
        if (agentId != null) {
            try {
                Agent agent = agentMapper.getById(agentId);
                if (agent != null && agent.getDiscountRate() != null) {
                    discountRate = agent.getDiscountRate();
                    log.info("calculatePriceDetail: 获取到代理商折扣率 {} 用于代理商ID {}", discountRate, agentId);
                } else {
                    log.warn("calculatePriceDetail: 代理商ID {} 存在但未找到代理商或折扣率为null", agentId);
                }
            } catch (Exception e) {
                log.error("calculatePriceDetail: 获取代理商信息失败: {}", e.getMessage(), e);
            }
        } else {
            log.info("calculatePriceDetail: 未提供代理商ID，不应用折扣");
        }
        
        // 应用折扣率计算折扣价
        discountedPrice = originalPrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        log.info("calculatePriceDetail: 原价 = {}, 折扣率 = {}, 折扣后价格 = {}", originalPrice, discountRate, discountedPrice);
        
        // 总价计算逻辑
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal basePrice = BigDecimal.ZERO; // 基础价格（不含额外房费）
        BigDecimal extraRoomFee = BigDecimal.ZERO; // 额外房费
        
        // 获取基准酒店等级
        String baseHotelLevel = hotelPriceService.getBaseHotelLevel();
        
        // 总人数
        int totalPeople = adultCount + childCount;
        
        // 计算成人总价格 = 成人数 * 折扣价
        BigDecimal adultTotalPrice = discountedPrice.multiply(BigDecimal.valueOf(adultCount));
        log.info("calculatePriceDetail: 成人总价 = {} (成人数 {} * 折扣后单价 {})", 
                adultTotalPrice, adultCount, discountedPrice);
        
        // 计算儿童总价格 = 儿童数 * (折扣价 - 50)，确保最低价格不小于0
        BigDecimal childDiscount = new BigDecimal("50"); // 儿童价格减少50
        BigDecimal childUnitPrice = discountedPrice.subtract(childDiscount);
        // 确保儿童单价不小于0
        if (childUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
            childUnitPrice = BigDecimal.ZERO;
        }
        BigDecimal childTotalPrice = childUnitPrice.multiply(BigDecimal.valueOf(childCount));
        log.info("calculatePriceDetail: 儿童总价 = {} (儿童数 {} * 儿童单价 {})", 
                childTotalPrice, childCount, childUnitPrice);
        
        // 基础总价 = 成人总价 + 儿童总价
        totalPrice = adultTotalPrice.add(childTotalPrice);
        basePrice = totalPrice; // 初始化基础价格
        log.info("calculatePriceDetail: 初始基础总价 = {}", basePrice);
        
        // 计算单房差和房间价格 - 仅适用于跟团游
        if ("group_tour".equals(tourType) && nights > 0) {
            // 从数据库直接获取单房差
            BigDecimal singleRoomSupplement = hotelPriceService.getDailySingleRoomSupplementByLevel(hotelLevel);
            
            // 从数据库获取房间价格，根据房型选择不同的价格
            BigDecimal roomPrice = getRoomPriceByType(hotelLevel, roomType);
            
            // 计算三人房差价费用（如果选择了三人房）
            if (roomType != null && (roomType.contains("三人间") || roomType.contains("三床") || roomType.contains("家庭") || 
                roomType.equalsIgnoreCase("triple") || roomType.equalsIgnoreCase("family"))) {
                // 获取三人房差价
                BigDecimal tripleDifference = hotelPriceService.getTripleBedRoomPriceDifferenceByLevel(hotelLevel);
                // 计算三人房差价费用 = 三人房差价 * 住宿晚数 * 房间数
                BigDecimal tripleRoomFee = tripleDifference.multiply(BigDecimal.valueOf(nights)).multiply(BigDecimal.valueOf(roomCount));
                
                totalPrice = totalPrice.add(tripleRoomFee);
                extraRoomFee = extraRoomFee.add(tripleRoomFee);
                
                log.info("三人房差价费用: 每晚差价={}, 住宿晚数={}, 房间数={}, 总差价费用={}",
                        tripleDifference, nights, roomCount, tripleRoomFee);
            }
            
            // 计算总房间数（可能有小数部分）
            double totalRooms = totalPeople / 2.0;
            
            // 包含在价格中的房间数向下取整
            int includedRoomsFloor = (int) Math.floor(totalRooms);
            
            // 包含在价格中的房间数向上取整
            int includedRoomsCeil = (int) Math.ceil(totalRooms);
            
            // 客户要求的房间数
            int requestedRooms = roomCount;
            
            // 情况1：客户要求的房间数 <= 总房间数向下取整，不收额外费用
            if (requestedRooms <= includedRoomsFloor) {
                log.info("客户人数={}, 要求房间数={}, 总房间数={}, 不收取额外费用",
                        totalPeople, requestedRooms, totalRooms);
                // 不收取额外费用，basePrice保持不变
            }
            // 情况2：客户要求的房间数 = 总房间数向上取整，只收取小数部分的单房差
            else if (requestedRooms == includedRoomsCeil && totalRooms > includedRoomsFloor) {
                // 单房差费用 = 单房差 * 住宿晚数
                // 不需要乘以小数部分，单房差是固定费用
                BigDecimal singleSupplementCost = singleRoomSupplement
                    .multiply(BigDecimal.valueOf(nights));
                
                totalPrice = totalPrice.add(singleSupplementCost);
                extraRoomFee = extraRoomFee.add(singleSupplementCost);
                
                log.info("应用单房差: 每晚单房差={}, 住宿晚数={}, 总单房差={}",
                        singleRoomSupplement, nights, singleSupplementCost);
            }
            // 情况3：客户要求的房间数 > 总房间数向上取整，需要额外房间费用
            else if (requestedRooms > includedRoomsCeil) {
                // 首先计算是否需要单房差（如果有小数部分）
                if (totalRooms > includedRoomsFloor) {
                    // 单房差费用 = 单房差 * 住宿晚数
                    BigDecimal singleSupplementCost = singleRoomSupplement
                        .multiply(BigDecimal.valueOf(nights));
                    
                    totalPrice = totalPrice.add(singleSupplementCost);
                    extraRoomFee = extraRoomFee.add(singleSupplementCost);
                    
                    log.info("应用单房差: 每晚单房差={}, 住宿晚数={}, 总单房差={}",
                            singleRoomSupplement, nights, singleSupplementCost);
                }
                
                // 计算额外房间数（超出包含房间数的部分）
                int extraRooms = requestedRooms - includedRoomsCeil;
                
                // 额外房间费用 = 房间价格 * 住宿晚数 * 额外房间数
                BigDecimal extraRoomCost = roomPrice.multiply(BigDecimal.valueOf(nights * extraRooms));
                
                // 额外房间不再收取单房差，只收取房间价格
                BigDecimal totalExtraCost = extraRoomCost;
                totalPrice = totalPrice.add(totalExtraCost);
                extraRoomFee = extraRoomFee.add(totalExtraCost);
                
                log.info("额外房间费用: 每晚房价={}, 住宿晚数={}, 额外房间数={}, 总额外费用={}",
                        roomPrice, nights, extraRooms, totalExtraCost);
            }
            
            // 酒店等级升级费用 - 只有当选择的酒店等级不是基准等级时
            if (!hotelLevel.equals(baseHotelLevel)) {
                // 从数据库直接获取酒店等级差价
                BigDecimal hotelPriceDiff = hotelPriceService.getPriceDifferenceByLevel(hotelLevel);
                
                // 计算总升级费用 = 夜数 * 实际房间数 * 酒店等级差价
                // 注意：只对客户实际使用的房间收取升级费用
                BigDecimal totalHotelUpgradeCost = hotelPriceDiff
                    .multiply(BigDecimal.valueOf(nights))
                    .multiply(BigDecimal.valueOf(requestedRooms));
                
                totalPrice = totalPrice.add(totalHotelUpgradeCost);
                extraRoomFee = extraRoomFee.add(totalHotelUpgradeCost); // 升级费用计入额外房费
                
                log.info("酒店升级费用: 每晚升级差价={}, 住宿晚数={}, 房间数={}, 总升级费用={}",
                        hotelPriceDiff, nights, requestedRooms, totalHotelUpgradeCost);
            }
        }
        
        // 记录用户ID用于可能的后续处理或日志记录
        if (userId != null) {
            log.info("用户ID: {}", userId);
            // 这里可以添加基于userId的特殊逻辑，如会员折扣等
        }
        
        // 保留两位小数
        totalPrice = totalPrice.setScale(2, RoundingMode.HALF_UP);
        basePrice = basePrice.setScale(2, RoundingMode.HALF_UP);
        
        // 计算非代理商价格（普通用户价格）- 只有在是代理商的情况下才不同
        BigDecimal nonAgentPrice = totalPrice;
        if (agentId != null && discountRate.compareTo(BigDecimal.ONE) < 0) {
            // 如果是代理商且有折扣，需要重新计算非代理商价格
            
            // 正确的计算方法：重新计算未打折的基础价格
            BigDecimal nonAgentBasePrice = BigDecimal.ZERO;
            
            // 成人原价总和（不打折）
            BigDecimal adultOriginalPrice = originalPrice.multiply(BigDecimal.valueOf(adultCount));
            
            // 儿童原价总和（不打折，但减去儿童折扣）
            BigDecimal childOriginalUnitPrice = originalPrice.subtract(childDiscount);
            if (childOriginalUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
                childOriginalUnitPrice = BigDecimal.ZERO;
            }
            BigDecimal childOriginalPrice = childOriginalUnitPrice.multiply(BigDecimal.valueOf(childCount));
            
            // 非代理商基础价格 = 成人原价 + 儿童原价
            nonAgentBasePrice = adultOriginalPrice.add(childOriginalPrice);
            
            // 酒店等级升级费用 - 只有当选择的酒店等级不是基准等级时
            if ("group_tour".equals(tourType) && nights > 0 && !hotelLevel.equals(hotelPriceService.getBaseHotelLevel())) {
                // 从数据库直接获取酒店等级差价
                BigDecimal hotelPriceDiff = hotelPriceService.getPriceDifferenceByLevel(hotelLevel);
                
                // 计算总升级费用 = 夜数 * 实际房间数 * 酒店等级差价
                BigDecimal totalHotelUpgradeCost = hotelPriceDiff
                    .multiply(BigDecimal.valueOf(nights))
                    .multiply(BigDecimal.valueOf(roomCount));
                
                // 酒店升级费用计入额外房费，而不是基础价格
                extraRoomFee = extraRoomFee.add(totalHotelUpgradeCost);
                
                log.info("非代理商酒店升级费用: 每晚升级差价={}, 住宿晚数={}, 房间数={}, 总升级费用={}",
                        hotelPriceDiff, nights, roomCount, totalHotelUpgradeCost);
            }
            
            // 非代理商价格 = 非代理商基础价格 + 额外房费（与代理商相同）
            nonAgentPrice = nonAgentBasePrice.add(extraRoomFee).setScale(2, RoundingMode.HALF_UP);
            
            log.info("代理商价格计算: 折扣率={}, 成人原价={}, 儿童原价={}, 非代理商基础价格={}, 额外房费={}, 代理商价格={}, 非代理商价格={}",
                    discountRate, adultOriginalPrice, childOriginalPrice, nonAgentBasePrice, extraRoomFee, totalPrice, nonAgentPrice);
        }
        
        log.info("最终价格明细: 总价={}, 基础价格={}, 额外房费={}, 非代理商价格={}", 
                totalPrice, basePrice, extraRoomFee, nonAgentPrice);
        log.info("计算订单价格: tourId={}, tourType={}, agentId={}, adultCount={}, childCount={}, hotelLevel={}, roomCount={}, userId={}, originalPrice={}, discountedPrice={}, totalPrice={}",
                tourId, tourType, agentId, adultCount, childCount, hotelLevel, roomCount, userId, originalPrice, discountedPrice, totalPrice);
        
        // 获取当前用户类型，判断是否为操作员
        String userType = BaseContext.getCurrentUserType();
        boolean isOperator = "agent_operator".equals(userType);
        
        // 构建并返回价格详情VO
        PriceDetailVO.PriceDetailVOBuilder builder = PriceDetailVO.builder()
                .basePrice(basePrice)
                .extraRoomFee(extraRoomFee)
                .nonAgentPrice(nonAgentPrice)
                .originalPrice(originalPrice)
                .discountRate(discountRate);
        
        if (isOperator) {
            // 操作员：显示原价，但内部记录实际支付价格（折扣价）
            builder.totalPrice(nonAgentPrice)  // 显示原价
                   .actualPaymentPrice(totalPrice)  // 实际支付价格（折扣价）
                   .showDiscount(false);  // 不显示折扣信息
            log.info("操作员价格显示: 显示价格={}, 实际支付价格={}", nonAgentPrice, totalPrice);
        } else {
            // 代理商主账号或普通用户：显示真实价格
            builder.totalPrice(totalPrice)
                   .actualPaymentPrice(totalPrice)
                   .showDiscount(agentId != null && discountRate.compareTo(BigDecimal.ONE) < 0);
            log.info("非操作员价格显示: 显示价格={}, 实际支付价格={}", totalPrice, totalPrice);
        }
        
        return builder.build();
    }

    /**
     * 根据ID获取一日游信息
     * 
     * @param tourId 一日游ID
     * @return 一日游信息
     */
    @Override
    public DayTour getDayTourById(Integer tourId) {
        if (tourId == null) {
            return null;
        }
        try {
            return dayTourMapper.getById(tourId);
        } catch (Exception e) {
            log.error("获取一日游信息失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 根据ID获取跟团游信息
     * 
     * @param tourId 跟团游ID
     * @return 跟团游信息
     */
    @Override
    public GroupTourDTO getGroupTourById(Integer tourId) {
        if (tourId == null) {
            return null;
        }
        try {
            return groupTourMapper.getById(tourId);
        } catch (Exception e) {
            log.error("获取跟团游信息失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取代理商折扣率
     * 
     * @param agentId 代理商ID
     * @return 折扣率（0-1之间的小数）
     */
    @Override
    public BigDecimal getAgentDiscountRate(Long agentId) {
        if (agentId == null) {
            log.info("获取代理商折扣率: 代理商ID为null，返回默认折扣率1.0");
            return BigDecimal.ONE;
        }
        
        try {
            Agent agent = agentMapper.getById(agentId);
            if (agent != null && agent.getDiscountRate() != null) {
                log.info("获取代理商折扣率成功: 代理商ID={}, 名称={}, 折扣率={}", 
                        agentId, agent.getCompanyName(), agent.getDiscountRate());
                return agent.getDiscountRate();
            } else {
                log.warn("获取代理商折扣率: 代理商ID={}不存在或折扣率为null，返回默认折扣率1.0", agentId);
            }
            return BigDecimal.ONE;
        } catch (Exception e) {
            log.error("获取代理商折扣率失败: {} - {}", agentId, e.getMessage(), e);
            return BigDecimal.ONE;
        }
    }

    /**
     * 计算订单总价（带成人数、儿童数、酒店等级、房间数量和用户ID参数）
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param adultCount 成人数量
     * @param childCount 儿童数量
     * @param hotelLevel 酒店等级
     * @param roomCount 房间数量
     * @param userId 用户ID
     * @return 计算得到的总价
     */
    @Override
    public BigDecimal calculateTotalPrice(Integer tourId, String tourType, Long agentId, Integer adultCount, 
                                         Integer childCount, String hotelLevel, Integer roomCount, Long userId) {
        // 调用价格详情方法，然后只返回总价
        PriceDetailVO priceDetail = calculatePriceDetail(tourId, tourType, agentId, adultCount, 
                                                        childCount, hotelLevel, roomCount, userId);
        return priceDetail.getTotalPrice();
    }

    /**
     * 计算订单总价（带酒店等级、房间数量和用户ID参数）
     * 
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型 (day_tour/group_tour)
     * @param agentId 代理商ID，如果是普通用户则为null
     * @param groupSize 团队人数
     * @param hotelLevel 酒店等级
     * @param roomCount 房间数量
     * @param userId 用户ID
     * @return 计算得到的总价
     */
    @Override
    public BigDecimal calculateTotalPrice(Integer tourId, String tourType, Long agentId, Integer groupSize, 
                                         String hotelLevel, Integer roomCount, Long userId) {
        // 调用新方法，将groupSize作为成人数量，儿童数量为0
        return calculateTotalPrice(tourId, tourType, agentId, groupSize, 0, hotelLevel, roomCount, userId);
    }

    /**
     * 支付订单
     * 
     * @param bookingId 订单ID
     * @param paymentDTO 支付信息
     * @return 是否支付成功
     */
    @Override
    @Transactional
    public Boolean payBooking(Integer bookingId, PaymentDTO paymentDTO) {
        try {
            log.info("处理订单支付，订单ID：{}，支付数据：{}", bookingId, paymentDTO);
            
            // 查询订单
            TourBooking tourBooking = tourBookingMapper.getById(bookingId);
            if (tourBooking == null) {
                log.error("订单不存在，ID：{}", bookingId);
                return false;
            }
            
            // 检查订单状态，只有未支付的订单可以支付
            if (!"unpaid".equals(tourBooking.getPaymentStatus())) {
                log.error("订单已支付，无需重复支付，ID：{}", bookingId);
                return false;
            }
            
            // 更新订单支付状态
            tourBooking.setPaymentStatus("paid");
            tourBookingMapper.updatePaymentStatus(bookingId, "paid");
            
            // 如果订单状态是pending，则更新为confirmed
            if ("pending".equals(tourBooking.getStatus())) {
                tourBooking.setStatus("confirmed");
                tourBookingMapper.updateStatus(bookingId, "confirmed");
            }
            
            // 更新订单的更新时间
            tourBooking.setUpdatedAt(LocalDateTime.now());
            // 使用update方法更新整个订单对象
            tourBookingMapper.update(tourBooking);
            
            // 🔔 发送订单支付成功通知
            try {
                Double amount = tourBooking.getTotalPrice() != null ? 
                              tourBooking.getTotalPrice().doubleValue() : 0.0;
                String actionDetail = String.format("支付金额: $%.2f", amount);
                
                sendDetailedOrderNotification(tourBooking, "payment", actionDetail);
            } catch (Exception e) {
                log.error("❌ 发送订单支付成功通知失败: {}", e.getMessage(), e);
            }
            
            log.info("订单支付成功，ID：{}", bookingId);
            return true;
        } catch (Exception e) {
            log.error("处理订单支付时发生错误", e);
            throw new RuntimeException("支付处理失败：" + e.getMessage());
        }
    }

    /**
     * 根据房型获取相应的房间价格
     * 
     * @param hotelLevel 酒店等级
     * @param roomType 房间类型
     * @return 房间价格
     */
    private BigDecimal getRoomPriceByType(String hotelLevel, String roomType) {
        // 默认使用标准双人房价格
        BigDecimal roomPrice = hotelPriceService.getHotelRoomPriceByLevel(hotelLevel);
        
        // 根据房型选择不同的价格
        if (roomType != null) {
            // 双人间相关的房型
            if (roomType.contains("双人间") || roomType.contains("双床") || roomType.contains("标准双") || 
                roomType.equalsIgnoreCase("twin") || roomType.equalsIgnoreCase("double")) {
                roomPrice = hotelPriceService.getHotelRoomPriceByLevel(hotelLevel);
                log.info("使用双人间房价格: {} (房型: {})", roomPrice, roomType);
            } 
            // 三人间相关的房型 - 使用基础价格加上三人房差价
            else if (roomType.contains("三人间") || roomType.contains("三床") || roomType.contains("家庭") || 
                     roomType.equalsIgnoreCase("triple") || roomType.equalsIgnoreCase("family")) {
                BigDecimal basePrice = hotelPriceService.getHotelRoomPriceByLevel(hotelLevel);
                BigDecimal tripleDifference = hotelPriceService.getTripleBedRoomPriceDifferenceByLevel(hotelLevel);
                roomPrice = basePrice.add(tripleDifference);
                log.info("使用三人间房价格: {} = 基础价格{} + 三人房差价{} (房型: {})", 
                         roomPrice, basePrice, tripleDifference, roomType);
            } 
            // 单人间相关的房型
            else if (roomType.contains("单人间") || roomType.contains("单床") || 
                     roomType.equalsIgnoreCase("single")) {
                roomPrice = hotelPriceService.getHotelRoomPriceByLevel(hotelLevel);
                log.info("使用单人间房价格: {} (房型: {})", roomPrice, roomType);
            } else {
                log.info("使用标准房价格: {} (未识别房型: {})", roomPrice, roomType);
            }
        } else {
            log.info("使用标准房价格: {} (房型为空)", roomPrice);
        }
        
        return roomPrice;
    }

    /**
     * 更新旅游订单详细信息（适用于代理商修改订单）
     * 
     * @param updateDTO 订单更新信息
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateBookingDetails(TourBookingUpdateDTO updateDTO) {
        log.info("更新订单详细信息: {}", updateDTO);
        
        if (updateDTO == null || updateDTO.getBookingId() == null) {
            throw new BusinessException("订单ID不能为空");
        }
        
        // 查询原订单信息
        TourBooking existingBooking = tourBookingMapper.getById(updateDTO.getBookingId());
        if (existingBooking == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 只有未支付且待确认的订单才可以修改
        if (!"unpaid".equals(existingBooking.getPaymentStatus()) || 
            !("pending".equals(existingBooking.getStatus()) || "confirmed".equals(existingBooking.getStatus()))) {
            throw new BusinessException("只有未支付且待确认或已确认的订单可以修改");
        }
        
        // 构建更新对象
        TourBooking bookingToUpdate = new TourBooking();
        bookingToUpdate.setBookingId(updateDTO.getBookingId());
        
        // 设置可以更新的字段
        // 1. 航班信息
        if (updateDTO.getFlightNumber() != null) {
            bookingToUpdate.setFlightNumber(updateDTO.getFlightNumber());
        }
        if (updateDTO.getReturnFlightNumber() != null) {
            bookingToUpdate.setReturnFlightNumber(updateDTO.getReturnFlightNumber());
        }
        if (updateDTO.getArrivalDepartureTime() != null) {
            bookingToUpdate.setArrivalDepartureTime(updateDTO.getArrivalDepartureTime());
        }
        if (updateDTO.getArrivalLandingTime() != null) {
            bookingToUpdate.setArrivalLandingTime(updateDTO.getArrivalLandingTime());
        }
        if (updateDTO.getDepartureDepartureTime() != null) {
            bookingToUpdate.setDepartureDepartureTime(updateDTO.getDepartureDepartureTime());
        }
        if (updateDTO.getDepartureLandingTime() != null) {
            bookingToUpdate.setDepartureLandingTime(updateDTO.getDepartureLandingTime());
        }
        
        // 2. 行程日期
        boolean datesChanged = false;
        if (updateDTO.getTourStartDate() != null) {
            bookingToUpdate.setTourStartDate(updateDTO.getTourStartDate());
            datesChanged = true;
        }
        if (updateDTO.getTourEndDate() != null) {
            bookingToUpdate.setTourEndDate(updateDTO.getTourEndDate());
            datesChanged = true;
        }
        
        // 3. 接送信息
        if (updateDTO.getPickupDate() != null) {
            bookingToUpdate.setPickupDate(updateDTO.getPickupDate());
        }
        if (updateDTO.getDropoffDate() != null) {
            bookingToUpdate.setDropoffDate(updateDTO.getDropoffDate());
        }
        if (updateDTO.getPickupLocation() != null) {
            bookingToUpdate.setPickupLocation(updateDTO.getPickupLocation());
        }
        if (updateDTO.getDropoffLocation() != null) {
            bookingToUpdate.setDropoffLocation(updateDTO.getDropoffLocation());
        }
        
        // 4. 人数信息
        boolean personCountChanged = false;
        if (updateDTO.getAdultCount() != null) {
            bookingToUpdate.setAdultCount(updateDTO.getAdultCount());
            personCountChanged = true;
        }
        if (updateDTO.getChildCount() != null) {
            bookingToUpdate.setChildCount(updateDTO.getChildCount());
            personCountChanged = true;
        }
        if (updateDTO.getLuggageCount() != null) {
            bookingToUpdate.setLuggageCount(updateDTO.getLuggageCount());
        }
        
        // 更新团队规模
        if (personCountChanged) {
            int adultCount = updateDTO.getAdultCount() != null ? updateDTO.getAdultCount() : existingBooking.getAdultCount();
            int childCount = updateDTO.getChildCount() != null ? updateDTO.getChildCount() : existingBooking.getChildCount();
            bookingToUpdate.setGroupSize(adultCount + childCount);
        }
        
        // 5. 联系人信息
        if (updateDTO.getContactPerson() != null) {
            bookingToUpdate.setContactPerson(updateDTO.getContactPerson());
        }
        if (updateDTO.getContactPhone() != null) {
            bookingToUpdate.setContactPhone(updateDTO.getContactPhone());
            bookingToUpdate.setPassengerContact(updateDTO.getContactPhone());
        }
        
        // 6. 酒店信息
        boolean hotelInfoChanged = false;
        if (updateDTO.getHotelLevel() != null) {
            bookingToUpdate.setHotelLevel(updateDTO.getHotelLevel());
            hotelInfoChanged = true;
        }
        if (updateDTO.getRoomType() != null) {
            bookingToUpdate.setRoomType(updateDTO.getRoomType());
            hotelInfoChanged = true;
        }
        if (updateDTO.getHotelRoomCount() != null) {
            bookingToUpdate.setHotelRoomCount(updateDTO.getHotelRoomCount());
            hotelInfoChanged = true;
        }
        if (updateDTO.getRoomDetails() != null) {
            bookingToUpdate.setRoomDetails(updateDTO.getRoomDetails());
        }
        if (updateDTO.getHotelCheckInDate() != null) {
            bookingToUpdate.setHotelCheckInDate(updateDTO.getHotelCheckInDate());
        }
        if (updateDTO.getHotelCheckOutDate() != null) {
            bookingToUpdate.setHotelCheckOutDate(updateDTO.getHotelCheckOutDate());
        }
        
        // 7. 特殊要求
        if (updateDTO.getSpecialRequests() != null) {
            bookingToUpdate.setSpecialRequests(updateDTO.getSpecialRequests());
        }
        
        // 如果人数、日期或酒店信息有变化，重新计算价格
        if (personCountChanged || datesChanged || hotelInfoChanged) {
            // 获取当前价格计算所需的信息
            Integer adultCount = updateDTO.getAdultCount() != null ? updateDTO.getAdultCount() : existingBooking.getAdultCount();
            Integer childCount = updateDTO.getChildCount() != null ? updateDTO.getChildCount() : existingBooking.getChildCount();
            String hotelLevel = updateDTO.getHotelLevel() != null ? updateDTO.getHotelLevel() : existingBooking.getHotelLevel();
            Integer hotelRoomCount = updateDTO.getHotelRoomCount() != null ? updateDTO.getHotelRoomCount() : existingBooking.getHotelRoomCount();
            String roomType = updateDTO.getRoomType() != null ? updateDTO.getRoomType() : existingBooking.getRoomType();
            
            try {
                // 重新计算价格
                PriceDetailVO priceDetail = calculatePriceDetail(
                    existingBooking.getTourId(),
                    existingBooking.getTourType(),
                    existingBooking.getAgentId() != null ? existingBooking.getAgentId().longValue() : null,
                    adultCount,
                    childCount,
                    hotelLevel,
                    hotelRoomCount,
                    existingBooking.getUserId() != null ? existingBooking.getUserId().longValue() : null,
                    roomType
                );
                
                // 更新订单总价
                bookingToUpdate.setTotalPrice(priceDetail.getTotalPrice());
            } catch (Exception e) {
                log.error("重新计算价格失败", e);
                throw new BusinessException("订单修改失败：重新计算价格时出错");
            }
        }
        
        // 更新修改时间
        bookingToUpdate.setUpdatedAt(LocalDateTime.now());
        
        // 执行更新
        tourBookingMapper.update(bookingToUpdate);
        
        // 如果有乘客信息需要更新
        if (updateDTO.getPassengers() != null && !updateDTO.getPassengers().isEmpty()) {
            try {
                // 使用passengerService更新乘客信息
                for (PassengerDTO passengerDTO : updateDTO.getPassengers()) {
                    // 如果乘客是已存在的，更新信息
                    if (passengerDTO.getPassengerId() != null) {
                        passengerService.update(passengerDTO);
                    } else {
                        // 否则添加新乘客到订单
                        passengerService.addPassengerToBooking(updateDTO.getBookingId(), passengerDTO);
                    }
                }
            } catch (Exception e) {
                log.error("更新乘客信息失败", e);
                throw new BusinessException("订单修改失败：更新乘客信息时出错");
            }
        }
        
        // 🔔 发送订单修改通知
        try {
            sendDetailedOrderNotification(existingBooking, "modify", "修改订单详细信息");
        } catch (Exception e) {
            log.error("❌ 发送订单修改通知失败: {}", e.getMessage(), e);
        }
        
        return true;
    }

    /**
     * 获取当前操作者信息
     * @return [操作者姓名, 操作者类型, 操作者ID]
     */
    private String[] getCurrentOperatorInfo() {
        try {
            String userType = BaseContext.getCurrentUserType();
            Long currentId = BaseContext.getCurrentId();
            Long agentId = BaseContext.getCurrentAgentId();
            Long operatorId = BaseContext.getCurrentOperatorId();

            if ("agent".equals(userType)) {
                // 代理商主账号
                if (agentId != null) {
                    Agent agent = agentMapper.getById(agentId);
                    String agentName = agent != null ? 
                        (agent.getCompanyName() != null ? agent.getCompanyName() : agent.getContactPerson()) 
                        : "未知中介";
                    return new String[]{agentName, "agent", String.valueOf(agentId)};
                }
            } else if ("agent_operator".equals(userType)) {
                // 操作员账号
                if (operatorId != null) {
                    // 这里需要假设有AgentOperator实体和相关方法，根据实际情况调整
                    // AgentOperator operator = agentOperatorMapper.getById(operatorId);
                    // String operatorName = operator != null ? operator.getName() : "未知操作员";
                    return new String[]{"操作员", "operator", String.valueOf(operatorId)};
                }
            } else if ("user".equals(userType)) {
                // 普通用户
                if (currentId != null) {
                    // 这里需要根据User实体的实际字段调整
                    // User user = userMapper.getById(currentId);
                    // String userName = user != null ? user.getName() : "未知用户";
                    return new String[]{"用户", "user", String.valueOf(currentId)};
                }
            }

            return new String[]{"未知操作者", "unknown", "0"};
        } catch (Exception e) {
            log.error("获取操作者信息失败: {}", e.getMessage(), e);
            return new String[]{"系统", "system", "0"};
        }
    }

    /**
     * 自动同步订单数据到排团表
     * 供订单创建时自动调用
     */
    @Override
    @Transactional
    public void autoSyncOrderToScheduleTable(Integer bookingId) {
        TourBooking tourBooking = tourBookingMapper.getById(bookingId);
        if (tourBooking == null) {
            log.warn("订单不存在，无法自动同步到排团表: {}", bookingId);
            return;
        }
        
        log.info("🔄 开始自动同步订单到排团表: 订单ID={}, 订单号={}", bookingId, tourBooking.getOrderNumber());
        
        try {
            // 先删除该订单可能已存在的排团记录（防止重复）
            tourScheduleOrderMapper.deleteByBookingId(bookingId);
            
            // 创建新的排团记录
            autoCreateScheduleOrderFromBooking(tourBooking);
            
            log.info("✅ 自动同步订单数据到排团表完成，订单ID: {}", bookingId);
        } catch (Exception e) {
            log.error("❌ 自动同步订单到排团表失败: 订单ID={}, 错误: {}", bookingId, e.getMessage(), e);
            throw e;
        }
    }
    


    /**
     * 自动创建排团记录
     * 根据行程天数创建多条排团记录
     */
    private void autoCreateScheduleOrderFromBooking(TourBooking tourBooking) {
        if (tourBooking == null || tourBooking.getBookingId() == null) {
            log.warn("订单信息为空，跳过自动创建排团记录");
            return;
        }

        try {
            // 计算行程天数
            int tourDays = calculateTourDays(tourBooking.getTourStartDate(), tourBooking.getTourEndDate());
            
            // 获取产品信息用于生成行程标题
            String tourName = getTourName(tourBooking.getTourId(), tourBooking.getTourType());
            
            log.info("📅 开始为订单 {} 创建 {} 天的排团记录", tourBooking.getOrderNumber(), tourDays);
            
            // 为每一天创建排团记录
            for (int day = 1; day <= tourDays; day++) {
                TourScheduleOrder scheduleOrder = createScheduleOrderFromBooking(tourBooking, day, tourName);
                
                // 🔥 重新设置智能航班信息分配
                setSmartFlightInfo(scheduleOrder, tourBooking, day, tourDays);
                
                tourScheduleOrderMapper.insert(scheduleOrder);
                log.info("✅ 创建排团记录: 订单ID={}, 第{}天, 日期={}", 
                    tourBooking.getBookingId(), day, scheduleOrder.getTourDate());
            }
            
            log.info("✅ 订单 {} 的所有排团记录创建完成，共{}天", tourBooking.getOrderNumber(), tourDays);
        } catch (Exception e) {
            log.error("❌ 自动创建排团记录时出错: 订单ID={}, 错误: {}", tourBooking.getBookingId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 计算行程天数
     */
    private int calculateTourDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 1; // 默认1天
        }
        
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return (int) Math.max(1, days); // 至少1天
    }

    /**
     * 获取产品名称
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
     * 从订单创建排团记录
     */
    private TourScheduleOrder createScheduleOrderFromBooking(TourBooking booking, int dayNumber, String tourName) {
        TourScheduleOrder scheduleOrder = new TourScheduleOrder();
        
        // 设置必填字段
        scheduleOrder.setBookingId(booking.getBookingId());
        scheduleOrder.setDayNumber(dayNumber);
        scheduleOrder.setTourId(booking.getTourId());
        scheduleOrder.setTourType(booking.getTourType());
        
        // 计算当天的日期
        if (booking.getTourStartDate() != null) {
            LocalDate tourDate = booking.getTourStartDate().plusDays(dayNumber - 1);
            scheduleOrder.setTourDate(tourDate);
        } else {
            scheduleOrder.setTourDate(LocalDate.now());
        }
        
        // 智能生成行程标题（从产品行程详情中获取）
        String title = getItineraryTitleFromProduct(booking.getTourId(), booking.getTourType(), dayNumber, tourName);
        scheduleOrder.setTitle(title);
        
        // 智能设置接送地点逻辑
        int totalDays = calculateTourDays(booking.getTourStartDate(), booking.getTourEndDate());
        boolean isFirstDay = dayNumber == 1;
        boolean isLastDay = dayNumber == totalDays;
        
        String pickupLocation = "";
        String dropoffLocation = "";
        
        if (isFirstDay) {
            // 第一天：接客地点=订单pickup_location，送客地点=酒店(未开发，暂时空着)
            pickupLocation = booking.getPickupLocation() != null ? booking.getPickupLocation() : "";
            dropoffLocation = ""; // 送客地点是酒店，等酒店系统开发完成
            scheduleOrder.setDescription("行程开始，机场/酒店接客服务");
            log.info("📍 第一天接送地点设置 - 订单{} 第{}天: 接客地点=\"{}\", 送客地点=酒店(未开发)", 
                    booking.getBookingId(), dayNumber, pickupLocation);
        } else if (isLastDay) {
            // 最后一天：接客地点=酒店(未开发，暂时空着)，送客地点=订单dropoff_location
            pickupLocation = ""; // 接客地点是酒店，等酒店系统开发完成
            dropoffLocation = booking.getDropoffLocation() != null ? booking.getDropoffLocation() : "";
            scheduleOrder.setDescription("行程结束，送客至机场/指定地点");
            log.info("📍 最后一天接送地点设置 - 订单{} 第{}天: 接客地点=酒店(未开发), 送客地点=\"{}\"", 
                    booking.getBookingId(), dayNumber, dropoffLocation);
        } else {
            // 中间天数：都是酒店到酒店，等酒店系统开发完成后再决定
            pickupLocation = ""; // 等酒店系统开发
            dropoffLocation = ""; // 等酒店系统开发
            scheduleOrder.setDescription(String.format("第%d天行程，酒店接送服务", dayNumber));
            log.info("📍 中间天数接送地点设置 - 订单{} 第{}天: 等酒店系统开发", 
                    booking.getBookingId(), dayNumber);
        }
        
        // 设置智能分配的接送地点
        scheduleOrder.setPickupLocation(pickupLocation);
        scheduleOrder.setDropoffLocation(dropoffLocation);
        
        // 设置显示顺序
        scheduleOrder.setDisplayOrder(dayNumber);
        
        // 设置排团特有字段
        scheduleOrder.setTourName(tourName);
        
        // 🆕 设置乘客信息（从乘客表获取完整信息）
        try {
            // 获取该订单的乘客信息
            List<PassengerVO> passengers = passengerService.getByBookingId(booking.getBookingId());
            if (passengers != null && !passengers.isEmpty()) {
                // 将乘客信息转换为详细格式存储到itinerary_details字段
                StringBuilder passengerInfo = new StringBuilder();
                passengerInfo.append("乘客信息:\n");
                
                int adultCount = 0;
                int childCount = 0;
                
                for (int i = 0; i < passengers.size(); i++) {
                    PassengerVO passenger = passengers.get(i);
                    String fullName = passenger.getFullName() != null ? passenger.getFullName() : "未知乘客";
                    boolean isChild = passenger.getIsChild() != null && passenger.getIsChild();
                    
                    if (isChild) {
                        childCount++;
                        passengerInfo.append(String.format("  儿童%d: %s", childCount, fullName));
                        if (passenger.getChildAge() != null && !passenger.getChildAge().trim().isEmpty()) {
                            passengerInfo.append(String.format("(年龄:%s)", passenger.getChildAge()));
                        }
                    } else {
                        adultCount++;
                        passengerInfo.append(String.format("  成人%d: %s", adultCount, fullName));
                    }
                    
                    // 添加护照信息（如果有）
                    if (passenger.getPassportNumber() != null && !passenger.getPassportNumber().trim().isEmpty()) {
                        passengerInfo.append(String.format(" (护照:%s)", passenger.getPassportNumber()));
                    }
                    
                    // 添加电话信息（如果有）
                    if (passenger.getPhone() != null && !passenger.getPhone().trim().isEmpty()) {
                        passengerInfo.append(String.format(" (电话:%s)", passenger.getPhone()));
                    }
                    
                    passengerInfo.append("\n");
                }
                
                passengerInfo.append(String.format("总计: 成人%d人, 儿童%d人", adultCount, childCount));
                
                // 更新itinerary_details字段包含乘客信息
                String originalItinerary = booking.getItineraryDetails() != null ? booking.getItineraryDetails() : "";
                String combinedDetails = originalItinerary.isEmpty() ? 
                    passengerInfo.toString() : 
                    originalItinerary + "\n\n" + passengerInfo.toString();
                scheduleOrder.setItineraryDetails(combinedDetails);
                
                log.info("✅ 已设置乘客信息到排团表: 订单ID={}, 乘客数量={}, 成人{}人, 儿童{}人", 
                        booking.getBookingId(), passengers.size(), adultCount, childCount);
            } else {
                // 如果没有乘客信息，保持原始的itinerary_details
                scheduleOrder.setItineraryDetails(booking.getItineraryDetails());
                log.warn("⚠️ 订单{}没有找到乘客信息", booking.getBookingId());
            }
        } catch (Exception e) {
            log.error("❌ 获取乘客信息失败: 订单ID={}, 错误: {}", booking.getBookingId(), e.getMessage(), e);
            // 失败时使用原始的itinerary_details
            scheduleOrder.setItineraryDetails(booking.getItineraryDetails());
        }
        
        // 复制订单的其他字段（完整同步所有订单信息到排团表）
        scheduleOrder.setOrderNumber(booking.getOrderNumber());
        scheduleOrder.setAdultCount(booking.getAdultCount());
        scheduleOrder.setChildCount(booking.getChildCount());
        scheduleOrder.setContactPerson(booking.getContactPerson());
        scheduleOrder.setContactPhone(booking.getContactPhone());
        scheduleOrder.setSpecialRequests(booking.getSpecialRequests());
        scheduleOrder.setLuggageCount(booking.getLuggageCount());
        scheduleOrder.setPassengerContact(booking.getPassengerContact());
        
        // 航班信息 - 用于机场接送航班显示
        scheduleOrder.setFlightNumber(booking.getFlightNumber());
        scheduleOrder.setArrivalDepartureTime(booking.getArrivalDepartureTime());
        scheduleOrder.setArrivalLandingTime(booking.getArrivalLandingTime());
        scheduleOrder.setReturnFlightNumber(booking.getReturnFlightNumber());
        scheduleOrder.setDepartureDepartureTime(booking.getDepartureDepartureTime());
        scheduleOrder.setDepartureLandingTime(booking.getDepartureLandingTime());
        
        // 酒店信息
        scheduleOrder.setHotelLevel(booking.getHotelLevel());
        scheduleOrder.setRoomType(booking.getRoomType());
        scheduleOrder.setHotelRoomCount(booking.getHotelRoomCount());
        scheduleOrder.setHotelCheckInDate(booking.getHotelCheckInDate());
        scheduleOrder.setHotelCheckOutDate(booking.getHotelCheckOutDate());
        scheduleOrder.setRoomDetails(booking.getRoomDetails());
        
        // 日期信息
        scheduleOrder.setTourStartDate(booking.getTourStartDate());
        scheduleOrder.setTourEndDate(booking.getTourEndDate());
        scheduleOrder.setPickupDate(booking.getPickupDate());
        scheduleOrder.setDropoffDate(booking.getDropoffDate());
        scheduleOrder.setBookingDate(booking.getBookingDate());
        
        // 业务信息
        scheduleOrder.setServiceType(booking.getServiceType());
        scheduleOrder.setPaymentStatus(booking.getPaymentStatus());
        scheduleOrder.setTotalPrice(booking.getTotalPrice());
        scheduleOrder.setUserId(booking.getUserId());
        scheduleOrder.setAgentId(booking.getAgentId());
        scheduleOrder.setOperatorId(booking.getOperatorId());
        scheduleOrder.setGroupSize(booking.getGroupSize());
        scheduleOrder.setStatus(booking.getStatus());
        // 注意：itineraryDetails已在上面的乘客信息处理中设置，这里不再覆盖
        
        // 标识字段
        scheduleOrder.setIsFirstOrder(booking.getIsFirstOrder() != null && booking.getIsFirstOrder() == 1);
        scheduleOrder.setFromReferral(booking.getFromReferral() != null && booking.getFromReferral() == 1);
        scheduleOrder.setReferralCode(booking.getReferralCode());
        
        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        scheduleOrder.setCreatedAt(now);
        scheduleOrder.setUpdatedAt(now);
        
        // 🔍 详细字段同步确认日志
        log.info("📋 排团记录字段同步确认 - 订单ID={}, 第{}天:", booking.getBookingId(), dayNumber);
        log.info("  └ 客人信息: 姓名=\"{}\", 电话=\"{}\", 成人{}人, 儿童{}人", 
                scheduleOrder.getContactPerson(), scheduleOrder.getContactPhone(), 
                scheduleOrder.getAdultCount(), scheduleOrder.getChildCount());
        log.info("  └ 特殊要求: \"{}\"", scheduleOrder.getSpecialRequests());
        log.info("  └ 行程标题: \"{}\"", scheduleOrder.getTitle());
        log.info("  └ 接送地点: 接=\"{}\", 送=\"{}\"", 
                scheduleOrder.getPickupLocation(), scheduleOrder.getDropoffLocation());
        
        return scheduleOrder;
    }

    /**
     * 智能设置航班信息
     * 根据行程天数智能分配航班信息：只有第一天和最后一天需要航班信息
     * 
     * @param scheduleOrder 排团记录
     * @param booking 原订单信息
     * @param dayNumber 当前是第几天
     * @param totalDays 总天数
     */
    private void setSmartFlightInfo(TourScheduleOrder scheduleOrder, TourBooking booking, int dayNumber, int totalDays) {
        boolean isFirstDay = dayNumber == 1;
        boolean isLastDay = dayNumber == totalDays;
        
        if (isFirstDay) {
            // 第一天：只设置到达航班信息，清空返程航班信息
            scheduleOrder.setFlightNumber(booking.getFlightNumber());
            scheduleOrder.setArrivalDepartureTime(booking.getArrivalDepartureTime());
            scheduleOrder.setArrivalLandingTime(booking.getArrivalLandingTime());
            
            // 清空返程航班信息
            scheduleOrder.setReturnFlightNumber("");
            scheduleOrder.setDepartureDepartureTime(null);
            scheduleOrder.setDepartureLandingTime(null);
            
            log.info("✈️ 第一天航班信息设置 - 订单{} 第{}天: 到达航班=\"{}\"", 
                    booking.getBookingId(), dayNumber, 
                    booking.getFlightNumber() != null ? booking.getFlightNumber() : "无");
                    
        } else if (isLastDay) {
            // 最后一天：只设置返程航班信息，清空到达航班信息
            scheduleOrder.setReturnFlightNumber(booking.getReturnFlightNumber());
            scheduleOrder.setDepartureDepartureTime(booking.getDepartureDepartureTime());
            scheduleOrder.setDepartureLandingTime(booking.getDepartureLandingTime());
            
            // 清空到达航班信息
            scheduleOrder.setFlightNumber("");
            scheduleOrder.setArrivalDepartureTime(null);
            scheduleOrder.setArrivalLandingTime(null);
            
            log.info("✈️ 최后一天航班信息设置 - 订单{} 第{}天: 返程航班=\"{}\"", 
                    booking.getBookingId(), dayNumber, 
                    booking.getReturnFlightNumber() != null ? booking.getReturnFlightNumber() : "无");
                    
        } else {
            // 中间天数：清空所有航班信息（中间天数不需要机场接送）
            scheduleOrder.setFlightNumber("");
            scheduleOrder.setArrivalDepartureTime(null);
            scheduleOrder.setArrivalLandingTime(null);
            scheduleOrder.setReturnFlightNumber("");
            scheduleOrder.setDepartureDepartureTime(null);
            scheduleOrder.setDepartureLandingTime(null);
            
            log.info("✈️ 중간天数航班信息设置 - 订单{} 第{}天: 清空所有航班信息", 
                    booking.getBookingId(), dayNumber);
        }
    }

    /**
     * 发送详细的订单通知
     * @param booking 订单信息
     * @param actionType 操作类型
     * @param actionDetail 操作详情
     */
    private void sendDetailedOrderNotification(TourBooking booking, String actionType, String actionDetail) {
        try {
            String[] operatorInfo = getCurrentOperatorInfo();
            String operatorName = operatorInfo[0];
            String operatorType = operatorInfo[1];

            String contactPerson = booking.getContactPerson();
            String orderNumber = booking.getOrderNumber();

            notificationService.createDetailedOrderNotification(
                Long.valueOf(booking.getBookingId()),
                operatorName,
                operatorType,
                contactPerson,
                orderNumber,
                actionType,
                actionDetail
            );

            log.info("🔔 已发送详细订单通知: 订单ID={}, 操作者={} ({}), 操作类型={}", 
                    booking.getBookingId(), operatorName, operatorType, actionType);
        } catch (Exception e) {
            log.error("❌ 发送详细订单通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从产品行程详情中获取行程标题（去掉"第n天: "前缀）
     * @param tourId 产品ID
     * @param tourType 产品类型
     * @param dayNumber 天数
     * @param tourName 产品名称（备用）
     * @return 行程标题（不含天数前缀）
     */
    private String getItineraryTitleFromProduct(Integer tourId, String tourType, int dayNumber, String tourName) {
        try {
            // 获取产品的行程信息
            List<Map<String, Object>> itineraryList = tourItineraryMapper.getItineraryByTourId(tourId, tourType);
            
            if (itineraryList != null && !itineraryList.isEmpty()) {
                // 根据天数找到对应的行程项
                for (Map<String, Object> itinerary : itineraryList) {
                    Integer itineraryDayNumber = null;
                    
                    // 对于跟团游，使用day_number字段
                    if ("group_tour".equals(tourType) && itinerary.get("day_number") != null) {
                        itineraryDayNumber = (Integer) itinerary.get("day_number");
                    }
                    // 对于一日游，使用display_order字段（通常为1）
                    else if ("day_tour".equals(tourType) && itinerary.get("display_order") != null) {
                        itineraryDayNumber = (Integer) itinerary.get("display_order");
                    }
                    
                    // 如果找到匹配的天数
                    if (itineraryDayNumber != null && itineraryDayNumber == dayNumber) {
                        String title = (String) itinerary.get("title");
                        if (title != null && !title.trim().isEmpty()) {
                            // 🔄 去掉"第n天: "前缀
                            String cleanedTitle = removeDayPrefix(title);
                            log.info("✅ 从产品行程中获取到标题: tourId={}, tourType={}, 第{}天, 原标题=\"{}\", 清理后=\"{}\"", 
                                    tourId, tourType, dayNumber, title, cleanedTitle);
                            return cleanedTitle;
                        }
                    }
                }
                
                // 如果没有找到匹配的天数，但有行程数据，使用第一个作为参考
                if (!itineraryList.isEmpty()) {
                    Map<String, Object> firstItinerary = itineraryList.get(0);
                    String firstTitle = (String) firstItinerary.get("title");
                    if (firstTitle != null && !firstTitle.trim().isEmpty()) {
                        // 对于一日游或者找不到具体天数的情况
                        if ("day_tour".equals(tourType)) {
                            String cleanedTitle = removeDayPrefix(firstTitle);
                            log.info("📝 一日游使用产品行程标题: tourId={}, 原标题=\"{}\", 清理后=\"{}\"", tourId, firstTitle, cleanedTitle);
                            return cleanedTitle;
                        } else {
                            // 对于跟团游，直接使用清理后的标题，不再添加天数前缀
                            String cleanedTitle = removeDayPrefix(firstTitle);
                            log.info("📝 跟团游使用产品行程标题: tourId={}, 第{}天, 原标题=\"{}\", 清理后=\"{}\"", tourId, dayNumber, firstTitle, cleanedTitle);
                            return cleanedTitle;
                        }
                    }
                }
            }
            
            log.warn("⚠️ 无法从产品行程中获取标题，使用默认格式: tourId={}, tourType={}, 第{}天", 
                    tourId, tourType, dayNumber);
            
        } catch (Exception e) {
            log.error("❌ 获取产品行程标题失败: tourId={}, tourType={}, 第{}天, 错误: {}", 
                    tourId, tourType, dayNumber, e.getMessage(), e);
        }
        
        // 默认回退：直接使用产品名称，不添加天数前缀
        return tourName;
    }
    
    /**
     * 去掉标题中的"第n天: "或"第n天-"前缀
     * @param title 原标题
     * @return 清理后的标题
     */
    private String removeDayPrefix(String title) {
        if (title == null || title.trim().isEmpty()) {
            return title;
        }
        
        // 匹配"第n天: "或"第n天-"格式，支持数字1-99
        String cleaned = title.replaceAll("^第\\d{1,2}天[:\\-：-]\\s*", "");
        
        // 如果清理后为空，返回原标题
        return cleaned.isEmpty() ? title : cleaned;
    }
} 