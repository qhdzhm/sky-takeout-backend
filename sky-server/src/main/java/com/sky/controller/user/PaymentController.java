package com.sky.controller.user;

import com.sky.dto.CreditPaymentDTO;
import com.sky.dto.PaymentDTO;
import com.sky.dto.PaymentPageQueryDTO;
import com.sky.result.PageResult;
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
    
    /**
     * 更新支付状态
     * @param id 支付ID
     * @param status 状态
     * @return 成功/失败
     */
    @PutMapping("/{id}/status")
    @ApiOperation("更新支付状态")
    public Result<Boolean> updatePaymentStatus(@PathVariable Integer id, @RequestParam String status) {
        log.info("更新支付状态，ID：{}，状态：{}", id, status);
        boolean success = paymentService.updatePaymentStatus(id, status);
        return Result.success(success);
    }
    
    /**
     * 退款处理
     * @param id 原支付ID
     * @param refundDTO 退款信息
     * @return 退款ID
     */
    @PostMapping("/{id}/refund")
    @ApiOperation("退款处理")
    public Result<Integer> refundPayment(@PathVariable Integer id, @RequestBody PaymentDTO refundDTO) {
        log.info("退款处理，支付ID：{}", id);
        Integer refundId = paymentService.refundPayment(id, refundDTO);
        return Result.success(refundId);
    }
    
    /**
     * 信用额度支付
     * @param creditPaymentDTO 信用额度支付信息
     * @return 成功/失败
     */
    @PostMapping("/credit")
    @ApiOperation("信用额度支付")
    public Result<Boolean> creditPayment(@RequestBody CreditPaymentDTO creditPaymentDTO) {
        log.info("信用额度支付：{}", creditPaymentDTO);
        boolean success = paymentService.processCreditPayment(creditPaymentDTO);
        return Result.success(success);
    }
    
    /**
     * 分页查询支付流水
     * @param pageQueryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询支付流水")
    public Result<PageResult> getPaymentPage(PaymentPageQueryDTO pageQueryDTO) {
        log.info("分页查询支付流水：{}", pageQueryDTO);
        PageResult pageResult = paymentService.getTransactionPage(pageQueryDTO);
        return Result.success(pageResult);
    }
} 