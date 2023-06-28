package com.lmeng.yupao.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/28
 */
//用户登录请求体
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 6955345963349173659L;

    private String userAccount;

    private String password;

}
