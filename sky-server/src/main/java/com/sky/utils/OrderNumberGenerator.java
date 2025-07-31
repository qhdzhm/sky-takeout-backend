package com.sky.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单号生成器
 */
public class OrderNumberGenerator {
    
    // 前缀，表示Happy Tassie Travel
    private static final String PREFIX = "HT";
    
    // 日期格式
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    // 序列号，用于同一毫秒内的订单编号区分
    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);
    
    // 序列号最大值
    private static final int MAX_SEQUENCE = 9999;
    
    // 随机数生成器
    private static final Random RANDOM = new Random();
    
    /**
     * 生成订单号
     * 格式: 前缀 + 日期 + 4位序列号 + 2位随机数
     * 例如: HT202503150001XX
     * @return 订单号
     */
    public static String generate() {
        // 获取当前日期时间
        LocalDateTime now = LocalDateTime.now();
        
        // 格式化日期
        String date = now.format(DATE_FORMATTER);
        
        // 获取序列号，并重置
        int sequence = SEQUENCE.getAndIncrement();
        if (sequence > MAX_SEQUENCE) {
            SEQUENCE.set(1);
            sequence = 1;
        }
        
        // 生成两位随机数
        int randomNum = RANDOM.nextInt(100);
        
        // 组合订单号: 前缀 + 日期 + 序列号(4位) + 随机数(2位)
        return String.format("%s%s%04d%02d", PREFIX, date, sequence, randomNum);
    }
} 