package com.webdev.webdev;

import lombok.Data;

/**
 * 通用接口返回结构：
 * success 是否成功
 * message 提示信息
 * data    具体数据
 */
@Data
public class Result<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.success = true;
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.success = false;
        r.message = msg;
        return r;
    }
}

