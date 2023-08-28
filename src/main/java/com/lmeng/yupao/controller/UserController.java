package com.lmeng.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lmeng.yupao.common.BaseResponse;
import com.lmeng.yupao.common.ErrorCode;
import com.lmeng.yupao.common.ResultUtils;
import com.lmeng.yupao.exception.BaseException;
import com.lmeng.yupao.model.domain.User;
import com.lmeng.yupao.model.request.UpdateTagRequest;
import com.lmeng.yupao.model.request.UserLoginRequest;
import com.lmeng.yupao.model.request.UserQueryRequest;
import com.lmeng.yupao.model.request.UserRegisterRequest;
import com.lmeng.yupao.model.vo.UserVO;
import com.lmeng.yupao.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.lmeng.yupao.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/28
 */

@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
@Api(tags = "用户管理模块")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        String username = userRegisterRequest.getUsername();
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(username, userAccount, userPassword, checkPassword);
        return ResultUtils.success(result, "注册成功");
    }

    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if(userLoginRequest == null) {
            throw new BaseException(ErrorCode.NOT_LOGIN,"");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        //先做一个简单校验
        if(StringUtils.isAnyBlank(userAccount,userPassword)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user,"登录成功");

    }

    @PostMapping("/logout")
    @ApiOperation(value = "用户注销")
    public BaseResponse<Boolean> userLogout(String id,HttpServletRequest request) {
        if (request == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = userService.userLogout(id, request);
        return ResultUtils.success(result);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询用户")
    public BaseResponse<UserVO> getUserById(@PathVariable("id") Long id,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        if (id == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        UserVO user = this.userService.getUserById(id,loginUser.getId());
        return ResultUtils.success(user);
    }

    @GetMapping("/current")
    @ApiOperation(value = "获取当前用户")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null) {
            throw new BaseException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        //校验用户是否合法
        return ResultUtils.success(userService.getSafetyUser(user));
    }

    @GetMapping("/search")
    @ApiOperation(value = "搜索用户")
    public BaseResponse<List<User>> searchUsers(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        //先鉴权，仅管理员可以查询
        if(!userService.isAdmin(request)) {
            throw new BaseException(ErrorCode.NO_AUTH);
        }
        //查询数据库
        String searchText = userQueryRequest.getSearchText();
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(User::getUsername, searchText).or().like(User::getProfile, searchText);
        List<User> userList = userService.list(lambdaQueryWrapper);
        return ResultUtils.success(userList);
    }

    @GetMapping("/search/tags")
    @ApiOperation(value = "根据标签搜索用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "tagNameList", value = "标签列表")})
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)) {
            throw new BaseException(ErrorCode.NULL_ERROR,"标签为空！");
        }
        List<User> userList = userService.searchByTagsBySQL(tagNameList);
        return ResultUtils.success(userList);
    }

    @GetMapping("/recommend")
    @ApiOperation(value = "用户推荐")
    public BaseResponse<List<User>> recommend(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) throw new BaseException(ErrorCode.NOT_LOGIN,"用户未登录");

        int left = (int) (pageSize * pageNum - pageSize);
        int right = (int) (pageSize * pageNum);

        List<User> userList = userService.getRecommendCache();
        if (userList == null){
            throw new BaseException(ErrorCode.SYSTEM_ERROR,"无用户信息");
        }
        if (left > userList.size()){
            throw new BaseException(ErrorCode.PARAMS_ERROR,"无用户信息");
        }
        //当left或right值超过了List长度
        if (right>userList.size()){
            right = userList.size();
        }
        List<User> selectedUsers = userList.subList(left, right);
        log.info("recommend users = " + selectedUsers);
        return ResultUtils.success(selectedUsers);
//        return ResultUtils.success(userList.subList(left, right));
    }

    @PostMapping("/update")
    @ApiOperation(value = "用户更新")
    public BaseResponse<Integer> updateById(@RequestBody User user,HttpServletRequest request) {
        //如果用户不存在
        if(user == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/update/tags")
    @ApiOperation(value = "用户更新标签")
    public BaseResponse<Integer> updateTagById(@RequestBody UpdateTagRequest tagRequest, HttpServletRequest request) {
        if (tagRequest == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        int updateTag = userService.updateTagById(tagRequest, currentUser);
        redisTemplate.delete(userService.redisFormat(currentUser.getId()));
        return ResultUtils.success(updateTag);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "用户删除")
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

    /**
     * 获取最匹配用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    @ApiOperation(value = "匹配模式")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if(num <= 0 || num > 20) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"请求数量不合法！");
        }
        User loginUser = userService.getLoginUser(request);
        log.info("match users = " + loginUser);
        return ResultUtils.success(userService.matchUsers(num,loginUser));
    }

}
