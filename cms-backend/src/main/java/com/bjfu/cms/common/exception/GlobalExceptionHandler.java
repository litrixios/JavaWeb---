package com.bjfu.cms.common.exception;

import com.bjfu.cms.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace(); // 打印堆栈到控制台方便调试
        return Result.error("系统错误: " + e.getMessage());
    }
}