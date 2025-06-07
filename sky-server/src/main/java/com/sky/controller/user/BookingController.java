package com.sky.controller.user;

import com.sky.dto.BookingDTO;
import com.sky.dto.GroupTourDTO;
import com.sky.dto.PaymentDTO;
import com.sky.dto.TourBookingDTO;
import com.sky.entity.DayTour;
import com.sky.entity.HotelPriceDifference;
import com.sky.entity.TourBooking;
import com.sky.result.Result;
import com.sky.service.BookingService;
import com.sky.service.HotelPriceService;
import com.sky.service.TourBookingService;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
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
        
        // 调用订单服务创建订单
        Integer bookingId = tourBookingService.save(tourBookingDTO);
        
        if (bookingId != null) {
            TourBookingVO bookingVO = tourBookingService.getById(bookingId);
            
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
     * 计算旅游订单价格
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
            @RequestParam(required = false) String roomType) {
        
        // 自动从BaseContext获取代理商ID，如果有的话
        Long currentAgentId = BaseContext.getCurrentAgentId();
        
        // 先尝试从BaseContext.getCurrentAgentId()获取
        if (currentAgentId != null) {
            // 如果从登录Token中获取到了代理商ID，优先使用这个ID
            agentId = currentAgentId;
            log.info("从Token中获取代理商ID: {}", agentId);
        } 
        // 再尝试通过用户类型和当前ID推断
        else {
            String userType = BaseContext.getCurrentUserType();
            Long currentId = BaseContext.getCurrentId();
            
            if ("agent".equals(userType) && currentId != null) {
                agentId = currentId;
                log.info("从用户类型和当前ID推断代理商ID: {}", agentId);
            } else {
                log.info("当前用户非代理商，不应用折扣。用户类型: {}, 当前ID: {}", userType, currentId);
            }
        }
        
        log.info("计算旅游订单价格，tourId: {}, tourType: {}, adultCount: {}, childCount: {}, hotelLevel: {}, roomCount: {}, userId: {}, childrenAges: {}, agentId: {}, roomType: {}", 
                tourId, tourType, adultCount, childCount, hotelLevel, roomCount, userId, childrenAges, agentId, roomType);
        
        try {
            // 获取原始价格（从产品获取）
            BigDecimal originalPrice = BigDecimal.ZERO;
            int nights = 0;
            BigDecimal discountRate = BigDecimal.ONE; // 默认折扣率为1（不打折）

            if ("day_tour".equals(tourType)) {
                // 获取一日游产品信息以获取原价
                DayTour dayTour = tourBookingService.getDayTourById(tourId);
                if (dayTour != null) {
                    originalPrice = dayTour.getPrice();
                }
            } else if ("group_tour".equals(tourType)) {
                // 获取跟团游产品信息以获取原价和天数
                GroupTourDTO groupTour = tourBookingService.getGroupTourById(tourId);
                if (groupTour != null) {
                    originalPrice = groupTour.getPrice();
                    
                    // 解析天数和夜数
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
                }
            }
            
            // 获取代理商折扣率
            if (agentId != null) {
                try {
                    discountRate = tourBookingService.getAgentDiscountRate(agentId);
                    log.info("获取到代理商折扣率: {}, 代理商ID: {}", discountRate, agentId);
                } catch (Exception e) {
                    log.error("获取代理商折扣率失败: {}", e.getMessage(), e);
                }
            }
            
            // 计算折扣价
            BigDecimal discountedPrice = originalPrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);

            // 获取价格详情 - 关键修改：调用带房型参数的方法
            log.info("正在调用价格计算详情方法，使用agentId={}, discountRate={}, roomType={}", agentId, discountRate, roomType);
            
            // 确保agentId的类型正确
            if (agentId != null) {
                log.info("代理商ID类型：{}", agentId.getClass().getName());
                
                // 如果需要，可以转换agentId类型
                if (!(agentId instanceof Long)) {
                    try {
                        agentId = Long.valueOf(agentId.toString());
                        log.info("成功将代理商ID转换为Long类型: {}", agentId);
                    } catch (Exception e) {
                        log.error("代理商ID类型转换失败: {}", e.getMessage(), e);
                        agentId = null; // 转换失败时设为null
                    }
                }
            }
            
            // 🔧 关键修改：调用带房型参数的价格计算方法
            PriceDetailVO priceDetail = tourBookingService.calculatePriceDetail(
                    tourId, tourType, agentId, adultCount, childCount, hotelLevel, roomCount, userId, roomType);
            
            // 提取总价，基础价格和额外房费
            BigDecimal totalPrice = priceDetail.getTotalPrice();
            BigDecimal basePrice = priceDetail.getBasePrice();
            BigDecimal extraRoomFee = priceDetail.getExtraRoomFee();
            BigDecimal nonAgentPrice = priceDetail.getNonAgentPrice(); // 获取非代理商价格
            
            // 获取基准酒店等级
            String baseHotelLevel = hotelPriceService.getBaseHotelLevel();
            
            // 获取酒店价格差异
            BigDecimal hotelPriceDiff = hotelPriceService.getPriceDifferenceByLevel(hotelLevel);
            
            // 获取酒店单房差
            BigDecimal singleRoomSupplement = hotelPriceService.getDailySingleRoomSupplementByLevel(hotelLevel);
            
            // 根据房型获取相应的房间价格
            BigDecimal hotelRoomPrice;
            BigDecimal tripleDifference = BigDecimal.ZERO;
            if (roomType != null && (roomType.contains("三人间") || roomType.contains("三床") || roomType.contains("家庭") || roomType.equalsIgnoreCase("triple") || roomType.equalsIgnoreCase("family"))) {
                BigDecimal roomBasePrice = hotelPriceService.getHotelRoomPriceByLevel(hotelLevel);
                tripleDifference = hotelPriceService.getTripleBedRoomPriceDifferenceByLevel(hotelLevel);
                hotelRoomPrice = roomBasePrice.add(tripleDifference);
                log.info("使用三人间房价: {} = 基础价格{} + 三人房差价{}", hotelRoomPrice, roomBasePrice, tripleDifference);
            } else {
                hotelRoomPrice = hotelPriceService.getHotelRoomPriceByLevel(hotelLevel);
                log.info("使用标准房价: {}", hotelRoomPrice);
            }
            
            // 判断是否需要单房差
            int totalPeople = adultCount + childCount;
            boolean needsSingleRoomSupplement = (totalPeople % 2 != 0) && (roomCount == Math.ceil(totalPeople / 2.0));
            
            // 额外房间数
            int theoreticalRoomCount = (int) Math.ceil(totalPeople / 2.0);
            int extraRooms = roomCount > theoreticalRoomCount ? roomCount - theoreticalRoomCount : 0;
            
            // 处理儿童年龄和相应的票价计算
            List<Map<String, Object>> childPrices = new ArrayList<>();
            
            // 解析儿童年龄字符串，格式可能是：1,2,3或者[1,2,3]
            Integer[] validChildrenAges = null;
            if (childrenAges != null && !childrenAges.trim().isEmpty()) {
                try {
                    // 先尝试移除可能存在的方括号
                    String cleanAges = childrenAges.replace("[", "").replace("]", "").trim();
                    
                    // 按逗号分隔并解析为整数
                    String[] agesArray = cleanAges.split(",");
                    validChildrenAges = new Integer[agesArray.length];
                    
                    for (int i = 0; i < agesArray.length; i++) {
                        validChildrenAges[i] = Integer.parseInt(agesArray[i].trim());
                    }
                    
                    log.info("成功解析儿童年龄数组: {}", Arrays.toString(validChildrenAges));
                } catch (Exception e) {
                    log.error("解析儿童年龄字符串失败: {}", e.getMessage(), e);
                    // 解析失败，使用默认值
                    validChildrenAges = null;
                }
            }
            
            // 如果解析失败或未提供，创建默认年龄数组
            if (validChildrenAges == null || validChildrenAges.length == 0) {
                // 如果未提供年龄，则假设所有儿童都是3-7岁，适用-50的折扣
                validChildrenAges = new Integer[childCount];
                Arrays.fill(validChildrenAges, 5); // 默认5岁
                log.info("使用默认儿童年龄: 5岁");
            }
            
            // 确保年龄数组长度与儿童数量匹配
            if (validChildrenAges.length < childCount) {
                // 如果年龄数组长度小于儿童数量，用默认值补齐
                Integer[] extendedAges = new Integer[childCount];
                System.arraycopy(validChildrenAges, 0, extendedAges, 0, validChildrenAges.length);
                
                // 剩余的用默认年龄填充
                for (int i = validChildrenAges.length; i < childCount; i++) {
                    extendedAges[i] = 5; // 默认5岁
                }
                
                validChildrenAges = extendedAges;
                log.info("扩展儿童年龄数组: {}", Arrays.toString(validChildrenAges));
            }
            
            // 根据不同年龄的儿童计算总价
            BigDecimal childrenTotalPrice = BigDecimal.ZERO;
            
            for (int i = 0; i < validChildrenAges.length && i < childCount; i++) {
                Integer age = validChildrenAges[i];
                BigDecimal childPrice;
                String priceType;
                
                if (age < 3) {
                    // 小于3岁半价(成人折扣价的一半)
                    childPrice = discountedPrice.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
                    priceType = "半价";
                } else if (age <= 7) {
                    // 3-7岁减50
                    childPrice = discountedPrice.subtract(new BigDecimal("50")).setScale(2, RoundingMode.HALF_UP);
                    priceType = "减50";
                    // 确保价格不小于0
                    if (childPrice.compareTo(BigDecimal.ZERO) < 0) {
                        childPrice = BigDecimal.ZERO;
                    }
                } else {
                    // 8岁及以上成人价
                    childPrice = discountedPrice;
                    priceType = "成人价";
                }
                
                childrenTotalPrice = childrenTotalPrice.add(childPrice);
                
                // 添加到儿童价格列表
                Map<String, Object> childPriceInfo = new HashMap<>();
                childPriceInfo.put("age", age);
                childPriceInfo.put("price", childPrice);
                childPriceInfo.put("priceType", priceType);
                childPrices.add(childPriceInfo);
            }
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("totalPrice", totalPrice);
            data.put("basePrice", basePrice);
            data.put("extraRoomFee", extraRoomFee);
            data.put("nonAgentPrice", nonAgentPrice);
            data.put("originalPrice", originalPrice);
            data.put("discountedPrice", discountedPrice);
            data.put("discountRate", discountRate);
            data.put("adultCount", adultCount);
            data.put("childCount", childCount);
            data.put("adultTotalPrice", discountedPrice.multiply(BigDecimal.valueOf(adultCount)));
            data.put("childrenTotalPrice", childrenTotalPrice);
            data.put("childPrices", childPrices);
            data.put("childrenAges", validChildrenAges);
            data.put("baseHotelLevel", baseHotelLevel);
            data.put("hotelPriceDifference", hotelPriceDiff);
            data.put("dailySingleRoomSupplement", singleRoomSupplement);
            data.put("hotelRoomPrice", hotelRoomPrice);
            data.put("roomCount", roomCount);
            data.put("roomType", roomType); // 添加房型信息
            data.put("hotelNights", nights);
            data.put("theoreticalRoomCount", theoreticalRoomCount);
            data.put("extraRooms", extraRooms);
            data.put("needsSingleRoomSupplement", needsSingleRoomSupplement);
            data.put("tripleBedRoomPriceDifference", tripleDifference); // 添加三人房差价信息
            
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
                PriceDetailVO priceDetail = tourBookingService.calculatePriceDetail(
                    booking.getTourId(), 
                    booking.getTourType(), 
                    agentId, 
                    booking.getAdultCount(), 
                    booking.getChildCount(), 
                    booking.getHotelLevel(), 
                    booking.getHotelRoomCount(),
                    userId
                );
                actualAmount = priceDetail.getActualPaymentPrice();
                log.info("操作员支付验证，重新计算的实际价格: {}", actualAmount);
            } else {
                // 其他用户：重新计算价格
                PriceDetailVO priceDetail = tourBookingService.calculatePriceDetail(
                    booking.getTourId(), 
                    booking.getTourType(), 
                    agentId, 
                    booking.getAdultCount(), 
                    booking.getChildCount(), 
                    booking.getHotelLevel(), 
                    booking.getHotelRoomCount(),
                    userId
                );
                actualAmount = priceDetail.getTotalPrice();
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
                PriceDetailVO priceDetail = tourBookingService.calculatePriceDetail(
                    booking.getTourId(), 
                    booking.getTourType(), 
                    agentId, 
                    booking.getAdultCount(), 
                    booking.getChildCount(), 
                    booking.getHotelLevel(), 
                    booking.getHotelRoomCount(),
                    userId
                );
                actualAmount = priceDetail.getActualPaymentPrice();
                log.info("操作员订单号支付验证，重新计算的实际价格: {}", actualAmount);
            } else {
                // 其他用户：重新计算价格
                PriceDetailVO priceDetail = tourBookingService.calculatePriceDetail(
                    booking.getTourId(), 
                    booking.getTourType(), 
                    agentId, 
                    booking.getAdultCount(), 
                    booking.getChildCount(), 
                    booking.getHotelLevel(), 
                    booking.getHotelRoomCount(),
                    userId
                );
                actualAmount = priceDetail.getTotalPrice();
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
     * 计算订单价格（带房型参数）
     */
    @GetMapping("/calculatePriceWithRoomType")
    @ApiOperation("计算订单价格（带房型参数）")
    public Result<Map<String, Object>> calculatePriceWithRoomType(
            @RequestParam Integer tourId,
            @RequestParam String tourType,
            @RequestParam(required = false) Integer adultCount,
            @RequestParam(required = false) Integer childCount,
            @RequestParam(required = false) String hotelLevel,
            @RequestParam(required = false) Integer roomCount,
            @RequestParam(required = false) String childrenAges,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) String roomType) {
        
        // 默认值设置
        if (adultCount == null || adultCount < 0) adultCount = 1;
        if (childCount == null || childCount < 0) childCount = 0;
        if (hotelLevel == null || hotelLevel.isEmpty()) hotelLevel = "4星";
        if (roomCount == null || roomCount <= 0) roomCount = 1;
        
        // 获取当前登录用户ID，如果未登录则为null
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            userId = 0L; // 提供默认值以防止空指针异常
        }
        
        log.info("计算旅游订单价格(带房型)，tourId: {}, tourType: {}, adultCount: {}, childCount: {}, hotelLevel: {}, roomCount: {}, userId: {}, childrenAges: {}, agentId: {}, roomType: {}", 
                tourId, tourType, adultCount, childCount, hotelLevel, roomCount, userId, childrenAges, agentId, roomType);
        
        try {
            // 调用服务层方法获取价格明细
            PriceDetailVO priceDetail = tourBookingService.calculatePriceDetail(
                    tourId, tourType, agentId, adultCount, childCount, hotelLevel, roomCount, userId, roomType);
            
            // 提取酒店相关信息
            String baseHotelLevel = hotelPriceService.getBaseHotelLevel();
            BigDecimal hotelPriceDiff = hotelPriceService.getPriceDifferenceByLevel(hotelLevel);
            BigDecimal singleRoomSupplement = hotelPriceService.getDailySingleRoomSupplementByLevel(hotelLevel);
            
            // 根据房型获取相应的房间价格
            BigDecimal hotelRoomPrice;
            if (roomType != null && (roomType.contains("双床") || roomType.equalsIgnoreCase("twin") || roomType.equalsIgnoreCase("double"))) {
                hotelRoomPrice = hotelPriceService.getHotelRoomPriceByLevel(hotelLevel);
            } else if (roomType != null && (roomType.contains("三床") || roomType.contains("家庭") || roomType.equalsIgnoreCase("triple") || roomType.equalsIgnoreCase("family"))) {
                BigDecimal roomBasePrice2 = hotelPriceService.getHotelRoomPriceByLevel(hotelLevel);
                BigDecimal tripleDifference2 = hotelPriceService.getTripleBedRoomPriceDifferenceByLevel(hotelLevel);
                hotelRoomPrice = roomBasePrice2.add(tripleDifference2);
            } else {
                hotelRoomPrice = hotelPriceService.getHotelRoomPriceByLevel(hotelLevel);
            }
            
            // 解析儿童年龄
            Integer[] validChildrenAges = null;
            try {
                if (childrenAges != null && !childrenAges.isEmpty()) {
                    // 处理可能的格式，移除括号并分割
                    String cleanAges = childrenAges.replaceAll("[\\[\\]\\s]", "");
                    String[] ageStrings = cleanAges.split(",");
                    validChildrenAges = new Integer[ageStrings.length];
                    
                    for (int i = 0; i < ageStrings.length; i++) {
                        try {
                            validChildrenAges[i] = Integer.parseInt(ageStrings[i].trim());
                        } catch (NumberFormatException e) {
                            validChildrenAges[i] = 7; // 默认为7岁
                        }
                    }
                } else {
                    // 如果未提供年龄，默认所有儿童为7岁
                    validChildrenAges = new Integer[childCount];
                    for (int i = 0; i < childCount; i++) {
                        validChildrenAges[i] = 7;
                    }
                }
            } catch (Exception e) {
                log.error("解析儿童年龄失败: {}", e.getMessage());
                validChildrenAges = new Integer[childCount];
                for (int i = 0; i < childCount; i++) {
                    validChildrenAges[i] = 7; // 默认为7岁
                }
            }
            
            // 组装返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("totalPrice", priceDetail.getTotalPrice());
            data.put("basePrice", priceDetail.getBasePrice());
            data.put("extraRoomFee", priceDetail.getExtraRoomFee());
            data.put("nonAgentPrice", priceDetail.getNonAgentPrice());
            data.put("baseHotelLevel", baseHotelLevel);
            data.put("hotelPriceDifference", hotelPriceDiff);
            data.put("dailySingleRoomSupplement", singleRoomSupplement);
            data.put("hotelRoomPrice", hotelRoomPrice);
            data.put("roomCount", roomCount);
            data.put("roomType", roomType);
            data.put("adultCount", adultCount);
            data.put("childCount", childCount);
            data.put("childrenAges", validChildrenAges);
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("计算价格失败: {}", e.getMessage(), e);
            return Result.error("计算价格失败: " + e.getMessage());
        }
        }
} 