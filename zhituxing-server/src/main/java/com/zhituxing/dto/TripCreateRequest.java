package com.zhituxing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record TripCreateRequest(
        @NotBlank(message = "行程名称不能为空") String title,
        @NotNull(message = "行程天数不能为空") @Min(value = 1, message = "天数至少为 1") Integer days,
        @NotNull(message = "景点列表不能为空") List<Long> spotIds,
        LocalDate startDate
) {
}
