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

    @PutMapping("/operator/resetPassword")
    @ApiOperation("重置操作员密码")
    public Result<String> resetPassword(@RequestBody java.util.Map<String, Object> params) {
        try {
            Long operatorId = Long.valueOf(params.get("id").toString());
            String newPassword = (String) params.get("newPassword");
            
            log.info("管理员重置操作员密码：operatorId={}", operatorId);
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }
            
            agentOperatorService.resetPassword(operatorId, newPassword);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            log.error("重置操作员密码失败", e);
            return Result.error("密码重置失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/operator/{id}")
    @ApiOperation("删除操作员")
    public Result<String> delete(@PathVariable Long id) {
        agentOperatorService.delete(id);
        return Result.success();
    }
}

