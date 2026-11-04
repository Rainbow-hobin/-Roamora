package com.zhituxing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 4, max = 20, message = "用户名长度须为 4-20 个字符")
        String account,

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
        String phone,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度须为 6-20 位")
        String password,

        @NotBlank(message = "验证码标识不能为空")
        String captchaKey,

        @NotBlank(message = "验证码不能为空")
        String captcha
) {
}
