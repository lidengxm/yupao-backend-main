package com.lmeng.yupao.once;

import com.lmeng.yupao.mapper.UserMapper;
import com.lmeng.yupao.model.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/7/6
 */
//被spring加载
@Component
public class InsertUsers {
    public static void main(String[] args) {
        new InsertUsers().doInsertUsers();
    }

    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户
     */
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
         final int INSERT_NUM = 1000000;
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
             user.setPlannetCode("22365");
             user.setTags("[]");
             user.setProfile("你好");
         }
         stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }


}
