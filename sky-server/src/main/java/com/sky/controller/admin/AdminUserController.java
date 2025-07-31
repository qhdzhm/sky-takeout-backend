package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserPageQueryDTO;
import com.sky.dto.UserStatusDTO;
import com.sky.dto.UserPasswordResetDTO;
import com.sky.entity.User;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理相关接口
 */
@RestController
@RequestMapping("/admin/users")
@Slf4j
@Api(tags = "管理员-用户管理相关接口")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户
     * @param userPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询用户")
    public Result<PageResult> page(UserPageQueryDTO userPageQueryDTO) {
        log.info("分页查询用户：{}", userPageQueryDTO);
        PageResult pageResult = userService.pageQuery(userPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询用户
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询用户")
    public Result<User> getById(@PathVariable Long id) {
        log.info("根据ID查询用户：{}", id);
        User user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 创建新用户
     * @param user
     * @return
     */
    @PostMapping
    @ApiOperation("创建新用户")
    public Result<String> create(@RequestBody User user) {
        log.info("创建新用户：{}", user);
        userService.createUser(user);
        return Result.success();
    }

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    @PutMapping
    @ApiOperation("修改用户信息")
    public Result<String> update(@RequestBody User user) {
        log.info("修改用户信息：{}", user);
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 删除用户
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除用户")
    public Result<String> delete(@PathVariable Long id) {
        log.info("删除用户：{}", id);
        userService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改用户状态
     * @param userStatusDTO
     * @return
     */
    @PutMapping("/status")
    @ApiOperation("修改用户状态")
    public Result<String> updateStatus(@RequestBody UserStatusDTO userStatusDTO) {
        log.info("修改用户状态：{}", userStatusDTO);
        userService.updateStatus(userStatusDTO);
        return Result.success();
    }

    /**
     * 重置用户密码
     * @param userPasswordResetDTO 包含用户ID和新密码的DTO
     * @return
     */
    @PutMapping("/password")
    @ApiOperation("重置用户密码")
    public Result<String> resetPassword(@RequestBody UserPasswordResetDTO userPasswordResetDTO) {
        log.info("重置用户密码：{}", userPasswordResetDTO);
        userService.resetPassword(userPasswordResetDTO);
        return Result.success();
    }
} 