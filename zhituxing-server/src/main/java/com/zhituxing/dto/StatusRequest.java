package com.zhituxing.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusRequest(
        @NotBlank(message = "目标状态不能为空") String status
) {
}
