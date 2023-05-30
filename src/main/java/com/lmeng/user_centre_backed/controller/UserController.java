package com.lmeng.user_centre_backed.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lmeng.user_centre_backed.common.BaseResponse;
import com.lmeng.user_centre_backed.common.ErrorCode;
import com.lmeng.user_centre_backed.common.ResultUtils;
import com.lmeng.user_centre_backed.exceeption.BaseException;
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
    public BaseResponse<Long> userRegistry(@RequestBody UserRegisterRequest userRegisterRequest) {
        if(userRegisterRequest == null) {
            throw new BaseException(ErrorCode.NOT_LOGIN,"");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String plannetCode = userRegisterRequest.getPlannetCode();

        //先做一个简单校验
        if(StringUtils.isAnyBlank(userAccount,password,checkPassword,plannetCode)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号或密码或星球编号为空");
        }
        long result = userService.userRegister(userAccount, password, checkPassword,plannetCode);

       // return new BaseResponse<>(0,result,"OK");
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userRegisterRequest, HttpServletRequest request) {
        if(userRegisterRequest == null) {
            throw new BaseException(ErrorCode.NOT_LOGIN,"");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getPassword();

        //先做一个简单校验
        if(StringUtils.isAnyBlank(userAccount,password)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }
        User user = userService.userLogin(userAccount, password, request);
        //return new BaseResponse<>(0,user,"OK");
        return ResultUtils.success(user);

    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if(request == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"");
        }
        int i = userService.userLogout(request);
        return ResultUtils.success(i);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null) {
            throw new BaseException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        //TODO 校验用户是否合法
        User user1 = userService.getSafetyUser(user);
        return ResultUtils.success(user1);
    }

    @GetMapping("/select")
    public BaseResponse<List<User>> selectAll(String username,HttpServletRequest request) {
        //先鉴权，仅管理员可以查询
        if(!isAdmin(request)) {
            throw new BaseException(ErrorCode.NO_AUTH,"该用户权限不够");
        }

        //查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)) {
            queryWrapper.like("username",username);
        }

        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
        //return userService.list(queryWrapper);
    }

    @DeleteMapping
    public BaseResponse<Boolean> deleteById(@RequestBody long id,HttpServletRequest request) {
        if(!isAdmin(request)) {
            throw new BaseException(ErrorCode.NO_AUTH,"用户权限不够");
        }

        if(id <= 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"要删除的用户不存在");
        }
        boolean flag = userService.removeById(id);
        return ResultUtils.success(flag);
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
