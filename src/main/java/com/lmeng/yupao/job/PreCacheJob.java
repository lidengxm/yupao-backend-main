package com.lmeng.yupao.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmeng.yupao.mapper.UserMapper;
import com.lmeng.yupao.model.User;
import com.lmeng.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/7/7
 */
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    //重点用户
    private List<Long> mainUserList = Arrays.asList();

    //每天8点整定时执行预热用户信息
    @Scheduled(cron = "0 0 8 * * *")
    public void doPreCacheJob() {
        for (Long userId : mainUserList) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            Page<User> userList = userService.page(new Page(1,20),queryWrapper);
            String redisKey = String.format("yupao:user:recommend:%s",userId);
            ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
            //写入缓存
            try {
                valueOperations.set(redisKey,userList,10, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("redis set key error",e);
            }
        }
    }
}
