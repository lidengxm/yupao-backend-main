package com.lmeng.yupao.service;

import com.lmeng.yupao.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/6/28
 */
@SpringBootTest
public class UserServiceTest {
    @Resource
    private UserService userService;

//    @Test
//    public void testSearchUserTags() {
//        List<String> tagNameList = Arrays.asList("java","python");
//        List<User> userList = userService.searchByTags(tagNameList);
//        Assert.assertNotNull(userList);
//    }
}
