package com.zhituxing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CommentCreateRequest(
        @NotBlank(message = "评论内容不能为空")
        @Size(max = 200, message = "评论不超过 200 字")
        String content,

        @Min(value = 1, message = "评分最低 1 星")
        @Max(value = 5, message = "评分最高 5 星")
        BigDecimal rating
) {
}
