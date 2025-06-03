package com.sky.controller.admin;

import com.sky.dto.OrderPageQueryDTO;
import com.sky.dto.OrderUpdateDTO;
import com.sky.dto.PassengerDTO;
import com.sky.dto.TourBookingDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.PassengerService;
import com.sky.service.TourBookingService;
import com.sky.service.GroupTourService;
import com.sky.service.DayTourService;
import com.sky.vo.OrderVO;
import com.sky.vo.PageResultVO;
import com.sky.vo.TourBookingVO;
import com.sky.vo.PassengerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理系统-订单管理接口
 */
@RestController
@RequestMapping("/admin/orders")
@Api(tags = "后台管理系统-订单管理接口")
@Slf4j
public class AdminOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private TourBookingService tourBookingService;
    
    @Autowired
    private PassengerService passengerService;
    
    @Autowired
    private GroupTourService groupTourService;
    
    @Autowired
    private DayTourService dayTourService;

    /**
     * 分页查询订单
     * @param orderPageQueryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/list")
    @ApiOperation("分页查询订单")
    public Result<PageResultVO<OrderVO>> pageQuery(OrderPageQueryDTO orderPageQueryDTO) {
        log.info("分页查询订单，参数：{}", orderPageQueryDTO);
        
        // 管理员查询所有订单，无需设置用户和代理商ID过滤
        // 查询条件中的userId和agentId如果前端传递了，则使用前端传递的值进行过滤
        
        PageResultVO<OrderVO> pageResult = orderService.pageQuery(orderPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 按用户类型和ID查询订单
     * @param userType 用户类型 (user-普通用户, agent-代理商)
     * @param userId 用户ID或代理商ID
     * @param orderPageQueryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/user/{userType}/{userId}")
    @ApiOperation("按用户类型和ID查询订单")
    public Result<PageResultVO<OrderVO>> queryByUserType(
            @ApiParam(name = "userType", value = "用户类型", required = true, allowableValues = "user,agent")
            @PathVariable String userType,
            @ApiParam(name = "userId", value = "用户ID或代理商ID", required = true)
            @PathVariable Integer userId,
            OrderPageQueryDTO orderPageQueryDTO) {
        log.info("按用户类型和ID查询订单，用户类型：{}，用户ID：{}，参数：{}", userType, userId, orderPageQueryDTO);
        
        // 根据用户类型设置对应的查询条件
        if ("agent".equals(userType)) {
            // 代理商订单
            orderPageQueryDTO.setAgentId(userId);
            orderPageQueryDTO.setUserId(null); // 确保不会同时过滤用户ID
        } else {
            // 普通用户订单
            orderPageQueryDTO.setUserId(userId);
            orderPageQueryDTO.setAgentId(null); // 确保不会同时过滤代理商ID
        }
        
        PageResultVO<OrderVO> pageResult = orderService.pageQuery(orderPageQueryDTO);
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
        
        // 管理员有权限查看所有订单，无需验证权限
        OrderVO orderVO = orderService.getById(bookingId);
        
        if (orderVO == null) {
            return Result.error("订单不存在");
        }
        
        return Result.success(orderVO);
    }

    /**
     * 根据订单号查询订单详情
     * @param orderNumber 订单号
     * @return 订单详情
     */
    @GetMapping("/number/{orderNumber}")
    @ApiOperation("根据订单号查询订单详情")
    public Result<TourBookingVO> getByOrderNumber(
            @ApiParam(name = "orderNumber", value = "订单号", required = true)
            @PathVariable String orderNumber) {
        log.info("根据订单号查询订单详情，订单号：{}", orderNumber);
        
        TourBookingVO tourBookingVO = tourBookingService.getByOrderNumber(orderNumber);
        
        if (tourBookingVO == null) {
            return Result.error("订单不存在");
        }
        
        return Result.success(tourBookingVO);
    }

    /**
     * 更新订单信息
     * @param bookingId 订单ID
     * @param tourBookingDTO 订单信息
     * @return 操作结果
     */
    @PutMapping("/{bookingId}")
    @ApiOperation("更新订单信息")
    public Result<String> updateOrder(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId,
            @RequestBody TourBookingDTO tourBookingDTO) {
        log.info("更新订单信息，订单ID：{}，订单信息：{}", bookingId, tourBookingDTO);
        
        // 设置订单ID
        tourBookingDTO.setBookingId(bookingId);
        
        // 调用服务更新订单
        Boolean success = tourBookingService.update(tourBookingDTO);
        
        if (success) {
            return Result.success("订单更新成功");
        } else {
            return Result.error("订单更新失败");
        }
    }

    /**
     * 确认订单
     * @param bookingId 订单ID
     * @return 操作结果
     */
    @PutMapping("/{bookingId}/confirm")
    @ApiOperation("确认订单")
    public Result<String> confirmOrder(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId) {
        log.info("确认订单，订单ID：{}", bookingId);
        
        // 调用服务确认订单
        Boolean success = tourBookingService.confirm(bookingId);
        
        if (success) {
            return Result.success("订单已确认");
        } else {
            return Result.error("订单确认失败");
        }
    }

    /**
     * 取消订单
     * @param bookingId 订单ID
     * @return 操作结果
     */
    @PutMapping("/{bookingId}/cancel")
    @ApiOperation("取消订单")
    public Result<String> cancelOrder(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId) {
        log.info("取消订单，订单ID：{}", bookingId);
        
        // 调用服务取消订单
        Boolean success = tourBookingService.cancel(bookingId);
        
        if (success) {
            return Result.success("订单已取消");
        } else {
            return Result.error("订单取消失败");
        }
    }

    /**
     * 完成订单
     * @param bookingId 订单ID
     * @return 操作结果
     */
    @PutMapping("/{bookingId}/complete")
    @ApiOperation("完成订单")
    public Result<String> completeOrder(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId) {
        log.info("完成订单，订单ID：{}", bookingId);
        
        // 调用服务完成订单
        Boolean success = tourBookingService.complete(bookingId);
        
        if (success) {
            return Result.success("订单已完成");
        } else {
            return Result.error("订单完成失败");
        }
    }

    /**
     * 更新订单状态
     * @param bookingId 订单ID
     * @param orderUpdateDTO 订单更新信息
     * @return 操作结果
     */
    @PutMapping("/{bookingId}/status")
    @ApiOperation("更新订单状态")
    public Result<String> updateOrderStatus(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId,
            @RequestBody OrderUpdateDTO orderUpdateDTO) {
        log.info("更新订单状态，订单ID：{}，状态：{}", bookingId, orderUpdateDTO);
        
        // 调用订单服务直接更新订单和乘客信息
        Boolean success = orderService.updateOrderStatus(bookingId, orderUpdateDTO);
        
        if (success) {
            return Result.success("订单信息已更新");
        } else {
            return Result.error("订单信息更新失败");
        }
    }

    /**
     * 获取订单的乘客信息
     * @param bookingId 订单ID
     * @return 乘客信息列表
     */
    @GetMapping("/{bookingId}/passengers")
    @ApiOperation("获取订单的乘客信息")
    public Result<List<PassengerVO>> getOrderPassengers(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId) {
        log.info("获取订单的乘客信息，订单ID：{}", bookingId);
        
        // 调用服务获取乘客列表
        List<PassengerVO> passengers = passengerService.getByBookingId(bookingId);
        
        return Result.success(passengers);
    }

    /**
     * 更新订单的乘客信息
     * @param bookingId 订单ID
     * @param passengers 乘客信息列表
     * @return 操作结果
     */
    @PutMapping("/{bookingId}/passengers")
    @ApiOperation("更新订单的乘客信息")
    public Result<String> updateOrderPassengers(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId,
            @RequestBody List<PassengerDTO> passengers) {
        log.info("更新订单的乘客信息，订单ID：{}，乘客数量：{}", bookingId, passengers.size());
        
        // 1. 首先获取订单当前的所有乘客
        List<PassengerVO> currentPassengers = passengerService.getByBookingId(bookingId);
        log.info("订单当前乘客数量：{}", currentPassengers.size());
        
        // 收集当前乘客ID列表
        List<Integer> currentPassengerIds = currentPassengers.stream()
                .map(PassengerVO::getPassengerId)
                .collect(java.util.stream.Collectors.toList());
        
        // 收集新乘客列表中的ID
        List<Integer> newPassengerIds = passengers.stream()
                .filter(p -> p.getPassengerId() != null)
                .map(PassengerDTO::getPassengerId)
                .collect(java.util.stream.Collectors.toList());
        
        log.info("当前乘客ID: {}", currentPassengerIds);
        log.info("新乘客列表中的ID: {}", newPassengerIds);
        
        // 找出需要删除的乘客ID (在当前列表中但不在新列表中的)
        List<Integer> idsToRemove = new ArrayList<>(currentPassengerIds);
        idsToRemove.removeAll(newPassengerIds);
        
        log.info("需要删除的乘客ID: {}", idsToRemove);
        
        // 2. 删除不在新列表中的乘客
        boolean deleteSuccess = true;
        for (Integer passengerId : idsToRemove) {
            Boolean removed = passengerService.removePassengerFromBooking(bookingId, passengerId);
            if (!removed) {
                deleteSuccess = false;
                log.error("从订单{}中删除乘客{}失败", bookingId, passengerId);
            } else {
                log.info("成功从订单{}中删除乘客{}", bookingId, passengerId);
            }
        }
        
        // 3. 处理新列表中的乘客
        boolean updateSuccess = true;
        for (PassengerDTO passengerDTO : passengers) {
            try {
                log.info("处理乘客DTO: {}", passengerDTO);
                
                if (passengerDTO.getPassengerId() != null) {
                    // 更新现有乘客
                    log.info("更新现有乘客ID: {}", passengerDTO.getPassengerId());
                    Boolean updated = passengerService.updatePassengerBookingInfo(bookingId, passengerDTO);
                    if (!updated) {
                        updateSuccess = false;
                        log.error("更新订单{}的乘客{}信息失败", bookingId, passengerDTO.getPassengerId());
                    } else {
                        log.info("成功更新订单{}的乘客{}", bookingId, passengerDTO.getPassengerId());
                    }
                } else {
                    // 添加新乘客
                    log.info("添加新乘客: {}", passengerDTO.getFullName());
                    Boolean added = passengerService.addPassengerToBooking(bookingId, passengerDTO);
                    if (!added) {
                        updateSuccess = false;
                        log.error("添加乘客{}到订单{}失败", passengerDTO.getFullName(), bookingId);
                    } else {
                        log.info("成功添加乘客{}到订单{}", passengerDTO.getFullName(), bookingId);
                    }
                }
            } catch (Exception e) {
                log.error("处理乘客时发生异常: {}", e.getMessage(), e);
                updateSuccess = false;
            }
        }
        
        if (deleteSuccess && updateSuccess) {
            return Result.success("乘客信息更新成功");
        } else if (!deleteSuccess && !updateSuccess) {
            return Result.error("乘客删除和更新操作都失败");
        } else if (!deleteSuccess) {
            return Result.error("部分乘客删除失败");
        } else {
            return Result.error("部分乘客信息更新失败");
        }
    }

    /**
     * 获取订单相关旅游产品的行程安排
     * @param bookingId 订单ID
     * @return 行程安排列表
     */
    @GetMapping("/{bookingId}/itinerary")
    @ApiOperation("获取订单相关旅游产品的行程安排")
    public Result<List<Map<String, Object>>> getOrderItinerary(
            @ApiParam(name = "bookingId", value = "订单ID", required = true)
            @PathVariable Integer bookingId) {
        log.info("获取订单相关旅游产品的行程安排，订单ID：{}", bookingId);
        
        // 获取订单详情
        OrderVO orderVO = orderService.getById(bookingId);
        
        if (orderVO == null) {
            return Result.error("订单不存在");
        }
        
        // 获取订单关联的旅游产品ID和类型
        Integer tourId = orderVO.getTourId();
        String tourType = orderVO.getTourType();
        
        if (tourId == null || tourType == null) {
            return Result.error("订单未关联旅游产品");
        }
        
        List<Map<String, Object>> itinerary = new ArrayList<>();
        
        // 根据旅游产品类型获取行程安排
        if ("group_tour".equals(tourType)) {
            // 获取团队游行程
            itinerary = groupTourService.getGroupTourItinerary(tourId);
        } else if ("day_tour".equals(tourType)) {
            // 获取一日游行程
            itinerary = dayTourService.getDayTourItinerary(tourId);
        }
        
        // 如果没有找到行程，返回空列表
        if (itinerary == null) {
            itinerary = new ArrayList<>();
        }
        
        return Result.success(itinerary);
    }
} 