package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.GroupTourDTO;
import com.sky.dto.OrderPageQueryDTO;
import com.sky.dto.OrderUpdateDTO;
import com.sky.dto.PassengerDTO;
import com.sky.dto.PaymentDTO;
import com.sky.dto.TourScheduleBatchSaveDTO;
import com.sky.dto.TourScheduleOrderDTO;
import com.sky.entity.BookingPassengerRelation;
import com.sky.entity.Passenger;
import com.sky.entity.TourBooking;
import com.sky.exception.BusinessException;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.PassengerMapper;
import com.sky.mapper.TourBookingMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.PassengerService;
import com.sky.service.PaymentService;
import com.sky.service.UserCreditService;
import com.sky.service.GroupTourService;
import com.sky.service.DayTourService;
import com.sky.service.NotificationService;
import com.sky.service.TourBookingService;
import com.sky.service.TourScheduleOrderService;
import com.sky.vo.OrderVO;
import com.sky.vo.PageResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sky.utils.SpringUtils;
import com.sky.mapper.TourScheduleOrderMapper;
import com.sky.entity.TourScheduleOrder;
import com.sky.vo.PassengerVO;
import com.sky.mapper.DayTourMapper;
import com.sky.mapper.GroupTourMapper;
import com.sky.mapper.TourItineraryMapper;
import com.sky.entity.DayTour;
import com.sky.entity.GroupTour;

/**
 * 订单服务实现类
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private PassengerMapper passengerMapper;

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private TourBookingMapper tourBookingMapper;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private GroupTourService groupTourService;
    
    @Autowired
    private DayTourService dayTourService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TourScheduleOrderMapper tourScheduleOrderMapper;
    
    @Autowired
    private DayTourMapper dayTourMapper;
    
    @Autowired
    private GroupTourMapper groupTourMapper;
    
    @Autowired
    private TourItineraryMapper tourItineraryMapper;

    @Autowired
    private TourScheduleOrderService tourScheduleOrderService;

    @Autowired
    private TourBookingService tourBookingService; // 新注入

    /**
     * 分页查询订单
     * @param orderPageQueryDTO 查询条件
     * @return 订单分页结果
     */
    @Override
    public PageResultVO<OrderVO> pageQuery(OrderPageQueryDTO orderPageQueryDTO) {
        // 设置分页参数
        PageHelper.startPage(orderPageQueryDTO.getPage(), orderPageQueryDTO.getPageSize());
        // 执行查询
        Page<OrderVO> page = orderMapper.pageQuery(orderPageQueryDTO);
        
        // 为每个订单添加行程详情
        for (OrderVO orderVO : page.getResult()) {
            if ("group_tour".equals(orderVO.getTourType()) && orderVO.getTourId() != null) {
                try {
                    // 获取行程详情并转换为JSON字符串
                    List<Map<String, Object>> itineraryDetails = fetchTourItineraryDetails(orderVO.getTourId(), orderVO.getTourType(), orderVO.getBookingId());
                    if (itineraryDetails != null && !itineraryDetails.isEmpty()) {
                        // 将行程详情设置到订单对象中
                        orderVO.setItineraryDetails(objectMapper.writeValueAsString(itineraryDetails));
                    }
                } catch (Exception e) {
                    log.error("获取订单行程详情失败: bookingId={}, tourId={}, error={}", 
                             orderVO.getBookingId(), orderVO.getTourId(), e.getMessage(), e);
                }
            }
        }
        
        // 将Page对象转换为PageResultVO对象
        return PageResultVO.<OrderVO>builder()
                .total(page.getTotal())
                .records(page.getResult())
                .build();
    }

    /**
     * 根据ID获取订单详情
     * @param bookingId 订单ID
     * @return 订单详情
     */
    @Override
    public OrderVO getById(Integer bookingId) {
        if (bookingId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        
        log.info("开始获取订单详情, 订单ID: {}", bookingId);
        
        OrderVO orderVO = orderMapper.getById(bookingId);
        
        if (orderVO != null) {
            log.info("成功获取订单: {}, 订单号: {}, 成人数: {}, 儿童数: {}", 
                    bookingId, orderVO.getOrderNumber(), 
                    orderVO.getAdultCount(), orderVO.getChildCount());
            
            // 检查乘客信息
            if (orderVO.getPassengers() != null) {
                log.info("订单关联的乘客数量: {}", orderVO.getPassengers().size());
                
                // 详细记录每个乘客信息
                orderVO.getPassengers().forEach(passenger -> {
                    log.info("乘客信息 - ID:{}, 姓名:{}", 
                            passenger.getPassengerId(), 
                            passenger.getFullName());
                    
                    // 记录乘客对象的所有字段，帮助诊断
                    log.info("乘客全部字段: {}", passenger);
                });
            } else {
                log.warn("订单 {} 没有关联的乘客信息", bookingId);
            }
            
            // 🔥 新增：获取用户在tour_schedule_order表中的具体行程选择
            try {
                List<TourScheduleOrder> userItinerary = tourScheduleOrderMapper.getByBookingId(orderVO.getBookingId());
                if (userItinerary != null && !userItinerary.isEmpty()) {
                    log.info("成功获取订单 {} 的用户行程选择，共 {} 天", orderVO.getBookingId(), userItinerary.size());
                    
                    // 转换为前端需要的格式
                    List<Map<String, Object>> userItineraryDetails = new ArrayList<>();
                    for (TourScheduleOrder scheduleOrder : userItinerary) {
                        Map<String, Object> dayItinerary = new HashMap<>();
                        dayItinerary.put("day_number", scheduleOrder.getDayNumber());
                        dayItinerary.put("tour_date", scheduleOrder.getTourDate());
                        dayItinerary.put("title", scheduleOrder.getTitle());
                        dayItinerary.put("description", scheduleOrder.getDescription());
                        dayItinerary.put("tour_name", scheduleOrder.getTourName());
                        dayItinerary.put("tour_location", scheduleOrder.getTourLocation());
                        // 注意：selectedOptionalTours字段可能需要通过其他方式获取
                        dayItinerary.put("pickup_location", scheduleOrder.getPickupLocation());
                        dayItinerary.put("dropoff_location", scheduleOrder.getDropoffLocation());
                        dayItinerary.put("special_requests", scheduleOrder.getSpecialRequests());
                        dayItinerary.put("luggage_count", scheduleOrder.getLuggageCount());
                        dayItinerary.put("hotel_level", scheduleOrder.getHotelLevel());
                        dayItinerary.put("room_type", scheduleOrder.getRoomType());
                        dayItinerary.put("hotel_room_count", scheduleOrder.getHotelRoomCount());
                        dayItinerary.put("hotel_check_in_date", scheduleOrder.getHotelCheckInDate());
                        dayItinerary.put("hotel_check_out_date", scheduleOrder.getHotelCheckOutDate());
                        dayItinerary.put("room_details", scheduleOrder.getRoomDetails());
                        
                        // 航班信息
                        dayItinerary.put("flight_number", scheduleOrder.getFlightNumber());
                        dayItinerary.put("arrival_departure_time", scheduleOrder.getArrivalDepartureTime());
                        dayItinerary.put("arrival_landing_time", scheduleOrder.getArrivalLandingTime());
                        dayItinerary.put("return_flight_number", scheduleOrder.getReturnFlightNumber());
                        dayItinerary.put("departure_departure_time", scheduleOrder.getDepartureDepartureTime());
                        dayItinerary.put("departure_landing_time", scheduleOrder.getDepartureLandingTime());
                        
                        userItineraryDetails.add(dayItinerary);
                    }
                    
                    // 将用户的具体行程选择设置到订单对象中
                    orderVO.setItineraryDetails(objectMapper.writeValueAsString(userItineraryDetails));
                    log.info("订单 {} 的用户行程详情已设置", orderVO.getBookingId());
                } else {
                    log.info("订单 {} 在tour_schedule_order表中没有找到行程记录，尝试获取通用行程模板", orderVO.getBookingId());
                    
                    // 如果tour_schedule_order表中没有记录，则获取通用行程模板（兜底逻辑）
                    if (("group_tour".equals(orderVO.getTourType()) || "day_tour".equals(orderVO.getTourType())) && orderVO.getTourId() != null) {
                    List<Map<String, Object>> itineraryDetails = fetchTourItineraryDetails(orderVO.getTourId(), orderVO.getTourType(), orderVO.getBookingId());
                    if (itineraryDetails != null && !itineraryDetails.isEmpty()) {
                        orderVO.setItineraryDetails(objectMapper.writeValueAsString(itineraryDetails));
                            log.info("已设置订单 {} 的通用行程模板", orderVO.getBookingId());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("获取订单行程详情失败: bookingId={}, error={}", 
                         orderVO.getBookingId(), e.getMessage(), e);
            }
        } else {
            log.error("未找到订单: {}", bookingId);
        }
        
        return orderVO;
    }

    /**
     * 获取旅游行程详情
     * @param tourId 旅游产品ID
     * @param tourType 旅游类型 (group_tour或day_tour)
     * @param bookingId 订单ID
     * @return 行程详情列表
     */
    private List<Map<String, Object>> fetchTourItineraryDetails(Integer tourId, String tourType, Integer bookingId) {
        try {
            if ("group_tour".equals(tourType)) {
                // 获取跟团游行程
                List<Map<String, Object>> itinerary = groupTourService.getGroupTourItinerary(tourId);
                
                if (itinerary != null && !itinerary.isEmpty()) {
                    log.info("成功获取跟团游行程数据: tourId={}, days={}", tourId, itinerary.size());
                    return itinerary;
                }
                
                // 如果无法通过行程表获取，尝试获取跟团游关联的一日游
                List<Map<String, Object>> dayTours = groupTourService.getGroupTourDayTours(tourId);
                if (dayTours != null && !dayTours.isEmpty()) {
                    log.info("成功获取跟团游关联的一日游: tourId={}, dayTours={}", tourId, dayTours.size());
                    
                    // 转换为行程格式
                    List<Map<String, Object>> formattedItinerary = new ArrayList<>();
                    for (Map<String, Object> dayTour : dayTours) {
                        Map<String, Object> day = new HashMap<>();
                        day.put("day_number", dayTour.get("day_number"));
                        day.put("title", dayTour.get("day_tour_name"));
                        day.put("location", dayTour.get("location"));
                        
                        // 获取一日游的详细信息
                        Integer dayTourId = (Integer) dayTour.get("day_tour_id");
                        if (dayTourId != null) {
                            List<Map<String, Object>> dayTourItinerary = dayTourService.getDayTourItinerary(dayTourId);
                            if (dayTourItinerary != null && !dayTourItinerary.isEmpty()) {
                                // 合并一日游描述
                                StringBuilder description = new StringBuilder();
                                for (Map<String, Object> item : dayTourItinerary) {
                                    if (item.get("description") != null) {
                                        if (description.length() > 0) {
                                            description.append("\n");
                                        }
                                        description.append(item.get("description"));
                                    }
                                }
                                day.put("description", description.toString());
                            }
                        }
                        
                        formattedItinerary.add(day);
                    }
                    
                    return formattedItinerary;
                }
            } else if ("day_tour".equals(tourType)) {
                // 获取一日游行程
                List<Map<String, Object>> itinerary = dayTourService.getDayTourItinerary(tourId);
                if (itinerary != null && !itinerary.isEmpty()) {
                    log.info("成功获取一日游行程数据: tourId={}, activities={}", tourId, itinerary.size());
                    return itinerary;
                }
            }
            
            log.warn("未找到旅游行程数据: tourId={}, tourType={}", tourId, tourType);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取旅游行程详情失败: tourId={}, tourType={}, error={}", 
                     tourId, tourType, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据订单号查询订单
     * @param orderNumber 订单号
     * @return 订单信息
     */
    @Override
    public TourBooking getByOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.isEmpty()) {
            throw new BusinessException("订单号不能为空");
        }
        
        // 根据订单号查询订单信息
        TourBooking tourBooking = tourBookingMapper.getByOrderNumber(orderNumber);
        
        return tourBooking;
    }

    /**
     * 创建订单
     * @param tourBooking 订单信息
     * @return 创建的订单ID
     */
    @Override
    @Transactional
    public Integer createOrder(TourBooking tourBooking) {
        if (tourBooking == null) {
            throw new BusinessException("订单信息不能为空");
        }
        
        // 设置初始状态和时间
        tourBooking.setStatus("pending");
        tourBooking.setPaymentStatus("unpaid");
        tourBooking.setCreatedAt(LocalDateTime.now());
        tourBooking.setUpdatedAt(LocalDateTime.now());
        
        // 插入订单
        orderMapper.insert(tourBooking);
        
        // ℹ️ 订单创建完成，等待支付后同步到排团表
        log.info("ℹ️ 订单创建完成，订单ID={}，将在支付成功后同步到排团表", tourBooking.getBookingId());
        
        // 🔔 发送新订单通知
        try {
            String customerName = tourBooking.getContactPerson() != null ? 
                                tourBooking.getContactPerson() : "未知客户";
            Double amount = tourBooking.getTotalPrice() != null ? 
                          tourBooking.getTotalPrice().doubleValue() : 0.0;
            
            notificationService.createOrderNotification(
                Long.valueOf(tourBooking.getBookingId()), 
                customerName, 
                amount
            );
            
            log.info("🔔 已发送新订单通知: 订单ID={}, 客户={}, 金额={}", 
                    tourBooking.getBookingId(), customerName, amount);
        } catch (Exception e) {
            log.error("❌ 发送新订单通知失败: {}", e.getMessage(), e);
        }
        
        return tourBooking.getBookingId();
    }

    /**
     * 更新订单信息
     * @param tourBooking 订单信息
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean updateOrder(TourBooking tourBooking) {
        if (tourBooking == null || tourBooking.getBookingId() == null) {
            throw new BusinessException("订单ID不能为空");
        }
        
        // 先检查订单是否存在
        OrderVO existingOrder = orderMapper.getById(tourBooking.getBookingId());
        if (existingOrder == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 设置更新时间
        tourBooking.setUpdatedAt(LocalDateTime.now());
        
        // 执行更新
        int result = orderMapper.update(tourBooking);
        
        // 🔔 发送订单修改通知
        if (result > 0) {
            try {
                String customerName = existingOrder.getContactPerson() != null ? 
                                    existingOrder.getContactPerson() : "未知客户";
                
                notificationService.createOrderModifyNotification(
                    Long.valueOf(tourBooking.getBookingId()), 
                    customerName, 
                    "修改订单信息"
                );
                
                log.info("🔔 已发送订单修改通知: 订单ID={}, 客户={}", tourBooking.getBookingId(), customerName);
            } catch (Exception e) {
                log.error("❌ 发送订单修改通知失败: {}", e.getMessage(), e);
            }
        }
        
        return result > 0;
    }

    /**
     * 确认订单
     * @param bookingId 订单ID
     * @param remark 备注信息
     * @return 是否确认成功
     */
    @Override
    @Transactional
    public boolean confirmOrder(Integer bookingId, String remark) {
        // 验证订单
        OrderVO order = validateOrderForStatusUpdate(bookingId, "pending");
        
        // 创建订单更新对象
        TourBooking updateOrder = new TourBooking();
        updateOrder.setBookingId(bookingId);
        updateOrder.setStatus("confirmed");
        updateOrder.setSpecialRequests(remark); // 使用specialRequests字段存储备注
        updateOrder.setUpdatedAt(LocalDateTime.now());
        
        // 更新订单
        int result = orderMapper.update(updateOrder);
        return result > 0;
    }

    /**
     * 取消订单
     * @param bookingId 订单ID
     * @param remark 取消原因
     * @return 是否取消成功
     */
    @Override
    @Transactional
    public boolean cancelOrder(Integer bookingId, String remark) {
        // 验证订单 - 可以从pending或confirmed状态取消
        OrderVO order = getById(bookingId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (!("pending".equals(order.getStatus()) || "confirmed".equals(order.getStatus()))) {
            throw new BusinessException("只有待确认或已确认的订单可以取消");
        }
        
        // 创建订单更新对象
        TourBooking updateOrder = new TourBooking();
        updateOrder.setBookingId(bookingId);
        updateOrder.setStatus("cancelled");
        updateOrder.setSpecialRequests(remark); // 使用specialRequests字段存储取消原因
        updateOrder.setUpdatedAt(LocalDateTime.now());
        
        // 更新订单
        int result = orderMapper.update(updateOrder);
        
        // 🔔 发送订单取消通知
        if (result > 0) {
            try {
                String customerName = order.getContactPerson() != null ? 
                                    order.getContactPerson() : "未知客户";
                
                notificationService.createOrderModifyNotification(
                    Long.valueOf(bookingId), 
                    customerName, 
                    "取消订单"
                );
                
                log.info("🔔 已发送订单取消通知: 订单ID={}, 客户={}", bookingId, customerName);
            } catch (Exception e) {
                log.error("❌ 发送订单取消通知失败: {}", e.getMessage(), e);
            }
        }
        
        return result > 0;
    }

    /**
     * 完成订单
     * @param bookingId 订单ID
     * @param remark 备注信息
     * @return 是否完成成功
     */
    @Override
    @Transactional
    public boolean completeOrder(Integer bookingId, String remark) {
        // 验证订单 - 只有已确认的订单可以完成
        OrderVO order = validateOrderForStatusUpdate(bookingId, "confirmed");
        
        // 检查支付状态 - 只有已支付的订单才能完成
        if (!"paid".equals(order.getPaymentStatus())) {
            throw new BusinessException("订单尚未完成支付，无法完成订单");
        }
        
        // 创建订单更新对象
        TourBooking updateOrder = new TourBooking();
        updateOrder.setBookingId(bookingId);
        updateOrder.setStatus("completed");
        updateOrder.setSpecialRequests(remark); // 使用specialRequests字段存储备注
        updateOrder.setUpdatedAt(LocalDateTime.now());
        
        // 更新订单
        int result = orderMapper.update(updateOrder);
        
        // 处理推荐返利
        if (result > 0 && order.getUserId() != null) {
            try {
                // 注入用户积分服务
                UserCreditService userCreditService = SpringUtils.getBean(UserCreditService.class);
                if (userCreditService != null) {
                    // 处理订单完成后的推荐积分奖励
                    userCreditService.processReferralReward(
                        bookingId,
                        order.getUserId().longValue(),
                        order.getTotalPrice()
                    );
                    log.info("订单完成后处理推荐奖励成功: bookingId={}, userId={}", bookingId, order.getUserId());
                }
            } catch (Exception e) {
                // 记录异常，但不影响订单完成
                log.error("处理订单推荐奖励时出错: bookingId={}, userId={}, error={}",
                        bookingId, order.getUserId(), e.getMessage(), e);
            }
        }
        
        return result > 0;
    }

    /**
     * 更新订单状态
     * @param bookingId 订单ID
     * @param orderUpdateDTO 订单状态更新信息
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean updateOrderStatus(Integer bookingId, OrderUpdateDTO orderUpdateDTO) {
        if (bookingId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        
        if (orderUpdateDTO == null) {
            throw new BusinessException("订单状态更新信息不能为空");
        }
        
        // 先检查订单是否存在
        OrderVO existingOrder = orderMapper.getById(bookingId);
        if (existingOrder == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 🔍 获取更新前的支付状态（用于支付状态变化检测）
        String originalPaymentStatus = existingOrder.getPaymentStatus();
        log.info("🔍 管理后台订单状态更新前支付状态检查，订单ID: {}, 原始支付状态: {}", bookingId, originalPaymentStatus);
        
        // 创建订单更新对象
        TourBooking updateOrder = new TourBooking();
        updateOrder.setBookingId(bookingId);
        
        // 设置订单状态（如果提供）
        if (StringUtils.hasText(orderUpdateDTO.getStatus())) {
            updateOrder.setStatus(orderUpdateDTO.getStatus());
        }
        
        // 设置支付状态（如果提供）
        if (StringUtils.hasText(orderUpdateDTO.getPaymentStatus())) {
            updateOrder.setPaymentStatus(orderUpdateDTO.getPaymentStatus());
        }
        
        // 设置备注（如果提供）
        if (StringUtils.hasText(orderUpdateDTO.getRemark())) {
            updateOrder.setSpecialRequests(orderUpdateDTO.getRemark());
        }
        
        // 🆕 设置联系人信息（如果提供）
        if (StringUtils.hasText(orderUpdateDTO.getContactPerson())) {
            updateOrder.setContactPerson(orderUpdateDTO.getContactPerson());
        }
        
        if (StringUtils.hasText(orderUpdateDTO.getContactPhone())) {
            updateOrder.setContactPhone(orderUpdateDTO.getContactPhone());
            updateOrder.setPassengerContact(orderUpdateDTO.getContactPhone());
        }
        
        updateOrder.setUpdatedAt(LocalDateTime.now());
        
        // 执行订单更新
        int result = orderMapper.update(updateOrder);
        
        // 🔍 获取更新后的订单状态（检测支付状态变化）
        if (result > 0 && StringUtils.hasText(orderUpdateDTO.getPaymentStatus())) {
            String newPaymentStatus = orderUpdateDTO.getPaymentStatus();
            log.info("🔍 管理后台订单状态更新后支付状态检查，订单ID: {}, 新支付状态: {}", bookingId, newPaymentStatus);
            
            // 🗑️ 检测支付状态变化：如果从已支付变为未支付，删除排团表数据
            if ("paid".equals(originalPaymentStatus) && !"paid".equals(newPaymentStatus)) {
                try {
                    log.warn("⚠️ 管理后台检测到支付状态从已支付变为未支付，开始清理排团表数据，订单ID: {}", bookingId);
                    
                    // 删除排团表中的相关记录
                    tourScheduleOrderMapper.deleteByBookingId(bookingId);
                    log.info("✅ 排团表数据清理完成，订单ID: {}", bookingId);
                    
                    // 记录操作日志
                    log.info("📝 管理后台支付状态变化日志：订单ID={}, 原状态={}, 新状态={}, 已清理排团表数据", 
                            bookingId, originalPaymentStatus, newPaymentStatus);
                            
                } catch (Exception e) {
                    log.error("❌ 管理后台清理排团表数据失败: 订单ID={}, 错误: {}", bookingId, e.getMessage(), e);
                    // 不抛出异常，避免影响订单状态更新
                }
            }
            // 🆕 检测支付状态变化：如果从未支付变为已支付，同步订单到排团表
            else if (!"paid".equals(originalPaymentStatus) && "paid".equals(newPaymentStatus)) {
                try {
                    log.info("🎉 管理后台检测到支付状态从未支付变为已支付，开始同步订单到排团表，订单ID: {}", bookingId);
                    
                    // 同步订单到排团表
                    syncBookingToScheduleTable(bookingId);
                    
                    // 记录操作日志
                    log.info("📝 管理后台支付状态变化日志：订单ID={}, 原状态={}, 新状态={}, 已同步到排团表", 
                            bookingId, originalPaymentStatus, newPaymentStatus);
                            
                } catch (Exception e) {
                    log.error("❌ 管理后台同步订单到排团表失败: 订单ID={}, 错误: {}", bookingId, e.getMessage(), e);
                    // 不抛出异常，避免影响订单状态更新
                }
            }
        }
        
        // 🔔 发送订单状态修改通知
        if (result > 0) {
            try {
                String customerName = existingOrder.getContactPerson() != null ? 
                                    existingOrder.getContactPerson() : "未知客户";
                
                String changeType = "修改订单状态";
                if (StringUtils.hasText(orderUpdateDTO.getStatus())) {
                    changeType = "修改订单状态为: " + orderUpdateDTO.getStatus();
                }
                if (StringUtils.hasText(orderUpdateDTO.getPaymentStatus())) {
                    changeType += ", 支付状态: " + orderUpdateDTO.getPaymentStatus();
                }
                
                notificationService.createOrderModifyNotification(
                    Long.valueOf(bookingId), 
                    customerName, 
                    changeType
                );
                
                log.info("🔔 已发送订单状态修改通知: 订单ID={}, 客户={}, 修改内容={}", 
                        bookingId, customerName, changeType);
                
                // 🆕 同步通知代理端（主号必收；若有操作员，仅通知对应操作员）
                try {
                    TourBooking tb = tourBookingMapper.getById(bookingId);
                    Long agentId = tb != null && tb.getAgentId() != null ? tb.getAgentId().longValue() : null;
                    Long operatorId = tb != null ? tb.getOperatorId() : null;
                    String orderNumber = existingOrder.getOrderNumber();
                    notificationService.createAgentOrderChangeNotification(
                        agentId,
                        operatorId,
                        Long.valueOf(bookingId),
                        orderNumber,
                        "订单状态更新",
                        changeType
                    );
                    log.info("🔔 已同步通知代理端订单状态变更: bookingId={}, agentId={}, operatorId={}, detail={}",
                            bookingId, agentId, operatorId, changeType);
                } catch (Exception ne) {
                    log.error("❌ 通知代理端订单状态变更失败: bookingId={}, error={}", bookingId, ne.getMessage(), ne);
                }
            } catch (Exception e) {
                log.error("❌ 发送订单状态修改通知失败: {}", e.getMessage(), e);
            }
        }
        
        // ⚠️  修复重复处理乘客信息的问题
        // 订单状态更新接口不应该处理乘客信息，乘客信息应该通过专门的接口处理
        // 如果需要在状态更新时处理乘客信息，应该在前端调用时明确分离这两个操作
        
        // 🔧 移除乘客信息处理逻辑，避免与 /passengers 接口重复
        if (orderUpdateDTO.getPassengers() != null && !orderUpdateDTO.getPassengers().isEmpty()) {
            log.warn("⚠️  订单状态更新接口收到乘客信息，但已禁用乘客处理以避免重复。请使用专门的乘客接口：PUT /admin/orders/{}/passengers", bookingId);
            log.warn("⚠️  收到的乘客数量: {}，已忽略处理", orderUpdateDTO.getPassengers().size());
        }
        
        // 🆕 同步联系人信息到排团表（如果订单已付款且联系人信息有更新）
        if (result > 0) {
            try {
                // 检查是否有联系人信息更新
                boolean contactInfoUpdated = StringUtils.hasText(orderUpdateDTO.getContactPerson()) || 
                                            StringUtils.hasText(orderUpdateDTO.getContactPhone());
                
                if (contactInfoUpdated) {
                    // 获取当前订单信息检查支付状态
                    OrderVO currentOrder = orderMapper.getById(bookingId);
                    if (currentOrder != null && "paid".equals(currentOrder.getPaymentStatus())) {
                        log.info("🔄 管理端联系人信息更新，开始同步到排团表，订单ID: {}", bookingId);
                        
                        // 同步联系人信息到排团表
                        String newContactPerson = StringUtils.hasText(orderUpdateDTO.getContactPerson()) ? 
                                                orderUpdateDTO.getContactPerson() : currentOrder.getContactPerson();
                        String newContactPhone = StringUtils.hasText(orderUpdateDTO.getContactPhone()) ? 
                                               orderUpdateDTO.getContactPhone() : currentOrder.getContactPhone();
                        
                        // 使用现有的同步方法
                        tourScheduleOrderMapper.updateContactPersonByBookingId(bookingId, newContactPerson);
                        tourScheduleOrderMapper.updateContactPhoneByBookingId(bookingId, newContactPhone);
                        
                        log.info("✅ 管理端联系人信息已同步到排团表: 订单ID={}, 联系人=\"{}\", 电话=\"{}\"", 
                                bookingId, newContactPerson, newContactPhone);
                    } else {
                        log.info("ℹ️ 订单未付款，跳过排团表联系人信息同步，订单ID: {}, 支付状态: {}", 
                                bookingId, currentOrder != null ? currentOrder.getPaymentStatus() : "未知");
                    }
                }
            } catch (Exception e) {
                log.error("❌ 管理端同步联系人信息到排团表失败: 订单ID={}, 错误: {}", bookingId, e.getMessage(), e);
                // 不抛出异常，避免影响订单更新
            }
        }
        
        return result > 0;
    }

    /**
     * 更新支付状态
     * @param bookingId 订单ID
     * @param paymentStatus 支付状态
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean updatePaymentStatus(Integer bookingId, String paymentStatus) {
        if (bookingId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        
        if (!StringUtils.hasText(paymentStatus)) {
            throw new BusinessException("支付状态不能为空");
        }
        
        // 先检查订单是否存在
        OrderVO existingOrder = orderMapper.getById(bookingId);
        if (existingOrder == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 执行更新
        int result = orderMapper.updatePaymentStatus(bookingId, paymentStatus);
        return result > 0;
    }
    
    /**
     * 验证订单状态更新的合法性
     * @param bookingId 订单ID
     * @param expectedStatus 期望的当前状态
     * @return 订单信息
     */
    private OrderVO validateOrderForStatusUpdate(Integer bookingId, String expectedStatus) {
        if (bookingId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        
        // 获取订单信息
        OrderVO order = orderMapper.getById(bookingId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证订单状态
        if (expectedStatus != null && !expectedStatus.equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确，无法执行此操作");
        }
        
        return order;
    }

    /**
     * 支付订单
     * @param bookingId 订单ID
     * @param paymentDTO 支付信息
     * @return 支付结果
     */
    @Override
    public Boolean payOrder(Integer bookingId, PaymentDTO paymentDTO) {
        if (bookingId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        
        if (paymentDTO == null) {
            throw new BusinessException("支付信息不能为空");
        }
        
        // 查询订单
        TourBooking tourBooking = tourBookingMapper.getById(bookingId);
        
        if (tourBooking == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 检查订单状态，只有未支付的订单可以支付
        if (!"unpaid".equals(tourBooking.getPaymentStatus())) {
            throw new BusinessException("订单已支付，无需重复支付");
        }
        
        // 处理支付 - 根据支付方式调用不同的支付服务
        boolean paymentResult = false;
        
        try {
            // 如果是信用额度支付
            if ("agent_credit".equals(paymentDTO.getPaymentMethod())) {
                // 假设这里调用了信用额度支付服务
                // paymentResult = creditPaymentService.processCreditPayment(...);
                paymentResult = true;
            } else {
                // 其他支付方式
                // 模拟支付成功
                paymentResult = true;
            }
            
            // 支付成功，更新订单状态
            if (paymentResult) {
                // 创建支付记录
                // paymentService.createPayment(...);
                
                // 更新订单支付状态
                tourBooking.setPaymentStatus("paid");
                tourBookingMapper.updatePaymentStatus(bookingId, "paid");
                
                // 同时将订单状态更新为已确认
                if ("pending".equals(tourBooking.getStatus())) {
                    tourBooking.setStatus("confirmed");
                    tourBookingMapper.updateStatus(bookingId, "confirmed");
                }
            }
            
            return paymentResult;
        } catch (Exception e) {
            log.error("处理支付时发生错误", e);
            return false;
        }
    }
    
    /**
     * 将TourBooking转换为OrderVO
     * @param tourBooking 订单实体
     * @return 订单VO
     */
    private OrderVO convertToOrderVO(TourBooking tourBooking) {
        if (tourBooking == null) {
            return null;
        }
        
        OrderVO orderVO = new OrderVO();
        
        // 设置基本订单信息
        orderVO.setBookingId(tourBooking.getBookingId());
        orderVO.setOrderNumber(tourBooking.getOrderNumber());
        orderVO.setTourId(tourBooking.getTourId());
        orderVO.setTourType(tourBooking.getTourType());
        orderVO.setUserId(tourBooking.getUserId() != null ? tourBooking.getUserId().intValue() : null);
        orderVO.setAgentId(tourBooking.getAgentId() != null ? tourBooking.getAgentId().intValue() : null);
        
        // 日期设置（直接使用LocalDate，无需转换）
        if (tourBooking.getBookingDate() != null) {
            orderVO.setBookingDate(tourBooking.getBookingDate().toLocalDate());
        }
        if (tourBooking.getTourStartDate() != null) {
            orderVO.setTourStartDate(tourBooking.getTourStartDate());
        }
        if (tourBooking.getTourEndDate() != null) {
            orderVO.setTourEndDate(tourBooking.getTourEndDate());
        }
        
        orderVO.setStatus(tourBooking.getStatus());
        orderVO.setPaymentStatus(tourBooking.getPaymentStatus());
        orderVO.setTotalPrice(tourBooking.getTotalPrice());
        
        // 设置详细信息
        orderVO.setAdultCount(tourBooking.getAdultCount());
        orderVO.setChildCount(tourBooking.getChildCount());
        orderVO.setHotelLevel(tourBooking.getHotelLevel());
        orderVO.setRoomType(tourBooking.getRoomType());
        orderVO.setHotelRoomCount(tourBooking.getHotelRoomCount());
        orderVO.setPickupLocation(tourBooking.getPickupLocation());
        orderVO.setDropoffLocation(tourBooking.getDropoffLocation());
        
        // 日期设置（直接使用LocalDate，无需转换）
        if (tourBooking.getPickupDate() != null) {
            orderVO.setPickupDate(tourBooking.getPickupDate());
        }
        if (tourBooking.getDropoffDate() != null) {
            orderVO.setDropoffDate(tourBooking.getDropoffDate());
        }
        
        orderVO.setSpecialRequests(tourBooking.getSpecialRequests());
        
        // 设置联系人信息
        orderVO.setContactPerson(tourBooking.getContactPerson());
        orderVO.setContactPhone(tourBooking.getContactPhone());
        
        // 🆕 设置团型管理字段
        orderVO.setGroupType(tourBooking.getGroupType());
        orderVO.setGroupSizeLimit(tourBooking.getGroupSizeLimit());
        
        // 🆕 设置接送机时间
        orderVO.setArrivalDepartureTime(tourBooking.getArrivalDepartureTime());
        orderVO.setDepartureDepartureTime(tourBooking.getDepartureDepartureTime());
        
        // 🆕 设置酒店入住退房日期
        orderVO.setHotelCheckInDate(tourBooking.getHotelCheckInDate());
        orderVO.setHotelCheckOutDate(tourBooking.getHotelCheckOutDate());
        
        // 🆕 解析房型数据：如果是JSON数组则解析，否则作为单个房型处理
        if (tourBooking.getRoomType() != null) {
            try {
                // 尝试解析为JSON数组
                if (tourBooking.getRoomType().startsWith("[") && tourBooking.getRoomType().endsWith("]")) {
                    List<String> roomTypesList = com.alibaba.fastjson.JSON.parseArray(tourBooking.getRoomType(), String.class);
                    orderVO.setRoomTypes(roomTypesList);
                    orderVO.setRoomType(tourBooking.getRoomType()); // 保持原始JSON
                    log.info("✅ 解析房型JSON数组成功: {}", roomTypesList);
                } else {
                    // 单个房型
                    orderVO.setRoomType(tourBooking.getRoomType());
                    orderVO.setRoomTypes(Arrays.asList(tourBooking.getRoomType()));
                    log.info("使用单个房型: {}", tourBooking.getRoomType());
                }
            } catch (Exception e) {
                log.warn("⚠️ 房型数据解析失败，使用原始数据: {}", e.getMessage());
                // 解析失败时的降级处理
                orderVO.setRoomType(tourBooking.getRoomType());
                orderVO.setRoomTypes(Arrays.asList(tourBooking.getRoomType()));
            }
        }
        
        return orderVO;
    }

    /**
     * 🆕 同步订单到排团表（当订单状态从未支付变为已支付时）
     * 使用现有的TourScheduleOrderService来处理同步逻辑
     * @param bookingId 订单ID
     */
    public void syncBookingToScheduleTable(Integer bookingId) {
        log.info("🔄 管理后台开始同步订单到排团表（完整逻辑），订单ID: {}", bookingId);

        TourBooking booking = tourBookingMapper.getById(bookingId);
        if (booking == null) {
            log.error("❌ 订单不存在，无法同步到排团表，订单ID: {}", bookingId);
            return;
        }

        if (!"group_tour".equals(booking.getTourType())) {
            log.info("ℹ️ 订单不是跟团游，无需同步到排团表，订单ID: {}, 类型: {}", bookingId, booking.getTourType());
            return;
        }

        if (booking.getTourStartDate() == null || booking.getTourEndDate() == null) {
            log.error("❌ 订单行程日期不完整，无法同步到排团表，订单ID: {}", bookingId);
            return;
        }

        try {
            // 先删除该订单可能已存在的排团记录（防止重复）
            log.info("🗑️ 删除可能存在的排团记录，订单ID: {}", bookingId);
            tourScheduleOrderMapper.deleteByBookingId(bookingId);

            // 创建新的排团记录
            log.info("🆕 开始创建新的排团记录，订单ID: {}", bookingId);
            autoCreateScheduleOrderFromBooking(booking);

            log.info("✅ 管理后台成功同步订单到排团表（完整逻辑），订单ID: {}", bookingId);
        } catch (Exception e) {
            log.error("❌ 管理后台同步订单到排团表失败: 订单ID={}, 错误: {}", bookingId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 自动创建排团记录（复制自TourBookingServiceImpl的逻辑）
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
                
                // 设置智能航班信息分配
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
        
        // 智能生成行程标题（从产品行程详情获取）
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
        
        // 设置乘客信息（从乘客表获取完整信息并存储到正确字段）
        try {
            // 直接使用订单的联系人信息，更简单可靠
            String contactPersonName = booking.getContactPerson() != null && !booking.getContactPerson().trim().isEmpty() 
                ? booking.getContactPerson() : "未知客户";
            String contactPhone = booking.getContactPhone() != null && !booking.getContactPhone().trim().isEmpty() 
                ? booking.getContactPhone() : "";
                
            scheduleOrder.setContactPerson(contactPersonName);
            scheduleOrder.setContactPhone(contactPhone);
            
            log.info("✅ 直接使用订单联系人信息: 订单ID={}, 姓名=\"{}\", 电话=\"{}\"", 
                    booking.getBookingId(), contactPersonName, contactPhone);
            
            // 获取乘客信息用于详细记录（存储到itinerary_details字段）
            List<PassengerVO> passengers = passengerService.getByBookingId(booking.getBookingId());
            log.info("🔍 调试 - 订单{}获取到的乘客数量: {}", booking.getBookingId(), passengers != null ? passengers.size() : 0);
            
            if (passengers != null && !passengers.isEmpty()) {
                // 详细打印每个乘客的信息
                for (int i = 0; i < passengers.size(); i++) {
                    PassengerVO p = passengers.get(i);
                    log.info("🔍 调试 - 乘客{}: ID={}, 姓名=\"{}\", 电话=\"{}\", isChild={}", 
                            i+1, p.getPassengerId(), p.getFullName(), p.getPhone(), p.getIsChild());
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
                
                log.info("✅ 已设置完整乘客信息到行程详情: 订单ID={}, 乘客数量={}, 成人{}人, 儿童{}人", 
                        booking.getBookingId(), passengers.size(), adultCount, childCount);
            } else {
                // 如果没有乘客信息，使用订单的行程详情
                scheduleOrder.setItineraryDetails(booking.getItineraryDetails());
                log.warn("⚠️ 订单{}没有找到乘客信息，使用订单行程详情", booking.getBookingId());
            }
        } catch (Exception e) {
            log.error("❌ 获取乘客信息失败: 订单ID={}, 错误: {}", booking.getBookingId(), e.getMessage(), e);
            // 失败时使用订单的联系人信息
            scheduleOrder.setContactPerson(booking.getContactPerson() != null ? booking.getContactPerson() : "未知客户");
            scheduleOrder.setContactPhone(booking.getContactPhone());
            scheduleOrder.setItineraryDetails(booking.getItineraryDetails());
        }
        
        // 复制订单的其他字段（完整同步所有订单信息到排团表）
        scheduleOrder.setOrderNumber(booking.getOrderNumber());
        scheduleOrder.setAdultCount(booking.getAdultCount());
        scheduleOrder.setChildCount(booking.getChildCount());
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
        
        // 标识字段
        scheduleOrder.setIsFirstOrder(booking.getIsFirstOrder() != null && booking.getIsFirstOrder() == 1);
        scheduleOrder.setFromReferral(booking.getFromReferral() != null && booking.getFromReferral() == 1);
        scheduleOrder.setReferralCode(booking.getReferralCode());
        
        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        scheduleOrder.setCreatedAt(now);
        scheduleOrder.setUpdatedAt(now);
        
        // 详细字段同步确认日志
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
            
            log.info("✈️ 最후一天航班信息设置 - 订单{} 第{}天: 返程航班=\"{}\"", 
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
     * 智能生成行程标题（从产品行程详情获取）
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
                            // 去掉"第n天: "前缀
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
     */
    private Map<String, Object> parseSelectedOptionalTours(String selectedOptionalTours) {
        try {
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
     * 去掉标题中的"第n天: "或"第n天-"前缀
     */
    private String removeDayPrefix(String title) {
        if (title == null) {
            return "";
        }
        
        // 去掉"第n天: "或"第n天-"前缀
        String result = title.replaceAll("^第\\d+天[:\\-]\\s*", "");
        return result.trim();
    }
} 