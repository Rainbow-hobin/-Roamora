package com.zhituxing.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "用户名或手机号不能为空") String account,
        @NotBlank(message = "密码不能为空") String password,
        @NotBlank(message = "验证码标识不能为空") String captchaKey,
        @NotBlank(message = "验证码不能为空") String captcha
) {
}
