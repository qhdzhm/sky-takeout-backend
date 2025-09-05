package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.dto.TransferDutyDTO;
import com.sky.result.Result;
import com.sky.service.DutyShiftService;
import com.sky.service.OperatorAssignmentService;
import com.sky.vo.CurrentDutyStatusVO;
import com.sky.vo.DutyShiftVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 值班管理Controller
 */
@RestController
@RequestMapping("/admin/duty-shift")
@Api(tags = "值班管理接口")
@Slf4j
public class DutyShiftController {

    @Autowired
    private DutyShiftService dutyShiftService;

    @Autowired
    private OperatorAssignmentService operatorAssignmentService;

    /**
     * 开始排团主管值班
     */
    @PostMapping("/start-tour-master")
    @ApiOperation("开始排团主管值班")
    public Result<String> startTourMasterDuty() {
        Long currentEmployeeId = BaseContext.getCurrentId();
        log.info("开始排团主管值班：{}", currentEmployeeId);

        // 检查是否可以开始值班
        if (!dutyShiftService.canStartTourMasterDuty(currentEmployeeId)) {
            return Result.error("无法开始排团主管值班：权限不足或已有其他人在值班");
        }

        dutyShiftService.startDuty(currentEmployeeId, "tour_master");
        return Result.success("排团主管值班开始");
    }

    /**
     * 转移排团主管值班
     */
    @PostMapping("/transfer-tour-master")
    @ApiOperation("转移排团主管值班")
    public Result<String> transferTourMasterDuty(@Valid @RequestBody TransferDutyDTO transferDutyDTO) {
        Long currentEmployeeId = BaseContext.getCurrentId();
        log.info("转移排团主管值班：{} -> {}", currentEmployeeId, transferDutyDTO.getToOperatorId());

        // 验证当前用户是否为排团主管
        if (!operatorAssignmentService.isTourMaster(currentEmployeeId)) {
            return Result.error("权限不足：只有当前排团主管可以转移值班");
        }

        transferDutyDTO.setDutyType("tour_master"); // 确保是排团主管转移
        dutyShiftService.transferDuty(currentEmployeeId, transferDutyDTO);
        
        return Result.success("排团主管值班转移成功");
    }

    /**
     * 结束值班
     */
    @PostMapping("/end/{dutyType}")
    @ApiOperation("结束值班")
    public Result<String> endDuty(@PathVariable String dutyType) {
        Long currentEmployeeId = BaseContext.getCurrentId();
        log.info("结束值班：{} 类型：{}", currentEmployeeId, dutyType);

        // 特殊处理：排团主管不允许直接结束值班，必须转移给其他人
        if ("tour_master".equals(dutyType)) {
            return Result.error("排团主管不能直接结束值班，请转移给其他操作员");
        }

        dutyShiftService.endDuty(currentEmployeeId, dutyType);
        return Result.success("值班已结束");
    }

    /**
     * 获取当前排团主管信息
     */
    @GetMapping("/current-tour-master")
    @ApiOperation("获取当前排团主管信息")
    public Result<CurrentDutyStatusVO> getCurrentTourMaster() {
        log.info("获取当前排团主管信息");

        CurrentDutyStatusVO currentStatus = dutyShiftService.getCurrentTourMaster();
        return Result.success(currentStatus);
    }

    /**
     * 获取当前值班状态
     */
    @GetMapping("/current/{dutyType}")
    @ApiOperation("获取当前值班状态")
    public Result<DutyShiftVO> getCurrentDutyByType(@PathVariable String dutyType) {
        log.info("获取当前值班状态：{}", dutyType);

        DutyShiftVO dutyShift = dutyShiftService.getCurrentDutyByType(dutyType);
        return Result.success(dutyShift);
    }

    /**
     * 获取我的值班历史
     */
    @GetMapping("/my-history")
    @ApiOperation("获取我的值班历史")
    public Result<List<DutyShiftVO>> getMyShiftHistory() {
        Long currentEmployeeId = BaseContext.getCurrentId();
        log.info("获取值班历史：{}", currentEmployeeId);

        List<DutyShiftVO> history = dutyShiftService.getShiftHistoryByOperatorId(currentEmployeeId);
        return Result.success(history);
    }

    /**
     * 获取指定操作员的值班历史
     */
    @GetMapping("/history/{operatorId}")
    @ApiOperation("获取指定操作员的值班历史")
    public Result<List<DutyShiftVO>> getShiftHistoryByOperatorId(@PathVariable Long operatorId) {
        log.info("获取操作员值班历史：{}", operatorId);

        // 只有排团主管可以查看其他操作员的值班历史
        Long currentEmployeeId = BaseContext.getCurrentId();
        if (!operatorAssignmentService.isTourMaster(currentEmployeeId) && !operatorId.equals(currentEmployeeId)) {
            return Result.error("权限不足：只能查看自己的值班历史");
        }

        List<DutyShiftVO> history = dutyShiftService.getShiftHistoryByOperatorId(operatorId);
        return Result.success(history);
    }

    /**
     * 获取所有值班记录
     */
    @GetMapping("/all")
    @ApiOperation("获取所有值班记录")
    public Result<List<DutyShiftVO>> getAllShifts() {
        log.info("获取所有值班记录");

        // 只有排团主管可以查看所有值班记录
        Long currentEmployeeId = BaseContext.getCurrentId();
        if (!operatorAssignmentService.isTourMaster(currentEmployeeId)) {
            return Result.error("权限不足：只有排团主管可以查看所有值班记录");
        }

        List<DutyShiftVO> shifts = dutyShiftService.getAllShifts();
        return Result.success(shifts);
    }

    /**
     * 获取值班统计信息
     */
    @GetMapping("/statistics/{operatorId}")
    @ApiOperation("获取值班统计信息")
    public Result<Object> getShiftStatistics(@PathVariable Long operatorId) {
        log.info("获取值班统计：{}", operatorId);

        // 只有排团主管可以查看其他操作员的统计，或者查看自己的统计
        Long currentEmployeeId = BaseContext.getCurrentId();
        if (!operatorAssignmentService.isTourMaster(currentEmployeeId) && !operatorId.equals(currentEmployeeId)) {
            return Result.error("权限不足：只能查看自己的统计信息");
        }

        Object statistics = dutyShiftService.getShiftStatistics(operatorId);
        return Result.success(statistics);
    }

    /**
     * 检查是否可以开始排团主管值班
     */
    @GetMapping("/can-start-tour-master")
    @ApiOperation("检查是否可以开始排团主管值班")
    public Result<Boolean> canStartTourMasterDuty() {
        Long currentEmployeeId = BaseContext.getCurrentId();
        log.info("检查是否可以开始排团主管值班：{}", currentEmployeeId);

        boolean canStart = dutyShiftService.canStartTourMasterDuty(currentEmployeeId);
        return Result.success(canStart);
    }

    /**
     * 获取可用的排团主管候选人
     */
    @GetMapping("/tour-master-candidates")
    @ApiOperation("获取可用的排团主管候选人")
    public Result<List<Object>> getAvailableTourMasterCandidates() {
        log.info("获取可用的排团主管候选人");

        // 只有当前排团主管或管理员可以查看候选人
        Long currentEmployeeId = BaseContext.getCurrentId();
        if (!operatorAssignmentService.isTourMaster(currentEmployeeId)) {
            return Result.error("权限不足：只有排团主管可以查看候选人");
        }

        List<Object> candidates = dutyShiftService.getAvailableTourMasterCandidates();
        return Result.success(candidates);
    }

    /**
     * 强制结束值班（管理员功能）
     */
    @PostMapping("/force-end/{operatorId}/{dutyType}")
    @ApiOperation("强制结束值班（管理员功能）")
    public Result<String> forceEndDuty(@PathVariable Long operatorId, @PathVariable String dutyType) {
        log.info("强制结束值班：操作员：{} 类型：{}", operatorId, dutyType);

        // 这个功能需要额外的管理员权限验证
        // TODO: 实现管理员权限检查
        
        dutyShiftService.endDuty(operatorId, dutyType);
        return Result.success("值班已强制结束");
    }

    /**
     * 紧急转移排团主管值班（系统管理员功能）
     */
    @PostMapping("/emergency-transfer")
    @ApiOperation("紧急转移排团主管值班（系统管理员功能）")
    public Result<String> emergencyTransferDuty(@RequestBody TransferDutyDTO transferDutyDTO) {
        log.info("紧急转移值班：{}", transferDutyDTO);

        // 获取当前排团主管
        CurrentDutyStatusVO currentTourMaster = dutyShiftService.getCurrentTourMaster();
        if (currentTourMaster.getCurrentTourMasterId() == null) {
            return Result.error("当前无排团主管在值班");
        }

        // TODO: 这里需要额外的管理员权限验证

        transferDutyDTO.setDutyType("tour_master");
        dutyShiftService.transferDuty(currentTourMaster.getCurrentTourMasterId(), transferDutyDTO);

        return Result.success("紧急转移成功");
    }
}
