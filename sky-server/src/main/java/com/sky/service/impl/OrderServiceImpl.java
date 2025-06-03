package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.OrderPageQueryDTO;
import com.sky.dto.OrderUpdateDTO;
import com.sky.dto.PassengerDTO;
import com.sky.dto.PaymentDTO;
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
import com.sky.vo.OrderVO;
import com.sky.vo.PageResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sky.utils.SpringUtils;

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
            
            // 添加行程详情
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
        
        updateOrder.setUpdatedAt(LocalDateTime.now());
        
        // 执行订单更新
        int result = orderMapper.update(updateOrder);
        
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
            } catch (Exception e) {
                log.error("❌ 发送订单状态修改通知失败: {}", e.getMessage(), e);
            }
        }
        
        // 处理乘客信息更新
        if (orderUpdateDTO.getPassengers() != null && !orderUpdateDTO.getPassengers().isEmpty()) {
            log.info("开始更新订单{}的乘客信息，共{}位乘客", bookingId, orderUpdateDTO.getPassengers().size());
            
            for (PassengerDTO passengerDTO : orderUpdateDTO.getPassengers()) {
                if (passengerDTO.getPassengerId() != null) {
                    // 如果乘客ID存在，更新乘客信息
                    Boolean updated = passengerService.updatePassengerBookingInfo(bookingId, passengerDTO);
                    if (!updated) {
                        log.warn("更新订单{}的乘客{}信息失败", bookingId, passengerDTO.getPassengerId());
                    }
                } else {
                    // 如果乘客ID不存在，添加新乘客到订单
                    Boolean added = passengerService.addPassengerToBooking(bookingId, passengerDTO);
                    if (!added) {
                        log.warn("添加乘客到订单{}失败", bookingId);
                    }
                }
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
        
        // 日期转换
        if (tourBooking.getBookingDate() != null) {
            orderVO.setBookingDate(Date.valueOf(tourBooking.getBookingDate().toLocalDate()));
        }
        if (tourBooking.getTourStartDate() != null) {
            orderVO.setTourStartDate(Date.valueOf(tourBooking.getTourStartDate()));
        }
        if (tourBooking.getTourEndDate() != null) {
            orderVO.setTourEndDate(Date.valueOf(tourBooking.getTourEndDate()));
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
        
        // 日期转换
        if (tourBooking.getPickupDate() != null) {
            orderVO.setPickupDate(Date.valueOf(tourBooking.getPickupDate()));
        }
        if (tourBooking.getDropoffDate() != null) {
            orderVO.setDropoffDate(Date.valueOf(tourBooking.getDropoffDate()));
        }
        
        orderVO.setSpecialRequests(tourBooking.getSpecialRequests());
        
        // 设置联系人信息
        orderVO.setContactPerson(tourBooking.getContactPerson());
        orderVO.setContactPhone(tourBooking.getContactPhone());
        
        return orderVO;
    }
} 