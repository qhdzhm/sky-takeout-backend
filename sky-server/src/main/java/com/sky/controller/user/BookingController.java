package com.sky.controller.user;

import com.sky.dto.BookingDTO;
import com.sky.dto.EmailConfirmationDTO;
import com.sky.dto.EmailInvoiceDTO;
import com.sky.dto.GroupTourDTO;
import com.sky.dto.PaymentDTO;
import com.sky.dto.TourBookingDTO;
import java.util.concurrent.CompletableFuture;
import com.sky.entity.DayTour;
import com.sky.entity.HotelPriceDifference;
import com.sky.entity.TourBooking;
import com.sky.mapper.DayTourMapper;
import com.sky.mapper.GroupTourMapper;
import com.sky.result.Result;
import com.sky.service.BookingService;
import com.sky.service.EmailService;
import com.sky.service.HotelPriceService;
import com.sky.service.TourBookingService;
import com.sky.vo.PassengerVO;
import com.sky.vo.PriceDetailVO;
import com.sky.vo.TourBookingVO;
import com.sky.context.BaseContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预订控制器
 */
@RestController
@RequestMapping("/user/bookings")
@Api(tags = "预订相关接口")
@Slf4j
public class BookingController {

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private TourBookingService tourBookingService;
    
    @Autowired
    private HotelPriceService hotelPriceService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private GroupTourMapper groupTourMapper;
    
    @Autowired
    private DayTourMapper dayTourMapper;

    /**
     * 创建预订
     * @param bookingDTO 预订信息
     * @return 预订ID
     */
    @PostMapping
    @ApiOperation("创建预订")
    public Result<Integer> createBooking(@RequestBody BookingDTO bookingDTO) {
        log.info("创建预订：{}", bookingDTO);
        Integer bookingId = bookingService.createBooking(bookingDTO);
        return Result.success(bookingId);
    }

    /**
     * 获取用户预订列表
     * @return 预订列表
     */
    @GetMapping
    @ApiOperation("获取用户预订列表")
    public Result<List<BookingDTO>> getUserBookings() {
        log.info("获取用户预订列表");
        List<BookingDTO> bookings = bookingService.getUserBookings();
        return Result.success(bookings);
    }

    /**
     * 根据ID获取预订详情
     * @param id 预订ID
     * @return 预订详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取预订详情")
    public Result<TourBooking> getBookingById(@PathVariable Integer id) {
        log.info("获取预订详情，ID：{}", id);
        TourBooking booking = bookingService.getBookingById(id);
        return Result.success(booking);
    }

    /**
     * 取消预订
     * @param id 预订ID
     * @return 操作结果
     */
    @PostMapping("/{id}/cancel")
    @ApiOperation("取消预订")
    public Result<String> cancelBooking(@PathVariable Integer id) {
        log.info("取消预订，ID：{}", id);
        bookingService.cancelBooking(id);
        return Result.success("预订已取消");
    }

    /**
     * 检查可用性
     * @param params 查询参数
     * @return 可用性信息
     */
    @GetMapping("/check-availability")
    @ApiOperation("检查可用性")
    public Result<Map<String, Object>> checkAvailability(@RequestParam Map<String, Object> params) {
        log.info("检查可用性，参数：{}", params);
        Map<String, Object> availability = bookingService.checkAvailability(params);
        return Result.success(availability);
    }
    
    /**
     * 创建旅游订单
     * @param tourBookingDTO 旅游订单信息
     * @return 订单信息
     */
    @PostMapping("/tour/create")
    @ApiOperation("创建旅游订单")
    public Result<Map<String, Object>> createTourBooking(@RequestBody TourBookingDTO tourBookingDTO) {
        log.info("创建旅游订单：{}", tourBookingDTO);
        
        // 🔧 修复用户ID设置问题：在订单创建前先获取并设置用户ID
        try {
            Long currentUserId = BaseContext.getCurrentId();
            String currentUserType = BaseContext.getCurrentUserType();
            Long currentAgentId = BaseContext.getCurrentAgentId();
            String currentUsername = BaseContext.getCurrentUsername();
            Long currentOperatorId = BaseContext.getCurrentOperatorId();
            
            log.info("🔍 创建订单前的用户上下文信息: userId={}, userType={}, agentId={}, username={}, operatorId={}", 
                    currentUserId, currentUserType, currentAgentId, currentUsername, currentOperatorId);
            
            if (currentUserId != null) {
                tourBookingDTO.setUserId(currentUserId.intValue());
                log.info("✅ 已设置订单用户ID: {}, 用户类型: {}", currentUserId, currentUserType);
                
                // 如果是代理商，确保设置代理商ID
                if ("agent".equals(currentUserType)) {
                    if (currentAgentId != null) {
                        tourBookingDTO.setAgentId(currentAgentId.intValue());
                        log.info("✅ 已设置订单代理商ID（来源：getCurrentAgentId）: {}", currentAgentId);
                    } else {
                        // 如果getCurrentAgentId为null，使用currentUserId作为agentId
                        tourBookingDTO.setAgentId(currentUserId.intValue());
                        log.warn("⚠️ getCurrentAgentId为null，使用currentUserId作为agentId: {}", currentUserId);
                    }
                } else if ("agent_operator".equals(currentUserType)) {
                    if (currentAgentId != null) {
                        tourBookingDTO.setAgentId(currentAgentId.intValue());
                        log.info("✅ 操作员下单，已设置订单代理商ID: {}", currentAgentId);
                    }
                    if (currentOperatorId != null) {
                        tourBookingDTO.setOperatorId(currentOperatorId);
                        log.info("✅ 操作员下单，已设置操作员ID: {}", currentOperatorId);
                    }
                }
            } else {
                log.info("⚠️ 无法获取当前用户ID，可能是游客模式");
            }
        } catch (Exception e) {
            log.error("⚠️ 获取用户信息失败，使用游客模式", e);
        }
        
        // 调用订单服务创建订单
        Integer bookingId = tourBookingService.save(tourBookingDTO);
        
        if (bookingId != null) {
            TourBookingVO bookingVO = tourBookingService.getById(bookingId);
            
            // 🔥 订单创建成功后，异步发送邮件（不阻塞响应）
            // ⚠️ 重要：在主线程中提前获取BaseContext信息，避免异步线程中ThreadLocal丢失
            // 支持游客模式：安全处理BaseContext调用
            Long orderIdLong = bookingId.longValue();
            TourBookingVO bookingVOFinal = bookingVO;
            String currentUserType = null;
            Long currentUserId = null; 
            Long currentAgentId = null;
            Long currentOperatorId = null;
            
            try {
                currentUserType = BaseContext.getCurrentUserType();
                currentUserId = BaseContext.getCurrentId(); 
                currentAgentId = BaseContext.getCurrentAgentId();
                currentOperatorId = BaseContext.getCurrentOperatorId();
                log.info("💾 保存用户上下文信息用于异步邮件: userType={}, userId={}, agentId={}, operatorId={}", 
                        currentUserType, currentUserId, currentAgentId, currentOperatorId);
            } catch (Exception e) {
                log.info("游客模式下单，无用户认证信息: {}", e.getMessage());
            }
            
            // 注释掉订单创建时的邮件发送，改为支付后发送
            // CompletableFuture.runAsync(() -> {
            //     try {
            //         log.info("开始异步发送邮件: orderId={}", orderIdLong);
            //         sendEmailsAfterOrderCreation(orderIdLong, bookingVOFinal, 
            //                 currentUserType, currentUserId, currentAgentId, currentOperatorId);
            //         log.info("异步邮件发送完成: orderId={}", orderIdLong);
            //     } catch (Exception e) {
            //         log.error("异步邮件发送失败: orderId={}", orderIdLong, e);
            //     }
            // });
            
            Map<String, Object> data = new HashMap<>();
            data.put("bookingId", bookingVO.getBookingId());
            data.put("orderNumber", bookingVO.getOrderNumber());
            data.put("totalPrice", bookingVO.getTotalPrice());
            data.put("status", bookingVO.getStatus());
            data.put("paymentStatus", bookingVO.getPaymentStatus());
            
            return Result.success(data);
        }
        
        return Result.error("订单创建失败");
    }
    
    /**
     * 计算旅游订单价格（统一接口，支持可选项目和多房间类型）
     */
    @PostMapping("/tour/calculate-price")
    @ApiOperation("计算旅游订单价格")
    public Result<Map<String, Object>> calculateTourPrice(
            @RequestParam Integer tourId,
            @RequestParam String tourType,
            @RequestParam(required = false) Long agentId,
            @RequestParam Integer adultCount,
            @RequestParam(required = false, defaultValue = "0") Integer childCount,
            @RequestParam(required = false, defaultValue = "4星") String hotelLevel,
            @RequestParam(required = false, defaultValue = "1") Integer roomCount,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String childrenAges,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String roomTypes,
            @RequestParam(required = false) String selectedOptionalTours) {
        
        // 自动从BaseContext获取代理商ID，如果有的话（支持游客模式）
        try {
            Long currentAgentId = BaseContext.getCurrentAgentId();
            
            // 先尝试从BaseContext.getCurrentAgentId()获取
            if (currentAgentId != null) {
                // 如果从登录Token中获取到了代理商ID，优先使用这个ID
                agentId = currentAgentId;
                log.info("从Token中获取代理商ID: {}", agentId);
            } 
            // 再尝试通过用户类型和当前ID推断
            else if (agentId == null) {
                String userType = BaseContext.getCurrentUserType();
                Long currentId = BaseContext.getCurrentId();
                
                if ("agent".equals(userType) && currentId != null) {
                    agentId = currentId;
                    log.info("从用户类型和当前ID推断代理商ID: {}", agentId);
                } else {
                    log.info("当前用户非代理商，不应用折扣。用户类型: {}, 当前ID: {}", userType, currentId);
                }
            }
        } catch (Exception e) {
            // 游客模式：BaseContext可能没有用户信息，这是正常的
            log.info("游客模式价格计算，无用户认证信息: {}", e.getMessage());
            agentId = null; // 确保游客没有代理商折扣
        }
        
        log.info("计算旅游订单价格: tourId={}, tourType={}, agentId={}, adultCount={}, childCount={}, hotelLevel={}, roomCount={}, userId={}, childrenAges={}, roomType={}, roomTypes={}, selectedOptionalTours={}", 
                tourId, tourType, agentId, adultCount, childCount, hotelLevel, roomCount, userId, childrenAges, roomType, roomTypes, selectedOptionalTours);
        
        try {
            log.info("使用统一价格计算方法");
            
            // 处理房间类型参数：如果传递了单个roomType，转换为roomTypes格式
            String finalRoomTypes = roomTypes;
            if ((finalRoomTypes == null || finalRoomTypes.trim().isEmpty()) && roomType != null && !roomType.trim().isEmpty()) {
                finalRoomTypes = roomType; // 单个房型
            }
            
            // 使用统一的价格计算方法
            Map<String, Object> data = tourBookingService.calculateUnifiedPrice(
                    tourId, tourType, agentId, adultCount, childCount, hotelLevel, roomCount, userId, 
                    finalRoomTypes, childrenAges, selectedOptionalTours);
            
            log.info("统一价格计算完成: {}", data);
            return Result.success(data);
        } catch (Exception e) {
            log.error("计算价格失败: {}", e.getMessage(), e);
            return Result.error("计算价格失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取酒店价格差异列表
     */
    @GetMapping("/hotel-prices")
    @ApiOperation("获取酒店价格差异列表")
    public Result<List<HotelPriceDifference>> getHotelPrices() {
        log.info("获取酒店价格差异列表");
        List<HotelPriceDifference> priceDifferences = hotelPriceService.getAllPriceDifferences();
        return Result.success(priceDifferences);
    }

    /**
     * 支付订单
     * @param id 订单ID
     * @param paymentDTO 支付数据
     * @return 支付结果
     */
    @PostMapping("/{id}/pay")
    @ApiOperation("支付订单")
    public Result<Boolean> payBooking(
            @ApiParam(name = "id", value = "订单ID", required = true)
            @PathVariable Integer id,
            @RequestBody PaymentDTO paymentDTO) {
        log.info("支付订单，ID：{}, 支付数据: {}", id, paymentDTO);
        
        try {
            // 检查订单
            TourBooking booking = bookingService.getBookingById(id);
            
            if (booking == null) {
                return Result.error("订单不存在");
            }
            
            // 只有未支付的订单可以支付
            if (!"unpaid".equals(booking.getPaymentStatus())) {
                return Result.error("订单已支付，无需重复支付");
            }
            
            // 🔒 安全验证：后端重新计算订单实际价格，不信任前端传来的价格
            Long agentId = BaseContext.getCurrentAgentId();
            Long userId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            
            // 重新计算订单实际应付金额
            BigDecimal actualAmount;
            if ("agent_operator".equals(userType) && agentId != null) {
                // 操作员：使用代理商折扣价
                Map<String, Object> priceResult = tourBookingService.calculateUnifiedPrice(
                    booking.getTourId(), 
                    booking.getTourType(), 
                    agentId, 
                    booking.getAdultCount(), 
                    booking.getChildCount(), 
                    booking.getHotelLevel(), 
                    booking.getHotelRoomCount(),
                    userId,
                    null, // roomTypes
                    null, // childrenAges
                    null  // selectedOptionalTours
                );
                actualAmount = BigDecimal.ZERO;
                if (priceResult != null && priceResult.get("data") != null) {
                    Map<String, Object> data = (Map<String, Object>) priceResult.get("data");
                    actualAmount = (BigDecimal) data.get("totalPrice");
                }
                log.info("操作员支付验证，重新计算的实际价格: {}", actualAmount);
            } else {
                // 其他用户：重新计算价格
                Map<String, Object> priceResult = tourBookingService.calculateUnifiedPrice(
                    booking.getTourId(), 
                    booking.getTourType(), 
                    agentId, 
                    booking.getAdultCount(), 
                    booking.getChildCount(), 
                    booking.getHotelLevel(), 
                    booking.getHotelRoomCount(),
                    userId,
                    null, // roomTypes
                    null, // childrenAges
                    null  // selectedOptionalTours
                );
                actualAmount = BigDecimal.ZERO;
                if (priceResult != null && priceResult.get("data") != null) {
                    Map<String, Object> data = (Map<String, Object>) priceResult.get("data");
                    actualAmount = (BigDecimal) data.get("totalPrice");
                }
                log.info("支付验证，重新计算的实际价格: {}", actualAmount);
            }
            
            // 验证前端传来的金额是否与实际计算金额一致（允许小数点误差）
            if (paymentDTO.getAmount() != null) {
                BigDecimal frontendAmount = paymentDTO.getAmount();
                BigDecimal difference = actualAmount.subtract(frontendAmount).abs();
                BigDecimal tolerance = new BigDecimal("0.01"); // 1分钱的误差容忍度
                
                if (difference.compareTo(tolerance) > 0) {
                    log.error("支付金额验证失败！前端金额: {}, 实际金额: {}, 差异: {}", 
                             frontendAmount, actualAmount, difference);
                    return Result.error("支付金额异常，请刷新页面重试");
                }
            }
            
            // 🔒 强制使用后端计算的价格
            paymentDTO.setAmount(actualAmount);
            
            // 检查支付数据
            if (paymentDTO.getBookingId() == null) {
                paymentDTO.setBookingId(id);
            }
            
            // 调用支付服务
            Boolean result = tourBookingService.payBooking(id, paymentDTO);
            
            if (result) {
                return Result.success(true);
            } else {
                return Result.error("支付处理失败");
            }
        } catch (Exception e) {
            log.error("支付订单时发生错误", e);
            return Result.error("支付处理异常: " + e.getMessage());
        }
    }

    /**
     * 根据订单号支付订单
     * @param orderNumber 订单号
     * @param paymentDTO 支付数据
     * @return 支付结果
     */
    @PostMapping("/order-numbers/{orderNumber}/pay")
    @ApiOperation("根据订单号支付订单")
    public Result<Boolean> payBookingByOrderNumber(
            @ApiParam(name = "orderNumber", value = "订单号", required = true)
            @PathVariable String orderNumber,
            @RequestBody PaymentDTO paymentDTO) {
        log.info("根据订单号支付订单，订单号：{}, 支付数据: {}", orderNumber, paymentDTO);
        
        try {
            // 查询订单
            TourBooking booking = bookingService.getBookingById(Integer.parseInt(orderNumber.replace("HT", "")));
            
            if (booking == null) {
                return Result.error("订单不存在");
            }
            
            // 只有未支付的订单可以支付
            if (!"unpaid".equals(booking.getPaymentStatus())) {
                return Result.error("订单已支付，无需重复支付");
            }
            
            // 🔒 安全验证：后端重新计算订单实际价格，不信任前端传来的价格
            Long agentId = BaseContext.getCurrentAgentId();
            Long userId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            
            // 重新计算订单实际应付金额
            BigDecimal actualAmount;
            if ("agent_operator".equals(userType) && agentId != null) {
                // 操作员：使用代理商折扣价
                Map<String, Object> priceResult = tourBookingService.calculateUnifiedPrice(
                    booking.getTourId(), 
                    booking.getTourType(), 
                    agentId, 
                    booking.getAdultCount(), 
                    booking.getChildCount(), 
                    booking.getHotelLevel(), 
                    booking.getHotelRoomCount(),
                    userId,
                    null, // roomTypes
                    null, // childrenAges
                    null  // selectedOptionalTours
                );
                actualAmount = BigDecimal.ZERO;
                if (priceResult != null && priceResult.get("data") != null) {
                    Map<String, Object> data = (Map<String, Object>) priceResult.get("data");
                    actualAmount = (BigDecimal) data.get("totalPrice");
                }
                log.info("操作员订单号支付验证，重新计算的实际价格: {}", actualAmount);
            } else {
                // 其他用户：重新计算价格
                Map<String, Object> priceResult = tourBookingService.calculateUnifiedPrice(
                    booking.getTourId(), 
                    booking.getTourType(), 
                    agentId, 
                    booking.getAdultCount(), 
                    booking.getChildCount(), 
                    booking.getHotelLevel(), 
                    booking.getHotelRoomCount(),
                    userId,
                    null, // roomTypes
                    null, // childrenAges
                    null  // selectedOptionalTours
                );
                actualAmount = BigDecimal.ZERO;
                if (priceResult != null && priceResult.get("data") != null) {
                    Map<String, Object> data = (Map<String, Object>) priceResult.get("data");
                    actualAmount = (BigDecimal) data.get("totalPrice");
                }
                log.info("订单号支付验证，重新计算的实际价格: {}", actualAmount);
            }
            
            // 验证前端传来的金额是否与实际计算金额一致（允许小数点误差）
            if (paymentDTO.getAmount() != null) {
                BigDecimal frontendAmount = paymentDTO.getAmount();
                BigDecimal difference = actualAmount.subtract(frontendAmount).abs();
                BigDecimal tolerance = new BigDecimal("0.01"); // 1分钱的误差容忍度
                
                if (difference.compareTo(tolerance) > 0) {
                    log.error("订单号支付金额验证失败！前端金额: {}, 实际金额: {}, 差异: {}", 
                             frontendAmount, actualAmount, difference);
                    return Result.error("支付金额异常，请刷新页面重试");
                }
            }
            
            // 🔒 强制使用后端计算的价格
            paymentDTO.setAmount(actualAmount);
            
            // 检查支付数据
            if (paymentDTO.getBookingId() == null) {
                paymentDTO.setBookingId(booking.getBookingId());
            }
            
            // 调用支付服务
            Boolean result = tourBookingService.payBooking(booking.getBookingId(), paymentDTO);
            
            if (result) {
                return Result.success(true);
            } else {
                return Result.error("支付处理失败");
            }
        } catch (Exception e) {
            log.error("支付订单时发生错误", e);
            return Result.error("支付处理异常: " + e.getMessage());
        }
    }

    /**
     * 订单创建成功后自动发送邮件
     * @param orderId 订单ID
     * @param bookingVO 订单信息
     * @param userType 用户类型
     * @param currentUserId 当前用户ID
     * @param agentId 代理商ID
     * @param operatorId 操作员ID
     */
    private void sendEmailsAfterOrderCreation(Long orderId, TourBookingVO bookingVO, 
            String userType, Long currentUserId, Long agentId, Long operatorId) {
        log.info("订单创建成功，开始自动发送邮件: orderId={}", orderId);
        
        try {
            log.info("🔄 使用传入的用户信息: userType={}, currentUserId={}, agentId={}, operatorId={}", 
                    userType, currentUserId, agentId, operatorId);
            
            // 确定实际的代理商ID和操作员ID
            Long actualAgentId;
            Long actualOperatorId = null;
            String recipientType;
            
            if (agentId != null) {
                // 有agentId说明是操作员
                actualAgentId = agentId;
                actualOperatorId = currentUserId;
                recipientType = "operator";
                log.info("✅ 操作员下单: 代理商ID={}, 操作员ID={}", actualAgentId, actualOperatorId);
            } else {
                // 没有agentId说明是代理商主号
                actualAgentId = currentUserId;
                recipientType = "agent";
                log.info("✅ 代理商主号下单: 代理商ID={}", actualAgentId);
            }
            
            // 构建订单详情
            EmailConfirmationDTO.OrderDetails orderDetails = buildOrderDetails(bookingVO);
            EmailInvoiceDTO.InvoiceDetails invoiceDetails = buildInvoiceDetails(bookingVO);
            
            // 1. 发送发票邮件给代理商主号（不管是主号下单还是操作员下单都要发）
            try {
                EmailInvoiceDTO invoiceDTO = new EmailInvoiceDTO();
                invoiceDTO.setOrderId(orderId);
                invoiceDTO.setAgentId(actualAgentId);
                invoiceDTO.setOperatorId(actualOperatorId);
                invoiceDTO.setInvoiceDetails(invoiceDetails);
                
                emailService.sendInvoiceEmail(invoiceDTO);
                log.info("✅ 发票邮件发送成功: orderId={}, agentId={}", orderId, actualAgentId);
            } catch (Exception e) {
                log.error("❌ 发票邮件发送失败: orderId={}", orderId, e);
            }
            
            // 2. 发送确认单邮件
            try {
                EmailConfirmationDTO confirmationDTO = new EmailConfirmationDTO();
                confirmationDTO.setOrderId(orderId);
                confirmationDTO.setRecipientType(recipientType);
                confirmationDTO.setAgentId(actualAgentId);
                confirmationDTO.setOperatorId(actualOperatorId);
                confirmationDTO.setOrderDetails(orderDetails);
                
                emailService.sendConfirmationEmail(confirmationDTO);
                
                if ("operator".equals(recipientType)) {
                    log.info("✅ 操作员下单确认单邮件发送成功: orderId={}, 发送给操作员和主号", orderId);
                } else {
                    log.info("✅ 主号下单确认单邮件发送成功: orderId={}, 发送给主号", orderId);
                }
            } catch (Exception e) {
                log.error("❌ 确认单邮件发送失败: orderId={}, recipientType={}", orderId, recipientType, e);
            }
            
            log.info("订单邮件发送处理完成: orderId={}, recipientType={}", orderId, recipientType);
            
        } catch (Exception e) {
            log.error("订单邮件发送处理异常: orderId={}", orderId, e);
        }
    }
    
    /**
     * 构建订单详情（用于确认邮件）
     */
    private EmailConfirmationDTO.OrderDetails buildOrderDetails(TourBookingVO bookingVO) {
        EmailConfirmationDTO.OrderDetails orderDetails = new EmailConfirmationDTO.OrderDetails();
        
        // 获取真实的产品名称
        String actualTourName = getTourNameByIdAndType(bookingVO.getTourId(), bookingVO.getTourType());
        orderDetails.setTourName(actualTourName != null ? actualTourName : 
                                (bookingVO.getTourName() != null ? bookingVO.getTourName() : "塔斯马尼亚旅游"));
        orderDetails.setTourType(bookingVO.getTourType());
        orderDetails.setStartDate(bookingVO.getTourStartDate() != null ? bookingVO.getTourStartDate().toString() : null);
        orderDetails.setEndDate(bookingVO.getTourEndDate() != null ? bookingVO.getTourEndDate().toString() : null);
        orderDetails.setAdultCount(bookingVO.getGroupSize() != null ? bookingVO.getGroupSize() : 1);
        orderDetails.setChildCount(0); // TourBookingVO中没有单独的childCount字段，暂时设为0
        orderDetails.setContactPerson(bookingVO.getContactPerson());
        orderDetails.setContactPhone(bookingVO.getContactPhone());
        orderDetails.setPickupLocation(bookingVO.getPickupLocation());
        orderDetails.setDropoffLocation(bookingVO.getDropoffLocation());
        orderDetails.setHotelLevel(bookingVO.getHotelLevel());
        orderDetails.setSpecialRequests(bookingVO.getSpecialRequests());
        return orderDetails;
    }
    
    /**
     * 构建发票详情（用于发票邮件）
     */
    private EmailInvoiceDTO.InvoiceDetails buildInvoiceDetails(TourBookingVO bookingVO) {
        EmailInvoiceDTO.InvoiceDetails invoiceDetails = new EmailInvoiceDTO.InvoiceDetails();
        
        // 获取真实的产品名称
        String actualTourName = getTourNameByIdAndType(bookingVO.getTourId(), bookingVO.getTourType());
        invoiceDetails.setTourName(actualTourName != null ? actualTourName : 
                                  (bookingVO.getTourName() != null ? bookingVO.getTourName() : "塔斯马尼亚旅游"));
        invoiceDetails.setTourType(bookingVO.getTourType());
        invoiceDetails.setStartDate(bookingVO.getTourStartDate() != null ? bookingVO.getTourStartDate().toString() : null);
        invoiceDetails.setEndDate(bookingVO.getTourEndDate() != null ? bookingVO.getTourEndDate().toString() : null);
        invoiceDetails.setAdultCount(bookingVO.getGroupSize() != null ? bookingVO.getGroupSize() : 1);
        invoiceDetails.setChildCount(0); // TourBookingVO中没有单独的childCount字段，暂时设为0
        invoiceDetails.setTotalPrice(bookingVO.getTotalPrice() != null ? bookingVO.getTotalPrice().doubleValue() : 0.0);
        return invoiceDetails;
    }
    
    /**
     * 根据tourId和tourType获取产品名称
     * @param tourId 产品ID
     * @param tourType 产品类型
     * @return 产品名称
     */
    private String getTourNameByIdAndType(Integer tourId, String tourType) {
        if (tourId == null || tourType == null) {
            return null;
        }
        
        try {
            if ("group_tour".equals(tourType)) {
                GroupTourDTO groupTour = groupTourMapper.getById(tourId);
                return groupTour != null ? groupTour.getName() : null;
            } else if ("day_tour".equals(tourType)) {
                DayTour dayTour = dayTourMapper.getById(tourId);
                return dayTour != null ? dayTour.getName() : null;
            }
        } catch (Exception e) {
            log.error("获取产品名称失败: tourId={}, tourType={}", tourId, tourType, e);
        }
        
        return null;
    }
} 