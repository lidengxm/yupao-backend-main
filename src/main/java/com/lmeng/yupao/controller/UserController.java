package com.lmeng.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lmeng.yupao.common.BaseResponse;
import com.lmeng.yupao.common.ErrorCode;
import com.lmeng.yupao.common.ResultUtils;
import com.lmeng.yupao.exceeption.BaseException;
import com.lmeng.yupao.model.User;
import com.lmeng.yupao.model.request.UserLoginRequest;
import com.lmeng.yupao.model.request.UserRegisterRequest;
import com.lmeng.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lmeng.yupao.constant.UserConstant.ADDMIN_ROLE;
import static com.lmeng.yupao.constant.UserConstant.USER_LOGIN_STATE;


/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/28
 */

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

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
        String userPassword = userRegisterRequest.getUserPassword();

        //先做一个简单校验
        if(StringUtils.isAnyBlank(userAccount,userPassword)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
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

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request) {
        //先鉴权，仅管理员可以查询
        if(!userService.isAdmin(request)) {
            throw new BaseException(ErrorCode.NO_AUTH,"该用户权限不够");
        }
        //查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)) {
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false)  List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        List<User> userList = userService.searchByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommend(long pageNum, long pageSize, HttpServletRequest request) {
        //先获取登录的用户
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("yupao:user:recommend:%s",loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //如果有缓存，就从缓存中取
        Page<User> userList = (Page<User>) valueOperations.get(redisKey);
        if(userList != null) {
            return ResultUtils.success(userList);
        }
        //没有缓存就查询数据库，构造一个空的查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userList = userService.page(new Page(pageNum,pageSize),queryWrapper);
        //写入缓存
        try {
            valueOperations.set(redisKey,userList,10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set key error",e);
        }
        return ResultUtils.success(userList);
    }

    @PostMapping("/update")
    public BaseResponse<User> updateById(@RequestBody User user,HttpServletRequest request) {
        //如果用户不存在
        if(user == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteById(@RequestBody long id,HttpServletRequest request) {
        if(!userService.isAdmin(request)) {
            throw new BaseException(ErrorCode.NO_AUTH,"用户权限不够");
        }
        if(id <= 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"要删除的用户不存在");
        }
        boolean flag = userService.removeById(id);
        return ResultUtils.success(flag);
    }




}
