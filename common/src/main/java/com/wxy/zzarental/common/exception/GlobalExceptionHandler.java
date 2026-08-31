package com.wxy.zzarental.common.exception;

import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.FileNotFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception e) {
        log.error("Exception, 错误信息: {}", e.getMessage(), e);
        return Result.fail();
    }

    @ExceptionHandler(ZZAException.class)
    @ResponseBody
    public Result handleException(ZZAException e) {
        log.error("ZZAException, 错误信息: {}", e.getMessage(), e);
        return Result.fail(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result handleException(MethodArgumentNotValidException e) {
        log.error("参数校验出错, 错误信息: {}", e.getMessage(), e);
        return Result.fail(999, "参数校验出错, 错误信息: " + e.getMessage());
    }
}
