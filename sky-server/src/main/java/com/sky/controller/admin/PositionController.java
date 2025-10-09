package com.sky.controller.admin;

import com.sky.dto.EmployeeAssignDTO;
import com.sky.dto.PositionDTO;
import com.sky.result.Result;
import com.sky.service.PositionService;
import com.sky.vo.PositionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职位管理Controller
 */
@RestController
@RequestMapping("/admin/positions")
@Api(tags = "职位管理相关接口")
@Slf4j
public class PositionController {

    @Autowired
    private PositionService positionService;

    /**
     * 获取所有职位列表
     */
    @GetMapping
    @ApiOperation("获取所有职位列表")
    public Result<List<PositionVO>> getAllPositions() {
        log.info("获取所有职位列表");
        
        List<PositionVO> result = positionService.getAllPositions();
        return Result.success(result);
    }

    /**
     * 根据ID获取职位信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取职位信息")
    public Result<PositionVO> getPositionById(
            @ApiParam(name = "id", value = "职位ID", required = true)
            @PathVariable Long id) {
        log.info("根据ID获取职位信息：{}", id);
        
        PositionVO result = positionService.getPositionById(id);
        return Result.success(result);
    }

    /**
     * 根据部门ID获取职位列表
     */
    @GetMapping("/by-department/{deptId}")
    @ApiOperation("根据部门ID获取职位列表")
    public Result<List<PositionVO>> getPositionsByDeptId(
            @ApiParam(name = "deptId", value = "部门ID", required = true)
            @PathVariable Long deptId) {
        log.info("根据部门ID获取职位列表：{}", deptId);
        
        List<PositionVO> result = positionService.getPositionsByDeptId(deptId);
        return Result.success(result);
    }

    /**
     * 创建职位
     */
    @PostMapping
    @ApiOperation("创建职位")
    public Result<Long> createPosition(@RequestBody PositionDTO positionDTO) {
        log.info("创建职位：{}", positionDTO);
        
        Long positionId = positionService.createPosition(positionDTO);
        return Result.success(positionId);
    }

    /**
     * 更新职位信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新职位信息")
    public Result<Void> updatePosition(
            @ApiParam(name = "id", value = "职位ID", required = true)
            @PathVariable Long id,
            @RequestBody PositionDTO positionDTO) {
        log.info("更新职位信息，ID：{}，数据：{}", id, positionDTO);
        
        positionDTO.setId(id);
        positionService.updatePosition(positionDTO);
        return Result.success();
    }

    /**
     * 删除职位
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除职位")
    public Result<Void> deletePosition(
            @ApiParam(name = "id", value = "职位ID", required = true)
            @PathVariable Long id) {
        log.info("删除职位：{}", id);
        
        positionService.deletePosition(id);
        return Result.success();
    }

    /**
     * 根据职位代码查询职位
     */
    @GetMapping("/by-code/{positionCode}")
    @ApiOperation("根据职位代码查询职位")
    public Result<PositionVO> getPositionByCode(
            @ApiParam(name = "positionCode", value = "职位代码", required = true)
            @PathVariable String positionCode) {
        log.info("根据职位代码查询职位：{}", positionCode);
        
        PositionVO result = positionService.getPositionByCode(positionCode);
        return Result.success(result);
    }

    /**
     * 获取职位详细信息
     */
    @GetMapping("/details")
    @ApiOperation("获取职位详细信息")
    public Result<List<PositionVO>> getPositionDetails() {
        log.info("获取职位详细信息");
        
        List<PositionVO> result = positionService.getPositionDetails();
        return Result.success(result);
    }

    /**
     * 分配员工到部门职位
     */
    @PostMapping("/assign-employee")
    @ApiOperation("分配员工到部门职位")
    public Result<Void> assignEmployeeToPosition(@RequestBody EmployeeAssignDTO employeeAssignDTO) {
        log.info("分配员工到部门职位：{}", employeeAssignDTO);
        
        positionService.assignEmployeeToPosition(employeeAssignDTO);
        return Result.success();
    }

    /**
     * 批量分配员工到部门职位
     */
    @PostMapping("/batch-assign-employees")
    @ApiOperation("批量分配员工到部门职位")
    public Result<Void> batchAssignEmployees(@RequestBody List<EmployeeAssignDTO> assignList) {
        log.info("批量分配员工到部门职位，数量：{}", assignList.size());
        
        positionService.batchAssignEmployees(assignList);
        return Result.success();
    }

    /**
     * 获取可分配的职位列表
     */
    @GetMapping("/available")
    @ApiOperation("获取可分配的职位列表")
    public Result<List<PositionVO>> getAvailablePositions(
            @ApiParam(name = "deptId", value = "部门ID（可选）")
            @RequestParam(required = false) Long deptId) {
        log.info("获取可分配的职位列表，部门ID：{}", deptId);
        
        List<PositionVO> result = positionService.getAvailablePositions(deptId);
        return Result.success(result);
    }
}

