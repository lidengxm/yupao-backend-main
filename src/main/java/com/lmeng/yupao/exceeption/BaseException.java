package com.lmeng.yupao.exceeption;

import com.lmeng.yupao.common.ErrorCode;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/29
 */
public class BaseException extends RuntimeException{

    private int code;

    private String description;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public BaseException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BaseException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
}
