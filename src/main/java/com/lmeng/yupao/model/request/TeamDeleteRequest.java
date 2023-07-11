package com.lmeng.yupao.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.IOException;
import java.io.Serializable;

/**
 * 解散队伍
 * @TableName team
 */
@Data
public class TeamDeleteRequest implements Serializable {

    /**
     * 队伍id
     */
    private static final long serialVersionUID = 3191241716373120793L;
    /**
     * 队伍id
     */
    @TableId(type = IdType.AUTO)
    private Long teamId;

}