package com.lmeng.yupao.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/7/8
 */
@SpringBootTest
@Slf4j
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        //list 数据存在本地JVM内存中
        List<String> list = new ArrayList<>();
        list.add("yupi");
        System.out.println("list:" + list.get(0));
        //list.remove(0);

        //redisson 数据存储在redis中
        RList<Object> list1 = redissonClient.getList("list-test");
        list1.add("yupi");
        System.out.println("list:" + list1.get(0));
        list1.remove(0);

        //map

        //set


        //stack
    }

    @Test
    void testWatchDog() {
        //Redisson实现分布式锁
        RLock lock = redissonClient.getLock("yupao:preCacheJob:doPreCache:lock");
        try {
            //如果当前线程获得了锁
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)) {
                //todo
                Thread.sleep(300000);
                System.out.println("getLock: "+Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            //执行完任务一定要释放锁（先检查是否是当前线程加的锁）
            System.out.println("unLock: "+Thread.currentThread().getId());
            if(lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
