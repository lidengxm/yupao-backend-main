package com.lmeng.yupao.constant;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/28
 */
public interface UserConstant {
    /**
     * 用户登录状态
     */
    String USER_LOGIN_STATE = "user login state";

    /**
     * 用户登录态hash保存时间
     */
    Long USER_LOGIN_STATE_TTL = 30L;

    /**
     * 页面大小
     */
    long PAGE_SIZE = 8;


    /**
     * 普通权限
     */
    int COMMON_ROLE = 0;

    /**
     * 管理员权限
     */
    int ADDMIN_ROLE = 1;

    /**
     * 默认权限
     */
    int DEFAULT_ROLE = 0;

    /**
     * 管理员权限
     */
    int ADMIN_ROLE = 1;

}

