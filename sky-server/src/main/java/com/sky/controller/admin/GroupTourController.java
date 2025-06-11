package com.sky.controller.admin;

import com.sky.dto.GroupTourDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.GroupTourService;
import com.sky.service.TourService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sky.service.ImageService;

import java.util.List;
import java.util.Map;

/**
 * 团队游管理控制器
 */
@RestController
@RequestMapping("/admin/grouptour")
@Api(tags = "团队游管理相关接口")
@Slf4j
public class GroupTourController {

    @Autowired
    private GroupTourService groupTourService;
    
    @Autowired
    private TourService tourService;
    
    @Autowired
    private ImageService imageService;

    /**
     * 获取团队游列表（分页）
     * @param params 查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("获取团队游列表（分页）")
    public Result<PageResult> page(@RequestParam Map<String, Object> params) {
        log.info("获取团队游列表：{}", params);
        PageResult pageResult = groupTourService.getAllGroupTours(params);
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取团队游详情
     * @param id 团队游ID
     * @return 团队游详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取团队游详情")
    public Result<GroupTourDTO> getById(@PathVariable Integer id) {
        log.info("根据ID获取团队游详情：{}", id);
        GroupTourDTO groupTour = groupTourService.getGroupTourById(id);
        return Result.success(groupTour);
    }

    /**
     * 更新团队游信息
     * @param groupTourDTO 团队游信息
     * @return 更新结果
     */
    @PutMapping
    @ApiOperation("更新团队游信息")
    public Result<String> update(@RequestBody GroupTourDTO groupTourDTO) {
        log.info("更新团队游信息：{}", groupTourDTO);
        groupTourService.updateGroupTour(groupTourDTO);
        return Result.success("团队游信息更新成功");
    }

    /**
     * 获取团队游行程安排
     * @param tourId 团队游ID
     * @return 行程安排列表
     */
    @GetMapping("/itinerary/{tourId}")
    @ApiOperation("获取团队游行程安排")
    public Result<List<Map<String, Object>>> getItinerary(@PathVariable Integer tourId) {
        log.info("获取团队游行程安排：{}", tourId);
        List<Map<String, Object>> itinerary = groupTourService.getGroupTourItinerary(tourId);
        return Result.success(itinerary);
    }

    /**
     * 添加团队游行程安排
     * @param data 行程数据
     * @return 操作结果
     */
    @PostMapping("/itinerary-add")
    @ApiOperation("添加团队游行程安排")
    public Result<String> addItinerary(@RequestBody Map<String, Object> data) {
        log.info("添加团队游行程安排：{}", data);
        try {
            // 确保类型转换正确
            Object tourIdObj = data.get("groupTourId");
            Integer groupTourId = null;
            if (tourIdObj instanceof Long) {
                groupTourId = ((Long) tourIdObj).intValue();
            } else if (tourIdObj instanceof Integer) {
                groupTourId = (Integer) tourIdObj;
            } else if (tourIdObj != null) {
                groupTourId = Integer.parseInt(tourIdObj.toString());
            }
            
            Object dayObj = data.get("day");
            Integer dayNumber = null;
            if (dayObj instanceof Long) {
                dayNumber = ((Long) dayObj).intValue();
            } else if (dayObj instanceof Integer) {
                dayNumber = (Integer) dayObj;
            } else if (dayObj != null) {
                dayNumber = Integer.parseInt(dayObj.toString());
            }
            
            String title = (String) data.get("title");
            String description = (String) data.get("description");
            String meals = (String) data.get("meals");
            String accommodation = (String) data.get("accommodation");
            
            // 调用服务添加行程
            groupTourService.addGroupTourItinerary(
                groupTourId, 
                dayNumber, 
                title, 
                description, 
                meals, 
                accommodation
            );
            
            return Result.success("添加行程成功");
        } catch (Exception e) {
            log.error("添加行程失败：", e);
            return Result.error("添加行程失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新团队游行程安排
     * @param data 行程数据
     * @return 操作结果
     */
    @PostMapping("/itinerary-update")
    @ApiOperation("更新团队游行程安排")
    public Result<String> updateItinerary(@RequestBody Map<String, Object> data) {
        log.info("更新团队游行程安排：{}", data);
        try {
            // 将Long类型的id转换为Integer类型
            Object idObj = data.get("id");
            Integer itineraryId = null;
            if (idObj instanceof Long) {
                itineraryId = ((Long) idObj).intValue();
            } else if (idObj instanceof Integer) {
                itineraryId = (Integer) idObj;
            } else if (idObj != null) {
                itineraryId = Integer.parseInt(idObj.toString());
            }
            
            Integer groupTourId = (Integer) data.get("groupTourId");
            Integer dayNumber = (Integer) data.get("day");
            String title = (String) data.get("title");
            String description = (String) data.get("description");
            String meals = (String) data.get("meals");
            String accommodation = (String) data.get("accommodation");
            
            // 调用服务更新行程
            groupTourService.updateGroupTourItinerary(
                itineraryId,
                groupTourId, 
                dayNumber, 
                title, 
                description, 
                meals, 
                accommodation
            );
            
            return Result.success("更新行程成功");
        } catch (Exception e) {
            log.error("更新行程失败：", e);
            return Result.error("更新行程失败：" + e.getMessage());
        }
    }

    /**
     * 获取团队游可用日期
     * @param tourId 团队游ID
     * @param params 查询参数
     * @return 可用日期列表
     */
    @GetMapping("/dates/{tourId}")
    @ApiOperation("获取团队游可用日期")
    public Result<List<Map<String, Object>>> getAvailableDates(@PathVariable Integer tourId, @RequestParam Map<String, Object> params) {
        log.info("获取团队游可用日期：{}, {}", tourId, params);
        List<Map<String, Object>> dates = groupTourService.getGroupTourAvailableDates(tourId, params);
        return Result.success(dates);
    }
    
    /**
     * 获取团队游关联的一日游
     * @param id 团队游ID
     * @return 关联的一日游列表
     */
    @GetMapping("/day-tours/{id}")
    @ApiOperation("获取团队游关联的一日游")
    public Result<List<Map<String, Object>>> getGroupTourDayTours(@PathVariable Integer id) {
        log.info("获取团队游关联的一日游，团队游ID：{}", id);
        List<Map<String, Object>> dayTours = groupTourService.getGroupTourDayTours(id);
        return Result.success(dayTours);
    }

    /**
     * 保存团队游关联的一日游
     * @param id 团队游ID
     * @param dayTours 关联的一日游数据
     * @return 操作结果
     */
    @PostMapping("/day-tours/{id}")
    @ApiOperation("保存团队游关联的一日游")
    public Result<String> saveGroupTourDayTours(@PathVariable Integer id, @RequestBody List<Map<String, Object>> dayTours) {
        log.info("保存团队游关联的一日游，团队游ID：{}，数据：{}", id, dayTours);
        groupTourService.saveGroupTourDayTours(id, dayTours);
        return Result.success("关联一日游保存成功");
    }

    /**
     * 获取所有可用的一日游
     * @return 一日游列表
     */
    @GetMapping("/available-daytours")
    @ApiOperation("获取所有可用的一日游")
    public Result<List<Map<String, Object>>> getAvailableDayTours() {
        log.info("获取所有可用的一日游");
        List<Map<String, Object>> dayTours = groupTourService.getAvailableDayTours();
        return Result.success(dayTours);
    }
    
    /**
     * 新增团队游
     * @param groupTourDTO 团队游信息
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("新增团队游")
    public Result<Integer> save(@RequestBody GroupTourDTO groupTourDTO) {
        log.info("新增团队游：{}", groupTourDTO);
        Integer id = groupTourService.saveGroupTour(groupTourDTO);
        return Result.success(id);
    }
    
    /**
     * 删除团队游
     * @param id 团队游ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除团队游")
    public Result<String> delete(@PathVariable Integer id) {
        log.info("删除团队游，ID：{}", id);
        groupTourService.deleteGroupTour(id);
        return Result.success("删除团队游成功");
    }
    
    /**
     * 获取团队游亮点
     * @param id 团队游ID
     * @return 亮点列表
     */
    @GetMapping("/highlights/{id}")
    @ApiOperation("获取团队游亮点")
    public Result<List<String>> getHighlights(@PathVariable Integer id) {
        log.info("获取团队游亮点，ID：{}", id);
        List<String> highlights = groupTourService.getGroupTourHighlights(id);
        return Result.success(highlights);
    }
    
    /**
     * 获取团队游包含项目
     * @param id 团队游ID
     * @return 包含项目列表
     */
    @GetMapping("/inclusions/{id}")
    @ApiOperation("获取团队游包含项目")
    public Result<List<String>> getInclusions(@PathVariable Integer id) {
        log.info("获取团队游包含项目，ID：{}", id);
        List<String> inclusions = groupTourService.getGroupTourInclusions(id);
        return Result.success(inclusions);
    }
    
    /**
     * 获取团队游不包含项目
     * @param id 团队游ID
     * @return 不包含项目列表
     */
    @GetMapping("/exclusions/{id}")
    @ApiOperation("获取团队游不包含项目")
    public Result<List<String>> getExclusions(@PathVariable Integer id) {
        log.info("获取团队游不包含项目，ID：{}", id);
        List<String> exclusions = groupTourService.getGroupTourExclusions(id);
        return Result.success(exclusions);
    }
    
    /**
     * 获取团队游常见问题
     * @param id 团队游ID
     * @return 常见问题列表
     */
    @GetMapping("/faqs/{id}")
    @ApiOperation("获取团队游常见问题")
    public Result<List<Map<String, Object>>> getFaqs(@PathVariable Integer id) {
        log.info("获取团队游常见问题，ID：{}", id);
        List<Map<String, Object>> faqs = groupTourService.getGroupTourFaqs(id);
        return Result.success(faqs);
    }
    
    /**
     * 获取团队游贴士
     * @param id 团队游ID
     * @return 贴士列表
     */
    @GetMapping("/tips/{id}")
    @ApiOperation("获取团队游贴士")
    public Result<List<String>> getTips(@PathVariable Integer id) {
        log.info("获取团队游贴士，ID：{}", id);
        List<String> tips = groupTourService.getGroupTourTips(id);
        return Result.success(tips);
    }
    
    /**
     * 团队游上架/下架
     * @param status 状态（0-下架，1-上架）
     * @param id 团队游ID
     * @return 操作结果
     */
    @PostMapping("/status/{status}")
    @ApiOperation("团队游上架/下架")
    public Result<String> enableOrDisable(@PathVariable Integer status, @RequestParam Integer id) {
        log.info("团队游上架/下架，状态：{}，ID：{}", status, id);
        groupTourService.enableOrDisableGroupTour(status, id);
        return Result.success("操作成功");
    }

    /**
     * 获取主题列表
     * @return 主题列表
     */
    @GetMapping("/themes")
    @ApiOperation("获取主题列表")
    public Result<List<Map<String, Object>>> getThemes() {
        log.info("获取主题列表");
        List<Map<String, Object>> themes = groupTourService.getGroupTourThemes();
        return Result.success(themes);
    }
    
    /**
     * 获取适合人群列表
     * @return 适合人群列表
     */
    @GetMapping("/suitables")
    @ApiOperation("获取适合人群列表")
    public Result<List<Map<String, Object>>> getSuitables() {
        log.info("获取适合人群列表");
        List<Map<String, Object>> suitables = tourService.getSuitableForOptions();
        return Result.success(suitables);
    }
    
    /**
     * 上传产品展示图片
     */
    @PostMapping("/product-showcase-image/{groupTourId}")
    @ApiOperation("上传产品展示图片")
    public Result<String> uploadProductShowcaseImage(@PathVariable Integer groupTourId,
                                                     @RequestParam("file") MultipartFile file) {
        log.info("上传团体游产品展示图片，ID：{}, 文件名：{}", groupTourId, file.getOriginalFilename());
        try {
            // 上传图片并获取URL
            String imageUrl = imageService.upload(file);
            
            // 更新团体游的产品展示图片字段
            groupTourService.updateProductShowcaseImage(groupTourId, imageUrl);
            
            return Result.success(imageUrl);
        } catch (Exception e) {
            log.error("上传产品展示图片失败：{}", e.getMessage(), e);
            return Result.error("上传产品展示图片失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除产品展示图片
     */
    @DeleteMapping("/product-showcase-image/{groupTourId}")
    @ApiOperation("删除产品展示图片")
    public Result<String> deleteProductShowcaseImage(@PathVariable Integer groupTourId) {
        log.info("删除团体游产品展示图片，ID：{}", groupTourId);
        try {
            // 清空团体游的产品展示图片字段
            groupTourService.updateProductShowcaseImage(groupTourId, null);
            return Result.success();
        } catch (Exception e) {
            log.error("删除产品展示图片失败：{}", e.getMessage(), e);
            return Result.error("删除产品展示图片失败：" + e.getMessage());
        }
    }
} 