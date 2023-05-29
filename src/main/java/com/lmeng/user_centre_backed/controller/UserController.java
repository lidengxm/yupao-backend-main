package com.lmeng.user_centre_backed.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lmeng.user_centre_backed.model.User;
import com.lmeng.user_centre_backed.model.request.UserLoginRequest;
import com.lmeng.user_centre_backed.model.request.UserRegisterRequest;
import com.lmeng.user_centre_backed.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.lmeng.user_centre_backed.constant.UserConstant.ADDMIN_ROLE;
import static com.lmeng.user_centre_backed.constant.UserConstant.USER_LOGIN_STATE;


/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/28
 */

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Long userRegistry(@RequestBody UserRegisterRequest userRegisterRequest) {
        if(userRegisterRequest == null) {
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        //先做一个简单校验
        if(StringUtils.isAnyBlank(userAccount,password,checkPassword)) {
            return null;
        }
        long id = userService.userRegister(userAccount, password, checkPassword);

        return id;
    }

    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userRegisterRequest, HttpServletRequest request) {
        if(userRegisterRequest == null) {
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getPassword();

        //先做一个简单校验
        if(StringUtils.isAnyBlank(userAccount,password)) {
            return null;
        }
        return userService.userLogin(userAccount, password, request);

    }

    @PostMapping("/logout")
    public Integer userLogout(HttpServletRequest request) {
        if(request == null) {
            return null;
        }
        return userService.userLogout(request);
    }

    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null) {
            return null;
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        //TODO 校验用户是否合法
        return userService.getSafetyUser(user);
    }

    @GetMapping("/select")
    public List<User> selectAll(String username,HttpServletRequest request) {
        //先鉴权，仅管理员可以查询
        if(!isAdmin(request)) {
            return new ArrayList<>();
        }

        //查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)) {
            queryWrapper.like("username",username);
        }

        List<User> userList = userService.list(queryWrapper);
        return userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());

        //return userService.list(queryWrapper);
    }

    @DeleteMapping
    public boolean deleteById(@RequestBody long id,HttpServletRequest request) {
        if(!isAdmin(request)) {
            return false;
        }

        if(id <= 0) {
            return false;
        }

        return userService.removeById(id);
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        //先鉴权，仅管理员可以查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if(user == null || user.getRole() != ADDMIN_ROLE) {
            return false;
        }
        return true;
    }


}
