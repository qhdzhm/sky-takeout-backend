package com.sky.controller;

import com.sky.context.BaseContext;
import com.sky.dto.BookingDTO;
import com.sky.dto.OrderPageQueryDTO;
import com.sky.dto.OrderUpdateDTO;
import com.sky.dto.PaymentDTO;
import com.sky.dto.TourBookingUpdateDTO;
import com.sky.entity.TourBooking;
import com.sky.entity.User;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.TourBookingService;
import com.sky.vo.OrderVO;
import com.sky.vo.PageResultVO;
import com.sky.vo.TourBookingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

/**
 * 订单相关接口
 */
@RestController
@RequestMapping("/orders")
@Api(tags = "订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private TourBookingService tourBookingService;

    /**
     * 分页查询订单
     * @param orderPageQueryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    @ApiOperation("分页查询订单")
    public Result<PageResultVO<OrderVO>> pageQuery(OrderPageQueryDTO orderPageQueryDTO) {
        log.info("分页查询订单，参数：{}", orderPageQueryDTO);
        
        // 从线程上下文中获取当前登录用户信息
        Long userId = BaseContext.getCurrentId();
        String userType = BaseContext.getCurrentUserType();
        
        // 🔍 详细调试日志
        log.info("🔍 订单查询调试信息:");
        log.info("  - 用户ID: {}", userId);
        log.info("  - 用户类型: '{}'", userType);
        log.info("  - 用户类型是否为null: {}", userType == null);
        log.info("  - 用户类型长度: {}", userType != null ? userType.length() : "null");
        if (userType != null) {
            log.info("  - 用户类型字节: {}", java.util.Arrays.toString(userType.getBytes()));
        }
        
        // 根据用户类型设置对应的查询条件
        if ("agent".equals(userType)) {
            // 代理商主账号：能查询自己直接下的订单 + 所有操作员代理下的订单
            // 只需要设置agentId，不设置operatorId，这样可以查询到该代理商下的所有订单
            orderPageQueryDTO.setAgentId(userId.intValue());
            log.info("代理商主账号查询订单，代理商ID: {}", userId);
        } else if ("agent_operator".equals(userType)) {
            // 操作员：只能查询自己下的订单
            Long agentId = BaseContext.getCurrentAgentId();
            Long operatorId = BaseContext.getCurrentOperatorId();
            
            // 设置代理商ID以确保安全性（操作员只能查询所属代理商的订单）
            if (agentId != null) {
                orderPageQueryDTO.setAgentId(agentId.intValue());
            }
            
            // 设置操作员ID过滤条件（只查询该操作员自己下的订单）
            if (operatorId != null) {
                orderPageQueryDTO.setOperatorId(operatorId);
            }
            
            log.info("操作员查询订单，代理商ID: {}, 操作员ID: {}", agentId, operatorId);
        } else {
            // 普通用户只能查询自己的订单
            if (userId != null) {
                orderPageQueryDTO.setUserId(userId.intValue());
                log.info("✅ 普通用户查询订单，设置用户ID: {}", userId);
            } else {
                log.error("❌ 普通用户查询订单，但用户ID为null");
                return Result.error("用户身份验证失败");
            }
            log.info("普通用户查询订单，用户ID: {}", userId);
        }
        
        // 🔍 查询前的参数确认
        log.info("🔍 最终查询参数:");
        log.info("  - userId: {}", orderPageQueryDTO.getUserId());
        log.info("  - agentId: {}", orderPageQueryDTO.getAgentId());
        log.info("  - operatorId: {}", orderPageQueryDTO.getOperatorId());
        
        PageResultVO<OrderVO> pageResult = orderService.pageQuery(orderPageQueryDTO);
        log.info("🔍 查询结果: 总记录数={}", pageResult != null ? pageResult.getTotal() : "null");
        
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询订单详情
     * @param bookingId 订单ID
     * @return 订单详情
     */
    @GetMapping("/{bookingId}")
    @ApiOperation("根据ID查询订单详情")
    public Result<OrderVO> getById(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId) {
        log.info("根据ID查询订单详情，订单ID：{}", bookingId);
        
        // 从线程上下文中获取当前登录用户信息
        Long userId = BaseContext.getCurrentId();
        String userType = BaseContext.getCurrentUserType();
        
        // 查询订单详情
        OrderVO orderVO = orderService.getById(bookingId);
        
        // 验证订单所属权限
        if (orderVO != null) {
            if ("agent".equals(userType)) {
                // 代理商主账号：能查看自己的订单 + 所有操作员代理下的订单
                if (orderVO.getAgentId() == null || !userId.equals(Long.valueOf(orderVO.getAgentId()))) {
                    log.warn("代理商 {} 尝试查看非自己的订单 {} (订单代理商ID: {})", userId, bookingId, orderVO.getAgentId());
                    return Result.error("无权限查看此订单");
                }
                log.info("代理商主账号查看订单详情，订单ID: {}, 订单代理商ID: {}, 操作员ID: {}", 
                        bookingId, orderVO.getAgentId(), orderVO.getOperatorId());
            } else if ("agent_operator".equals(userType)) {
                // 操作员：只能查看自己下的订单
                Long operatorId = BaseContext.getCurrentOperatorId();
                Long agentId = BaseContext.getCurrentAgentId();
                
                // 验证订单是否属于该操作员且属于正确的代理商
                boolean hasPermission = (operatorId != null && operatorId.equals(orderVO.getOperatorId())) &&
                                       (agentId != null && orderVO.getAgentId() != null && agentId.equals(Long.valueOf(orderVO.getAgentId())));
                
                if (!hasPermission) {
                    log.warn("操作员 {} (代理商: {}) 尝试查看非自己的订单 {} (订单代理商ID: {}, 订单操作员ID: {})", 
                            operatorId, agentId, bookingId, orderVO.getAgentId(), orderVO.getOperatorId());
                    return Result.error("无权限查看此订单");
                }
                log.info("操作员查看订单详情，订单ID: {}, 操作员ID: {}, 代理商ID: {}", 
                        bookingId, operatorId, agentId);
            } else {
                // 普通用户只能查看自己的订单
                if (orderVO.getUserId() == null || !userId.equals(Long.valueOf(orderVO.getUserId()))) {
                    log.warn("普通用户 {} 尝试查看非自己的订单 {} (订单用户ID: {})", userId, bookingId, orderVO.getUserId());
                    return Result.error("无权限查看此订单");
                }
                log.info("普通用户查看订单详情，订单ID: {}, 用户ID: {}", bookingId, userId);
            }
        }
        
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     * @param bookingId 订单ID
     * @return 操作结果
     */
    @PostMapping("/{bookingId}/cancel")
    @ApiOperation("取消订单")
    public Result<String> cancelOrder(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId) {
        log.info("取消订单，订单ID：{}", bookingId);
        
        // 从线程上下文中获取当前登录用户信息
        Long userId = BaseContext.getCurrentId();
        String userType = BaseContext.getCurrentUserType();
        
        // 查询订单详情，验证权限
        OrderVO orderVO = orderService.getById(bookingId);
        
        // 验证订单所属权限
        if (orderVO == null) {
            return Result.error("订单不存在");
        }
        
        boolean hasPermission = false;
        if ("agent".equals(userType)) {
            // 代理商主账号：能取消自己的订单 + 所有操作员代理下的订单
            hasPermission = userId.equals(Long.valueOf(orderVO.getAgentId()));
            log.info("代理商主账号取消订单，订单ID: {}, 代理商ID: {}, 权限验证: {}", 
                    bookingId, orderVO.getAgentId(), hasPermission);
        } else if ("agent_operator".equals(userType)) {
            // 操作员：只能取消自己下的订单
            Long operatorId = BaseContext.getCurrentOperatorId();
            Long agentId = BaseContext.getCurrentAgentId();
            
            hasPermission = (operatorId != null && operatorId.equals(orderVO.getOperatorId())) &&
                           (agentId != null && agentId.equals(Long.valueOf(orderVO.getAgentId())));
            
            log.info("操作员取消订单，订单ID: {}, 操作员ID: {}, 代理商ID: {}, 权限验证: {}", 
                    bookingId, operatorId, agentId, hasPermission);
        } else {
            // 普通用户只能取消自己的订单
            hasPermission = userId.equals(Long.valueOf(orderVO.getUserId()));
            log.info("普通用户取消订单，订单ID: {}, 用户ID: {}, 权限验证: {}", 
                    bookingId, userId, hasPermission);
        }
        
        if (!hasPermission) {
            return Result.error("无权限取消此订单");
        }
        
        // 只有未支付的订单可以取消
        if (!"unpaid".equals(orderVO.getPaymentStatus())) {
            return Result.error("只有未支付的订单可以取消");
        }
        
        // 调用服务取消订单
        Boolean success = tourBookingService.cancel(bookingId);
        
        if (success) {
            return Result.success("订单已取消");
        } else {
            return Result.error("订单取消失败");
        }
    }

    /**
     * 根据订单号查询订单详情
     * @param orderNumber 订单号
     * @return 订单详情
     */
    @GetMapping("/order-numbers/{orderNumber}")
    @ApiOperation("根据订单号查询订单详情")
    public Result<OrderVO> getByOrderNumber(
            @ApiParam(name = "orderNumber", value = "订单号", required = true)
            @PathVariable String orderNumber) {
        log.info("根据订单号查询订单详情，订单号：{}", orderNumber);
        
        try {
            // 查询订单详情
            TourBooking tourBooking = orderService.getByOrderNumber(orderNumber);
            
            if (tourBooking == null) {
                return Result.error("订单不存在");
            }
            
            // 转换为OrderVO对象
            OrderVO orderVO = convertToOrderVO(tourBooking);
            
            // 从线程上下文中获取当前登录用户信息
            Long userId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            
            // 验证订单所属权限
            boolean hasPermission = false;
            if ("agent".equals(userType)) {
                // 代理商主账号：能查看自己的订单 + 所有操作员代理下的订单
                hasPermission = userId.equals(tourBooking.getAgentId());
                log.info("代理商主账号根据订单号查看订单，订单号: {}, 代理商ID: {}, 权限验证: {}", 
                        orderNumber, tourBooking.getAgentId(), hasPermission);
            } else if ("agent_operator".equals(userType)) {
                // 操作员：只能查看自己下的订单
                Long operatorId = BaseContext.getCurrentOperatorId();
                Long agentId = BaseContext.getCurrentAgentId();
                
                hasPermission = (operatorId != null && operatorId.equals(tourBooking.getOperatorId())) &&
                               (agentId != null && agentId.equals(tourBooking.getAgentId()));
                
                log.info("操作员根据订单号查看订单，订单号: {}, 操作员ID: {}, 代理商ID: {}, 权限验证: {}", 
                        orderNumber, operatorId, agentId, hasPermission);
            } else {
                // 普通用户只能查看自己的订单
                hasPermission = userId.equals(tourBooking.getUserId());
                log.info("普通用户根据订单号查看订单，订单号: {}, 用户ID: {}, 权限验证: {}", 
                        orderNumber, userId, hasPermission);
            }
            
            if (!hasPermission) {
                return Result.error("无权限查看此订单");
            }
            
            return Result.success(orderVO);
        } catch (Exception e) {
            log.error("查询订单详情失败", e);
            return Result.error("查询订单详情失败：" + e.getMessage());
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
    public Result<Boolean> payOrderByNumber(
            @ApiParam(name = "orderNumber", value = "订单号", required = true)
            @PathVariable String orderNumber,
            @RequestBody PaymentDTO paymentDTO) {
        log.info("根据订单号支付订单，订单号：{}, 支付数据: {}", orderNumber, paymentDTO);

        try {
            // 查询订单
            TourBooking tourBooking = orderService.getByOrderNumber(orderNumber);
            
            if (tourBooking == null) {
                return Result.error("订单不存在");
            }
            
            // 从线程上下文中获取当前登录用户信息
            Long userId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            
            // 验证订单所属权限
            boolean hasPermission = false;
            if ("agent".equals(userType)) {
                // 代理商只能支付自己的订单
                hasPermission = userId.equals(tourBooking.getAgentId());
            } else {
                // 普通用户只能支付自己的订单
                hasPermission = userId.equals(tourBooking.getUserId());
            }
            
            if (!hasPermission) {
                return Result.error("无权限支付此订单");
            }
            
            // 只有未支付的订单可以支付
            if (!"unpaid".equals(tourBooking.getPaymentStatus())) {
                return Result.error("订单已支付，无需重复支付");
            }
            
            // 设置支付参数
            Integer bookingId = tourBooking.getBookingId();
            
            // 在paymentDTO中设置bookingId
            if (paymentDTO.getBookingId() == null) {
                paymentDTO.setBookingId(bookingId);
            }
            
            // 调用支付服务
            Boolean result = orderService.payOrder(bookingId, paymentDTO);
            
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
     * 根据ID支付订单
     * @param bookingId 订单ID
     * @param paymentDTO 支付数据
     * @return 支付结果
     */
    @PostMapping("/{bookingId}/pay")
    @ApiOperation("根据ID支付订单")
    public Result<Boolean> payOrder(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId,
            @RequestBody PaymentDTO paymentDTO) {
        log.info("根据ID支付订单，订单ID：{}, 支付数据: {}", bookingId, paymentDTO);

        try {
            // 查询订单详情
            OrderVO orderVO = orderService.getById(bookingId);
            
            if (orderVO == null) {
                return Result.error("订单不存在");
            }
            
            // 从线程上下文中获取当前登录用户信息
            Long userId = BaseContext.getCurrentId();
            String userType = BaseContext.getCurrentUserType();
            
            // 验证订单所属权限
            boolean hasPermission = false;
            if ("agent".equals(userType)) {
                // 代理商只能支付自己的订单
                hasPermission = userId.equals(Long.valueOf(orderVO.getAgentId()));
            } else {
                // 普通用户只能支付自己的订单
                hasPermission = userId.equals(Long.valueOf(orderVO.getUserId()));
            }
            
            if (!hasPermission) {
                return Result.error("无权限支付此订单");
            }
            
            // 只有未支付的订单可以支付
            if (!"unpaid".equals(orderVO.getPaymentStatus())) {
                return Result.error("订单已支付，无需重复支付");
            }
            
            // 在paymentDTO中设置bookingId
            if (paymentDTO.getBookingId() == null) {
                paymentDTO.setBookingId(bookingId);
            }
            
            // 调用支付服务
            Boolean result = orderService.payOrder(bookingId, paymentDTO);
            
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
     * 代理商修改订单
     * @param updateDTO 订单更新数据
     * @return 操作结果
     */
    @PutMapping("/agent/update")
    @ApiOperation("代理商修改订单")
    public Result<String> updateBookingByAgent(@RequestBody TourBookingUpdateDTO updateDTO) {
        log.info("代理商修改订单，订单ID：{}，更新数据：{}", updateDTO.getBookingId(), updateDTO);
        
        // 从线程上下文中获取当前登录用户信息
        Long userId = BaseContext.getCurrentId();
        String userType = BaseContext.getCurrentUserType();
        
        // 只有代理商可以使用此接口
        if (!"agent".equals(userType)) {
            return Result.error("只有代理商可以使用此接口");
        }
        
        // 查询订单详情
        OrderVO orderVO = orderService.getById(updateDTO.getBookingId());
        
        if (orderVO == null) {
            return Result.error("订单不存在");
        }
        
        // 验证订单所属权限
        if (!userId.equals(Long.valueOf(orderVO.getAgentId()))) {
            return Result.error("无权限修改此订单");
        }
        
        // 只有未完成和未取消的订单才可以修改
        if ("completed".equals(orderVO.getStatus()) || "cancelled".equals(orderVO.getStatus())) {
            return Result.error("已完成或已取消的订单无法修改");
        }
        
        try {
            // 调用服务更新订单
            Boolean success = tourBookingService.updateBookingDetails(updateDTO);
            
            if (success) {
                return Result.success("订单修改成功");
            } else {
                return Result.error("订单修改失败");
            }
        } catch (Exception e) {
            log.error("修改订单失败", e);
            return Result.error("修改订单失败：" + e.getMessage());
        }
    }

    /**
     * 修改订单（通用接口）
     * @param bookingId 订单ID
     * @param updateDTO 订单更新数据
     * @return 操作结果
     */
    @PutMapping("/{bookingId}")
    @ApiOperation("修改订单")
    public Result<String> updateBooking(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId,
            @RequestBody TourBookingUpdateDTO updateDTO) {
        log.info("修改订单，订单ID：{}，更新数据：{}", bookingId, updateDTO);
        
        // 设置订单ID
        updateDTO.setBookingId(bookingId);
        
        // 从线程上下文中获取当前登录用户信息
        Long userId = BaseContext.getCurrentId();
        String userType = BaseContext.getCurrentUserType();
        
        // 查询订单详情
        OrderVO orderVO = orderService.getById(bookingId);
        
        if (orderVO == null) {
            return Result.error("订单不存在");
        }
        
        // 验证订单所属权限
        boolean hasPermission = false;
        if ("agent".equals(userType)) {
            // 代理商只能修改自己的订单
            hasPermission = userId.equals(Long.valueOf(orderVO.getAgentId()));
        } else if ("agent_operator".equals(userType)) {
            // 操作员只能修改自己下的订单
            Long operatorId = BaseContext.getCurrentOperatorId();
            hasPermission = operatorId != null && operatorId.equals(orderVO.getOperatorId());
        } else {
            // 普通用户只能修改自己的订单
            hasPermission = userId.equals(Long.valueOf(orderVO.getUserId()));
        }
        
        if (!hasPermission) {
            return Result.error("无权限修改此订单");
        }
        
        // 只有未支付且未取消的订单才可以修改
        if (!"unpaid".equals(orderVO.getPaymentStatus()) || "cancelled".equals(orderVO.getStatus())) {
            return Result.error("只有未支付且未取消的订单可以修改");
        }
        
        try {
            // 调用服务更新订单
            Boolean success = tourBookingService.updateBookingDetails(updateDTO);
            
            if (success) {
                return Result.success("订单修改成功");
            } else {
                return Result.error("订单修改失败");
            }
        } catch (Exception e) {
            log.error("修改订单失败", e);
            return Result.error("修改订单失败：" + e.getMessage());
        }
    }

    /**
     * 将TourBooking对象转换为OrderVO对象
     * @param tourBooking 订单实体
     * @return 订单VO对象
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

    /**
     * 手动同步订单到排团表（测试用）
     * @param bookingId 订单ID
     * @return 操作结果
     */
    @PostMapping("/{bookingId}/sync-to-schedule")
    @ApiOperation("手动同步订单到排团表")
    public Result<String> syncOrderToSchedule(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId) {
        log.info("手动同步订单到排团表，订单ID：{}", bookingId);
        
        try {
            tourBookingService.autoSyncOrderToScheduleTable(bookingId);
            return Result.success("同步成功");
        } catch (Exception e) {
            log.error("手动同步订单到排团表失败：{}", e.getMessage(), e);
            return Result.error("同步失败：" + e.getMessage());
        }
    }

    /**
     * 简化测试同步方法（调试用）
     * @param bookingId 订单ID
     * @return 操作结果
     */
    @PostMapping("/{bookingId}/test-sync")
    @ApiOperation("简化测试同步方法")
    public Result<String> testSyncOrderToSchedule(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId) {
        log.info("🧪 开始简化测试同步，订单ID：{}", bookingId);
        
        try {
            // 直接查询订单信息
            TourBookingVO booking = tourBookingService.getById(bookingId);
            if (booking == null) {
                return Result.error("订单不存在");
            }
            
            log.info("🧪 找到订单：{}, 类型：{}, 行程ID：{}", 
                booking.getOrderNumber(), booking.getTourType(), booking.getTourId());
            
            return Result.success("测试完成，订单信息：" + booking.getOrderNumber());
        } catch (Exception e) {
            log.error("🧪 简化测试失败：{}", e.getMessage(), e);
            return Result.error("测试失败：" + e.getMessage());
        }
    }

}
