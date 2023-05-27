package com.lmeng.user_centre_backed;

import com.lmeng.user_centre_backed.entity.User;
import com.lmeng.user_centre_backed.mapper.UserMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/27
 */
//@SpringBootTest(classes = )
@RunWith(SpringRunner.class)
public class SampleTest {
    //默认按照javabean的名称注入
    @Resource
    private UserMapper userMapper;

    @Test
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        List<User> userList = userMapper.selectList(null);
        Assert.assertEquals(5, userList.size());
        userList.forEach(System.out::println);
    }

}
