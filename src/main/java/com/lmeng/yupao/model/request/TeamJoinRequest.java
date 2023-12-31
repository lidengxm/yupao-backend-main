package com.lmeng.yupao.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍
 * @TableName team
 */
@Data
public class TeamJoinRequest implements Serializable {
    //作为序列化对象的版本标识，用于在反序列化过程中验证版本一致性
    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

}