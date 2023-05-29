package com.lmeng.user_centre_backed;

import com.lmeng.user_centre_backed.mapper.UserMapper;
import com.lmeng.user_centre_backed.model.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class UserCentreBackedApplicationTests {

    @Resource
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        System.out.println(("----- selectAll method test ------"));
        List<User> userList = userMapper.selectList(null);
        Assert.assertEquals(5, userList.size());
        userList.forEach(System.out::println);
    }

    @Test
    void testPassword() {

    }


}
