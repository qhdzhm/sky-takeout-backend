package com.sky.controller.admin;

import com.sky.dto.AgentDTO;
import com.sky.dto.AgentPageQueryDTO;
import com.sky.dto.AgentPasswordResetDTO;
import com.sky.dto.AgentStatusDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AgentService;
import com.sky.vo.AgentSimpleVO;
import com.sky.vo.AgentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代理商管理控制器
 */
@RestController
@RequestMapping("/admin/agent")
@Api(tags = "代理商管理相关接口")
@Slf4j
public class AgentController {

    @Autowired
    private AgentService agentService;

    /**
     * 分页查询代理商列表
     * @param agentPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询代理商列表")
    public Result<PageResult> page(AgentPageQueryDTO agentPageQueryDTO) {
        log.info("分页查询代理商：{}", agentPageQueryDTO);
        PageResult pageResult = agentService.pageQuery(agentPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 新增代理商
     * @param agentDTO 代理商信息
     * @return 结果
     */
    @PostMapping
    @ApiOperation("新增代理商")
    public Result<String> save(@RequestBody AgentDTO agentDTO) {
        log.info("新增代理商：{}", agentDTO);
        agentService.save(agentDTO);
        return Result.success();
    }

    /**
     * 修改代理商信息
     * @param agentDTO 代理商信息
     * @return 结果
     */
    @PutMapping
    @ApiOperation("修改代理商信息")
    public Result<String> update(@RequestBody AgentDTO agentDTO) {
        log.info("修改代理商：{}", agentDTO);
        agentService.update(agentDTO);
        return Result.success();
    }

    /**
     * 根据ID获取代理商详情
     * @param id 代理商ID
     * @return 代理商详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取代理商详情")
    public Result<AgentVO> getById(@PathVariable Long id) {
        log.info("根据ID获取代理商：{}", id);
        AgentVO agentVO = agentService.getById(id);
        return Result.success(agentVO);
    }

    /**
     * 删除代理商
     * @param id 代理商ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除代理商")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除代理商：{}", id);
        agentService.deleteById(id);
        return Result.success();
    }

    /**
     * 启用/禁用代理商账号
     * @param status 状态
     * @param agentStatusDTO 代理商状态信息
     * @return 结果
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用/禁用代理商账号")
    public Result<String> updateStatus(@PathVariable Integer status, @RequestBody AgentStatusDTO agentStatusDTO) {
        log.info("更新代理商状态：{}, {}", status, agentStatusDTO);
        agentService.updateStatus(agentStatusDTO.getId(), status);
        return Result.success();
    }

    /**
     * 重置代理商密码
     * @param agentPasswordResetDTO 密码重置信息
     * @return 结果
     */
    @PutMapping("/resetPassword")
    @ApiOperation("重置代理商密码")
    public Result<String> resetPassword(@RequestBody AgentPasswordResetDTO agentPasswordResetDTO) {
        log.info("重置代理商密码：{}", agentPasswordResetDTO);
        agentService.resetPassword(agentPasswordResetDTO);
        return Result.success();
    }

    /**
     * 更新代理商折扣等级
     * @param agentId 代理商ID
     * @param discountLevelId 折扣等级ID
     * @return 结果
     */
    @PutMapping("/{agentId}/discount-level/{discountLevelId}")
    @ApiOperation("更新代理商折扣等级")
    public Result<String> updateDiscountLevel(@PathVariable Long agentId, @PathVariable Long discountLevelId) {
        log.info("更新代理商折扣等级：agentId={}, discountLevelId={}", agentId, discountLevelId);
        agentService.updateDiscountLevel(agentId, discountLevelId);
        return Result.success();
    }

    /**
     * 获取代理商列表（下拉选择使用）
     * @param name 代理商名称（可选，用于搜索）
     * @param id 代理商ID（可选，精确查询）
     * @return 代理商列表
     */
    @GetMapping("/list")
    @ApiOperation("获取代理商列表")
    public Result<List<AgentSimpleVO>> getAgentList(@RequestParam(required = false) String name, 
                                                  @RequestParam(required = false) Long id) {
        log.info("获取代理商列表，name: {}, id: {}", name, id);
        List<AgentSimpleVO> agents = agentService.getAgentOptions(name, id);
        return Result.success(agents);
    }
} 