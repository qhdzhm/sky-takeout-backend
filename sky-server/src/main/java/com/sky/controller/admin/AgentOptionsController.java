package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.AgentService;
import com.sky.vo.AgentSimpleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 代理商下拉选项相关接口
 */
@RestController
@RequestMapping("/admin/agent")
@Api(tags = "代理商下拉选项相关接口")
@Slf4j
public class AgentOptionsController {

    @Autowired
    private AgentService agentService;

    /**
     * 获取代理商下拉选项列表（支持名称模糊搜索）
     * @param name 代理商名称关键字（可选）
     * @param id 代理商ID（可选，用于精确查询）
     * @return 代理商简略信息列表
     */
    @GetMapping("/options")
    @ApiOperation("获取代理商下拉选项")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "代理商名称关键字", dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "id", value = "代理商ID", dataType = "integer", paramType = "query")
    })
    public Result<List<AgentSimpleVO>> getAgentOptions(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "id", required = false) Long id) {
        log.info("获取代理商下拉选项，name: {}, id: {}", name, id);
        List<AgentSimpleVO> list = agentService.getAgentOptions(name, id);
        return Result.success(list);
    }
} 