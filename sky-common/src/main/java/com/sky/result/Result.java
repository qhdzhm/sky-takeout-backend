package com.sky.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败，2为警告确认
    private String msg; //错误信息
    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 1;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = 0;
        return result;
    }

    /**
     * 返回警告确认结果
     * @param msg 警告消息
     * @return 结果对象
     */
    public static <T> Result<T> warningConfirm(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = 2; // 2表示警告确认，需要前端二次确认
        return result;
    }
    
    /**
     * 返回自定义错误码的错误结果
     * @param code 自定义错误码
     * @param msg 错误消息
     * @return 结果对象
     */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = code;
        return result;
    }
}
