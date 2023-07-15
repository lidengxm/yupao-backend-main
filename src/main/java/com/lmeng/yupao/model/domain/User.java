package com.lmeng.yupao.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 用户id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 角色
     */
    private int userRole;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态
     */
    private Integer userStatus;

    /**
     * 电话
     */
    private String phone;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 星球编号
     */
    private String planetCode;

    /**
     * 标签
     */
    private String tags;

    /**
     * 简介
     */
    private String profile;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}