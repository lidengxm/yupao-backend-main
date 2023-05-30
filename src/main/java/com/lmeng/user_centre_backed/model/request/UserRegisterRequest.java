package com.lmeng.user_centre_backed.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/28
 */
//用户注册请求体
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 6955345963349173659L;

    private String userAccount;

    private String password;

    private String checkPassword;

    private String plannetCode;
}
