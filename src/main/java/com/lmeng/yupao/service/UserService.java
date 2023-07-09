package com.lmeng.yupao.service;

import com.lmeng.yupao.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lmeng
*/
public interface UserService extends IService<User> {

    //String USER_LOGIN_STATE = "user login state";

    //用户注册
    long userRegister(String userAccount, String userPassword, String checkPassword,String plannetCode);

    //用户登录
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    //用户脱敏
    User getSafetyUser(User orignUser);

    //判断用户是否登录
    User getLoginUser(HttpServletRequest request);

    //用户注销
    int userLogout(HttpServletRequest request);

    //用户修改
    int updateUser(User user, User loginUser);

    //根据标签搜索用户
    List<User> searchByTags(List<String> tagNameList);

    //判断用户是否是管理员
    boolean isAdmin(HttpServletRequest request);
    boolean isAdmin(User loginUser);
}
