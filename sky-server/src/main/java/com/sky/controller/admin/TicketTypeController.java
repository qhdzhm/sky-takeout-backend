package com.sky.controller.admin;

import com.sky.entity.TicketType;
import com.sky.result.Result;
import com.sky.service.TicketTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 票务类型Controller - 对标酒店房型Controller
 */
@RestController
@RequestMapping("/admin/ticket-types")
@Api(tags = "票务类型管理接口")
@Slf4j
public class TicketTypeController {

    @Autowired
    private TicketTypeService ticketTypeService;

    /**
     * 根据景点ID获取票务类型列表
     */
    @GetMapping("/attraction/{attractionId}")
    @ApiOperation("根据景点ID获取票务类型列表")
    public Result<List<TicketType>> getTicketTypesByAttractionId(@PathVariable Long attractionId) {
        log.info("根据景点ID获取票务类型列表：{}", attractionId);
        List<TicketType> ticketTypes = ticketTypeService.getTicketTypesByAttractionId(attractionId);
        return Result.success(ticketTypes);
    }

    /**
     * 根据景点ID获取活跃票务类型列表
     */
    @GetMapping("/attraction/{attractionId}/active")
    @ApiOperation("根据景点ID获取活跃票务类型列表")
    public Result<List<TicketType>> getActiveTicketTypesByAttractionId(@PathVariable Long attractionId) {
        log.info("根据景点ID获取活跃票务类型列表：{}", attractionId);
        List<TicketType> ticketTypes = ticketTypeService.getActiveTicketTypesByAttractionId(attractionId);
        return Result.success(ticketTypes);
    }

    /**
     * 获取所有活跃票务类型
     */
    @GetMapping("/active")
    @ApiOperation("获取所有活跃票务类型")
    public Result<List<TicketType>> getAllActiveTicketTypes() {
        log.info("获取所有活跃票务类型");
        List<TicketType> ticketTypes = ticketTypeService.getAllActiveTicketTypes();
        return Result.success(ticketTypes);
    }

    /**
     * 根据ID获取票务类型详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取票务类型详情")
    public Result<TicketType> getTicketTypeById(@PathVariable Long id) {
        log.info("根据ID获取票务类型详情：{}", id);
        TicketType ticketType = ticketTypeService.getTicketTypeById(id);
        return Result.success(ticketType);
    }

    /**
     * 根据票务代码获取票务类型
     */
    @GetMapping("/code/{ticketCode}")
    @ApiOperation("根据票务代码获取票务类型")
    public Result<TicketType> getTicketTypeByCode(@PathVariable String ticketCode) {
        log.info("根据票务代码获取票务类型：{}", ticketCode);
        TicketType ticketType = ticketTypeService.getTicketTypeByCode(ticketCode);
        return Result.success(ticketType);
    }

    /**
     * 创建票务类型
     */
    @PostMapping
    @ApiOperation("创建票务类型")
    public Result<String> createTicketType(@RequestBody TicketType ticketType) {
        log.info("创建票务类型：{}", ticketType);
        ticketTypeService.createTicketType(ticketType);
        return Result.success("创建成功");
    }

    /**
     * 更新票务类型
     */
    @PutMapping
    @ApiOperation("更新票务类型")
    public Result<String> updateTicketType(@RequestBody TicketType ticketType) {
        log.info("更新票务类型：{}", ticketType);
        ticketTypeService.updateTicketType(ticketType);
        return Result.success("更新成功");
    }

    /**
     * 更新票务类型（通过路径参数ID）
     */
    @PutMapping("/{id}")
    @ApiOperation("更新票务类型")
    public Result<String> updateTicketTypeById(@PathVariable Long id, @RequestBody TicketType ticketType) {
        log.info("更新票务类型：id={}, ticketType={}", id, ticketType);
        ticketType.setId(id);
        ticketTypeService.updateTicketType(ticketType);
        return Result.success("更新成功");
    }

    /**
     * 删除票务类型
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除票务类型")
    public Result<String> deleteTicketType(@PathVariable Long id) {
        log.info("删除票务类型：{}", id);
        ticketTypeService.deleteTicketType(id);
        return Result.success("删除成功");
    }

    /**
     * 更新票务类型状态
     */
    @PutMapping("/{id}/status")
    @ApiOperation("更新票务类型状态")
    public Result<String> updateTicketTypeStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新票务类型状态：id={}, status={}", id, status);
        ticketTypeService.updateTicketTypeStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 根据景点ID删除所有票务类型
     */
    @DeleteMapping("/attraction/{attractionId}")
    @ApiOperation("根据景点ID删除所有票务类型")
    public Result<String> deleteTicketTypesByAttractionId(@PathVariable Long attractionId) {
        log.info("根据景点ID删除所有票务类型：{}", attractionId);
        ticketTypeService.deleteTicketTypesByAttractionId(attractionId);
        return Result.success("删除成功");
    }

    /**
     * 批量创建票务类型
     */
    @PostMapping("/batch")
    @ApiOperation("批量创建票务类型")
    public Result<String> batchCreateTicketTypes(@RequestBody List<TicketType> ticketTypes) {
        log.info("批量创建票务类型，数量：{}", ticketTypes.size());
        ticketTypeService.batchCreateTicketTypes(ticketTypes);
        return Result.success("批量创建成功");
    }

    /**
     * 根据条件搜索票务类型
     */
    @GetMapping("/search")
    @ApiOperation("根据条件搜索票务类型")
    public Result<List<TicketType>> searchTicketTypes(@RequestParam(required = false) Long attractionId,
                                                     @RequestParam(required = false) String ticketType,
                                                     @RequestParam(required = false) String status) {
        log.info("根据条件搜索票务类型：attractionId={}, ticketType={}, status={}", 
                attractionId, ticketType, status);
        List<TicketType> ticketTypes = ticketTypeService.searchTicketTypes(attractionId, ticketType, status);
        return Result.success(ticketTypes);
    }
}

