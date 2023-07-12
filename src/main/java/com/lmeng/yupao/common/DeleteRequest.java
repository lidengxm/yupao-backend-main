package com.lmeng.yupao.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/7/8
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = -4162304142710323660L;

    private Long id;
}
