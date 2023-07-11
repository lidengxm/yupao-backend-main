package com.lmeng.yupao.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 队伍
 * @TableName team
 */
@Data
public class TeamExitRequest implements Serializable {
    //作为序列化对象的版本标识，用于在反序列化过程中验证版本一致性
    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 队伍id
     */
    private Long teamId;


}