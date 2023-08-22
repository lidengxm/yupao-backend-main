package com.lmeng.yupao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmeng.yupao.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lmeng.yupao.model.request.UpdateTagRequest;
import com.lmeng.yupao.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
* @author lmeng
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String username, String userAccount, String userPassword, String checkPassword);
    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    Boolean userLogout(String id,HttpServletRequest request);

    /**
     * 用户脱敏
     * @param orignUser
     * @return
     */
    User getSafetyUser(User orignUser);

    /**
     * 判断用户是否登录
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户修改信息
     * @param user
     * @param loginUser
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 根据标签搜索用户（内存查询）
     * @param tagNameList
     * @return
     */
    List<User> searchByTags(Set<String> tagNameList);

    /**
     * 根据标签搜索用户（SQL查询）
     * @param tagNameList
     * @return
     */
    List<User> searchByTagsBySQL(List<String> tagNameList);

    /**
     * 判断用户是否是管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 判断用户是否是管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);

    /**
     * redisKey
     *
     * @param key
     * @return
     */
    String redisFormat(Long key);


    /**
     * 修改标签
     *
     * @param updateTag   修改标签dto
     * @param currentUser 当前用户
     * @return
     */
    int updateTagById(UpdateTagRequest updateTag, User currentUser);

    /**
     * 获取当前用户信息
     * @param userId
     * @Param loginUserId
     * @return
     */
    UserVO getUserById(Long userId, Long loginUserId);
}
