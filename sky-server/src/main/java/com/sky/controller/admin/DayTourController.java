package com.sky.controller.admin;

import com.sky.dto.DayTourDTO;
import com.sky.dto.DayTourPageQueryDTO;
import com.sky.entity.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DayTourService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 一日游管理控制器
 */
@RestController
@RequestMapping("/admin/daytour")
@Api(tags = "一日游管理接口")
@Slf4j
public class DayTourController {

    @Autowired
    private DayTourService dayTourService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(DayTourPageQueryDTO dayTourPageQueryDTO) {
        log.info("分页查询：{}", dayTourPageQueryDTO);
        PageResult pageResult = dayTourService.pageQuery(dayTourPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询")
    public Result<DayTour> getById(@PathVariable Integer id) {
        log.info("根据ID查询：{}", id);
        DayTour dayTour = dayTourService.getById(id);
        return Result.success(dayTour);
    }

    /**
     * 新增一日游
     */
    @PostMapping
    @ApiOperation("新增一日游")
    public Result save(@RequestBody DayTourDTO dayTourDTO) {
        log.info("新增一日游：{}", dayTourDTO);
        dayTourService.save(dayTourDTO);
        return Result.success();
    }

    /**
     * 修改一日游
     */
    @PutMapping
    @ApiOperation("修改一日游")
    public Result update(@RequestBody DayTourDTO dayTourDTO) {
        log.info("修改一日游：{}", dayTourDTO);
        dayTourService.update(dayTourDTO);
        return Result.success();
    }

    /**
     * 删除一日游
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除一日游")
    public Result delete(@PathVariable Integer id) {
        log.info("删除一日游：{}", id);
        dayTourService.deleteById(id);
        return Result.success();
    }

    /**
     * 上下架一日游
     */
    @PostMapping("/status/{status}")
    @ApiOperation("上下架一日游")
    public Result startOrStop(@PathVariable Integer status, Integer id) {
        log.info("上下架一日游：status={}, id={}", status, id);
        dayTourService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 获取一日游亮点
     */
    @GetMapping("/highlights/{dayTourId}")
    @ApiOperation("获取一日游亮点")
    public Result<List<DayTourHighlight>> getHighlights(@PathVariable Integer dayTourId) {
        log.info("获取一日游亮点：{}", dayTourId);
        List<DayTourHighlight> highlights = dayTourService.getHighlightsByDayTourId(dayTourId);
        return Result.success(highlights);
    }

    /**
     * 新增一日游亮点
     */
    @PostMapping("/highlight")
    @ApiOperation("新增一日游亮点")
    public Result saveHighlight(@RequestBody DayTourHighlight dayTourHighlight) {
        log.info("新增一日游亮点：{}", dayTourHighlight);
        
        try {
            // 确保description字段不为空
            if (dayTourHighlight.getDescription() == null || dayTourHighlight.getDescription().trim().isEmpty()) {
                return Result.error("亮点描述不能为空");
            }
            
            dayTourHighlight.setCreatedAt(LocalDateTime.now());
            dayTourHighlight.setUpdatedAt(LocalDateTime.now());
            dayTourService.saveHighlight(dayTourHighlight);
            return Result.success();
        } catch (Exception e) {
            log.error("添加亮点失败：", e);
            return Result.error("添加亮点失败：" + e.getMessage());
        }
    }

    /**
     * 删除一日游亮点
     */
    @DeleteMapping("/highlight/{id}")
    @ApiOperation("删除一日游亮点")
    public Result deleteHighlight(@PathVariable Integer id) {
        log.info("删除一日游亮点：{}", id);
        dayTourService.deleteHighlight(id);
        return Result.success();
    }

    /**
     * 获取一日游包含项
     */
    @GetMapping("/inclusions/{dayTourId}")
    @ApiOperation("获取一日游包含项")
    public Result<List<DayTourInclusion>> getInclusions(@PathVariable Integer dayTourId) {
        log.info("获取一日游包含项：{}", dayTourId);
        List<DayTourInclusion> inclusions = dayTourService.getInclusionsByDayTourId(dayTourId);
        return Result.success(inclusions);
    }

    /**
     * 新增一日游包含项
     */
    @PostMapping("/inclusion")
    @ApiOperation("新增一日游包含项")
    public Result saveInclusion(@RequestBody DayTourInclusion dayTourInclusion) {
        log.info("新增一日游包含项：{}", dayTourInclusion);
        dayTourInclusion.setCreatedAt(LocalDateTime.now());
        dayTourInclusion.setUpdatedAt(LocalDateTime.now());
        dayTourService.saveInclusion(dayTourInclusion);
        return Result.success();
    }

    /**
     * 删除一日游包含项
     */
    @DeleteMapping("/inclusion/{id}")
    @ApiOperation("删除一日游包含项")
    public Result deleteInclusion(@PathVariable Integer id) {
        log.info("删除一日游包含项：{}", id);
        dayTourService.deleteInclusion(id);
        return Result.success();
    }

    /**
     * 获取一日游不包含项
     */
    @GetMapping("/exclusions/{dayTourId}")
    @ApiOperation("获取一日游不包含项")
    public Result<List<DayTourExclusion>> getExclusions(@PathVariable Integer dayTourId) {
        log.info("获取一日游不包含项：{}", dayTourId);
        List<DayTourExclusion> exclusions = dayTourService.getExclusionsByDayTourId(dayTourId);
        return Result.success(exclusions);
    }

    /**
     * 新增一日游不包含项
     */
    @PostMapping("/exclusion")
    @ApiOperation("新增一日游不包含项")
    public Result saveExclusion(@RequestBody DayTourExclusion dayTourExclusion) {
        log.info("新增一日游不包含项：{}", dayTourExclusion);
        dayTourExclusion.setCreatedAt(LocalDateTime.now());
        dayTourExclusion.setUpdatedAt(LocalDateTime.now());
        dayTourService.saveExclusion(dayTourExclusion);
        return Result.success();
    }

    /**
     * 删除一日游不包含项
     */
    @DeleteMapping("/exclusion/{id}")
    @ApiOperation("删除一日游不包含项")
    public Result deleteExclusion(@PathVariable Integer id) {
        log.info("删除一日游不包含项：{}", id);
        dayTourService.deleteExclusion(id);
        return Result.success();
    }

    /**
     * 获取一日游常见问题
     */
    @GetMapping("/faqs/{dayTourId}")
    @ApiOperation("获取一日游常见问题")
    public Result<List<DayTourFaq>> getFaqs(@PathVariable Integer dayTourId) {
        log.info("获取一日游常见问题：{}", dayTourId);
        List<DayTourFaq> faqs = dayTourService.getFaqsByDayTourId(dayTourId);
        return Result.success(faqs);
    }

    /**
     * 新增一日游常见问题
     */
    @PostMapping("/faq")
    @ApiOperation("新增一日游常见问题")
    public Result saveFaq(@RequestBody DayTourFaq dayTourFaq) {
        log.info("新增一日游常见问题：{}", dayTourFaq);
        dayTourFaq.setCreatedAt(LocalDateTime.now());
        dayTourFaq.setUpdatedAt(LocalDateTime.now());
        dayTourService.saveFaq(dayTourFaq);
        return Result.success();
    }

    /**
     * 删除一日游常见问题
     */
    @DeleteMapping("/faq/{id}")
    @ApiOperation("删除一日游常见问题")
    public Result deleteFaq(@PathVariable Integer id) {
        log.info("删除一日游常见问题：{}", id);
        dayTourService.deleteFaq(id);
        return Result.success();
    }

    /**
     * 获取一日游行程
     */
    @GetMapping("/itineraries/{dayTourId}")
    @ApiOperation("获取一日游行程")
    public Result<List<DayTourItinerary>> getItineraries(@PathVariable Integer dayTourId) {
        log.info("获取一日游行程：{}", dayTourId);
        List<DayTourItinerary> itineraries = dayTourService.getItinerariesByDayTourId(dayTourId);
        return Result.success(itineraries);
    }

    /**
     * 新增一日游行程
     */
    @PostMapping("/itinerary")
    @ApiOperation("新增一日游行程")
    public Result saveItinerary(@RequestBody DayTourItinerary dayTourItinerary) {
        log.info("新增一日游行程：{}", dayTourItinerary);
        dayTourItinerary.setCreatedAt(LocalDateTime.now());
        dayTourItinerary.setUpdatedAt(LocalDateTime.now());
        dayTourService.saveItinerary(dayTourItinerary);
        return Result.success();
    }

    /**
     * 删除一日游行程
     */
    @DeleteMapping("/itinerary/{id}")
    @ApiOperation("删除一日游行程")
    public Result deleteItinerary(@PathVariable Integer id) {
        log.info("删除一日游行程：{}", id);
        dayTourService.deleteItinerary(id);
        return Result.success();
    }

    /**
     * 获取一日游旅行提示
     */
    @GetMapping("/tips/{dayTourId}")
    @ApiOperation("获取一日游旅行提示")
    public Result<List<DayTourTip>> getTips(@PathVariable Integer dayTourId) {
        log.info("获取一日游旅行提示：{}", dayTourId);
        List<DayTourTip> tips = dayTourService.getTipsByDayTourId(dayTourId);
        return Result.success(tips);
    }

    /**
     * 新增一日游旅行提示
     */
    @PostMapping("/tip")
    @ApiOperation("新增一日游旅行提示")
    public Result saveTip(@RequestBody DayTourTip dayTourTip) {
        log.info("新增一日游旅行提示：{}", dayTourTip);
        dayTourTip.setCreatedAt(LocalDateTime.now());
        dayTourTip.setUpdatedAt(LocalDateTime.now());
        dayTourService.saveTip(dayTourTip);
        return Result.success();
    }

    /**
     * 删除一日游旅行提示
     */
    @DeleteMapping("/tip/{id}")
    @ApiOperation("删除一日游旅行提示")
    public Result deleteTip(@PathVariable Integer id) {
        log.info("删除一日游旅行提示：{}", id);
        dayTourService.deleteTip(id);
        return Result.success();
    }

    /**
     * 获取一日游日程安排
     */
    @GetMapping("/schedules/{dayTourId}")
    @ApiOperation("获取一日游日程安排")
    public Result<List<DayTourSchedule>> getSchedules(@PathVariable Integer dayTourId) {
        log.info("获取一日游日程安排：{}", dayTourId);
        List<DayTourSchedule> schedules = dayTourService.getSchedulesByDayTourId(dayTourId);
        return Result.success(schedules);
    }

    /**
     * 新增一日游日程安排
     */
    @PostMapping("/schedule")
    @ApiOperation("新增一日游日程安排")
    public Result saveSchedule(@RequestBody DayTourSchedule dayTourSchedule) {
        log.info("新增一日游日程安排：{}", dayTourSchedule);
        dayTourService.saveSchedule(dayTourSchedule);
        return Result.success();
    }

    /**
     * 删除一日游日程安排
     */
    @DeleteMapping("/schedule/{scheduleId}")
    @ApiOperation("删除一日游日程安排")
    public Result deleteSchedule(@PathVariable Integer scheduleId) {
        log.info("删除一日游日程安排：{}", scheduleId);
        dayTourService.deleteSchedule(scheduleId);
        return Result.success();
    }

    /**
     * 获取一日游图片列表
     */
    @GetMapping("/images/{dayTourId}")
    @ApiOperation("获取一日游图片列表")
    public Result<List<DayTourImage>> getImages(@PathVariable Integer dayTourId) {
        log.info("获取一日游图片列表：{}", dayTourId);
        List<DayTourImage> images = dayTourService.getImagesByDayTourId(dayTourId);
        return Result.success(images);
    }
} 