package com.sky.exception;

/**
 * 价格变动异常
 * 当订单创建时价格与前端计算的价格不一致时抛出
 */
public class PriceChangedException extends RuntimeException {
    
    public PriceChangedException() {
        super();
    }
    
    public PriceChangedException(String message) {
        super(message);
    }
    
    public PriceChangedException(String message, Throwable cause) {
        super(message, cause);
    }
}







