package com.zhituxing.controller;

import com.zhituxing.common.Result;
import com.zhituxing.dto.CaptchaResponse;
import com.zhituxing.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/captchas")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    /** 3.5 获取图形验证码（公开） */
    @GetMapping
    public Result<CaptchaResponse> generate() {
        return Result.ok(captchaService.generate());
    }
}
