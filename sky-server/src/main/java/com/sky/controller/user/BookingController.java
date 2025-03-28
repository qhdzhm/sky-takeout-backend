package com.sky.controller.user;

import com.sky.dto.BookingDTO;
import com.sky.result.Result;
import com.sky.service.BookingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<BookingDTO> getBookingById(@PathVariable Integer id) {
        log.info("获取预订详情，ID：{}", id);
        BookingDTO bookingDTO = bookingService.getBookingById(id);
        return Result.success(bookingDTO);
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
} 