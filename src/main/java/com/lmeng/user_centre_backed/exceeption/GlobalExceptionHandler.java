package com.lmeng.user_centre_backed.exceeption;

import com.lmeng.user_centre_backed.common.BaseResponse;
import com.lmeng.user_centre_backed.common.ErrorCode;
import com.lmeng.user_centre_backed.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/5/30
 */

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    //捕获BaseException，及自定义的异常
    @ExceptionHandler(BaseException.class)
    public BaseResponse businessExceptionHandler(BaseException e) {
        log.info("businessException" + e.getMessage(),e);
        return ResultUtils.error(e.getCode(),e.getMessage(),e.getDescription());
    }

    //捕获运行时异常
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        log.info("runtimeException" + e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(),"");
    }
}
