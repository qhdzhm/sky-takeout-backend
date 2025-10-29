package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.exception.BusinessException;
import com.sky.exception.PriceChangedException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException e){
        String message = e.getMessage();
        log.error("SQL约束违反异常：{}", message, e);
        if (message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username = split[2];
            return Result.error(username+" "+ MessageConstant.ALREADY_EXISTS);
        }else if (message.contains("cannot be null")) {
            return Result.error("数据完整性约束违反：必填字段不能为空");
        }else if (message.contains("foreign key constraint")) {
            return Result.error("外键约束违反：关联数据不存在");
        }else {
            return Result.error("数据库约束违反：" + message);
        }
    }

    @ExceptionHandler
    @ResponseBody
    public Result exceptionHandler(BusinessException ex) {
        log.error("业务异常：{}", ex.getMessage());
        if (ex.isWarning()) {
            // 如果是警告类型的异常，返回特殊的状态码和消息
            return Result.warningConfirm(ex.getMessage());
        }
        return Result.error(ex.getMessage());
    }

    /**
     * 🔒 P0安全修复：捕获价格变动异常
     * 当订单创建时价格与前端计算不一致时触发
     */
    @ExceptionHandler
    @ResponseBody
    public Result exceptionHandler(PriceChangedException ex) {
        log.error("🔒 价格变动异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获所有其他未处理的异常
     */
    @ExceptionHandler
    public Result exceptionHandler(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return Result.error("系统异常：" + e.getMessage());
    }
}
