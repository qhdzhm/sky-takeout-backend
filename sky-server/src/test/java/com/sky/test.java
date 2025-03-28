package com.sky;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: test
 * Package: com.sky
 * Description:
 *
 * @Author Tangshifu
 * @Create 2024/7/14 17:31
 * @Version 1.0
 */
//@SpringBootTest
public class test {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test(){
        System.out.println(redisTemplate);
        redisTemplate.opsForValue().set("code","1234",30, TimeUnit.SECONDS);
        redisTemplate.opsForValue().setIfAbsent("lock",1);
        redisTemplate.opsForValue().setIfAbsent("lock",2);
    }
}
