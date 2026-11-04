package com.zhituxing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SpotCreateRequest(
        @NotBlank(message = "景点名称不能为空") String name,
        @NotBlank(message = "城市不能为空") String city,
        @NotBlank(message = "分类不能为空") String category,
        @NotNull(message = "价格不能为空") BigDecimal price,
        String description,
        String cover,
        String address,
        String openingHours
) {
}
