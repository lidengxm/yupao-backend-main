package com.lmeng.yupao.service;

import com.lmeng.yupao.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/7/6
 */
@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    /**
     * 批量插入用户（串行插入）
     */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<User> list = new ArrayList<>();
        final int INSERT_NUM = 100000;
        for(int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假用户");
            user.setUserAccount("fakeyu");
            user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
            user.setGender(0);
            user.setUserPassword("123456");
            user.setUserRole(0);
            user.setEmail("66556");
            user.setUserStatus(0);
            user.setPhone("895645");
            user.setPlanetCode("22365");
            user.setTags("[]");
            user.setProfile("你好");
        }
        //批量插入
        userService.saveBatch(list,10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    private ExecutorService executorService = new ThreadPoolExecutor(40,1000,10000,
            TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));
    /**
     * 并发执行（并发插入）
     */
    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //分10组
        int batchSize = 5000;
        int j=0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("假数据");
                user.setUserAccount("fakeaccount");
                user.setAvatarUrl("https://img1.baidu.com/it/u=1645832847,2375824523&fm=253&fmt=auto&app=138&f=JPEG?w=480&h=480");
                user.setGender(0);
                user.setUserPassword("231313123");
                user.setPhone("1231312");
                user.setEmail("12331234@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("213123");
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize==0){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
                System.out.println("threadName:"+Thread.currentThread().getName());
                userService.saveBatch(userList,batchSize);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
