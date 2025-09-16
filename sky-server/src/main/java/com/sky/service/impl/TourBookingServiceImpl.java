package com.sky.service.impl;

import com.sky.dto.EmailConfirmationDTO;
import com.sky.dto.EmailInvoiceDTO;
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
import com.sky.service.AgentCreditService;
import com.sky.service.PriceModificationService;
import com.sky.vo.AgentCreditVO;
import com.sky.mapper.PaymentAuditLogMapper;
import com.sky.mapper.PriceModificationRequestMapper;
import com.sky.entity.PaymentAuditLog;
import com.sky.entity.PriceModificationRequest;
import java.util.UUID;
import com.sky.service.EmailService;
import com.sky.service.HotelPriceService;
import com.sky.service.NotificationService;
import com.sky.service.DiscountService;
import com.sky.service.EmailAsyncService;
import com.sky.service.OrderService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sky.mapper.UserMapper;
import com.sky.mapper.AgentOperatorMapper;
import com.sky.mapper.TourScheduleOrderMapper;
import com.sky.entity.TourScheduleOrder;
import com.sky.mapper.TourItineraryMapper;
import com.sky.mapper.GroupTourDayTourRelationMapper;

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

    // 已在上方声明

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
    
    @Autowired
    private AgentCreditService agentCreditService;
    
    @Autowired
    private PaymentAuditLogMapper paymentAuditLogMapper;
    
    @Autowired
    private PriceModificationRequestMapper priceModificationRequestMapper;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private GroupTourDayTourRelationMapper groupTourDayTourRelationMapper;
    
    @Autowired
    private DiscountService discountService;
    
    @Autowired
    private EmailAsyncService emailAsyncService;
    
    @Autowired
    private OrderService orderService;

    @Autowired
    private NotificationService notificationService;

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
        // 生成订单号 - 尝试使用代理商前缀
        String orderNumber;
        try {
            // 获取当前代理商ID
            Long agentId = BaseContext.getCurrentAgentId();
            if (agentId != null) {
                // 查询代理商信息
                Agent agent = agentMapper.getById(agentId);
                if (agent != null && agent.getCompanyName() != null) {
                    // 使用代理商公司名生成订单号
                    orderNumber = OrderNumberGenerator.generateWithAgent(agent.getCompanyName());
                    log.info("使用代理商前缀生成订单号: {} (代理商: {})", orderNumber, agent.getCompanyName());
                } else {
                    // 代理商信息不完整，使用默认生成方法
                    orderNumber = OrderNumberGenerator.generate();
                    log.info("代理商信息不完整，使用默认前缀生成订单号: {}", orderNumber);
                }
            } else {
                // 没有代理商ID，使用默认生成方法
                orderNumber = OrderNumberGenerator.generate();
                log.info("未获取到代理商ID，使用默认前缀生成订单号: {}", orderNumber);
            }
        } catch (Exception e) {
            // 发生异常，使用默认生成方法
            orderNumber = OrderNumberGenerator.generate();
            log.warn("生成代理商订单号时发生异常，使用默认前缀: {}", e.getMessage());
        }
        
        tourBookingDTO.setOrderNumber(orderNumber);
        System.out.println(tourBookingDTO);
        // 将DTO转换为实体
        TourBooking tourBooking = new TourBooking();
        
        // 首先打印整个DTO查看所有字段
        log.info("DTO原始数据: {}", tourBookingDTO);
        
        // 将DTO的属性复制到实体中
        BeanUtils.copyProperties(tourBookingDTO, tourBooking);
        
        // 确保可选行程数据被正确设置
        if (tourBookingDTO.getSelectedOptionalTours() != null) {
            tourBooking.setSelectedOptionalTours(tourBookingDTO.getSelectedOptionalTours());
            log.info("设置可选行程数据: {}", tourBookingDTO.getSelectedOptionalTours());
        } else {
            log.info("没有可选行程数据");
        }
        
        // 从拦截器中获取userId和agentId（支持游客模式）
        try {
            // 获取所有用户相关信息
            String userType = BaseContext.getCurrentUserType();
            Long currentId = BaseContext.getCurrentId();
            Long agentId = BaseContext.getCurrentAgentId();
            Long operatorId = BaseContext.getCurrentOperatorId();
            String username = BaseContext.getCurrentUsername();
            
            log.info("🔍 TourBookingService获取到的用户上下文: userType={}, currentId={}, agentId={}, operatorId={}, username={}", 
                    userType, currentId, agentId, operatorId, username);
                    
            // 详细调试日志
            log.info("🔍 BaseContext详细状态:");
            log.info("  - 用户类型 (userType): {} (是否为空: {})", userType, userType == null);
            log.info("  - 当前用户ID (currentId): {} (是否为空: {})", currentId, currentId == null);
            log.info("  - 代理商ID (agentId): {} (是否为空: {})", agentId, agentId == null);
            log.info("  - 操作员ID (operatorId): {} (是否为空: {})", operatorId, operatorId == null);
            log.info("  - 用户名 (username): {} (是否为空: {})", username, username == null);
            
            // 检查前端是否已经传递了agentId或userId
            boolean frontendProvidedAgentId = tourBookingDTO.getAgentId() != null;
            boolean frontendProvidedUserId = tourBookingDTO.getUserId() != null;
            
            log.info("前端提供的ID信息: agentId={}, userId={}", tourBookingDTO.getAgentId(), tourBookingDTO.getUserId());
            
            if (userType != null && "agent".equals(userType)) {
                // 代理商主账号登录，只设置agentId，不设置userId
                Integer finalAgentId = null;
                
                // 优先使用前端传递的agentId
                if (frontendProvidedAgentId) {
                    finalAgentId = tourBookingDTO.getAgentId();
                    log.info("✅ 使用前端传递的代理商ID: {}", finalAgentId);
                } else if (agentId != null) {
                    finalAgentId = agentId.intValue();
                    log.info("✅ 使用BaseContext获取的代理商ID: {}", finalAgentId);
                } else if (currentId != null) {
                    // 如果没有单独的代理商ID，则使用当前ID作为代理商ID
                    finalAgentId = currentId.intValue();
                    log.warn("⚠️ getCurrentAgentId为null，使用currentId作为代理商ID: {}", finalAgentId);
                } else {
                    log.error("❌ 无法获取代理商ID：getCurrentAgentId和getCurrentId都为null");
                }
                
                if (finalAgentId != null) {
                    tourBooking.setAgentId(finalAgentId);
                    log.info("最终设置代理商ID: {}", finalAgentId);
                }
                
                // 代理商登录不设置userId
                tourBooking.setUserId(null);
                
            } else if (userType != null && "agent_operator".equals(userType)) {
                // 操作员登录，设置agentId为所属代理商ID，同时记录操作员ID
                Integer finalAgentId = null;
                
                // 优先使用前端传递的agentId
                if (frontendProvidedAgentId) {
                    finalAgentId = tourBookingDTO.getAgentId();
                    log.info("✅ 操作员使用前端传递的代理商ID: {}", finalAgentId);
                } else if (agentId != null) {
                    finalAgentId = agentId.intValue();
                    log.info("✅ 操作员使用BaseContext获取的代理商ID: {}", finalAgentId);
                }
                
                if (finalAgentId != null) {
                    tourBooking.setAgentId(finalAgentId);
                }
                
                // 记录操作员ID到数据库
                if (operatorId != null) {
                    tourBooking.setOperatorId(operatorId);
                    log.info("✅ 操作员下单，设置操作员ID: {}", operatorId);
                }
                
                // 操作员登录不设置userId
                tourBooking.setUserId(null);
                
            } else if (userType != null) {
                // 普通用户登录，只设置userId，不设置agentId
                Integer finalUserId = null;
                
                // 优先使用前端传递的userId
                if (frontendProvidedUserId) {
                    finalUserId = tourBookingDTO.getUserId();
                    log.info("✅ 使用前端传递的用户ID: {}", finalUserId);
                } else if (currentId != null) {
                    finalUserId = currentId.intValue();
                    log.info("✅ 使用BaseContext获取的用户ID: {}", finalUserId);
                }
                
                if (finalUserId != null) {
                    tourBooking.setUserId(finalUserId);
                }
                
                // 普通用户登录不设置agentId
                tourBooking.setAgentId(null);
                
            } else {
                // 游客模式：没有用户类型，不设置任何用户ID
                log.info("游客模式下单，不设置用户ID和代理商ID");
                tourBooking.setUserId(null);
                tourBooking.setAgentId(null);
            }
            
            log.info("🎯 最终订单用户信息: userId={}, agentId={}, operatorId={}", 
                    tourBooking.getUserId(), tourBooking.getAgentId(), tourBooking.getOperatorId());
                    
        } catch (Exception e) {
            // BaseContext调用失败，但现在订单创建需要认证，这不应该发生
            log.error("❌ 获取用户认证信息失败，这是一个异常情况！", e);
            // 不再信任前端传递的代理商ID，订单创建必须基于已验证的BaseContext
            log.error("❌ 获取用户认证信息失败，拒绝创建订单：不信任前端提供的agentId/operatorId");
            throw new BusinessException("未登录或认证无效，无法创建订单");
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
        tourBooking.setStatus("pending");
        
        // 设置默认支付状态
        tourBooking.setPaymentStatus("unpaid");
        
        // 设置创建时间
        tourBooking.setCreatedAt(java.time.LocalDateTime.now());
        tourBooking.setUpdatedAt(java.time.LocalDateTime.now());
        
        // 打印所有字段的值进行调试
        log.info("订单详细信息: {}", tourBooking);
        
        // 使用统一价格计算方法
        Map<String, Object> priceResult = calculateUnifiedPrice(
            tourBooking.getTourId(), 
            tourBooking.getTourType(), 
            tourBooking.getAgentId() != null ? Long.valueOf(tourBooking.getAgentId()) : null, 
            tourBooking.getAdultCount() != null ? tourBooking.getAdultCount() : 1,  // 成人数量
            tourBooking.getChildCount() != null ? tourBooking.getChildCount() : 0,  // 儿童数量
            tourBooking.getHotelLevel() != null ? tourBooking.getHotelLevel() : "4星",  // 酒店等级
            tourBooking.getHotelRoomCount() != null ? tourBooking.getHotelRoomCount() : 1,  // 房间数量
            null,  // userId参数
            null,  // roomTypes
            null,  // childrenAges
            tourBookingDTO.getSelectedOptionalTours()   // 使用DTO中的可选行程数据
        );
        
        // 获取总价
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (priceResult != null && priceResult.get("data") != null) {
            Map<String, Object> data = (Map<String, Object>) priceResult.get("data");
            totalPrice = (BigDecimal) data.get("totalPrice");
        }
        
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
        String primaryContactName = null;
        String primaryContactPhone = null;
        
        if (passengers != null && !passengers.isEmpty()) {
            log.info("开始处理乘客信息，共{}个乘客", passengers.size());
            
            // 首先找到主要联系人信息（用于设置订单的contact_person和contact_phone）
            PassengerDTO primaryPassenger = null;
            for (PassengerDTO passenger : passengers) {
                // 优先选择有电话的成人乘客
                if ((passenger.getIsChild() == null || !passenger.getIsChild()) && 
                    passenger.getPhone() != null && !passenger.getPhone().trim().isEmpty()) {
                    primaryPassenger = passenger;
                    break;
                }
                // 备选：第一个有姓名的乘客
                if (primaryPassenger == null && passenger.getFullName() != null && 
                    !passenger.getFullName().trim().isEmpty()) {
                    primaryPassenger = passenger;
                }
            }
            
            // 如果没有找到合适的主要联系人，使用第一个乘客
            if (primaryPassenger == null && !passengers.isEmpty()) {
                primaryPassenger = passengers.get(0);
            }
            
            // 设置主要联系人信息
            if (primaryPassenger != null) {
                primaryContactName = primaryPassenger.getFullName();
                primaryContactPhone = primaryPassenger.getPhone();
                log.info("🎯 确定主要联系人: 姓名='{}', 电话='{}'", primaryContactName, primaryContactPhone);
            }
            
            // 保存每个乘客信息
            for (int i = 0; i < passengers.size(); i++) {
                PassengerDTO passengerDTO = passengers.get(i);
                log.info("处理第{}个乘客: {}", i + 1, passengerDTO);
                
                // 检查是否是有效的乘客信息
                boolean isValidPassenger = false;
                
                // 如果有姓名，视为有效
                if (passengerDTO.getFullName() != null && !passengerDTO.getFullName().trim().isEmpty()) {
                    isValidPassenger = true;
                    log.info("乘客{}有效：有姓名 '{}'", i + 1, passengerDTO.getFullName());
                }
                
                // 如果有电话号码，也视为有效（游客下单可能只填电话）
                if (passengerDTO.getPhone() != null && !passengerDTO.getPhone().trim().isEmpty()) {
                    isValidPassenger = true;
                    log.info("乘客{}有效：有电话号码 '{}'", i + 1, passengerDTO.getPhone());
                }
                
                // 如果是儿童且有年龄，则视为有效
                if (Boolean.TRUE.equals(passengerDTO.getIsChild()) && passengerDTO.getChildAge() != null && !passengerDTO.getChildAge().trim().isEmpty()) {
                    isValidPassenger = true;
                    log.info("乘客{}有效：是儿童且有年龄 '{}'", i + 1, passengerDTO.getChildAge());
                }
                
                if (!isValidPassenger) {
                    log.warn("跳过无效乘客记录第{}个: 姓名='{}', 电话='{}', 是否儿童={}, 儿童年龄='{}'", 
                        i + 1, passengerDTO.getFullName(), passengerDTO.getPhone(), 
                        passengerDTO.getIsChild(), passengerDTO.getChildAge());
                    continue;
                }
                
                // 确保乘客信息完整
                log.info("保存有效乘客信息第{}个: {}", i + 1, passengerDTO);
                try {
                    passengerService.addPassengerToBooking(tourBooking.getBookingId(), passengerDTO);
                    log.info("乘客{}保存成功", i + 1);
                } catch (Exception e) {
                    log.error("乘客{}保存失败: {}", i + 1, e.getMessage(), e);
                }
            }
        } else {
            log.warn("没有乘客信息需要处理");
        }
        
        // 🆕 更新订单的联系人信息（将主要联系人信息保存到订单表）
        if (primaryContactName != null || primaryContactPhone != null) {
            try {
                // 更新订单的联系人信息
                tourBooking.setContactPerson(primaryContactName);
                tourBooking.setContactPhone(primaryContactPhone);
                
                // 更新数据库中的订单记录
                tourBookingMapper.update(tourBooking);
                
                log.info("✅ 已更新订单联系人信息: 订单ID={}, 联系人='{}', 电话='{}'", 
                        tourBooking.getBookingId(), primaryContactName, primaryContactPhone);
            } catch (Exception e) {
                log.error("❌ 更新订单联系人信息失败: 订单ID={}, 错误: {}", 
                        tourBooking.getBookingId(), e.getMessage(), e);
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

        log.info("✅ 订单创建完成，订单ID: {}，支付后将同步到排团表", tourBooking.getBookingId());
        
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
        
        // 🔍 获取更新前的订单状态（用于支付状态变化检测和价格变化检测）
        TourBooking originalBooking = tourBookingMapper.getById(tourBookingDTO.getBookingId());
        String originalPaymentStatus = originalBooking != null ? originalBooking.getPaymentStatus() : null;
        BigDecimal originalPrice = originalBooking != null ? originalBooking.getTotalPrice() : null;
        log.info("🔍 订单更新前状态检查，订单ID: {}, 原始支付状态: {}, 原始价格: {}", 
                tourBookingDTO.getBookingId(), originalPaymentStatus, originalPrice);
        
        // 获取必要的字段值
        Integer tourId = tourBookingDTO.getTourId();
        String tourType = tourBookingDTO.getTourType();
        
        // 根据乘客列表获取团队规模
        Integer groupSize = null;
        if (tourBookingDTO.getPassengers() != null) {
            groupSize = tourBookingDTO.getPassengers().size();
        }
        
        // 仅当未显式传入总价时才自动重算价格；若前端（如管理后台）已给出 totalPrice，则尊重手工改价
        if (tourBookingDTO.getTotalPrice() == null && (tourId != null || tourType != null || groupSize != null)) {
            
            // 获取当前订单信息，确保有必要的数据用于价格计算
            TourBooking currentBooking = tourBookingMapper.getById(tourBookingDTO.getBookingId());
            
            tourId = tourId != null ? tourId : currentBooking.getTourId();
            tourType = tourType != null ? tourType : currentBooking.getTourType();
            groupSize = groupSize != null ? groupSize : currentBooking.getGroupSize();
            
            Long agentId = currentBooking.getAgentId() != null ? 
                Long.valueOf(currentBooking.getAgentId()) : null;
                
            // 使用统一价格计算方法
            Map<String, Object> priceResult = calculateUnifiedPrice(
                tourId, tourType, agentId, groupSize, 0, "4星", 1, null, null, null, null
            );
            BigDecimal totalPrice = BigDecimal.ZERO;
            if (priceResult != null && priceResult.get("data") != null) {
                Map<String, Object> data = (Map<String, Object>) priceResult.get("data");
                totalPrice = (BigDecimal) data.get("totalPrice");
            }
            tourBookingDTO.setTotalPrice(totalPrice);
        }
        
        // 将DTO转换为实体
        TourBooking tourBooking = new TourBooking();
        BeanUtils.copyProperties(tourBookingDTO, tourBooking);
        
        // 更新订单基本信息
        tourBookingMapper.update(tourBooking);

        // 若传入了 totalPrice，则再强制落库一次，避免通用 update 映射出于安全策略忽略了价格字段
        if (tourBookingDTO.getTotalPrice() != null) {
            try {
                tourBookingMapper.updateTotalPrice(tourBookingDTO.getBookingId(), tourBookingDTO.getTotalPrice());
            } catch (Exception e) {
                log.warn("价格字段单独更新失败，将以通用更新为准: bookingId={}, err={}", tourBookingDTO.getBookingId(), e.getMessage());
            }
        }
        
        // 🔍 获取更新后的订单状态（检测支付状态变化）
        TourBooking updatedBooking = tourBookingMapper.getById(tourBookingDTO.getBookingId());
        String newPaymentStatus = updatedBooking != null ? updatedBooking.getPaymentStatus() : null;
        log.info("🔍 订单更新后支付状态检查，订单ID: {}, 新支付状态: {}", tourBookingDTO.getBookingId(), newPaymentStatus);
        
        // 🗑️ 检测支付状态变化：如果从已支付变为未支付，删除排团表数据
        if ("paid".equals(originalPaymentStatus) && !"paid".equals(newPaymentStatus)) {
            try {
                log.warn("⚠️ 检测到支付状态从已支付变为未支付，开始清理排团表数据，订单ID: {}", tourBookingDTO.getBookingId());
                
                // 删除排团表中的相关记录
                tourScheduleOrderMapper.deleteByBookingId(tourBookingDTO.getBookingId());
                log.info("✅ 排团表数据清理完成，订单ID: {}", tourBookingDTO.getBookingId());
                
                // 记录操作日志
                log.info("📝 支付状态变化日志：订单ID={}, 原状态={}, 新状态={}, 已清理排团表数据", 
                        tourBookingDTO.getBookingId(), originalPaymentStatus, newPaymentStatus);
                        
                return true; // 状态变为未支付后直接返回，不再进行排团表同步
                
            } catch (Exception e) {
                log.error("❌ 清理排团表数据失败: 订单ID={}, 错误: {}", tourBookingDTO.getBookingId(), e.getMessage(), e);
                // 继续执行后续逻辑，不中断订单更新
            }
        }
        // 🆕 检测支付状态变化：如果从未支付变为已支付，同步订单到排团表
        if (!"paid".equals(originalPaymentStatus) && "paid".equals(newPaymentStatus)) {
            try {
                log.info("🎉 检测到支付状态从未支付变为已支付，开始同步订单到排团表，订单ID: {}", tourBookingDTO.getBookingId());
                
                // 调用OrderService的同步方法（避免重复代码）
                orderService.syncBookingToScheduleTable(tourBookingDTO.getBookingId());
                
                // 记录操作日志
                log.info("📝 支付状态变化日志：订单ID={}, 原状态={}, 新状态={}, 已同步到排团表", 
                        tourBookingDTO.getBookingId(), originalPaymentStatus, newPaymentStatus);
                        
                return true; // 状态变为已支付后直接返回，不再进行排团表同步
                
            } catch (Exception e) {
                log.error("❌ 同步订单到排团表失败: 订单ID={}, 错误: {}", tourBookingDTO.getBookingId(), e.getMessage(), e);
                // 继续执行后续逻辑，不中断订单更新
            }
        }
        
        // 🆕 处理价格变化：降价自动退款，涨价需要确认
        try {
            if (tourBookingDTO.getTotalPrice() != null && originalPrice != null) {
                BigDecimal newPrice = tourBookingDTO.getTotalPrice();
                BigDecimal priceDifference = newPrice.subtract(originalPrice);
                
                // 只有价格真的发生变化时才处理
                if (priceDifference.compareTo(BigDecimal.ZERO) != 0) {
                    log.info("💰 检测到价格变化：订单ID={}, 原价={}, 新价={}, 差额={}", 
                            tourBookingDTO.getBookingId(), originalPrice, newPrice, priceDifference);
                    
                    String changeReason = tourBookingDTO.getSpecialRequests() != null ? 
                            tourBookingDTO.getSpecialRequests().trim() : "管理员调价";
                    
                    if (priceDifference.compareTo(BigDecimal.ZERO) < 0) {
                        // 降价：自动退款 + 通知
                        processPriceDecrease(originalBooking, newPrice, priceDifference.abs(), changeReason);
                    } else {
                        // 涨价：创建确认请求 + 通知
                        processPriceIncrease(originalBooking, newPrice, priceDifference, changeReason);
                    }
                    return true; // 价格变化处理完成，提前返回
                }
            }
            
            // 非价格变化的其他修改通知
            String changeTitle = "订单修改";
            String changeDetail = "订单信息已修改";
            if (tourBookingDTO.getSpecialRequests() != null) {
                changeDetail = "备注更新：" + tourBookingDTO.getSpecialRequests();
            } else if (tourBookingDTO.getPickupLocation() != null || tourBookingDTO.getDropoffLocation() != null) {
                changeDetail = "接送信息已更新";
            } else if (tourBookingDTO.getPassengers() != null) {
                changeDetail = "乘客信息已更新";
            }
            TourBooking notifyBooking = tourBookingMapper.getById(tourBookingDTO.getBookingId());
            if (notifyBooking != null) {
                notificationService.createAgentOrderChangeNotification(
                        notifyBooking.getAgentId() != null ? notifyBooking.getAgentId().longValue() : null,
                        notifyBooking.getOperatorId(),
                        notifyBooking.getBookingId().longValue(),
                        notifyBooking.getOrderNumber(),
                        changeTitle,
                        changeDetail
                );
            }
        } catch (Exception e) {
            log.error("❌ 处理订单变更失败: {}", e.getMessage(), e);
        }

        // 🔄 同步更新排团表（管理后台修改订单）
        try {
            TourBooking currentBooking = tourBookingMapper.getById(tourBookingDTO.getBookingId());
            if (currentBooking != null && "paid".equals(currentBooking.getPaymentStatus())) {
                log.info("🔄 管理后台修改订单，开始同步排团表信息，订单ID: {}", tourBookingDTO.getBookingId());
                
                // 同步联系人信息
                if (tourBookingDTO.getContactPerson() != null || tourBookingDTO.getContactPhone() != null) {
                    updateScheduleTableContactInfo(tourBookingDTO.getBookingId(), 
                        tourBookingDTO.getContactPerson(), tourBookingDTO.getContactPhone());
                    log.info("✅ 排团表联系人信息同步完成");
                }
                
                // 同步特殊要求信息
                if (tourBookingDTO.getSpecialRequests() != null) {
                    int updatedCount = tourScheduleOrderMapper.updateSpecialRequestsByBookingId(
                        tourBookingDTO.getBookingId(), tourBookingDTO.getSpecialRequests());
                    log.info("✅ 排团表特殊要求同步完成，更新记录数: {}", updatedCount);
                }
                
                // 同步接送地点信息
                if (tourBookingDTO.getPickupLocation() != null || tourBookingDTO.getDropoffLocation() != null) {
                    int updatedCount = tourScheduleOrderMapper.updatePickupDropoffByBookingId(
                        tourBookingDTO.getBookingId(), 
                        tourBookingDTO.getPickupLocation() != null ? tourBookingDTO.getPickupLocation() : currentBooking.getPickupLocation(),
                        tourBookingDTO.getDropoffLocation() != null ? tourBookingDTO.getDropoffLocation() : currentBooking.getDropoffLocation());
                    log.info("✅ 排团表接送地点同步完成，更新记录数: {}", updatedCount);
                }
                
                // 同步航班信息
                if (tourBookingDTO.getFlightNumber() != null || tourBookingDTO.getReturnFlightNumber() != null ||
                    tourBookingDTO.getArrivalLandingTime() != null || tourBookingDTO.getDepartureDepartureTime() != null) {
                    int updatedCount = tourScheduleOrderMapper.updateFlightInfoByBookingId(
                        tourBookingDTO.getBookingId(),
                        tourBookingDTO.getFlightNumber() != null ? tourBookingDTO.getFlightNumber() : currentBooking.getFlightNumber(),
                        tourBookingDTO.getReturnFlightNumber() != null ? tourBookingDTO.getReturnFlightNumber() : currentBooking.getReturnFlightNumber(),
                        tourBookingDTO.getArrivalLandingTime() != null ? tourBookingDTO.getArrivalLandingTime() : currentBooking.getArrivalLandingTime(),
                        tourBookingDTO.getDepartureDepartureTime() != null ? tourBookingDTO.getDepartureDepartureTime() : currentBooking.getDepartureDepartureTime());
                    log.info("✅ 排团表航班信息同步完成，更新记录数: {}", updatedCount);
                    
                    // 🆕 根据航班信息更新第一天和最后一天的接送地点（管理后台修改）
                    String finalFlightNumber = tourBookingDTO.getFlightNumber() != null ? tourBookingDTO.getFlightNumber() : currentBooking.getFlightNumber();
                    String finalReturnFlightNumber = tourBookingDTO.getReturnFlightNumber() != null ? tourBookingDTO.getReturnFlightNumber() : currentBooking.getReturnFlightNumber();
                    
                    boolean hasArrivalFlight = finalFlightNumber != null && !finalFlightNumber.trim().isEmpty();
                    boolean hasDepartureFlight = finalReturnFlightNumber != null && !finalReturnFlightNumber.trim().isEmpty();
                    
                    int totalUpdatedCount = 0;
                    if (hasArrivalFlight) {
                        int pickupUpdatedCount = tourScheduleOrderMapper.updateFirstDayPickupLocation(
                            tourBookingDTO.getBookingId(), finalFlightNumber);
                        totalUpdatedCount += pickupUpdatedCount;
                        log.info("✅ 第一天接机地点更新完成（管理后台），更新记录数: {}, 到达航班: {}", 
                                pickupUpdatedCount, finalFlightNumber);
                    }
                    
                    if (hasDepartureFlight) {
                        int dropoffUpdatedCount = tourScheduleOrderMapper.updateLastDayDropoffLocation(
                            tourBookingDTO.getBookingId(), finalReturnFlightNumber);
                        totalUpdatedCount += dropoffUpdatedCount;
                        log.info("✅ 最后一天送机地点更新完成（管理后台），更新记录数: {}, 离开航班: {}", 
                                dropoffUpdatedCount, finalReturnFlightNumber);
                    }
                    
                    if (totalUpdatedCount > 0) {
                        log.info("✅ 排团表航班接送地点同步完成（管理后台），总更新记录数: {}", totalUpdatedCount);
                    }
                }
                
                log.info("✅ 管理后台订单修改同步排团表完成，订单ID: {}", tourBookingDTO.getBookingId());
            } else {
                log.info("ℹ️ 订单未付款，跳过排团表信息同步，订单ID: {}, 支付状态: {}", 
                        tourBookingDTO.getBookingId(), currentBooking != null ? currentBooking.getPaymentStatus() : "未知");
            }
        } catch (Exception e) {
            log.error("❌ 管理后台修改订单同步排团表失败: 订单ID={}, 错误: {}", tourBookingDTO.getBookingId(), e.getMessage(), e);
            // 不抛出异常，避免影响订单更新
        }
        
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
            
            // 使用专门的方法更新订单状态为已取消
            int statusUpdateResult = tourBookingMapper.updateStatus(bookingId, "cancelled");
            if (statusUpdateResult <= 0) {
                log.error("更新订单状态失败，订单ID: {}", bookingId);
                return false;
            }
            log.info("✅ 订单状态已更新为cancelled，影响行数: {}", statusUpdateResult);
            
            // 添加取消原因到special_requests字段
            String cancelReason = "用户取消 - " + LocalDateTime.now();
            String specialRequests = tourBooking.getSpecialRequests();
            if (specialRequests != null && !specialRequests.isEmpty()) {
                specialRequests += "\n取消原因: " + cancelReason;
            } else {
                specialRequests = "取消原因: " + cancelReason;
            }
            tourBooking.setSpecialRequests(specialRequests);
            tourBooking.setUpdatedAt(LocalDateTime.now());
            
            // 更新其他字段（除了状态）
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

        // 🆕 同步通知代理端（主号必收，若有操作员，仅通知该操作员）
        try {
            notificationService.createAgentOrderChangeNotification(
                    tourBooking.getAgentId() != null ? tourBooking.getAgentId().longValue() : null,
                    tourBooking.getOperatorId(),
                    tourBooking.getBookingId().longValue(),
                    tourBooking.getOrderNumber(),
                    "订单已确认",
                    "订单已确认，可进行支付"
            );
        } catch (Exception ignore) {}
        
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

        // 🆕 同步通知代理端
        try {
            notificationService.createAgentOrderChangeNotification(
                    tourBooking.getAgentId() != null ? tourBooking.getAgentId().longValue() : null,
                    tourBooking.getOperatorId(),
                    tourBooking.getBookingId().longValue(),
                    tourBooking.getOrderNumber(),
                    "订单已完成",
                    "订单已完成，感谢您的配合"
            );
        } catch (Exception ignore) {}
        
        return true;
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
            
            // 🆕 通知代理商端：支付成功
            try {
                notificationService.createAgentOrderChangeNotification(
                        tourBooking.getAgentId() != null ? tourBooking.getAgentId().longValue() : null,
                        tourBooking.getOperatorId(),
                        tourBooking.getBookingId().longValue(),
                        tourBooking.getOrderNumber(),
                        "支付成功",
                        String.format("已支付金额：$%.2f", tourBooking.getTotalPrice() != null ? tourBooking.getTotalPrice() : BigDecimal.ZERO)
                );
            } catch (Exception ignore) {}

            // 🔔 支付成功后异步发送确认信和发票邮件（不阻塞响应）
            try {
                emailAsyncService.sendEmailsAfterPaymentAsync(bookingId.longValue(), tourBooking);
                log.info("✅ 异步邮件发送任务已提交: orderId={}", bookingId);
            } catch (Exception e) {
                log.error("❌ 提交异步邮件发送任务失败: {}", e.getMessage(), e);
            }
            
            // 🆕 支付成功后同步订单数据到排团表（只有付款后才进入排团系统）
            try {
                log.info("🔄 开始支付后同步订单到排团表，订单ID: {}", bookingId);
                autoSyncOrderToScheduleTable(bookingId);
                log.info("✅ 订单支付成功，已同步到排团表，订单ID: {}", bookingId);
            } catch (Exception e) {
                log.error("❌ 支付后同步订单到排团表失败: 订单ID={}, 错误类型: {}, 错误消息: {}", 
                    bookingId, e.getClass().getSimpleName(), e.getMessage(), e);
                // 不抛出异常，避免影响支付流程，但记录错误供后续处理
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
        
        // 🔒 安全检查：禁止用户修改价格相关字段（DTO本身已经限制了可修改字段，这里做额外验证）
        if (updateDTO.getTourStartDate() != null || updateDTO.getTourEndDate() != null || 
            updateDTO.getAdultCount() != null || updateDTO.getChildCount() != null || 
            updateDTO.getHotelLevel() != null || updateDTO.getRoomType() != null || 
            updateDTO.getHotelRoomCount() != null || updateDTO.getHotelCheckInDate() != null ||
            updateDTO.getHotelCheckOutDate() != null) {
            log.warn("⚠️ 安全警告：尝试修改价格相关字段被阻止，订单ID: {}", updateDTO.getBookingId());
            throw new BusinessException("禁止修改影响价格的字段，如需修改请联系客服");
        }
        
        // 查询原订单信息
        TourBooking existingBooking = tourBookingMapper.getById(updateDTO.getBookingId());
        if (existingBooking == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 🔍 获取更新前的支付状态（用于支付状态变化检测）
        String originalPaymentStatus = existingBooking.getPaymentStatus();
        log.info("🔍 用户端订单更新前支付状态检查，订单ID: {}, 原始支付状态: {}", updateDTO.getBookingId(), originalPaymentStatus);
        
        // 只有未完成和未取消的订单才可以修改
        if ("completed".equals(existingBooking.getStatus()) || "cancelled".equals(existingBooking.getStatus())) {
            throw new BusinessException("已完成或已取消的订单无法修改");
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
        
        // 2. 联系人信息（允许修改）
        if (updateDTO.getContactPerson() != null) {
            bookingToUpdate.setContactPerson(updateDTO.getContactPerson());
        }
        if (updateDTO.getContactPhone() != null) {
            bookingToUpdate.setContactPhone(updateDTO.getContactPhone());
            bookingToUpdate.setPassengerContact(updateDTO.getContactPhone());
        }
        
        // 3. 接送信息（允许修改）
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
        
        // 4. 行李和其他非价格相关信息（允许修改）
        if (updateDTO.getLuggageCount() != null) {
            bookingToUpdate.setLuggageCount(updateDTO.getLuggageCount());
        }
        if (updateDTO.getRoomDetails() != null) {
            bookingToUpdate.setRoomDetails(updateDTO.getRoomDetails());
        }
        if (updateDTO.getSpecialRequests() != null) {
            bookingToUpdate.setSpecialRequests(updateDTO.getSpecialRequests());
        }
        
        // 注意：以下价格相关字段在Mapper层已被禁止更新，确保价格不会改变
        // 禁止字段：tourStartDate, tourEndDate, adultCount, childCount  
        // 禁止字段：hotelLevel, roomType, hotelRoomCount, hotelCheckInDate, hotelCheckOutDate
        // 允许字段：航班信息、接送信息（pickupDate, dropoffDate, pickupLocation, dropoffLocation）、联系人信息、行李数量、特殊要求
        
        // 价格相关字段已在Mapper层被禁止更新，此处不再进行价格重新计算
        log.info("ℹ️ 订单修改完成，价格保持不变: {}", existingBooking.getTotalPrice());
        
        // 更新修改时间
        bookingToUpdate.setUpdatedAt(LocalDateTime.now());
        
        // 执行更新
        tourBookingMapper.update(bookingToUpdate);
        
        // 🔍 获取更新后的订单状态（检测支付状态变化）
        TourBooking updatedBooking = tourBookingMapper.getById(updateDTO.getBookingId());
        String newPaymentStatus = updatedBooking != null ? updatedBooking.getPaymentStatus() : null;
        log.info("🔍 用户端订单更新后支付状态检查，订单ID: {}, 新支付状态: {}", updateDTO.getBookingId(), newPaymentStatus);
        
        // 🗑️ 检测支付状态变化：如果从已支付变为未支付，删除排团表数据
        if ("paid".equals(originalPaymentStatus) && !"paid".equals(newPaymentStatus)) {
            try {
                log.warn("⚠️ 用户端检测到支付状态从已支付变为未支付，开始清理排团表数据，订单ID: {}", updateDTO.getBookingId());
                
                // 删除排团表中的相关记录
                tourScheduleOrderMapper.deleteByBookingId(updateDTO.getBookingId()); 
                log.info("✅ 排团表数据清理完成，订单ID: {}", updateDTO.getBookingId());
                
                // 记录操作日志
                log.info("📝 用户端支付状态变化日志：订单ID={}, 原状态={}, 新状态={}, 已清理排团表数据", 
                        updateDTO.getBookingId(), originalPaymentStatus, newPaymentStatus);
                        
                return true; // 状态变为未支付后直接返回，不再进行排团表同步
                
            } catch (Exception e) {
                log.error("❌ 用户端清理排团表数据失败: 订单ID={}, 错误: {}", updateDTO.getBookingId(), e.getMessage(), e);
                // 继续执行后续逻辑，不中断订单更新
            }
        }

        // 🆕 给代理商端发送订单变更通知（价格/备注/接送等修改）
        try {
            String detail = "订单信息已更新";
            if (updateDTO.getSpecialRequests() != null) {
                detail = "备注更新：" + updateDTO.getSpecialRequests();
            } else if (updateDTO.getPickupLocation() != null || updateDTO.getDropoffLocation() != null) {
                detail = "接送信息已更新";
            }
            notificationService.createAgentOrderChangeNotification(
                    updatedBooking.getAgentId() != null ? updatedBooking.getAgentId().longValue() : null,
                    updatedBooking.getOperatorId(),
                    updatedBooking.getBookingId().longValue(),
                    updatedBooking.getOrderNumber(),
                    "订单修改",
                    detail
            );
        } catch (Exception ignore) {}
        // 🆕 检测支付状态变化：如果从未支付变为已支付，同步订单到排团表
        if (!"paid".equals(originalPaymentStatus) && "paid".equals(newPaymentStatus)) {
            try {
                log.info("🎉 用户端检测到支付状态从未支付变为已支付，开始同步订单到排团表，订单ID: {}", updateDTO.getBookingId());
                
                // 调用OrderService的同步方法（避免重复代码）
                orderService.syncBookingToScheduleTable(updateDTO.getBookingId());
                
                // 记录操作日志
                log.info("📝 用户端支付状态变化日志：订单ID={}, 原状态={}, 新状态={}, 已同步到排团表", 
                        updateDTO.getBookingId(), originalPaymentStatus, newPaymentStatus);
                        
                return true; // 状态变为已支付后直接返回，不再进行排团表同步
                
            } catch (Exception e) {
                log.error("❌ 用户端同步订单到排团表失败: 订单ID={}, 错误: {}", updateDTO.getBookingId(), e.getMessage(), e);
                // 继续执行后续逻辑，不中断订单更新
            }
        }
        
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
        
        // 🔄 增强的排团表同步逻辑 - 同步更多字段到tour_schedule_order表
        try {
            TourBooking currentBooking = tourBookingMapper.getById(updateDTO.getBookingId());
            if (currentBooking != null && "paid".equals(currentBooking.getPaymentStatus())) {
                log.info("🔄 开始全面同步更新排团表信息（已付款订单），订单ID: {}", updateDTO.getBookingId());
                
                // 同步联系人信息
                boolean contactInfoChanged = updateDTO.getContactPerson() != null || updateDTO.getContactPhone() != null;
                if (contactInfoChanged) {
                    updateScheduleTableContactInfo(updateDTO.getBookingId(), updateDTO.getContactPerson(), updateDTO.getContactPhone());
                    log.info("✅ 排团表联系人信息同步完成");
                }
                
                // 🆕 同步特殊要求信息
                if (updateDTO.getSpecialRequests() != null) {
                    int updatedCount = tourScheduleOrderMapper.updateSpecialRequestsByBookingId(
                        updateDTO.getBookingId(), updateDTO.getSpecialRequests());
                    log.info("✅ 排团表特殊要求同步完成，更新记录数: {}", updatedCount);
                }
                
                // 🆕 同步接送地点信息
                boolean pickupInfoChanged = updateDTO.getPickupLocation() != null || updateDTO.getDropoffLocation() != null;
                if (pickupInfoChanged) {
                    int updatedCount = tourScheduleOrderMapper.updatePickupDropoffByBookingId(
                        updateDTO.getBookingId(), 
                        updateDTO.getPickupLocation() != null ? updateDTO.getPickupLocation() : currentBooking.getPickupLocation(),
                        updateDTO.getDropoffLocation() != null ? updateDTO.getDropoffLocation() : currentBooking.getDropoffLocation());
                    log.info("✅ 排团表接送地点同步完成，更新记录数: {}", updatedCount);
                }
                
                // 🆕 同步航班信息
                boolean flightInfoChanged = updateDTO.getFlightNumber() != null || updateDTO.getReturnFlightNumber() != null ||
                    updateDTO.getArrivalLandingTime() != null || updateDTO.getDepartureDepartureTime() != null;
                if (flightInfoChanged) {
                    int updatedCount = tourScheduleOrderMapper.updateFlightInfoByBookingId(
                        updateDTO.getBookingId(),
                        updateDTO.getFlightNumber() != null ? updateDTO.getFlightNumber() : currentBooking.getFlightNumber(),
                        updateDTO.getReturnFlightNumber() != null ? updateDTO.getReturnFlightNumber() : currentBooking.getReturnFlightNumber(),
                        updateDTO.getArrivalLandingTime() != null ? updateDTO.getArrivalLandingTime() : currentBooking.getArrivalLandingTime(),
                        updateDTO.getDepartureDepartureTime() != null ? updateDTO.getDepartureDepartureTime() : currentBooking.getDepartureDepartureTime());
                    log.info("✅ 排团表航班信息同步完成，更新记录数: {}", updatedCount);
                    
                    // 🆕 根据航班信息更新第一天和最后一天的接送地点
                    String finalFlightNumber = updateDTO.getFlightNumber() != null ? updateDTO.getFlightNumber() : currentBooking.getFlightNumber();
                    String finalReturnFlightNumber = updateDTO.getReturnFlightNumber() != null ? updateDTO.getReturnFlightNumber() : currentBooking.getReturnFlightNumber();
                    
                    boolean hasArrivalFlight = finalFlightNumber != null && !finalFlightNumber.trim().isEmpty();
                    boolean hasDepartureFlight = finalReturnFlightNumber != null && !finalReturnFlightNumber.trim().isEmpty();
                    
                    int totalUpdatedCount = 0;
                    if (hasArrivalFlight) {
                        int pickupUpdatedCount = tourScheduleOrderMapper.updateFirstDayPickupLocation(
                            updateDTO.getBookingId(), finalFlightNumber);
                        totalUpdatedCount += pickupUpdatedCount;
                        log.info("✅ 第一天接机地点更新完成，更新记录数: {}, 到达航班: {}", 
                                pickupUpdatedCount, finalFlightNumber);
                    }
                    
                    if (hasDepartureFlight) {
                        int dropoffUpdatedCount = tourScheduleOrderMapper.updateLastDayDropoffLocation(
                            updateDTO.getBookingId(), finalReturnFlightNumber);
                        totalUpdatedCount += dropoffUpdatedCount;
                        log.info("✅ 最后一天送机地点更新完成，更新记录数: {}, 离开航班: {}", 
                                dropoffUpdatedCount, finalReturnFlightNumber);
                    }
                    
                    if (totalUpdatedCount > 0) {
                        log.info("✅ 排团表航班接送地点同步完成，总更新记录数: {}", totalUpdatedCount);
                    }
                }
                
                log.info("✅ 排团表全面同步更新完成，订单ID: {}", updateDTO.getBookingId());
            } else {
                log.info("ℹ️ 订单未付款，跳过排团表信息同步，订单ID: {}, 支付状态: {}", 
                        updateDTO.getBookingId(), currentBooking != null ? currentBooking.getPaymentStatus() : "未知");
            }
        } catch (Exception e) {
            log.error("❌ 同步更新排团表信息失败: 订单ID={}, 错误: {}", updateDTO.getBookingId(), e.getMessage(), e);
            // 不抛出异常，避免影响订单更新
        }

        // 🔄 同步更新乘客表的联系人信息
        try {
            boolean contactInfoChanged = updateDTO.getContactPerson() != null || updateDTO.getContactPhone() != null;
            if (contactInfoChanged) {
                log.info("🔄 开始同步更新乘客表联系人信息，订单ID: {}", updateDTO.getBookingId());
                syncContactInfoToPassengerTable(updateDTO.getBookingId(), updateDTO.getContactPerson(), updateDTO.getContactPhone());
                log.info("✅ 乘客表联系人信息同步更新完成，订单ID: {}", updateDTO.getBookingId());
            }
        } catch (Exception e) {
            log.error("❌ 同步更新乘客表联系人信息失败: 订单ID={}, 错误: {}", updateDTO.getBookingId(), e.getMessage(), e);
            // 不抛出异常，避免影响订单更新
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
     * 同步订单联系人信息到乘客表
     * 当订单的联系人信息发生变化时，同步更新相应的主要乘客信息
     */
    private void syncContactInfoToPassengerTable(Integer bookingId, String newContactPerson, String newContactPhone) {
        try {
            // 获取订单的所有乘客
            List<PassengerVO> passengers = passengerService.getByBookingId(bookingId);
            if (passengers == null || passengers.isEmpty()) {
                log.info("ℹ️ 订单{}没有关联的乘客信息，尝试基于联系人信息创建主要乘客", bookingId);
                
                // 当没有乘客信息时，基于联系人信息创建一个主要乘客
                if ((newContactPerson != null && !newContactPerson.trim().isEmpty()) || 
                    (newContactPhone != null && !newContactPhone.trim().isEmpty())) {
                    
                    PassengerDTO newPassengerDTO = new PassengerDTO();
                    newPassengerDTO.setFullName(newContactPerson);
                    newPassengerDTO.setPhone(newContactPhone);
                    newPassengerDTO.setIsPrimary(true);
                    newPassengerDTO.setIsChild(false);
                    
                    log.info("📝 创建主要乘客: 姓名=\"{}\", 电话=\"{}\"", newContactPerson, newContactPhone);
                    
                    Boolean addResult = passengerService.addPassengerToBooking(bookingId, newPassengerDTO);
                    if (Boolean.TRUE.equals(addResult)) {
                        log.info("✅ 成功基于联系人信息创建主要乘客: 订单ID={}", bookingId);
                    } else {
                        log.warn("⚠️ 基于联系人信息创建主要乘客失败: 订单ID={}", bookingId);
                    }
                } else {
                    log.warn("⚠️ 订单{}没有有效的联系人信息，无法创建乘客", bookingId);
                }
                return;
            }

            // 查找主要联系人（通常是第一个成人乘客，或者姓名匹配的乘客）
            PassengerVO primaryPassenger = null;
            
            // 优先查找姓名匹配的乘客
            if (newContactPerson != null) {
                for (PassengerVO passenger : passengers) {
                    if (newContactPerson.equals(passenger.getFullName())) {
                        primaryPassenger = passenger;
                        log.info("✅ 找到姓名匹配的主要联系人: 乘客ID={}, 姓名={}", 
                                passenger.getPassengerId(), passenger.getFullName());
                        break;
                    }
                }
            }
            
            // 如果没有找到姓名匹配的，使用第一个成人乘客
            if (primaryPassenger == null) {
                for (PassengerVO passenger : passengers) {
                    if (passenger.getIsChild() == null || !passenger.getIsChild()) {
                        primaryPassenger = passenger;
                        log.info("✅ 使用第一个成人乘客作为主要联系人: 乘客ID={}, 姓名={}", 
                                passenger.getPassengerId(), passenger.getFullName());
                        break;
                    }
                }
            }
            
            // 如果还是没有找到，使用第一个乘客
            if (primaryPassenger == null && !passengers.isEmpty()) {
                primaryPassenger = passengers.get(0);
                log.info("✅ 使用第一个乘客作为主要联系人: 乘客ID={}, 姓名={}", 
                        primaryPassenger.getPassengerId(), primaryPassenger.getFullName());
            }
            
            // 更新主要乘客的信息
            if (primaryPassenger != null) {
                boolean needUpdate = false;
                PassengerDTO updatePassengerDTO = new PassengerDTO();
                updatePassengerDTO.setPassengerId(primaryPassenger.getPassengerId());
                
                // 如果联系人姓名发生变化，更新乘客姓名
                if (newContactPerson != null && !newContactPerson.equals(primaryPassenger.getFullName())) {
                    updatePassengerDTO.setFullName(newContactPerson);
                    needUpdate = true;
                    log.info("📝 准备更新乘客姓名: {} -> {}", primaryPassenger.getFullName(), newContactPerson);
                }
                
                // 如果联系电话发生变化，更新乘客电话
                if (newContactPhone != null && !newContactPhone.equals(primaryPassenger.getPhone())) {
                    updatePassengerDTO.setPhone(newContactPhone);
                    needUpdate = true;
                    log.info("📞 准备更新乘客电话: {} -> {}", primaryPassenger.getPhone(), newContactPhone);
                }
                
                // 执行更新
                if (needUpdate) {
                    // 复制其他不变的字段
                    updatePassengerDTO.setPassportNumber(primaryPassenger.getPassportNumber());
                    updatePassengerDTO.setEmail(primaryPassenger.getEmail());
                    updatePassengerDTO.setWechatId(primaryPassenger.getWechatId());
                    updatePassengerDTO.setIsChild(primaryPassenger.getIsChild());
                    updatePassengerDTO.setChildAge(primaryPassenger.getChildAge());
                    updatePassengerDTO.setSpecialRequests(primaryPassenger.getSpecialRequests());
                    
                    Boolean updateResult = passengerService.update(updatePassengerDTO);
                    if (Boolean.TRUE.equals(updateResult)) {
                        log.info("✅ 成功同步更新乘客表联系人信息: 订单ID={}, 乘客ID={}", 
                                bookingId, primaryPassenger.getPassengerId());
                    } else {
                        log.warn("⚠️ 乘客表联系人信息更新失败: 订单ID={}, 乘客ID={}", 
                                bookingId, primaryPassenger.getPassengerId());
                    }
                } else {
                    log.info("ℹ️ 乘客信息无需更新: 订单ID={}, 乘客ID={}", 
                            bookingId, primaryPassenger.getPassengerId());
                }
            } else {
                log.warn("⚠️ 订单{}没有找到可更新的主要乘客", bookingId);
            }
            
        } catch (Exception e) {
            log.error("❌ 同步乘客表联系人信息时发生异常: 订单ID={}, 错误: {}", bookingId, e.getMessage(), e);
            throw e;
        }
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
     * 直接更新排团表联系人信息
     * 不重新生成记录，只更新联系人字段
     */
    @Transactional
    private void updateScheduleTableContactInfo(Integer bookingId, String newContactPerson, String newContactPhone) {
        log.info("🔄 开始直接更新排团表联系人信息: 订单ID={}, 新联系人=\"{}\", 新电话=\"{}\"", 
                bookingId, newContactPerson, newContactPhone);
        
        try {
            // 获取当前订单信息
            TourBooking tourBooking = tourBookingMapper.getById(bookingId);
            if (tourBooking == null) {
                log.warn("订单不存在，无法更新排团表: {}", bookingId);
                return;
            }
            
            // 确定要更新的联系人信息
            String finalContactPerson = newContactPerson != null ? newContactPerson : tourBooking.getContactPerson();
            String finalContactPhone = newContactPhone != null ? newContactPhone : tourBooking.getContactPhone();
            
            // 直接更新排团表中该订单的所有记录的联系人信息
            int updatedCount = tourScheduleOrderMapper.updateContactInfoByBookingId(
                bookingId, finalContactPerson, finalContactPhone);

            if (updatedCount > 0) {
                log.info("✅ 成功更新排团表联系人信息: 订单ID={}, 更新记录数={}, 联系人=\"{}\", 电话=\"{}\"", 
                        bookingId, updatedCount, finalContactPerson, finalContactPhone);
            } else {
                log.warn("⚠️ 未找到需要更新的排团记录: 订单ID={}", bookingId);
            } 
        } catch (Exception e) {
            log.error("❌ 直接更新排团表联系人信息失败: 订单ID={}, 错误: {}", bookingId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 自动同步订单数据到排团表
     * 供订单创建时自动调用
     */
    @Override
    @Transactional
    public void autoSyncOrderToScheduleTable(Integer bookingId) {
        log.info("🔄 进入autoSyncOrderToScheduleTable方法，订单ID: {}", bookingId);
        
        TourBooking tourBooking = tourBookingMapper.getById(bookingId);
        if (tourBooking == null) {
            log.warn("订单不存在，无法自动同步到排团表: {}", bookingId);
            return;
        }
        
        log.info("🔄 开始自动同步订单到排团表: 订单ID={}, 订单号={}, 行程类型={}, 行程ID={}", 
            bookingId, tourBooking.getOrderNumber(), tourBooking.getTourType(), tourBooking.getTourId());
        
        try {
            // 先删除该订单可能已存在的排团记录（防止重复）
            log.info("🗑️ 删除可能存在的排团记录，订单ID: {}", bookingId);
            tourScheduleOrderMapper.deleteByBookingId(bookingId);
            
            // 创建新的排团记录
            log.info("🆕 开始创建新的排团记录，订单ID: {}", bookingId);
            autoCreateScheduleOrderFromBooking(tourBooking);
            
            log.info("✅ 自动同步订单数据到排团表完成，订单ID: {}", bookingId);
        } catch (Exception e) {
            log.error("❌ 自动同步订单到排团表失败: 订单ID={}, 错误类型: {}, 错误消息: {}", 
                bookingId, e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }
    


    /**
     * 自动创建排团记录
     * 根据行程天数创建多条排团记录
     */
    private void autoCreateScheduleOrderFromBooking(TourBooking tourBooking) {
        log.info("🔄 进入autoCreateScheduleOrderFromBooking方法，订单ID: {}", 
            tourBooking != null ? tourBooking.getBookingId() : "null");
        
        if (tourBooking == null || tourBooking.getBookingId() == null) {
            log.warn("订单信息为空，跳过自动创建排团记录");
            return;
        }

        try {
            // 计算行程天数
            log.info("📅 计算行程天数: 开始日期={}, 结束日期={}", 
                tourBooking.getTourStartDate(), tourBooking.getTourEndDate());
            int tourDays = calculateTourDays(tourBooking.getTourStartDate(), tourBooking.getTourEndDate());
            log.info("📅 计算得出行程天数: {}", tourDays);
            
            // 获取产品信息用于生成行程标题
            log.info("🏷️ 获取产品名称: tourId={}, tourType={}", 
                tourBooking.getTourId(), tourBooking.getTourType());
            String tourName = getTourName(tourBooking.getTourId(), tourBooking.getTourType());
            log.info("🏷️ 获取到产品名称: {}", tourName);
            
            log.info("📅 开始为订单 {} 创建 {} 天的排团记录", tourBooking.getOrderNumber(), tourDays);
            
            // 为每一天创建排团记录
            for (int day = 1; day <= tourDays; day++) {
                log.info("🆕 创建第{}天的排团记录", day);
                TourScheduleOrder scheduleOrder = createScheduleOrderFromBooking(tourBooking, day, tourName);
                log.info("🆕 第{}天排团记录创建完成，准备设置航班信息", day);
                
                // 🔥 重新设置智能航班信息分配
                setSmartFlightInfo(scheduleOrder, tourBooking, day, tourDays);
                log.info("🆕 第{}天航班信息设置完成，准备插入数据库", day);
                
                tourScheduleOrderMapper.insert(scheduleOrder);
                log.info("✅ 创建排团记录: 订单ID={}, 第{}天, 日期={}", 
                    tourBooking.getBookingId(), day, scheduleOrder.getTourDate());
            }
            
            log.info("✅ 订单 {} 的所有排团记录创建完成，共{}天", tourBooking.getOrderNumber(), tourDays);
        } catch (Exception e) {
            log.error("❌ 自动创建排团记录时出错: 订单ID={}, 错误类型: {}, 错误消息: {}", 
                tourBooking.getBookingId(), e.getClass().getSimpleName(), e.getMessage(), e);
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
        
        // 智能生成行程标题（优先使用用户选择的可选项目，回退到产品行程详情）
        String title = getItineraryTitleFromProduct(booking.getTourId(), booking.getTourType(), dayNumber, tourName, booking.getSelectedOptionalTours());
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
        
        // 🆕 设置乘客信息（从乘客表获取完整信息并存储到正确字段）
        try {
            // 获取该订单的乘客信息
            List<PassengerVO> passengers = passengerService.getByBookingId(booking.getBookingId());
            if (passengers != null && !passengers.isEmpty()) {
                // 获取主要联系人信息（通常是第一个成人乘客）
                PassengerVO primaryPassenger = null;
                String primaryPhone = null;
                
                // 查找主要联系人（优先选择有电话的成人乘客）
                for (PassengerVO passenger : passengers) {
                    if (passenger.getIsChild() == null || !passenger.getIsChild()) {
                        // 成人乘客
                        if (passenger.getPhone() != null && !passenger.getPhone().trim().isEmpty()) {
                            primaryPassenger = passenger;
                            primaryPhone = passenger.getPhone();
                            break; // 找到有电话的成人乘客，优先使用
                        } else if (primaryPassenger == null) {
                            primaryPassenger = passenger; // 备选：没有电话的成人乘客
                        }
                    }
                }
                
                // 如果没有成人乘客，使用第一个乘客
                if (primaryPassenger == null && !passengers.isEmpty()) {
                    primaryPassenger = passengers.get(0);
                    if (primaryPassenger.getPhone() != null && !primaryPassenger.getPhone().trim().isEmpty()) {
                        primaryPhone = primaryPassenger.getPhone();
                    }
                }
                
                // 设置主要联系人信息到contact_person和contact_phone字段
                if (primaryPassenger != null) {
                    String contactPersonName = primaryPassenger.getFullName() != null ? 
                        primaryPassenger.getFullName() : "未知乘客";
                    scheduleOrder.setContactPerson(contactPersonName);
                    
                    if (primaryPhone != null) {
                        scheduleOrder.setContactPhone(primaryPhone);
                    } else {
                        // 如果主要联系人没有电话，尝试使用订单的联系电话
                        scheduleOrder.setContactPhone(booking.getContactPhone());
                    }
                    
                    log.info("✅ 设置主要联系人: 订单ID={}, 姓名=\"{}\", 电话=\"{}\"", 
                            booking.getBookingId(), contactPersonName, scheduleOrder.getContactPhone());
                }
                
                // 将完整乘客信息存储到itinerary_details字段作为详细记录
                StringBuilder passengerInfo = new StringBuilder();
                passengerInfo.append("乘客信息:\n");
                
                int adultCount = 0;
                int childCount = 0;
                
                for (PassengerVO passenger : passengers) {
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
                
                // 将乘客详细信息存储到itinerary_details字段
                String originalItinerary = booking.getItineraryDetails() != null ? booking.getItineraryDetails() : "";
                String combinedDetails = originalItinerary.isEmpty() ? 
                    passengerInfo.toString() : 
                    originalItinerary + "\n\n" + passengerInfo.toString();
                scheduleOrder.setItineraryDetails(combinedDetails);
                
                log.info("✅ 已设置完整乘客信息: 订单ID={}, 乘客数量={}, 成人{}人, 儿童{}人", 
                        booking.getBookingId(), passengers.size(), adultCount, childCount);
            } else {
                // 如果没有乘客信息，使用订单的联系人信息
                scheduleOrder.setContactPerson(booking.getContactPerson());
                scheduleOrder.setContactPhone(booking.getContactPhone());
                scheduleOrder.setItineraryDetails(booking.getItineraryDetails());
                log.warn("⚠️ 订单{}没有找到乘客信息，使用订单联系人信息", booking.getBookingId());
            }
        } catch (Exception e) {
            log.error("❌ 获取乘客信息失败: 订单ID={}, 错误: {}", booking.getBookingId(), e.getMessage(), e);
            // 失败时使用订单的联系人信息
            scheduleOrder.setContactPerson(booking.getContactPerson());
            scheduleOrder.setContactPhone(booking.getContactPhone());
            scheduleOrder.setItineraryDetails(booking.getItineraryDetails());
        }
        
        // 复制订单的其他字段（完整同步所有订单信息到排团表）
        scheduleOrder.setOrderNumber(booking.getOrderNumber());
        scheduleOrder.setAdultCount(booking.getAdultCount());
        scheduleOrder.setChildCount(booking.getChildCount());
        // 注意：contactPerson和contactPhone已在上面的乘客信息处理中设置，这里不再覆盖
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
     * 从产品行程中获取行程标题（支持可选项目）
     * @param tourId 产品ID
     * @param tourType 产品类型
     * @param dayNumber 天数
     * @param tourName 产品名称（备用）
     * @param selectedOptionalTours 用户选择的可选项目（JSON字符串）
     * @return 行程标题（不含天数前缀）
     */
    private String getItineraryTitleFromProduct(Integer tourId, String tourType, int dayNumber, String tourName, String selectedOptionalTours) {
        try {
            // 1. 如果是跟团游且有用户选择的可选项目，优先使用用户选择
            if ("group_tour".equals(tourType) && selectedOptionalTours != null && !selectedOptionalTours.trim().isEmpty()) {
                try {
                    // 解析用户选择的可选项目JSON
                    Map<String, Object> selectedTours = parseSelectedOptionalTours(selectedOptionalTours);
                    String dayKey = String.valueOf(dayNumber);
                    
                    if (selectedTours.containsKey(dayKey)) {
                        Integer selectedDayTourId = Integer.valueOf(selectedTours.get(dayKey).toString());
                        
                        // 获取选择的一日游信息
                        DayTour selectedDayTour = dayTourMapper.getById(selectedDayTourId);
                        if (selectedDayTour != null) {
                            log.info("✅ 使用用户选择的可选项目: tourId={}, 第{}天, 选择的一日游=\"{}\"", 
                                    tourId, dayNumber, selectedDayTour.getName());
                            return selectedDayTour.getName();
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析用户选择的可选项目失败: {}", e.getMessage());
                }
            }
            
            // 2. 获取产品的行程信息（优先使用关联一日游，回退到标准行程）
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
     * 解析用户选择的可选项目JSON字符串
     * @param selectedOptionalTours JSON字符串
     * @return 解析后的Map
     */
    private Map<String, Object> parseSelectedOptionalTours(String selectedOptionalTours) {
        try {
            // 简单的JSON解析（这里可以使用Jackson或其他JSON库）
            Map<String, Object> result = new HashMap<>();
            
            // 移除大括号和空格
            String cleaned = selectedOptionalTours.replaceAll("[{}\\s]", "");
            
            if (!cleaned.isEmpty()) {
                // 按逗号分割键值对
                String[] pairs = cleaned.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        String key = keyValue[0].replaceAll("\"", "");
                        String value = keyValue[1].replaceAll("\"", "");
                        result.put(key, value);
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("解析可选项目JSON失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * 从产品行程中获取行程标题（兼容旧版本）
     * @param tourId 产品ID
     * @param tourType 产品类型
     * @param dayNumber 天数
     * @param tourName 产品名称（备用）
     * @return 行程标题（不含天数前缀）
     */
    private String getItineraryTitleFromProduct(Integer tourId, String tourType, int dayNumber, String tourName) {
        return getItineraryTitleFromProduct(tourId, tourType, dayNumber, tourName, null);
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

    /**
     * 计算价格明细（带儿童年龄详细信息）
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
     * @param childrenAges 儿童年龄数组
     * @return 价格明细和儿童详细价格信息
     */
    public Map<String, Object> calculatePriceDetailWithChildrenAges(Integer tourId, String tourType, Long agentId, 
                                                                   Integer adultCount, Integer childCount, String hotelLevel, 
                                                                   Integer roomCount, Long userId, String roomType, 
                                                                   String childrenAges) {
        log.info("计算价格明细（支持儿童年龄）: tourId={}, tourType={}, agentId={}, adultCount={}, childCount={}, hotelLevel={}, roomCount={}, userId={}, roomType={}, childrenAges={}", 
                tourId, tourType, agentId, adultCount, childCount, hotelLevel, roomCount, userId, roomType, childrenAges);
        
        // 获取基础价格信息
        BigDecimal baseUnitPrice = BigDecimal.ZERO;
        int nights = 0;
        
        if ("day_tour".equals(tourType)) {
            DayTour dayTour = dayTourMapper.getById(tourId);
            if (dayTour == null) {
                log.error("找不到一日游产品: {}", tourId);
                return buildErrorResponse("找不到指定的一日游产品");
            }
            baseUnitPrice = dayTour.getPrice();
            nights = 0; // 一日游无住宿
        } else if ("group_tour".equals(tourType)) {
            GroupTourDTO groupTour = groupTourMapper.getById(tourId);
            if (groupTour == null) {
                log.error("找不到跟团游产品: {}", tourId);
                return buildErrorResponse("找不到指定的跟团游产品");
            }
            
            if (groupTour.getDiscountedPrice() != null && groupTour.getDiscountedPrice().compareTo(BigDecimal.ZERO) > 0) {
                baseUnitPrice = groupTour.getDiscountedPrice();
            } else {
                baseUnitPrice = groupTour.getPrice();
            }
            
            // 解析住宿夜数
            try {
                String duration = groupTour.getDuration();
                if (duration != null && duration.contains("天")) {
                    String daysStr = duration.substring(0, duration.indexOf("天"));
                    int days = Integer.parseInt(daysStr);
                    nights = days > 1 ? days - 1 : 0;
                }
            } catch (Exception e) {
                log.warn("解析行程天数失败: {}", e.getMessage());
                nights = 1;
            }
        } else {
            log.error("无效的旅游类型: {}", tourType);
            return buildErrorResponse("无效的旅游类型");
        }
        
        // 确保参数有效性
        if (adultCount == null || adultCount < 0) adultCount = 0;
        if (childCount == null || childCount < 0) childCount = 0;
        if (roomCount == null || roomCount <= 0) roomCount = 1;
        
        // 使用智能折扣系统计算代理商折扣
        BigDecimal discountRate = BigDecimal.ONE;
        if (agentId != null) {
            try {
                // 使用智能折扣服务，优先使用产品级别折扣，回退到统一折扣
                Map<String, Object> discountResult = discountService.calculateTourDiscount(
                    tourId.longValue(), tourType, baseUnitPrice, agentId);
                
                if (discountResult != null && discountResult.get("discountRate") != null) {
                    discountRate = (BigDecimal) discountResult.get("discountRate");
                    boolean enhancedMode = Boolean.TRUE.equals(discountResult.get("enhancedMode"));
                    log.info("获取到代理商折扣率: {} (代理商ID: {}, 使用{}模式)", 
                            discountRate, agentId, enhancedMode ? "产品级别折扣" : "统一折扣");
                } else {
                    log.warn("折扣计算服务返回空结果，使用默认折扣率");
                }
            } catch (Exception e) {
                log.error("获取代理商折扣信息失败，使用默认折扣率: {}", e.getMessage(), e);
            }
        }
        
        // 应用折扣率到基础单价
        BigDecimal discountedUnitPrice = baseUnitPrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        log.info("价格计算: 基础单价={}, 折扣率={}, 折扣后单价={}", baseUnitPrice, discountRate, discountedUnitPrice);
        
        // 计算成人总价格
        BigDecimal adultTotalPrice = discountedUnitPrice.multiply(BigDecimal.valueOf(adultCount));
        
        // 计算儿童总价格（根据年龄区分定价）
        BigDecimal childTotalPrice = BigDecimal.ZERO;
        if (childCount > 0 && childrenAges != null && !childrenAges.trim().isEmpty()) {
            try {
                String[] ageArray = childrenAges.split(",");
                for (String ageStr : ageArray) {
                    if (ageStr != null && !ageStr.trim().isEmpty()) {
                        int age = Integer.parseInt(ageStr.trim());
                        BigDecimal childPrice;
                        
                        if (age >= 1 && age <= 2) {
                            // 1-2岁：半价
                            childPrice = discountedUnitPrice.multiply(new BigDecimal("0.5"));
                            log.info("儿童{}岁，半价: {}", age, childPrice);
                        } else if (age >= 3) {
                            // 3岁以上：成人价减50元
                            BigDecimal childDiscount = new BigDecimal("50");
                            childPrice = discountedUnitPrice.subtract(childDiscount);
                            if (childPrice.compareTo(BigDecimal.ZERO) < 0) {
                                childPrice = BigDecimal.ZERO;
                            }
                            log.info("儿童{}岁，成人价减50元: {}", age, childPrice);
                        } else {
                            // 0岁：免费
                            childPrice = BigDecimal.ZERO;
                            log.info("儿童{}岁，免费", age);
                        }
                        
                        childTotalPrice = childTotalPrice.add(childPrice);
                    }
                }
            } catch (Exception e) {
                log.error("解析儿童年龄失败: {}", e.getMessage(), e);
                // 如果解析失败，使用默认的儿童价格计算（减50元）
                BigDecimal childDiscount = new BigDecimal("50");
                BigDecimal childUnitPrice = discountedUnitPrice.subtract(childDiscount);
                if (childUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
                    childUnitPrice = BigDecimal.ZERO;
                }
                childTotalPrice = childUnitPrice.multiply(BigDecimal.valueOf(childCount));
            }
        }
        
        // 基础总价（人员费用）
        BigDecimal baseTotalPrice = adultTotalPrice.add(childTotalPrice);
        BigDecimal extraRoomFee = BigDecimal.ZERO;
        
        // 计算酒店相关费用（如果有住宿夜数）
        if (nights > 0 && hotelLevel != null) {
            try {
                // 获取酒店价格差异（相对于基准酒店等级的差价）
                BigDecimal hotelPriceDiff = hotelPriceService.getPriceDifferenceByLevel(hotelLevel);
                
                // 计算酒店差价总额: 差价 * 夜数 * 人数
                int totalPeople = adultCount + childCount;
                BigDecimal totalHotelPriceDiff = hotelPriceDiff.multiply(BigDecimal.valueOf(nights))
                                                              .multiply(BigDecimal.valueOf(totalPeople));
                baseTotalPrice = baseTotalPrice.add(totalHotelPriceDiff);
                extraRoomFee = extraRoomFee.add(totalHotelPriceDiff);
                
                log.info("酒店差价计算: 酒店等级={}, 每人每晚差价={}, 住宿夜数={}, 总人数={}, 酒店差价总额={}", 
                        hotelLevel, hotelPriceDiff, nights, totalPeople, totalHotelPriceDiff);
                
                // 计算三人房差价费用
                if (roomType != null && (roomType.contains("三人间") || roomType.contains("三床") || 
                    roomType.contains("家庭") || roomType.equalsIgnoreCase("triple") || 
                    roomType.equalsIgnoreCase("family"))) {
                    BigDecimal tripleDifference = hotelPriceService.getTripleBedRoomPriceDifferenceByLevel(hotelLevel);
                    BigDecimal tripleRoomFee = tripleDifference.multiply(BigDecimal.valueOf(nights))
                                                             .multiply(BigDecimal.valueOf(roomCount));
                    baseTotalPrice = baseTotalPrice.add(tripleRoomFee);
                    extraRoomFee = extraRoomFee.add(tripleRoomFee);
                    log.info("三人房差价费用: {}", tripleRoomFee);
                }
                
                // 计算单房差
                double totalRooms = totalPeople / 2.0;
                int includedRoomsFloor = (int) Math.floor(totalRooms);
                int includedRoomsCeil = (int) Math.ceil(totalRooms);
                
                if (roomCount == includedRoomsCeil && totalRooms > includedRoomsFloor) {
                    BigDecimal singleRoomSupplement = hotelPriceService.getDailySingleRoomSupplementByLevel(hotelLevel);
                    BigDecimal singleSupplementCost = singleRoomSupplement.multiply(BigDecimal.valueOf(nights));
                    baseTotalPrice = baseTotalPrice.add(singleSupplementCost);
                    extraRoomFee = extraRoomFee.add(singleSupplementCost);
                    log.info("单房差费用: {}", singleSupplementCost);
                } else if (roomCount > includedRoomsCeil) {
                    // 额外房间费用计算逻辑
                    BigDecimal roomPrice = getRoomPriceByType(hotelLevel, roomType);
                    int extraRooms = roomCount - includedRoomsCeil;
                    BigDecimal extraRoomCost = roomPrice.multiply(BigDecimal.valueOf(nights))
                                                       .multiply(BigDecimal.valueOf(extraRooms));
                    baseTotalPrice = baseTotalPrice.add(extraRoomCost);
                    extraRoomFee = extraRoomFee.add(extraRoomCost);
                    log.info("额外房间费用: {}", extraRoomCost);
                }
            } catch (Exception e) {
                log.error("计算酒店相关费用失败: {}", e.getMessage(), e);
            }
        }
        
        // 计算非代理商价格（原价）
        BigDecimal nonAgentPrice = baseTotalPrice.divide(discountRate, 2, RoundingMode.HALF_UP);
        
        log.info("价格计算完成（支持儿童年龄）: 总价={}, 基础价格={}, 额外房费={}, 非代理商价格={}, 成人数={}, 儿童数={}", 
                baseTotalPrice, adultTotalPrice.add(childTotalPrice), extraRoomFee, nonAgentPrice, adultCount, childCount);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 1);
        result.put("msg", "计算成功");
        
        Map<String, Object> data = new HashMap<>();
        data.put("totalPrice", baseTotalPrice);
        data.put("basePrice", adultTotalPrice.add(childTotalPrice));
        data.put("extraRoomFee", extraRoomFee);
        data.put("nonAgentPrice", nonAgentPrice);
        data.put("originalPrice", baseUnitPrice.multiply(BigDecimal.valueOf(adultCount + childCount)));
        data.put("discountedPrice", baseTotalPrice);
        
        result.put("data", data);
        return result;
    }
    
    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", message);
        result.put("data", null);
        return result;
    }

    /** 
     * 统一的价格计算方法（支持所有功能）
     * 这个方法整合了所有价格计算功能，包括：
     * - 多房间类型支持
     * - 儿童年龄详细定价
     * - 可选行程价格差异
     * - 代理商折扣
     * - 酒店等级差价
     * - 单房差和额外房间费用
     */
    @Override
    public Map<String, Object> calculateUnifiedPrice(Integer tourId, String tourType, Long agentId, 
                                                              Integer adultCount, Integer childCount, String hotelLevel, 
                                                   Integer roomCount, Long userId, String roomTypes, 
                                                   String childrenAges, String selectedOptionalTours) {
        log.info("统一价格计算: tourId={}, tourType={}, agentId={}, adultCount={}, childCount={}, hotelLevel={}, roomCount={}, userId={}, roomTypes={}, childrenAges={}, selectedOptionalTours={}", 
                tourId, tourType, agentId, adultCount, childCount, hotelLevel, roomCount, userId, roomTypes, childrenAges, selectedOptionalTours);
        
        // 参数验证
        if (tourId == null || tourType == null) {
            log.error("必要参数缺失: tourId={}, tourType={}", tourId, tourType);
            return buildErrorResponse("旅游产品ID和类型不能为空");
        }
        
        // 设置默认值
        if (adultCount == null || adultCount < 0) adultCount = 0;
        if (childCount == null || childCount < 0) childCount = 0;
        if (roomCount == null || roomCount <= 0) roomCount = 1;
        if (hotelLevel == null || hotelLevel.trim().isEmpty()) hotelLevel = "4星";
        
        // 解析房间类型数组
        List<String> roomTypeList = parseRoomTypes(roomTypes, roomCount);
        log.info("解析房间类型: {}", roomTypeList);
        
        // 获取基础价格信息（不包含可选行程）
        PriceBaseInfo baseInfo = getBasePriceInfo(tourId, tourType, null, adultCount, childCount);
        if (baseInfo == null) {
            return buildErrorResponse("获取产品基础信息失败");
        }
        
        // 使用智能折扣系统计算代理商折扣
        BigDecimal discountRate = BigDecimal.ONE;
        if (agentId != null) {
            try {
                // 使用智能折扣服务，优先使用产品级别折扣，回退到统一折扣
                Map<String, Object> discountResult = discountService.calculateTourDiscount(
                    tourId.longValue(), tourType, baseInfo.baseUnitPrice, agentId);
                
                if (discountResult != null && discountResult.get("discountRate") != null) {
                    discountRate = (BigDecimal) discountResult.get("discountRate");
                    boolean enhancedMode = Boolean.TRUE.equals(discountResult.get("enhancedMode"));
                    log.info("统一价格计算获取到代理商折扣率: {} (代理商ID: {}, 使用{}模式)", 
                            discountRate, agentId, enhancedMode ? "产品级别折扣" : "统一折扣");
                } else {
                    log.warn("统一价格计算折扣服务返回空结果，使用默认折扣率");
                }
            } catch (Exception e) {
                log.error("统一价格计算获取代理商折扣信息失败，使用默认折扣率: {}", e.getMessage(), e);
            }
        }
        
        BigDecimal discountedBaseUnitPrice = baseInfo.baseUnitPrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        log.info("价格计算: 基础单价={}, 折扣率={}, 折扣后基础单价={}", baseInfo.baseUnitPrice, discountRate, discountedBaseUnitPrice);
        
        // 单独计算可选行程差价（不打折）
        BigDecimal optionalTourPriceDiff = BigDecimal.ZERO;
        if (selectedOptionalTours != null && !selectedOptionalTours.trim().isEmpty()) {
            optionalTourPriceDiff = calculateOptionalTourPriceDiff(tourId, selectedOptionalTours, adultCount, childCount);
            log.info("可选行程差价（不打折）: {}元", optionalTourPriceDiff);
        }
        
        // 最终单价 = 折扣后基础单价 + 可选行程差价
        BigDecimal finalUnitPrice = discountedBaseUnitPrice.add(optionalTourPriceDiff);
        log.info("最终单价: 折扣后基础单价={} + 可选行程差价={} = {}", discountedBaseUnitPrice, optionalTourPriceDiff, finalUnitPrice);
        
        // 计算人员费用
        PersonPriceInfo personPrice = calculatePersonPrice(finalUnitPrice, adultCount, childCount, childrenAges);
        
        // 计算住宿相关费用
        AccommodationPriceInfo accommodationPrice = calculateAccommodationPrice(
            hotelLevel, baseInfo.nights, adultCount, childCount, roomCount, roomTypeList);
        
        // 汇总总价
        BigDecimal totalPrice = personPrice.totalPersonPrice.add(accommodationPrice.totalAccommodationFee);
        BigDecimal nonAgentPrice = totalPrice.divide(discountRate, 2, RoundingMode.HALF_UP);
        
        log.info("统一价格计算完成: 总价={}, 人员费用={}, 住宿费用={}, 非代理商价格={}", 
                totalPrice, personPrice.totalPersonPrice, accommodationPrice.totalAccommodationFee, nonAgentPrice);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("code", 1);
        result.put("msg", "计算成功");
        
        Map<String, Object> data = new HashMap<>();
        data.put("totalPrice", totalPrice);
        data.put("basePrice", personPrice.totalPersonPrice);
        data.put("extraRoomFee", accommodationPrice.totalAccommodationFee);
        data.put("nonAgentPrice", nonAgentPrice);
        data.put("originalPrice", baseInfo.baseUnitPrice.multiply(BigDecimal.valueOf(adultCount + childCount)));
        data.put("discountedPrice", totalPrice);
        data.put("roomTypes", roomTypeList);
        
        // 如果有儿童详细信息，添加到结果中
        if (personPrice.childrenDetails != null && !personPrice.childrenDetails.isEmpty()) {
            data.put("childrenDetails", personPrice.childrenDetails);
        }
        
        result.put("data", data);
        return result;
    }
    
    /**
     * 解析房间类型
     */
    private List<String> parseRoomTypes(String roomTypes, Integer roomCount) {
        List<String> roomTypeList = new ArrayList<>();
        
        if (roomTypes != null && !roomTypes.trim().isEmpty()) {
            try {
                // 如果是JSON数组格式
                if (roomTypes.startsWith("[") && roomTypes.endsWith("]")) {
                    String cleanRoomTypes = roomTypes.replace("[", "").replace("]", "").replace("\"", "");
                    String[] roomTypeArray = cleanRoomTypes.split(",");
                    for (String roomType : roomTypeArray) {
                        roomTypeList.add(roomType.trim());
                    }
                } else {
                    // 单个房型字符串
                    roomTypeList.add(roomTypes.trim());
                }
            } catch (Exception e) {
                log.error("解析房间类型失败: {}", e.getMessage(), e);
            }
        }
        
        // 确保房间类型数量与房间数量一致
        while (roomTypeList.size() < roomCount) {
            roomTypeList.add("大床房"); // 补充默认房型
        }
        if (roomTypeList.size() > roomCount) {
            roomTypeList = roomTypeList.subList(0, roomCount); // 截取到指定数量
        }
        
        return roomTypeList;
    }
    
    /**
     * 获取基础价格信息
     */
    private PriceBaseInfo getBasePriceInfo(Integer tourId, String tourType, String selectedOptionalTours, 
                                         Integer adultCount, Integer childCount) {
        BigDecimal baseUnitPrice = BigDecimal.ZERO;
        int nights = 0;
        
        if ("day_tour".equals(tourType)) {
            DayTour dayTour = dayTourMapper.getById(tourId);
            if (dayTour == null) {
                log.error("找不到一日游产品: {}", tourId);
                return null;
            }
            baseUnitPrice = dayTour.getPrice();
            nights = 0; // 一日游无住宿
        } else if ("group_tour".equals(tourType)) {
            GroupTourDTO groupTour = groupTourMapper.getById(tourId);
            if (groupTour == null) {
                log.error("找不到跟团游产品: {}", tourId);
                return null;
            }
            
            if (groupTour.getDiscountedPrice() != null && groupTour.getDiscountedPrice().compareTo(BigDecimal.ZERO) > 0) {
                baseUnitPrice = groupTour.getDiscountedPrice();
            } else {
                baseUnitPrice = groupTour.getPrice();
            }
            
            // 解析住宿夜数
            try {
                String duration = groupTour.getDuration();
                if (duration != null && duration.contains("天")) {
                    String daysStr = duration.substring(0, duration.indexOf("天"));
                    int days = Integer.parseInt(daysStr);
                    nights = days > 1 ? days - 1 : 0;
                }
            } catch (Exception e) {
                log.warn("解析行程天数失败: {}", e.getMessage());
                nights = 1;
            }
            
            // 处理可选行程价格差异
            baseUnitPrice = processOptionalTours(baseUnitPrice, tourId, selectedOptionalTours, adultCount, childCount);
        } else {
            log.error("无效的旅游类型: {}", tourType);
            return null;
        }
        
        return new PriceBaseInfo(baseUnitPrice, nights);
    }
    
    /**
     * 处理可选行程价格差异（旧方法，保持向后兼容）
     */
    private BigDecimal processOptionalTours(BigDecimal baseUnitPrice, Integer tourId, String selectedOptionalTours, 
                                          Integer adultCount, Integer childCount) {
        if (selectedOptionalTours == null || selectedOptionalTours.trim().isEmpty()) {
            return baseUnitPrice;
        }
        
        BigDecimal priceDiff = calculateOptionalTourPriceDiff(tourId, selectedOptionalTours, adultCount, childCount);
        return baseUnitPrice.add(priceDiff);
    }
    
    /**
     * 计算可选行程价格差异（新方法，返回纯差价不修改基础价格）
     */
    private BigDecimal calculateOptionalTourPriceDiff(Integer tourId, String selectedOptionalTours, 
                                                    Integer adultCount, Integer childCount) {
        if (selectedOptionalTours == null || selectedOptionalTours.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            Map<String, Object> selectedTours = parseSelectedOptionalTours(selectedOptionalTours);
            log.info("用户选择了可选项目: {}", selectedTours);
            
            BigDecimal totalOptionalPriceDiff = BigDecimal.ZERO;
            
            for (Map.Entry<String, Object> entry : selectedTours.entrySet()) {
                try {
                    Integer dayNumber = Integer.valueOf(entry.getKey());
                    Integer dayTourId = Integer.valueOf(entry.getValue().toString());
                    
                    BigDecimal priceDiff = groupTourDayTourRelationMapper.getPriceDifferenceByTourAndDay(
                        tourId, dayTourId, dayNumber);
                    
                    if (priceDiff != null && priceDiff.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal totalPeoplePriceDiff = priceDiff.multiply(BigDecimal.valueOf(adultCount + childCount));
                        totalOptionalPriceDiff = totalOptionalPriceDiff.add(totalPeoplePriceDiff);
                        
                        DayTour dayTour = dayTourMapper.getById(dayTourId);
                        String tourName = dayTour != null ? dayTour.getName() : "未知行程";
                        log.info("第{}天选择的可选项目: {} (价格差异: {}元/人, 总差异: {}元)", 
                                dayNumber, tourName, priceDiff, totalPeoplePriceDiff);
                    }
                } catch (Exception e) {
                    log.warn("解析第{}天的可选项目价格差异失败: {}", entry.getKey(), e.getMessage());
                }
            }
            
            if (totalOptionalPriceDiff.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal perPersonPriceDiff = totalOptionalPriceDiff.divide(
                    BigDecimal.valueOf(adultCount + childCount), 2, RoundingMode.HALF_UP);
                log.info("可选项目价格差异: 总差异={}元, 人均差异={}元", 
                        totalOptionalPriceDiff, perPersonPriceDiff);
                return perPersonPriceDiff;
            }
        } catch (Exception e) {
            log.error("计算可选项目价格差异失败: {}", e.getMessage(), e);
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * 获取折扣率
     */
    private BigDecimal getDiscountRate(Long agentId) {
        if (agentId == null) {
            return BigDecimal.ONE;
        }
        
            try {
                Agent agent = agentMapper.getById(agentId);
                if (agent != null && agent.getDiscountRate() != null) {
                log.info("获取到代理商折扣率: {} (代理商ID: {})", agent.getDiscountRate(), agentId);
                return agent.getDiscountRate();
                }
            } catch (Exception e) {
                log.error("获取代理商信息失败: {}", e.getMessage(), e);
            }
        
        return BigDecimal.ONE;
    }
    
    /**
     * 计算人员费用
     */
    private PersonPriceInfo calculatePersonPrice(BigDecimal discountedUnitPrice, Integer adultCount, 
                                               Integer childCount, String childrenAges) {
        // 计算成人总价格
        BigDecimal adultTotalPrice = discountedUnitPrice.multiply(BigDecimal.valueOf(adultCount));
        
        // 计算儿童总价格
        BigDecimal childTotalPrice = BigDecimal.ZERO;
        List<Map<String, Object>> childrenDetails = new ArrayList<>();
        
        if (childCount > 0) {
            if (childrenAges != null && !childrenAges.trim().isEmpty()) {
                // 根据年龄详细计算儿童价格
                try {
                    String[] ageArray = childrenAges.split(",");
                    for (int i = 0; i < ageArray.length && i < childCount; i++) {
                        String ageStr = ageArray[i].trim();
                        if (!ageStr.isEmpty()) {
                            int age = Integer.parseInt(ageStr);
                            BigDecimal childPrice = calculateChildPrice(discountedUnitPrice, age);
                            childTotalPrice = childTotalPrice.add(childPrice);
                            
                            Map<String, Object> childDetail = new HashMap<>();
                            childDetail.put("age", age);
                            childDetail.put("price", childPrice);
                            childDetail.put("priceRule", getChildPriceRule(age));
                            childrenDetails.add(childDetail);
                            
                            log.info("儿童{}岁，价格: {}", age, childPrice);
                        }
                    }
                } catch (Exception e) {
                    log.error("解析儿童年龄失败: {}", e.getMessage(), e);
                    // 如果解析失败，使用默认的儿童价格计算
                    childTotalPrice = calculateDefaultChildPrice(discountedUnitPrice, childCount);
                }
            } else {
                // 没有年龄信息，使用默认儿童价格
                childTotalPrice = calculateDefaultChildPrice(discountedUnitPrice, childCount);
            }
        }
        
        BigDecimal totalPersonPrice = adultTotalPrice.add(childTotalPrice);
        return new PersonPriceInfo(totalPersonPrice, childrenDetails);
    }
    
    /**
     * 根据年龄计算儿童价格
     */
    private BigDecimal calculateChildPrice(BigDecimal adultPrice, int age) {
        if (age >= 1 && age <= 2) {
            // 1-2岁：半价
            return adultPrice.multiply(new BigDecimal("0.5"));
        } else if (age >= 3) {
            // 3岁以上：成人价减50元
        BigDecimal childDiscount = new BigDecimal("50");
            BigDecimal childPrice = adultPrice.subtract(childDiscount);
            return childPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : childPrice;
        } else {
            // 0岁：免费
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 获取儿童价格规则描述
     */
    private String getChildPriceRule(int age) {
        if (age >= 1 && age <= 2) {
            return "1-2岁半价";
        } else if (age >= 3) {
            return "3岁以上成人价减50元";
        } else {
            return "0岁免费";
        }
    }
    
    /**
     * 计算默认儿童价格
     */
    private BigDecimal calculateDefaultChildPrice(BigDecimal adultPrice, Integer childCount) {
        BigDecimal childDiscount = new BigDecimal("50");
        BigDecimal childUnitPrice = adultPrice.subtract(childDiscount);
        if (childUnitPrice.compareTo(BigDecimal.ZERO) < 0) {
            childUnitPrice = BigDecimal.ZERO;
        }
        return childUnitPrice.multiply(BigDecimal.valueOf(childCount));
    }
    
    /**
     * 计算住宿相关费用
     */
    private AccommodationPriceInfo calculateAccommodationPrice(String hotelLevel, int nights, 
                                                             Integer adultCount, Integer childCount, 
                                                             Integer roomCount, List<String> roomTypeList) {
        BigDecimal totalAccommodationFee = BigDecimal.ZERO;
        
        if (nights <= 0) {
            return new AccommodationPriceInfo(totalAccommodationFee);
        }
        
        try {
            // 获取酒店价格差异
                BigDecimal hotelPriceDiff = hotelPriceService.getPriceDifferenceByLevel(hotelLevel);
                int totalPeople = adultCount + childCount;
                BigDecimal totalHotelPriceDiff = hotelPriceDiff.multiply(BigDecimal.valueOf(nights))
                                                              .multiply(BigDecimal.valueOf(totalPeople));
            totalAccommodationFee = totalAccommodationFee.add(totalHotelPriceDiff);
                
                log.info("酒店差价计算: 酒店等级={}, 每人每晚差价={}, 住宿夜数={}, 总人数={}, 酒店差价总额={}", 
                        hotelLevel, hotelPriceDiff, nights, totalPeople, totalHotelPriceDiff);
                
            // 计算单房差和额外房间费用
            double totalRooms = totalPeople / 2.0;
            int includedRoomsFloor = (int) Math.floor(totalRooms);
            int includedRoomsCeil = (int) Math.ceil(totalRooms);
            
            // 计算基础房间的特殊费用（如三人房差价）- 只对基础需求内的房间收取差价
            int basicRoomsNeeded = Math.min(includedRoomsCeil, roomTypeList.size());
            for (int i = 0; i < basicRoomsNeeded; i++) {
                String roomType = roomTypeList.get(i);
                log.info("计算基础房间{}的费用，房型: {}", i + 1, roomType);
                
                if (roomType != null && (roomType.contains("三人间") || roomType.contains("三床") || 
                    roomType.contains("家庭") || roomType.equalsIgnoreCase("triple") || 
                    roomType.equalsIgnoreCase("family"))) {
                    BigDecimal tripleDifference = hotelPriceService.getTripleBedRoomPriceDifferenceByLevel(hotelLevel);
                    BigDecimal tripleRoomFee = tripleDifference.multiply(BigDecimal.valueOf(nights));
                    totalAccommodationFee = totalAccommodationFee.add(tripleRoomFee);
                    log.info("基础房间{}三人房差价费用: {}", i + 1, tripleRoomFee);
                }
            }
            
            log.info("房间计算: 总人数={}, 理论房间数={}, 向下取整={}, 向上取整={}, 实际房间数={}", 
                    totalPeople, totalRooms, includedRoomsFloor, includedRoomsCeil, roomCount);
            
            // 单房差计算
                if (roomCount == includedRoomsCeil && totalRooms > includedRoomsFloor) {
                    BigDecimal singleRoomSupplement = hotelPriceService.getDailySingleRoomSupplementByLevel(hotelLevel);
                    BigDecimal singleSupplementCost = singleRoomSupplement.multiply(BigDecimal.valueOf(nights));
                totalAccommodationFee = totalAccommodationFee.add(singleSupplementCost);
                log.info("单房差费用: {} (每晚{}元 × {}晚)", singleSupplementCost, singleRoomSupplement, nights);
            }
            
            // 额外房间费用计算
            if (roomCount > includedRoomsCeil) {
                // 先计算单房差（如果需要的话）
                if (totalRooms > includedRoomsFloor) {
                    BigDecimal singleRoomSupplement = hotelPriceService.getDailySingleRoomSupplementByLevel(hotelLevel);
                    BigDecimal singleSupplementCost = singleRoomSupplement.multiply(BigDecimal.valueOf(nights));
                    totalAccommodationFee = totalAccommodationFee.add(singleSupplementCost);
                    log.info("单房差费用: {} (每晚{}元 × {}晚)", singleSupplementCost, singleRoomSupplement, nights);
                }
                
                // 再计算额外房间费用
                for (int i = includedRoomsCeil; i < roomCount; i++) {
                    String roomType = i < roomTypeList.size() ? roomTypeList.get(i) : "大床房";
                    BigDecimal roomPrice = getRoomPriceByType(hotelLevel, roomType);
                    BigDecimal extraRoomCost = roomPrice.multiply(BigDecimal.valueOf(nights));
                    totalAccommodationFee = totalAccommodationFee.add(extraRoomCost);
                    log.info("额外房间{}费用（房型: {}）: {} (每晚{}元 × {}晚)", i + 1, roomType, extraRoomCost, roomPrice, nights);
                }
                }
            } catch (Exception e) {
            log.error("计算住宿相关费用失败: {}", e.getMessage(), e);
        }
        
        return new AccommodationPriceInfo(totalAccommodationFee);
    }
    
    /**
     * 基础价格信息内部类
     */
    private static class PriceBaseInfo {
        final BigDecimal baseUnitPrice;
        final int nights;
        
        PriceBaseInfo(BigDecimal baseUnitPrice, int nights) {
            this.baseUnitPrice = baseUnitPrice;
            this.nights = nights;
        }
    }
    
    /**
     * 人员价格信息内部类
     */
    private static class PersonPriceInfo {
        final BigDecimal totalPersonPrice;
        final List<Map<String, Object>> childrenDetails;
        
        PersonPriceInfo(BigDecimal totalPersonPrice, List<Map<String, Object>> childrenDetails) {
            this.totalPersonPrice = totalPersonPrice;
            this.childrenDetails = childrenDetails;
        }
    }
    
    /**
     * 住宿价格信息内部类
     */
    private static class AccommodationPriceInfo {
        final BigDecimal totalAccommodationFee;
        
        AccommodationPriceInfo(BigDecimal totalAccommodationFee) {
            this.totalAccommodationFee = totalAccommodationFee;
        }
    }

    /**
     * 删除订单（只能删除已取消的订单）
     * 
     * @param bookingId 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean delete(Integer bookingId) {
        log.info("删除订单, 订单ID: {}", bookingId);
        
        try {
            TourBooking tourBooking = tourBookingMapper.getById(bookingId);
            if (tourBooking == null) {
                log.error("订单不存在: {}", bookingId);
                return false;
            }
            
            // 只有已取消的订单可以删除
            String status = tourBooking.getStatus();
            if (!"cancelled".equals(status)) {
                log.error("只能删除已取消的订单，当前状态: {}", status);
                return false;
            }
            
            // 1. 先删除相关的乘客信息
            try {
                List<PassengerVO> passengers = passengerService.getByBookingId(bookingId);
                for (PassengerVO passenger : passengers) {
                    passengerMapper.deleteById(passenger.getPassengerId());
                }
                log.info("已删除订单{}的所有乘客信息，共{}个乘客", bookingId, passengers.size());
            } catch (Exception e) {
                log.warn("删除订单{}的乘客信息时出错: {}", bookingId, e.getMessage());
                // 不抛出异常，继续删除订单主体
            }
            
            // 2. 删除相关的排团信息（如果存在）
            try {
                tourScheduleOrderMapper.deleteByBookingId(bookingId);
                log.info("已删除订单{}的排团信息", bookingId);
            } catch (Exception e) {
                log.warn("删除订单{}的排团信息时出错: {}", bookingId, e.getMessage());
                // 不抛出异常，继续删除订单主体
            }
            
            // 3. 最后删除订单主体
            tourBookingMapper.deleteById(bookingId);
            
            // 🔔 发送订单删除通知
            try {
                sendDetailedOrderNotification(tourBooking, "delete", "管理员删除已取消订单");
            } catch (Exception e) {
                log.error("❌ 发送订单删除通知失败: {}", e.getMessage(), e);
            }
            
            log.info("订单删除完成, 订单ID: {}", bookingId);
            return true;
        } catch (Exception e) {
            log.error("删除订单出错, 订单ID: {}, 错误: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("删除订单出错: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Boolean confirmOrderByAdmin(Integer bookingId, Double adjustedPrice, String adjustmentReason) {
        log.info("🔒 管理员确认订单开始，订单ID: {}, 调整价格: {}, 调整原因: {}", bookingId, adjustedPrice, adjustmentReason);
        
        try {
            // 🔒 权限检查：只有管理员和操作员才能确认订单
            String currentUserType = BaseContext.getCurrentUserType();
            Long currentUserId = BaseContext.getCurrentId();
            
            if (!"admin".equals(currentUserType) && !"operator".equals(currentUserType)) {
                log.error("❌ 权限不足：只有管理员和操作员才能确认订单，当前用户类型: {}, 用户ID: {}", currentUserType, currentUserId);
                throw new BusinessException("权限不足，只有管理员和操作员才能确认订单");
            }
            
            // 🔒 价格合理性预检查
            if (adjustedPrice != null) {
                if (adjustedPrice < 0) {
                    throw new BusinessException("调整后的价格不能为负数");
                }
                if (adjustedPrice > 50000) { // 设置一个合理的上限
                    throw new BusinessException("调整后的价格超出合理范围，请联系系统管理员");
                }
            }
            
            log.info("✅ 权限验证通过，管理员ID: {}, 开始处理订单确认", currentUserId);
            // 1. 获取订单信息
            TourBooking tourBooking = tourBookingMapper.getById(bookingId);
            if (tourBooking == null) {
                log.error("订单不存在，订单ID: {}", bookingId);
                throw new BusinessException("订单不存在");
            }
            
            // 2. 检查订单状态，只有pending状态的订单才能确认
            if (!"pending".equals(tourBooking.getStatus())) {
                log.error("订单状态不正确，只能确认待处理状态的订单，当前状态: {}", tourBooking.getStatus());
                throw new BusinessException("订单状态不正确，只能确认待处理状态的订单");
            }
            
            // 3. 更新订单状态为confirmed
            tourBooking.setStatus("confirmed");
            
            // 4. 如果有价格调整，更新价格并记录调整原因
            if (adjustedPrice != null && adjustedPrice > 0) {
                BigDecimal oldPrice = tourBooking.getTotalPrice();
                BigDecimal newPrice = BigDecimal.valueOf(adjustedPrice);
                
                log.info("价格调整：原价格: {}, 新价格: {}, 调整原因: {}", oldPrice, newPrice, adjustmentReason);
                
                tourBooking.setTotalPrice(newPrice);
                
                // 如果有调整原因，添加到特殊要求中
                if (adjustmentReason != null && !adjustmentReason.trim().isEmpty()) {
                    String existingRequests = tourBooking.getSpecialRequests();
                    String priceAdjustmentNote = String.format("[价格调整] %s (原价: $%.2f → 调整后: $%.2f)", 
                        adjustmentReason, oldPrice.doubleValue(), newPrice.doubleValue());
                    
                    if (existingRequests != null && !existingRequests.trim().isEmpty()) {
                        tourBooking.setSpecialRequests(existingRequests + "\n" + priceAdjustmentNote);
                    } else {
                        tourBooking.setSpecialRequests(priceAdjustmentNote);
                    }
                }
            }
            
            // 5. 更新订单更新时间
            tourBooking.setUpdatedAt(LocalDateTime.now());
            
            // 6. 🔒 使用安全的更新方法（包含业务逻辑验证）
            String operatorInfo = String.format("管理员确认订单 - 操作员ID: %s, 操作时间: %s", 
                BaseContext.getCurrentId(), LocalDateTime.now());
            
            int updatedRows = tourBookingMapper.confirmOrderByAdmin(
                bookingId,
                "confirmed",
                adjustedPrice,
                tourBooking.getSpecialRequests(),
                operatorInfo
            );
            
            // 检查更新是否成功（如果返回0表示不满足安全条件）
            if (updatedRows == 0) {
                log.error("❌ 订单确认失败，可能原因：订单状态不是pending，或价格不合理，订单ID: {}", bookingId);
                throw new BusinessException("订单确认失败，请检查订单状态或价格设置");
            }
            
            // 7. 不在确认阶段发送确认单邮件；仅在付款成功后发送（见 payBooking -> sendEmailsAfterPaymentAsync）
            log.info("📧 已按策略禁用‘确认时发邮件’，将于付款成功后自动发送确认单与发票: 订单ID={}", bookingId);

            // 同步给代理端一条订单变化通知（包含价格调整信息）
            try {
                String title = adjustedPrice != null ? "订单已确认并调整价格" : "订单已确认";
                String detail = adjustedPrice != null
                        ? String.format("订单已确认，价格调整为 $%.2f。%s", adjustedPrice,
                                (adjustmentReason != null ? ("原因: " + adjustmentReason) : ""))
                        : "订单已确认，可进行支付";
                notificationService.createAgentOrderChangeNotification(
                        tourBooking.getAgentId() != null ? tourBooking.getAgentId().longValue() : null,
                        tourBooking.getOperatorId(),
                        tourBooking.getBookingId().longValue(),
                        tourBooking.getOrderNumber(),
                        title,
                        detail
                );
            } catch (Exception e) {
                log.warn("⚠️ 创建代理通知失败（确认阶段）: {}", e.getMessage());
            }
            
            log.info("✅ 订单确认完成，订单号: {}, 确认时间: {}", tourBooking.getOrderNumber(), LocalDateTime.now());
            
            // 8. 发送系统通知
            try {
                String notificationMsg = adjustedPrice != null ? 
                    String.format("订单已确认，价格已调整为 $%.2f", adjustedPrice) : 
                    "订单已确认，可以进行支付";
                sendDetailedOrderNotification(tourBooking, "confirmed", notificationMsg);
            } catch (Exception e) {
                log.error("❌ 发送订单确认通知失败: {}", e.getMessage());
                // 不影响订单确认流程
            }
            
            log.info("✅ 管理员确认订单成功，订单ID: {}", bookingId);
            return true;
            
        } catch (BusinessException e) {
            log.error("❌ 管理员确认订单失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ 管理员确认订单出错，订单ID: {}, 错误: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("订单确认处理出错: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取客户邮箱地址
     */
    private String getCustomerEmail(TourBooking tourBooking) {
        // 如果是代理商订单，获取代理商邮箱
        if (tourBooking.getAgentId() != null) {
            // 可以从agent表获取邮箱，这里暂时使用联系人信息
            return tourBooking.getContactPerson() + "@example.com"; // 临时处理
        }
        
        // 如果是普通用户订单，从用户表获取邮箱
        if (tourBooking.getUserId() != null) {
            // 从用户表获取邮箱，这里暂时使用联系人信息
            return tourBooking.getContactPerson() + "@example.com"; // 临时处理
        }
        
        return "customer@example.com"; // 默认邮箱
    }

    /**
     * 用户隐藏订单（软删除）
     * 
     * @param bookingId 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean hideOrder(Integer bookingId, Integer userId) {
        log.info("用户隐藏订单: bookingId={}, userId={}", bookingId, userId);
        
        try {
            // 检查订单是否存在和权限
            TourBooking tourBooking = tourBookingMapper.getById(bookingId);
            if (tourBooking == null) {
                log.error("订单不存在: {}", bookingId);
                return false;
            }
            
            // 权限验证：只能隐藏自己的订单
            if (!userId.equals(tourBooking.getUserId()) && !userId.equals(tourBooking.getAgentId())) {
                log.error("无权限隐藏此订单: bookingId={}, userId={}, orderUserId={}, orderAgentId={}", 
                         bookingId, userId, tourBooking.getUserId(), tourBooking.getAgentId());
                return false;
            }
            
            // 只有已取消的订单才能隐藏
            if (!"cancelled".equals(tourBooking.getStatus())) {
                log.error("只能隐藏已取消的订单: bookingId={}, status={}", bookingId, tourBooking.getStatus());
                return false;
            }
            
            // 执行隐藏操作
            int result = tourBookingMapper.hideOrderByUser(bookingId, userId);
            if (result > 0) {
                log.info("✅ 订单隐藏成功: bookingId={}", bookingId);
                return true;
            } else {
                log.error("❌ 订单隐藏失败: bookingId={}", bookingId);
                return false;
            }
        } catch (Exception e) {
            log.error("隐藏订单出错: bookingId={}, userId={}, 错误: {}", bookingId, userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 用户恢复已隐藏的订单
     * 
     * @param bookingId 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public Boolean restoreOrder(Integer bookingId, Integer userId) {
        log.info("用户恢复隐藏订单: bookingId={}, userId={}", bookingId, userId);
        
        try {
            // 执行恢复操作
            int result = tourBookingMapper.restoreOrderByUser(bookingId, userId);
            if (result > 0) {
                log.info("✅ 订单恢复成功: bookingId={}", bookingId);
                return true;
            } else {
                log.error("❌ 订单恢复失败（可能订单不存在或无权限）: bookingId={}", bookingId);
                return false;
            }
        } catch (Exception e) {
            log.error("恢复订单出错: bookingId={}, userId={}, 错误: {}", bookingId, userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 处理降价：自动退款 + 通知
     * 
     * @param booking 当前订单信息
     * @param newPrice 新价格
     * @param refundAmount 退款金额（绝对值）
     * @param reason 修改原因
     */
    @Transactional
    private void processPriceDecrease(TourBooking booking, BigDecimal newPrice, BigDecimal refundAmount, String reason) {
        log.info("🔻 处理降价：订单ID={}, 原价={}, 新价={}, 退款={}", 
                booking.getBookingId(), booking.getTotalPrice(), newPrice, refundAmount);

        try {
            // 1. 更新订单价格
            booking.setTotalPrice(newPrice);
            tourBookingMapper.updateTotalPrice(booking.getBookingId(), newPrice);

            // 2. 退款到代理商信用账户 - 获取余额信息
            BigDecimal balanceBefore = null;
            BigDecimal balanceAfter = null;
            
            if (booking.getAgentId() != null) {
                // 获取退款前余额
                AgentCreditVO creditInfoBefore = agentCreditService.getCreditInfo(Long.valueOf(booking.getAgentId()));
                balanceBefore = creditInfoBefore != null ? creditInfoBefore.getDepositBalance() : BigDecimal.ZERO;
                
                boolean refundResult = agentCreditService.addCredit(
                        Long.valueOf(booking.getAgentId()), 
                        refundAmount, 
                        String.format("订单%s降价退款：%s", booking.getOrderNumber(), reason)
                );
                
                if (!refundResult) {
                    log.error("❌ 退款失败：代理商ID={}, 退款金额={}", booking.getAgentId(), refundAmount);
                    throw new BusinessException("退款处理失败");
                }
                
                // 获取退款后余额
                AgentCreditVO creditInfoAfter = agentCreditService.getCreditInfo(Long.valueOf(booking.getAgentId()));
                balanceAfter = creditInfoAfter != null ? creditInfoAfter.getDepositBalance() : BigDecimal.ZERO;
            }

            // 3. 记录审计日志 - 包含完整操作者和余额信息
            Integer currentAdminId = getCurrentAdminId();
            String currentUsername = BaseContext.getCurrentUsername();
            String operatorInfo = String.format("管理员: %s (ID: %s)", 
                    currentUsername != null ? currentUsername : "未知", currentAdminId);
            
            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .requestId(UUID.randomUUID().toString())
                    .action("price_decrease")
                    .bookingId(booking.getBookingId())
                    .orderNumber(booking.getOrderNumber())
                    .agentId(booking.getAgentId() != null ? Long.valueOf(booking.getAgentId()) : null)
                    .operatorId(currentAdminId != null ? Long.valueOf(currentAdminId) : null)
                    .operatorType("admin")
                    .operatorName(currentUsername != null ? currentUsername : "管理员")
                    .amount(refundAmount.negate()) // 负数表示退款
                    .balanceBefore(balanceBefore)
                    .balanceAfter(balanceAfter)
                    .note(String.format("订单降价自动退款：%s [操作人: %s]", reason, operatorInfo))
                    .ip(getClientIP())
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentAuditLogMapper.insert(auditLog);

            // 4. 发送通知给代理商
            String notificationMessage = String.format(
                    "您的订单 %s 价格已调整，降价 ¥%.2f，已自动退款到信用账户。原因：%s",
                    booking.getOrderNumber(), refundAmount, reason
            );
            notificationService.createAgentOrderChangeNotification(
                    booking.getAgentId() != null ? booking.getAgentId().longValue() : null,
                    booking.getOperatorId(),
                    booking.getBookingId().longValue(),
                    booking.getOrderNumber(),
                    "订单降价通知",
                    notificationMessage
            );

            log.info("✅ 降价处理完成：订单ID={}, 退款金额={}", booking.getBookingId(), refundAmount);

        } catch (Exception e) {
            log.error("❌ 降价处理失败：订单ID={}, 错误: {}", booking.getBookingId(), e.getMessage(), e);
            throw new BusinessException("降价处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理涨价：创建确认请求 + 通知
     * 
     * @param booking 当前订单信息
     * @param newPrice 新价格
     * @param increaseAmount 涨价金额
     * @param reason 修改原因
     */
    @Transactional
    private void processPriceIncrease(TourBooking booking, BigDecimal newPrice, BigDecimal increaseAmount, String reason) {
        log.info("🔺 处理涨价：订单ID={}, 原价={}, 新价={}, 涨价={}", 
                booking.getBookingId(), booking.getTotalPrice(), newPrice, increaseAmount);

        try {
            // 1. 创建价格修改请求
            PriceModificationRequest request = PriceModificationRequest.builder()
                    .bookingId(booking.getBookingId())
                    .originalPrice(booking.getTotalPrice())
                    .newPrice(newPrice)
                    .priceDifference(increaseAmount)
                    .modificationType("increase")
                    .status("pending")
                    .reason(reason)
                    .createdByAdmin(getCurrentAdminId())
                    .createdAt(LocalDateTime.now())
                    .build();
            priceModificationRequestMapper.insert(request);

            // 2. 涨价不立即记录audit日志，等用户确认后再记录
            log.info("💡 涨价请求已创建，audit记录将在用户确认后生成");

            // 3. 发送通知给代理商
            String notificationMessage = String.format(
                    "您的订单 %s 价格需要调整，涨价 ¥%.2f，请在订单详情中确认是否同意补款。原因：%s",
                    booking.getOrderNumber(), increaseAmount, reason
            );
            notificationService.createAgentOrderChangeNotification(
                    booking.getAgentId() != null ? booking.getAgentId().longValue() : null,
                    booking.getOperatorId(),
                    booking.getBookingId().longValue(),
                    booking.getOrderNumber(),
                    "订单涨价确认",
                    notificationMessage
            );

            log.info("✅ 涨价请求创建完成：订单ID={}, 涨价金额={}", booking.getBookingId(), increaseAmount);

        } catch (Exception e) {
            log.error("❌ 涨价请求创建失败：订单ID={}, 错误: {}", booking.getBookingId(), e.getMessage(), e);
            throw new BusinessException("涨价请求创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前管理员ID
     */
    private Integer getCurrentAdminId() {
        try {
            Long currentId = BaseContext.getCurrentId();
            return currentId != null ? currentId.intValue() : null;
        } catch (Exception e) {
            log.warn("获取管理员ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIP() {
        try {
            // 这里可以从请求上下文获取IP，暂时返回默认值
            return "127.0.0.1";
        } catch (Exception e) {
            return "unknown";
        }
    }
} 