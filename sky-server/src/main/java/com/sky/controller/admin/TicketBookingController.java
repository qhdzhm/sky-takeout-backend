package com.sky.controller.admin;

import com.sky.entity.TicketBooking;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.TicketBookingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 票务预订Controller - 基于酒店预订Controller设计
 */
@RestController
@RequestMapping("/admin/ticket-bookings")
@Api(tags = "票务预订管理接口")
@Slf4j
public class TicketBookingController {

    @Autowired
    private TicketBookingService ticketBookingService;

    /**
     * 分页查询票务预订
     */
    @GetMapping("/page")
    @ApiOperation("分页查询票务预订")
    public Result<PageResult> pageQuery(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String guestName,
                                       @RequestParam(required = false) String guestPhone,
                                       @RequestParam(required = false) Long attractionId,
                                       @RequestParam(required = false) String bookingStatus,
                                       @RequestParam(required = false) String bookingMethod,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDateStart,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDateEnd,
                                       @RequestParam(required = false) String ticketSpecialist) {
        log.info("分页查询票务预订：page={}, pageSize={}", page, pageSize);
        PageResult pageResult = ticketBookingService.pageQuery(page, pageSize, guestName, guestPhone,
                attractionId, bookingStatus, bookingMethod, visitDateStart, visitDateEnd, ticketSpecialist);
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取票务预订详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取票务预订详情")
    public Result<TicketBooking> getTicketBookingById(@PathVariable Long id) {
        log.info("根据ID获取票务预订详情：{}", id);
        TicketBooking ticketBooking = ticketBookingService.getTicketBookingById(id);
        return Result.success(ticketBooking);
    }

    /**
     * 根据预订参考号获取票务预订
     */
    @GetMapping("/reference/{bookingReference}")
    @ApiOperation("根据预订参考号获取票务预订")
    public Result<TicketBooking> getTicketBookingByReference(@PathVariable String bookingReference) {
        log.info("根据预订参考号获取票务预订：{}", bookingReference);
        TicketBooking ticketBooking = ticketBookingService.getTicketBookingByReference(bookingReference);
        return Result.success(ticketBooking);
    }

    /**
     * 根据确认号获取票务预订
     */
    @GetMapping("/confirmation/{confirmationNumber}")
    @ApiOperation("根据确认号获取票务预订")
    public Result<TicketBooking> getTicketBookingByConfirmationNumber(@PathVariable String confirmationNumber) {
        log.info("根据确认号获取票务预订：{}", confirmationNumber);
        TicketBooking ticketBooking = ticketBookingService.getTicketBookingByConfirmationNumber(confirmationNumber);
        return Result.success(ticketBooking);
    }

    /**
     * 根据排团记录ID获取票务预订
     */
    @GetMapping("/schedule-order/{scheduleOrderId}")
    @ApiOperation("根据排团记录ID获取票务预订")
    public Result<TicketBooking> getTicketBookingByScheduleOrderId(@PathVariable Long scheduleOrderId) {
        log.info("根据排团记录ID获取票务预订：{}", scheduleOrderId);
        TicketBooking ticketBooking = ticketBookingService.getTicketBookingByScheduleOrderId(scheduleOrderId);
        return Result.success(ticketBooking);
    }

    /**
     * 根据旅游订单ID获取票务预订列表
     */
    @GetMapping("/tour-booking/{tourBookingId}")
    @ApiOperation("根据旅游订单ID获取票务预订列表")
    public Result<List<TicketBooking>> getTicketBookingsByTourBookingId(@PathVariable Long tourBookingId) {
        log.info("根据旅游订单ID获取票务预订列表：{}", tourBookingId);
        List<TicketBooking> ticketBookings = ticketBookingService.getTicketBookingsByTourBookingId(tourBookingId);
        return Result.success(ticketBookings);
    }

    /**
     * 创建票务预订
     */
    @PostMapping
    @ApiOperation("创建票务预订")
    public Result<String> createTicketBooking(@RequestBody TicketBooking ticketBooking) {
        log.info("创建票务预订：{}", ticketBooking);
        String bookingReference = ticketBookingService.createTicketBooking(ticketBooking);
        return Result.success(bookingReference);
    }

    /**
     * 更新票务预订
     */
    @PutMapping
    @ApiOperation("更新票务预订")
    public Result<String> updateTicketBooking(@RequestBody TicketBooking ticketBooking) {
        log.info("更新票务预订：{}", ticketBooking);
        ticketBookingService.updateTicketBooking(ticketBooking);
        return Result.success("更新成功");
    }

    /**
     * 删除票务预订
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除票务预订")
    public Result<String> deleteTicketBooking(@PathVariable Long id) {
        log.info("删除票务预订：{}", id);
        ticketBookingService.deleteTicketBooking(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除票务预订
     */
    @DeleteMapping("/batch")
    @ApiOperation("批量删除票务预订")
    public Result<String> batchDeleteTicketBookings(@RequestBody List<Long> ids) {
        log.info("批量删除票务预订，数量：{}", ids.size());
        ticketBookingService.batchDeleteTicketBookings(ids);
        return Result.success("批量删除成功");
    }

    /**
     * 更新预订状态
     */
    @PutMapping("/{id}/status")
    @ApiOperation("更新预订状态")
    public Result<String> updateBookingStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新预订状态：id={}, status={}", id, status);
        ticketBookingService.updateBookingStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 批量更新预订状态
     */
    @PutMapping("/status/batch")
    @ApiOperation("批量更新预订状态")
    public Result<String> batchUpdateBookingStatus(@RequestBody List<Long> ids, @RequestParam String status) {
        log.info("批量更新预订状态：ids={}, status={}", ids, status);
        ticketBookingService.batchUpdateBookingStatus(ids, status);
        return Result.success("批量状态更新成功");
    }

    /**
     * 确认预订并设置确认号
     */
    @PostMapping("/{id}/confirm-with-number")
    @ApiOperation("确认预订并设置确认号")
    public Result<String> confirmBookingWithNumber(@PathVariable Long id, @RequestParam String confirmationNumber) {
        log.info("确认预订并设置确认号：id={}, confirmationNumber={}", id, confirmationNumber);
        ticketBookingService.confirmBookingWithNumber(id, confirmationNumber);
        return Result.success("预订确认成功");
    }

    /**
     * 发送预订邮件
     */
    @PostMapping("/send-email")
    @ApiOperation("发送预订邮件")
    public Result<String> sendBookingEmail(@RequestBody Map<String, Object> emailRequest) {
        Long bookingId = Long.valueOf(String.valueOf(emailRequest.get("bookingId")));
        String emailContent = (String) emailRequest.get("content");
        String recipientEmail = (String) emailRequest.get("recipientEmail");
        String subject = (String) emailRequest.get("subject");
        
        log.info("发送预订邮件：bookingId={}, recipientEmail={}", bookingId, recipientEmail);
        
        boolean success = ticketBookingService.sendBookingEmail(bookingId, emailContent, recipientEmail, subject);
        return success ? Result.success("邮件发送成功") : Result.error("邮件发送失败");
    }

    /**
     * 根据景点ID和游览日期查询预订列表
     */
    @GetMapping("/attraction/{attractionId}/date/{visitDate}")
    @ApiOperation("根据景点ID和游览日期查询预订列表")
    public Result<List<TicketBooking>> getBookingsByAttractionAndDate(@PathVariable Long attractionId,
                                                                     @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
        log.info("根据景点ID和游览日期查询预订列表：attractionId={}, visitDate={}", attractionId, visitDate);
        List<TicketBooking> bookings = ticketBookingService.getBookingsByAttractionAndDate(attractionId, visitDate);
        return Result.success(bookings);
    }

    /**
     * 根据游览日期范围查询预订列表
     */
    @GetMapping("/date-range")
    @ApiOperation("根据游览日期范围查询预订列表")
    public Result<List<TicketBooking>> getBookingsByVisitDateRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("根据游览日期范围查询预订列表：startDate={}, endDate={}", startDate, endDate);
        List<TicketBooking> bookings = ticketBookingService.getBookingsByVisitDateRange(startDate, endDate);
        return Result.success(bookings);
    }

    /**
     * 统计预订状态
     */
    @GetMapping("/statistics")
    @ApiOperation("统计预订状态")
    public Result<Map<String, Object>> getBookingStatusStatistics(@RequestParam(required = false) Long attractionId,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("统计预订状态：attractionId={}, startDate={}, endDate={}", attractionId, startDate, endDate);
        Map<String, Object> statistics = ticketBookingService.getBookingStatusStatistics(attractionId, startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 根据票务专员查询预订列表
     */
    @GetMapping("/specialist/{ticketSpecialist}")
    @ApiOperation("根据票务专员查询预订列表")
    public Result<List<TicketBooking>> getBookingsByTicketSpecialist(@PathVariable String ticketSpecialist,
                                                                    @RequestParam(required = false) String bookingStatus) {
        log.info("根据票务专员查询预订列表：ticketSpecialist={}, bookingStatus={}", ticketSpecialist, bookingStatus);
        List<TicketBooking> bookings = ticketBookingService.getBookingsByTicketSpecialist(ticketSpecialist, bookingStatus);
        return Result.success(bookings);
    }

    /**
     * 从排团订单创建票务预订
     */
    @PostMapping("/from-schedule")
    @ApiOperation("从排团订单创建票务预订")
    public Result<String> createFromScheduleOrder(@RequestParam Long scheduleOrderId,
                                                 @RequestParam Long attractionId,
                                                 @RequestParam Long ticketTypeId) {
        log.info("从排团订单创建票务预订：scheduleOrderId={}, attractionId={}, ticketTypeId={}", 
                scheduleOrderId, attractionId, ticketTypeId);
        String bookingReference = ticketBookingService.createFromScheduleOrder(scheduleOrderId, attractionId, ticketTypeId);
        return Result.success(bookingReference);
    }

    /**
     * 从导游车辆分配批量创建票务预订
     */
    @PostMapping("/from-assignments")
    @ApiOperation("从导游车辆分配批量创建票务预订")
    public Result<String> createFromAssignments(@RequestParam List<Long> assignmentIds,
                                               @RequestBody TicketBooking ticketBooking) {
        log.info("从导游车辆分配批量创建票务预订：assignmentIds={}", assignmentIds);
        int count = ticketBookingService.createFromAssignments(assignmentIds, ticketBooking);
        return Result.success("成功创建" + count + "个预订");
    }

}

