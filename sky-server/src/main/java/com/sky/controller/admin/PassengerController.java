package com.sky.controller.admin;

import com.sky.dto.PassengerDTO;
import com.sky.result.Result;
import com.sky.service.PassengerService;
import com.sky.vo.PassengerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 乘客管理控制器
 */
@RestController
@RequestMapping("/admin/passengers")
@Api(tags = "乘客管理相关接口")
@Slf4j
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    /**
     * 根据ID查询乘客
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询乘客")
    public Result<PassengerVO> getById(@PathVariable("id") Integer id) {
        log.info("根据ID查询乘客：{}", id);
        PassengerVO passengerVO = passengerService.getById(id);
        return Result.success(passengerVO);
    }

    /**
     * 根据护照号查询乘客
     */
    @GetMapping("/passport/{passportNumber}")
    @ApiOperation("根据护照号查询乘客")
    public Result<PassengerVO> getByPassportNumber(@PathVariable("passportNumber") String passportNumber) {
        log.info("根据护照号查询乘客：{}", passportNumber);
        PassengerVO passengerVO = passengerService.getByPassportNumber(passportNumber);
        return Result.success(passengerVO);
    }

    /**
     * 根据订单ID查询乘客列表
     */
    @GetMapping("/booking/{bookingId}")
    @ApiOperation("根据订单ID查询乘客列表")
    public Result<List<PassengerVO>> getByBookingId(@PathVariable("bookingId") Integer bookingId) {
        log.info("根据订单ID查询乘客列表：{}", bookingId);
        List<PassengerVO> passengerVOs = passengerService.getByBookingId(bookingId);
        return Result.success(passengerVOs);
    }

    /**
     * 新增乘客
     */
    @PostMapping
    @ApiOperation("新增乘客")
    public Result<Integer> save(@RequestBody PassengerDTO passengerDTO) {
        log.info("新增乘客：{}", passengerDTO);
        Integer passengerId = passengerService.save(passengerDTO);
        return Result.success(passengerId);
    }

    /**
     * 修改乘客
     */
    @PutMapping
    @ApiOperation("修改乘客")
    public Result<Boolean> update(@RequestBody PassengerDTO passengerDTO) {
        log.info("修改乘客：{}", passengerDTO);
        Boolean result = passengerService.update(passengerDTO);
        return Result.success(result);
    }

    /**
     * 删除乘客
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除乘客")
    public Result<Boolean> delete(@PathVariable("id") Integer id) {
        log.info("删除乘客：{}", id);
        Boolean result = passengerService.deleteById(id);
        return Result.success(result);
    }

    /**
     * 添加乘客到订单
     */
    @PostMapping("/booking/{bookingId}")
    @ApiOperation("添加乘客到订单")
    public Result<Boolean> addPassengerToBooking(
            @PathVariable("bookingId") Integer bookingId,
            @RequestBody PassengerDTO passengerDTO) {
        log.info("添加乘客到订单 {} ：{}", bookingId, passengerDTO);
        Boolean result = passengerService.addPassengerToBooking(bookingId, passengerDTO);
        return Result.success(result);
    }

    /**
     * 从订单中移除乘客
     */
    @DeleteMapping("/booking/{bookingId}/passenger/{passengerId}")
    @ApiOperation("从订单中移除乘客")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bookingId", value = "订单ID", required = true, dataType = "int"),
            @ApiImplicitParam(name = "passengerId", value = "乘客ID", required = true, dataType = "int")
    })
    public Result<Boolean> removePassengerFromBooking(
            @PathVariable("bookingId") Integer bookingId,
            @PathVariable("passengerId") Integer passengerId) {
        log.info("从订单 {} 中移除乘客 {}", bookingId, passengerId);
        Boolean result = passengerService.removePassengerFromBooking(bookingId, passengerId);
        return Result.success(result);
    }

    /**
     * 更新乘客在订单中的信息
     */
    @PutMapping("/booking/{bookingId}")
    @ApiOperation("更新乘客在订单中的信息")
    public Result<Boolean> updatePassengerBookingInfo(
            @PathVariable("bookingId") Integer bookingId,
            @RequestBody PassengerDTO passengerDTO) {
        log.info("更新乘客在订单 {} 中的信息：{}", bookingId, passengerDTO);
        Boolean result = passengerService.updatePassengerBookingInfo(bookingId, passengerDTO);
        return Result.success(result);
    }
}