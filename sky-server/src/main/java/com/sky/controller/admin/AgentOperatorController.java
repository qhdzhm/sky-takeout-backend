package com.sky.controller.admin;

import com.sky.entity.AgentOperator;
import com.sky.result.Result;
import com.sky.service.AgentOperatorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/agent")
@Api(tags = "代理商操作员管理")
@Slf4j
public class AgentOperatorController {

    @Autowired
    private AgentOperatorService agentOperatorService;

    @GetMapping("/{agentId}/operators")
    @ApiOperation("根据代理商ID获取操作员列表")
    public Result<List<AgentOperator>> list(@PathVariable Long agentId) {
        return Result.success(agentOperatorService.getByAgentId(agentId));
    }

    @PostMapping("/{agentId}/operator")
    @ApiOperation("为代理商创建操作员")
    public Result<String> create(@PathVariable Long agentId, @RequestBody AgentOperator body) {
        body.setAgentId(agentId);
        body.setCreatedAt(LocalDateTime.now());
        body.setUpdatedAt(LocalDateTime.now());
        agentOperatorService.create(body);
        return Result.success();
    }

    @PutMapping("/operator")
    @ApiOperation("更新操作员信息")
    public Result<String> update(@RequestBody AgentOperator body) {
        agentOperatorService.update(body);
        return Result.success();
    }

    @PutMapping("/operator/status/{status}")
    @ApiOperation("更新操作员状态")
    public Result<String> updateStatus(@PathVariable Integer status, @RequestBody AgentOperator body) {
        agentOperatorService.updateStatus(body.getId(), status);
        return Result.success();
    }

    // 预留：重置密码接口（前端暂未使用），避免误用

    @DeleteMapping("/operator/{id}")
    @ApiOperation("删除操作员")
    public Result<String> delete(@PathVariable Long id) {
        agentOperatorService.delete(id);
        return Result.success();
    }
}

