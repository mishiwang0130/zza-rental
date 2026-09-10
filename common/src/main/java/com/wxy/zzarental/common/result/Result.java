package com.wxy.zzarental.common.result;

import lombok.Data;

/**
 * 全局统一返回结果类。
 *
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    private static <T> Result<T> build(T data) {
        Result<T> result = new Result<>();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> build(T data, ResultCodeEnum resultCode) {
        Result<T> result = build(data);
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }

    public static <T> Result<T> ok(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }

    public static Result<Void> ok() {
        return Result.ok(null);
    }

    public static <T> Result<T> fail() {
        return build(null, ResultCodeEnum.FAIL);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = build(null);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
