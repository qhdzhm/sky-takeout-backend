package com.sky.controller.admin;

import com.sky.dto.ItineraryOptionGroupDTO;
import com.sky.entity.GroupTourDayTourRelation;
import com.sky.result.Result;
import com.sky.service.ItineraryOptionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理 - 行程选项Controller
 */
@RestController
@RequestMapping("/admin/itinerary")
@Api(tags = "后台管理 - 行程选项相关接口")
@Slf4j
public class ItineraryOptionController {

    @Autowired
    private ItineraryOptionService itineraryOptionService;

    /**
     * 根据跟团游ID获取所有行程选项组
     */
    @GetMapping("/option-groups/{groupTourId}")
    @ApiOperation("获取跟团游的所有行程选项组")
    public Result<List<ItineraryOptionGroupDTO>> getItineraryOptionGroups(@PathVariable Integer groupTourId) {
        log.info("获取跟团游{}的所有行程选项组", groupTourId);
        List<ItineraryOptionGroupDTO> optionGroups = itineraryOptionService.getItineraryOptionGroups(groupTourId);
        return Result.success(optionGroups);
    }

    /**
     * 根据跟团游ID获取可选行程选项组
     */
    @GetMapping("/optional-groups/{groupTourId}")
    @ApiOperation("获取跟团游的可选行程选项组")
    public Result<List<ItineraryOptionGroupDTO>> getOptionalItineraryGroups(@PathVariable Integer groupTourId) {
        log.info("获取跟团游{}的可选行程选项组", groupTourId);
        List<ItineraryOptionGroupDTO> optionalGroups = itineraryOptionService.getOptionalItineraryGroups(groupTourId);
        return Result.success(optionalGroups);
    }

    /**
     * 根据跟团游ID和天数获取可选项
     */
    @GetMapping("/optional/{groupTourId}/{dayNumber}")
    @ApiOperation("根据跟团游ID和天数获取可选项")
    public Result<List<GroupTourDayTourRelation>> getOptionalByGroupTourIdAndDay(
            @PathVariable Integer groupTourId, 
            @PathVariable Integer dayNumber) {
        log.info("获取跟团游{}第{}天的可选项", groupTourId, dayNumber);
        List<GroupTourDayTourRelation> options = itineraryOptionService.getOptionalByGroupTourIdAndDay(groupTourId, dayNumber);
        return Result.success(options);
    }

    /**
     * 更新关联记录的可选项信息
     */
    @PutMapping("/optional-info")
    @ApiOperation("更新关联记录的可选项信息")
    public Result<String> updateOptionalInfo(@RequestBody GroupTourDayTourRelation relation) {
        log.info("更新关联记录{}的可选项信息", relation.getId());
        boolean success = itineraryOptionService.updateOptionalInfo(relation);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 批量更新关联记录的可选项信息
     */
    @PutMapping("/batch-optional-info")
    @ApiOperation("批量更新关联记录的可选项信息")
    public Result<String> batchUpdateOptionalInfo(@RequestBody List<GroupTourDayTourRelation> relations) {
        log.info("批量更新{}条关联记录的可选项信息", relations.size());
        boolean success = itineraryOptionService.batchUpdateOptionalInfo(relations);
        return success ? Result.success("批量更新成功") : Result.error("批量更新失败");
    }

    /**
     * 获取跟团游的默认行程
     */
    @GetMapping("/default-itinerary/{groupTourId}")
    @ApiOperation("获取跟团游的默认行程")
    public Result<List<GroupTourDayTourRelation>> getDefaultItinerary(@PathVariable Integer groupTourId) {
        log.info("获取跟团游{}的默认行程", groupTourId);
        List<GroupTourDayTourRelation> defaultItinerary = itineraryOptionService.getDefaultItinerary(groupTourId);
        return Result.success(defaultItinerary);
    }

    /**
     * 获取跟团游的必选行程
     */
    @GetMapping("/required-itinerary/{groupTourId}")
    @ApiOperation("获取跟团游的必选行程")
    public Result<List<GroupTourDayTourRelation>> getRequiredItinerary(@PathVariable Integer groupTourId) {
        log.info("获取跟团游{}的必选行程", groupTourId);
        List<GroupTourDayTourRelation> requiredItinerary = itineraryOptionService.getRequiredItinerary(groupTourId);
        return Result.success(requiredItinerary);
    }
} 