package com.zhituxing.common;

/**
 * 通用业务错误码（与接口文档 2.4 一致）
 */
public enum ErrorCode {

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或令牌失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突（如重复注册）"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    BAD_CREDENTIALS(1001, "账号或密码错误"),
    CAPTCHA_ERROR(1002, "验证码错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
