package com.cm.web;

/** 业务异常，由全局处理器转为 400 响应 */
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(String message) {
        this(400, message);
    }

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
