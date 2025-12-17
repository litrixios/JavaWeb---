package com.bjfu.cms.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 200成功, 500失败
    private String msg;   // 提示信息
    private T data;       // 返回数据

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "操作成功";
        r.data = data;
        return r;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.msg = msg;
        return r;
    }
}