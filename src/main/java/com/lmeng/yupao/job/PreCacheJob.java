package com.lmeng.yupao.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmeng.yupao.model.domain.User;
import com.lmeng.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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

    @Resource
    private RedissonClient redissonClient;

    //重点用户
    private List<Long> mainUserList = Arrays.asList();

    //每天12点整定时执行预热用户信息
    @Scheduled(cron = "0 0 12 * * *")
    public void doPreCacheJob() {
        //Redisson实现分布式锁
        RLock lock = redissonClient.getLock("yupao:preCacheJob:doPreCache:lock");
        try {
            //如果当前线程获得了锁
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)) {
                System.out.println("getLock: "+Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userList = userService.page(new Page(1,20),queryWrapper);
                    String redisKey = String.format("yupao:user:recommend:%s",userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    //写入缓存
                    try {
                        valueOperations.set(redisKey,userList,10, TimeUnit.HOURS);
                    } catch (Exception e) {
                        log.error("redis set key error",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.info("doPreCacheJob error",e);
        } finally {
            //执行完任务一定要释放锁（先检查是否是当前线程加的锁）
            System.out.println("unLock: "+Thread.currentThread().getId());
            if(lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }
}
