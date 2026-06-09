package com.example.springboot.exception;

import com.example.springboot.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomerException.class)
    @ResponseBody
    public Result handleCustomerException(CustomerException e) {
        return Result.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseBody
    public Result handleNoResource(NoResourceFoundException e) {
        return Result.error("404", "接口不存在，请重启后端服务后再试");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public Result handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return Result.error("400", "视频文件过大，请上传小于 500MB 的文件");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception e) {
        log.error("未捕获异常: {}", e.getMessage(), e);
        return Result.error("500", "服务器内部错误");
    }
}
