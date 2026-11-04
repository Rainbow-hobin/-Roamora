package com.zhituxing.dto;

import java.time.LocalDate;

public record TripVO(
        Long id,
        String title,
        Integer days,
        long spotCount,
        LocalDate startDate,
        LocalDate endDate,
        String status
) {
}
