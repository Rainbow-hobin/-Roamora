package com.zhituxing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 50, message = "标题不超过 50 字")
        String title,

        @NotBlank(message = "正文不能为空")
        String content,

        String board,
        List<String> images
) {
}
