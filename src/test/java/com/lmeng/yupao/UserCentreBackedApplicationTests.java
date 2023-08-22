package com.lmeng.yupao;

import com.lmeng.yupao.mapper.UserMapper;
import com.lmeng.yupao.model.domain.User;
import com.lmeng.yupao.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.*;

@SpringBootTest
class UserCentreBackedApplicationTests {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @Test
    void testHashMap() {

    }

    @Test
    void contextLoads() {
        System.out.println(("----- selectAll method test ------"));
        List<User> userList = userMapper.selectList(null);
        Assert.assertEquals(5, userList.size());
        userList.forEach(System.out::println);
    }

    @Test
    void testPassword() {
        Map<String,Object> map = new HashMap<>();
        map.put("lili",new ArrayList<>());

    }

//    @Test
//    public void testSearchUserTags() {
//        List<String> tagNameList = Arrays.asList("java","python");
//        List<User> userList = userService.searchByTags(tagNameList);
//        Assert.assertNotNull(userList);
//    }


}
