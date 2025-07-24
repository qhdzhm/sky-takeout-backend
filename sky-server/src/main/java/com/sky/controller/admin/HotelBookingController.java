package com.sky.controller.admin;

import com.sky.dto.HotelBookingDTO;
import com.sky.dto.HotelBookingEmailDTO;
import com.sky.entity.HotelBooking;
import com.sky.result.Result;
import com.sky.result.PageResult;
import com.sky.service.HotelBookingService;
import com.sky.vo.HotelBookingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 酒店预订管理Controller
 */
@RestController
@RequestMapping("/admin/hotel-bookings")
@Api(tags = "酒店预订管理接口")
@Slf4j
public class HotelBookingController {

    @Autowired
    private HotelBookingService hotelBookingService;

    /**
     * 创建酒店预订
     */
    @PostMapping
    @ApiOperation("创建酒店预订")
    public Result<Integer> createBooking(@RequestBody HotelBookingDTO hotelBookingDTO) {
        log.info("创建酒店预订：{}", hotelBookingDTO);
        Integer bookingId = hotelBookingService.createBooking(hotelBookingDTO);
        return Result.success(bookingId);
    }

    /**
     * 基于排团记录创建酒店预订
     */
    @PostMapping("/from-schedule")
    @ApiOperation("基于排团记录创建酒店预订")
    public Result<Integer> createBookingFromScheduleOrder(
            @ApiParam(name = "scheduleOrderId", value = "排团记录ID", required = true)
            @RequestParam Integer scheduleOrderId,
            @ApiParam(name = "hotelId", value = "酒店ID", required = true)
            @RequestParam Integer hotelId,
            @ApiParam(name = "roomTypeId", value = "房型ID", required = true)
            @RequestParam Integer roomTypeId) {
        log.info("基于排团记录创建酒店预订，排团记录ID：{}，酒店ID：{}，房型ID：{}", scheduleOrderId, hotelId, roomTypeId);
        Integer bookingId = hotelBookingService.createBookingFromScheduleOrder(scheduleOrderId, hotelId, roomTypeId);
        return Result.success(bookingId);
    }

    /**
     * 批量创建酒店预订（基于导游车辆分配）
     */
    @PostMapping("/batch-from-assignment")
    @ApiOperation("批量创建酒店预订（基于导游车辆分配）")
    public Result<Integer> batchCreateBookingsFromAssignment(
            @ApiParam(name = "assignmentId", value = "导游车辆分配ID", required = true)
            @RequestParam Integer assignmentId,
            @ApiParam(name = "hotelId", value = "酒店ID", required = true)
            @RequestParam Integer hotelId,
            @ApiParam(name = "roomTypeId", value = "房型ID", required = true)
            @RequestParam Integer roomTypeId) {
        log.info("批量创建酒店预订，分配ID：{}，酒店ID：{}，房型ID：{}", assignmentId, hotelId, roomTypeId);
        Integer createdCount = hotelBookingService.batchCreateBookingsFromAssignment(assignmentId, hotelId, roomTypeId);
        return Result.success(createdCount);
    }

    /**
     * 根据ID查询酒店预订详细信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询酒店预订详细信息")
    public Result<HotelBookingVO> getDetailById(@PathVariable Integer id) {
        log.info("根据ID查询酒店预订详细信息：{}", id);
        HotelBookingVO hotelBookingVO = hotelBookingService.getDetailById(id);
        return Result.success(hotelBookingVO);
    }

    /**
     * 根据预订参考号查询酒店预订
     */
    @GetMapping("/reference/{bookingReference}")
    @ApiOperation("根据预订参考号查询酒店预订")
    public Result<HotelBooking> getByBookingReference(@PathVariable String bookingReference) {
        log.info("根据预订参考号查询酒店预订：{}", bookingReference);
        HotelBooking hotelBooking = hotelBookingService.getByBookingReference(bookingReference);
        return Result.success(hotelBooking);
    }

    /**
     * 根据排团记录ID查询酒店预订
     */
    @GetMapping("/schedule-order/{scheduleOrderId}")
    @ApiOperation("根据排团记录ID查询酒店预订")
    public Result<HotelBooking> getByScheduleOrderId(@PathVariable Integer scheduleOrderId) {
        log.info("根据排团记录ID查询酒店预订：{}", scheduleOrderId);
        HotelBooking hotelBooking = hotelBookingService.getByScheduleOrderId(scheduleOrderId);
        return Result.success(hotelBooking);
    }

    /**
     * 分页查询酒店预订列表
     */
    @GetMapping("/page")
    @ApiOperation("分页查询酒店预订列表")
    public Result<PageResult> pageQuery(
            @ApiParam(name = "page", value = "页码", required = true)
            @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(name = "pageSize", value = "每页记录数", required = true)
            @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam(name = "status", value = "预订状态")
            @RequestParam(required = false) String status,
            @ApiParam(name = "guestName", value = "客人姓名")
            @RequestParam(required = false) String guestName,
            @ApiParam(name = "guestPhone", value = "客人电话")
            @RequestParam(required = false) String guestPhone,
            @ApiParam(name = "hotelId", value = "酒店ID")
            @RequestParam(required = false) Integer hotelId,
            @ApiParam(name = "checkInDate", value = "入住日期开始")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
            @ApiParam(name = "checkOutDate", value = "入住日期结束")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate) {
        log.info("分页查询酒店预订列表，页码：{}，每页记录数：{}", page, pageSize);
        PageResult pageResult = hotelBookingService.pageQuery(page, pageSize, status, guestName, guestPhone,
                hotelId, checkInDate, checkOutDate);
        return Result.success(pageResult);
    }

    /**
     * 获取酒店预订统计数据
     */
    @GetMapping("/stats")
    @ApiOperation("获取酒店预订统计数据")
    public Result<Object> getHotelBookingStats() {
        log.info("获取酒店预订统计数据");
        try {
            // 这里可以返回一些基本的统计数据
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalBookings", 0);
            stats.put("pendingBookings", 0);
            stats.put("confirmedBookings", 0);
            stats.put("checkedInBookings", 0);
            stats.put("checkedOutBookings", 0);
            stats.put("cancelledBookings", 0);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取统计数据失败");
        }
    }

    /**
     * 更新酒店预订信息
     */
    @PutMapping
    @ApiOperation("更新酒店预订信息")
    public Result<String> updateBooking(@RequestBody HotelBookingDTO hotelBookingDTO) {
        log.info("更新酒店预订信息：{}", hotelBookingDTO);
        Boolean success = hotelBookingService.updateBooking(hotelBookingDTO);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 确认酒店预订
     */
    @PostMapping("/{id}/confirm")
    @ApiOperation("确认酒店预订")
    public Result<String> confirmBooking(@PathVariable Integer id) {
        log.info("确认酒店预订：{}", id);
        Boolean success = hotelBookingService.confirmBooking(id);
        return success ? Result.success("确认成功") : Result.error("确认失败");
    }

    /**
     * 取消酒店预订
     */
    @PostMapping("/{id}/cancel")
    @ApiOperation("取消酒店预订")
    public Result<String> cancelBooking(@PathVariable Integer id) {
        log.info("取消酒店预订：{}", id);
        Boolean success = hotelBookingService.cancelBooking(id);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    /**
     * 更新预订状态
     */
    @PutMapping("/{id}/status")
    @ApiOperation("更新预订状态")
    public Result<String> updateBookingStatus(
            @PathVariable Integer id,
            @RequestBody java.util.Map<String, String> request) {
        String status = request.get("status");
        log.info("更新预订状态：ID={}, 新状态={}", id, status);
        
        try {
            Boolean success = false;
            switch (status) {
                case "pending":
                case "email_sent":
                case "confirmed":
                case "cancelled":
                case "rescheduled":
                    success = hotelBookingService.updateBookingStatus(id, status);
                    break;
                case "checked_in":
                    success = hotelBookingService.checkIn(id);
                    break;
                case "checked_out":
                    success = hotelBookingService.checkOut(id);
                    break;
                default:
                    return Result.error("不支持的状态：" + status);
            }
            
            return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
        } catch (Exception e) {
            log.error("更新预订状态失败", e);
            return Result.error("状态更新失败：" + e.getMessage());
        }
    }

    /**
     * 办理入住
     */
    @PostMapping("/{id}/check-in")
    @ApiOperation("办理入住")
    public Result<String> checkIn(@PathVariable Integer id) {
        log.info("办理入住：{}", id);
        Boolean success = hotelBookingService.checkIn(id);
        return success ? Result.success("入住成功") : Result.error("入住失败");
    }

    /**
     * 办理退房
     */
    @PostMapping("/{id}/check-out")
    @ApiOperation("办理退房")
    public Result<String> checkOut(@PathVariable Integer id) {
        log.info("办理退房：{}", id);
        Boolean success = hotelBookingService.checkOut(id);
        return success ? Result.success("退房成功") : Result.error("退房失败");
    }

    /**
     * 更新支付状态
     */
    @PostMapping("/{id}/payment-status")
    @ApiOperation("更新支付状态")
    public Result<String> updatePaymentStatus(
            @PathVariable Integer id,
            @ApiParam(name = "paymentStatus", value = "支付状态", required = true)
            @RequestParam String paymentStatus) {
        log.info("更新支付状态：{}, {}", id, paymentStatus);
        Boolean success = hotelBookingService.updatePaymentStatus(id, paymentStatus);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 检查酒店房间可用性
     */
    @GetMapping("/check-availability")
    @ApiOperation("检查酒店房间可用性")
    public Result<Boolean> checkRoomAvailability(
            @ApiParam(name = "hotelId", value = "酒店ID", required = true)
            @RequestParam Integer hotelId,
            @ApiParam(name = "roomTypeId", value = "房型ID", required = true)
            @RequestParam Integer roomTypeId,
            @ApiParam(name = "checkInDate", value = "入住日期", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
            @ApiParam(name = "checkOutDate", value = "退房日期", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate,
            @ApiParam(name = "roomCount", value = "房间数量", required = true)
            @RequestParam Integer roomCount) {
        log.info("检查酒店房间可用性");
        Boolean available = hotelBookingService.checkRoomAvailability(hotelId, roomTypeId, 
                checkInDate, checkOutDate, roomCount);
        return Result.success(available);
    }

    /**
     * 根据代理商ID查询酒店预订列表
     */
    @GetMapping("/agent/{agentId}")
    @ApiOperation("根据代理商ID查询酒店预订列表")
    public Result<List<HotelBookingVO>> getByAgentId(@PathVariable Integer agentId) {
        log.info("根据代理商ID查询酒店预订列表：{}", agentId);
        List<HotelBookingVO> bookings = hotelBookingService.getByAgentId(agentId);
        return Result.success(bookings);
    }

    /**
     * 根据导游车辆分配ID查询酒店预订列表
     */
    @GetMapping("/assignment/{assignmentId}")
    @ApiOperation("根据导游车辆分配ID查询酒店预订列表")
    public Result<List<HotelBooking>> getByAssignmentId(@PathVariable Integer assignmentId) {
        log.info("根据导游车辆分配ID查询酒店预订列表：{}", assignmentId);
        List<HotelBooking> bookings = hotelBookingService.getByAssignmentId(assignmentId);
        return Result.success(bookings);
    }

    /**
     * 发送酒店预订邮件
     */
    @PostMapping("/send-email")
    @ApiOperation("发送酒店预订邮件")
    public Result<String> sendBookingEmail(@RequestBody HotelBookingEmailDTO emailDTO) {
        log.info("发送酒店预订邮件：{}", emailDTO);
        try {
            Boolean success = hotelBookingService.sendBookingEmail(emailDTO);
            return success ? Result.success("邮件发送成功") : Result.error("邮件发送失败");
        } catch (Exception e) {
            log.error("发送酒店预订邮件失败", e);
            return Result.error("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 删除酒店预订
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除酒店预订")
    public Result<String> deleteBooking(@PathVariable Integer id) {
        log.info("删除酒店预订：{}", id);
        Boolean success = hotelBookingService.deleteBooking(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }


} 