package com.zhituxing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchReviewRequest(
        @NotEmpty(message = "评论 ID 列表不能为空") List<Long> ids,
        @NotBlank(message = "目标状态不能为空") String status
) {
}
