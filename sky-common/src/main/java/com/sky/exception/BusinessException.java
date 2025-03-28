package com.sky.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private boolean isWarning; // 是否为警告（可以继续操作）
    private Integer code;

    public BusinessException() {
    }

    public BusinessException(String message) {
        super(message);
        this.isWarning = false;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.isWarning = false;
    }
    
    public BusinessException(String message, boolean isWarning) {
        super(message);
        this.isWarning = isWarning;
    }

    public boolean isWarning() {
        return isWarning;
    }
    
    public void setWarning(boolean isWarning) {
        this.isWarning = isWarning;
    }

    public Integer getCode() {
        return code;
    }
} 