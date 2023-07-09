package com.lmeng.yupao.service;

import com.lmeng.yupao.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/7/7
 */
@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations operations = redisTemplate.opsForValue();
        //设置值
        operations.set("name","dog");
        operations.set("userAccount","yupi");
        operations.set("userPassword","123456");
        User user = new User();
        user.setId(1L);
        operations.set("name","dogyupi");
        //查
        Object name = operations.get("name");
        Assertions.assertTrue("dogyupi".equals(name));
        Object userAccount = operations.get("userAccount");
        Assertions.assertTrue("yupi".equals(userAccount));
        System.out.println(operations.get("name"));
    }

}
