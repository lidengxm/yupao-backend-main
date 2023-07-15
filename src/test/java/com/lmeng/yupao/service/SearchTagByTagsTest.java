package com.lmeng.yupao.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
 * @date 2023/7/14
 */
@SpringBootTest
public class SearchTagByTagsTest {
    @Resource
    private UserService userService;

    @Test
    public void testSelectByTags() {
        List<String> tagNameList = Arrays.asList("java","男");
        List<User> userList = userService.searchByTagsBySQL(tagNameList);

        System.out.println(userList);
        Assert.assertNotNull(userList);
    }
    
}
