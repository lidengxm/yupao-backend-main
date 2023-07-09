package com.lmeng.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lmeng.yupao.common.ErrorCode;
import com.lmeng.yupao.constant.UserConstant;
import com.lmeng.yupao.exceeption.BaseException;
import com.lmeng.yupao.model.domain.User;
import com.lmeng.yupao.service.UserService;
import com.lmeng.yupao.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lmeng.yupao.constant.UserConstant.ADDMIN_ROLE;
import static com.lmeng.yupao.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author lmeng
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Resource
    private UserMapper userMapper;

    //盐值，混淆密码
    private final String SALT = "yupi";

    //private final static String USER_LOGIN_STATE = "user login state";

    /**
     * 用户注册
     * @param userAccount
     * @param password
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String password, String checkPassword, String plannetCode) {
        //1.校验
        if(StringUtils.isAnyBlank(userAccount,password,checkPassword,plannetCode)) {
            // todo 后面封装成异常类
            throw new BaseException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(password.length() < 8 && checkPassword.length() < 8) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }

        if(plannetCode.length() > 5) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }

        //账号中不能有特殊字符
        //String validPattern = "||pP|\\pS|\\s+";
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"用户参数错误");
        }

        //密码和校验密码是否一致
        if(!password.equals(checkPassword)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }

        //校验账号不能重复（放在校验账号密码之后，可以省去账号密码不符合要求时的查询数据库操作）
        QueryWrapper<User> queryWrapper  = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if(count > 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号重复");
        }

        //星球编号不能重复
        QueryWrapper<User> queryWrapper1  = new QueryWrapper<>();
        queryWrapper1.eq("plannetCode", plannetCode);
        long count1 = userMapper.selectCount(queryWrapper1);
        if(count > 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"星球编号重复");
        }

        //2.加密
        String SecondPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());

        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(SecondPassword);
        user.setPlannetCode(plannetCode);
        boolean flag = this.save(user);
        if(!flag) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"注册失败");
        }

        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验账号和密码是否合法
        if(StringUtils.isAnyBlank(userAccount,userPassword)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }
        if(userAccount.length() < 4) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号长度过短");
        }
        if(userPassword.length() < 8) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }

        //账号中不能有特殊字符
        //String validPattern = "||pP|\\pS|\\s+";
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号中含特殊字符");
        }

        //2.加密
        String SecondPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //根据账号密码查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword", SecondPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if(user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BaseException(ErrorCode.NULL_ERROR,"用户不存在");
        }

        //3.用户脱敏
        User safetyUser = getSafetyUser(user);

        //4.记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);

        return user;
    }

    /**
     * 用户脱敏
     * @param orignUser
     * @return
     */
    @Override
    public User getSafetyUser(User orignUser) {
        if(orignUser == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        User safetyUser = new User();
        safetyUser.setId(orignUser.getId());
        safetyUser.setUsername(orignUser.getUsername());
        safetyUser.setUserAccount(orignUser.getUserAccount());
        safetyUser.setAvatarUrl(orignUser.getAvatarUrl());
        safetyUser.setGender(orignUser.getGender());
        safetyUser.setEmail(orignUser.getEmail());
        safetyUser.setUserStatus(0);
        safetyUser.setPlannetCode(orignUser.getPlannetCode());
        safetyUser.setUserRole(orignUser.getUserRole());
        safetyUser.setPhone(orignUser.getPhone());
        safetyUser.setCreateTime(new Date());
        safetyUser.setTags(orignUser.getTags());
        safetyUser.setProfile(orignUser.getProfile());
        return safetyUser;
    }

    /**
     * 获取登录用户
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BaseException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 修改用户
     * @param user
     * @param loginUser
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        // todo 补充校验，如果用户没有传任何要更新的值，就直接报错，不用执行 update 语句
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BaseException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 根据标签搜索用户 SQL查询
     * @param tagNameList
     * @return
     */
    @Deprecated
    public List<User> searchByTagsBySQL(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        //SQL查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接and查询
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags",tagName);
        }
        //对查询条件进行查询并返回List集合
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map((this::getSafetyUser)).collect(Collectors.toList());
    }

    /**
     * 根据标签搜索用户 内存查询
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchByTags(List<String> tagNameList) {
        //内存查询
        //1.先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //2.在内存中判断是否有包含要求的标签
        //改成并发，stream改成paralleStream并行流，但用的公共线程池不安全
        return userList.stream().filter(user -> {
            String tagStr = user.getTags();
            //如果从数据中返回的值是空就直接返回
            if(StringUtils.isBlank(tagStr)) {
                return false;
            }
            //将JSON类型字符串转换成Set<String>类型
            Set<String> tempTagNameList = gson.fromJson(tagStr, new TypeToken<Set<String>>() {}.getType());
            tempTagNameList = Optional.ofNullable(tempTagNameList).orElse(new HashSet<>());
            for (String tagName : tempTagNameList) {
                if(tempTagNameList.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
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
        if(user == null || user.getUserRole() != ADDMIN_ROLE) {
            return false;
        }
        return true;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

}




