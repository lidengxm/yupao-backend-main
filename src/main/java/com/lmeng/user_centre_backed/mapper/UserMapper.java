package com.lmeng.user_centre_backed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmeng.user_centre_backed.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/27
 */
//继承BaseMapper类就不用再写增删改查的方法
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
