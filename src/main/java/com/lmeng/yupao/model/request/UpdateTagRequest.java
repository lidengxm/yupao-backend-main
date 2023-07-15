package com.lmeng.yupao.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;


@Data
public class UpdateTagRequest implements Serializable {
    private static final long serialVersionUID = 5482203079092270874L;
    private long id;
    private Set<String> tagList;
}
