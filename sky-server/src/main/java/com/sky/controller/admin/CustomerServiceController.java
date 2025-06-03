package com.sky.controller.admin;

import com.sky.dto.CustomerServiceDTO;
import com.sky.dto.CustomerServicePageQueryDTO;
import com.sky.dto.OnlineStatusDTO;
import com.sky.dto.ServiceLoginDTO;
import com.sky.entity.CustomerService;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CustomerServiceService;
import com.sky.vo.CustomerServiceStatisticsVO;
import com.sky.vo.CustomerServiceVO;
import com.sky.vo.ServiceLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客服管理Controller
 */
@RestController
@RequestMapping("/admin/customer-service")
@Slf4j
@Api(tags = "客服管理相关接口")
public class CustomerServiceController {

    @Autowired
    private CustomerServiceService customerServiceService;

    /**
     * 客服登录
     */
    @PostMapping("/login")
    @ApiOperation("客服登录")
    public Result<ServiceLoginVO> login(@RequestBody ServiceLoginDTO serviceLoginDTO) {
        log.info("客服登录：{}", serviceLoginDTO.getUsername());
        
        ServiceLoginVO serviceLoginVO = customerServiceService.login(serviceLoginDTO);
        
        return Result.success(serviceLoginVO);
    }

    /**
     * 客服登出
     */
    @PostMapping("/logout")
    @ApiOperation("客服登出")
    public Result<String> logout() {
        // 这里应该从JWT中获取客服ID，暂时使用固定值
        // Long serviceId = BaseContext.getCurrentId();
        // customerServiceService.logout(serviceId);
        
        return Result.success();
    }

    /**
     * 分页查询客服列表
     */
    @GetMapping("/list")
    @ApiOperation("分页查询客服列表")
    public Result<PageResult> getServiceList(CustomerServicePageQueryDTO queryDTO) {
        log.info("分页查询客服列表：{}", queryDTO);
        PageResult pageResult = customerServiceService.getServiceList(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取客服统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation("获取客服统计信息")
    public Result<CustomerServiceStatisticsVO> getStatistics() {
        CustomerServiceStatisticsVO statistics = customerServiceService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 创建客服
     */
    @PostMapping
    @ApiOperation("创建客服")
    public Result<String> createService(@RequestBody CustomerServiceDTO customerServiceDTO) {
        log.info("创建客服：{}", customerServiceDTO);
        customerServiceService.createService(customerServiceDTO);
        return Result.success();
    }

    /**
     * 更新客服信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新客服信息")
    public Result<String> updateService(@PathVariable Long id, @RequestBody CustomerServiceDTO customerServiceDTO) {
        log.info("更新客服信息：id={}, data={}", id, customerServiceDTO);
        customerServiceService.updateService(id, customerServiceDTO);
        return Result.success();
    }

    /**
     * 获取客服详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取客服详情")
    public Result<CustomerServiceVO> getServiceDetail(@PathVariable Long id) {
        CustomerServiceVO serviceDetail = customerServiceService.getServiceDetail(id);
        return Result.success(serviceDetail);
    }

    /**
     * 更新客服在线状态
     */
    @PutMapping("/{id}/online-status")
    @ApiOperation("更新客服在线状态")
    public Result<String> updateOnlineStatus(@PathVariable Long id, @RequestBody OnlineStatusDTO onlineStatusDTO) {
        log.info("更新客服在线状态：id={}, status={}", id, onlineStatusDTO.getOnlineStatus());
        customerServiceService.updateOnlineStatus(id, onlineStatusDTO.getOnlineStatus());
        return Result.success();
    }

    /**
     * 禁用客服
     */
    @PutMapping("/{id}/disable")
    @ApiOperation("禁用客服")
    public Result<String> disableService(@PathVariable Long id) {
        log.info("禁用客服：id={}", id);
        customerServiceService.disableService(id);
        return Result.success();
    }

    /**
     * 获取在线客服列表
     */
    @GetMapping("/online")
    @ApiOperation("获取在线客服列表")
    public Result<List<CustomerService>> getOnlineServices() {
        List<CustomerService> services = customerServiceService.getOnlineServices();
        return Result.success(services);
    }
} 