package com.lmeng.user_centre_backed.common;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/29
 */

/**
 * 返回工具类
 */
public class ResultUtils {
    //成功
    public static <T> BaseResponse success(T data) {
        return new BaseResponse(0,data,"ok");
    }

    //失败
    public static BaseResponse error(ErrorCode errorCode) {
        //return new BaseResponse(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
        return new BaseResponse(errorCode);
    }

    public static BaseResponse error(int code, String  message, String description) {
        return new BaseResponse(code,null,message,description);
    }

    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse(errorCode.getCode(),null,message,description);
    }

    public static BaseResponse error(ErrorCode errorCode,String description) {
        return new BaseResponse(errorCode.getCode(),errorCode.getMessage(),description);
    }
}
