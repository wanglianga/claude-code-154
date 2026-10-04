package com.community.market.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(0, "ok", data);
    }

    public static <T> ApiResult<T> ok() {
        return new ApiResult<>(0, "ok", null);
    }

    public static <T> ApiResult<T> fail(String message) {
        return new ApiResult<>(500, message, null);
    }
}
