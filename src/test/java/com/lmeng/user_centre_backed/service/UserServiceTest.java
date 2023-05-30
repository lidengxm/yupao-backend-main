package com.lmeng.user_centre_backed.service;

import com.lmeng.user_centre_backed.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/27
 */

/**
 * 用户服务测试
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("yupi");
        user.setUserAccount("123");
        user.setAvatarUrl("git@gitee.com:Lmeng-r2coding/user_centre.git");
        user.setGender(0);
        user.setPassword("123456");
        user.setEmail("556565");
        user.setPhone("666");

        boolean save = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(save);
    }


    @Test
    void userRegister() {
        String userAccount = "yupi";
        String password = "";
        String checkPassword = "123456";
        String plannetCode = "22342";
        long result = userService.userRegister(userAccount, password, checkPassword,plannetCode);
        Assertions.assertEquals(-1,result);

        userAccount = "yu";
        result = userService.userRegister(userAccount, password, checkPassword,plannetCode);
        Assertions.assertEquals(-1,result);

        userAccount = "yupi";
        password = "123456";
        result = userService.userRegister(userAccount, password, checkPassword,plannetCode);
        Assertions.assertEquals(-1,result);

        userAccount = "yu pi";
        password = "12345678";
        result = userService.userRegister(userAccount, password, checkPassword,plannetCode);
        Assertions.assertEquals(-1,result);

        checkPassword = "123456789";
        result = userService.userRegister(userAccount, password, checkPassword,plannetCode);
        Assertions.assertEquals(-1,result);

        userAccount = "dogyupi";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, password, checkPassword,plannetCode);
        Assertions.assertEquals(-1,result);

        userAccount = "yupi";
        result = userService.userRegister(userAccount, password, checkPassword,plannetCode);
        Assertions.assertTrue(result > 0);

    }
}