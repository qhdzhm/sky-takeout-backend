package com.sky.controller.user;

import com.sky.dto.PaymentDTO;
import com.sky.result.Result;
import com.sky.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 支付控制器
 */
@RestController
@RequestMapping("/user/payments")
@Api(tags = "支付相关接口")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付
     * @param paymentDTO 支付信息
     * @return 支付ID
     */
    @PostMapping
    @ApiOperation("创建支付")
    public Result<Integer> createPayment(@RequestBody PaymentDTO paymentDTO) {
        log.info("创建支付：{}", paymentDTO);
        Integer paymentId = paymentService.createPayment(paymentDTO);
        return Result.success(paymentId);
    }

    /**
     * 根据ID获取支付详情
     * @param id 支付ID
     * @return 支付详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取支付详情")
    public Result<PaymentDTO> getPaymentById(@PathVariable Integer id) {
        log.info("获取支付详情，ID：{}", id);
        PaymentDTO paymentDTO = paymentService.getPaymentById(id);
        return Result.success(paymentDTO);
    }

    /**
     * 根据预订ID获取支付列表
     * @param bookingId 预订ID
     * @return 支付列表
     */
    @GetMapping("/booking/{bookingId}")
    @ApiOperation("根据预订ID获取支付列表")
    public Result<List<PaymentDTO>> getPaymentsByBookingId(@PathVariable Integer bookingId) {
        log.info("获取预订支付列表，预订ID：{}", bookingId);
        List<PaymentDTO> payments = paymentService.getPaymentsByBookingId(bookingId);
        return Result.success(payments);
    }
} 