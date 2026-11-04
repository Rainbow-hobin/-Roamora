package com.zhituxing.dto;

import java.time.LocalDate;
import java.util.List;

public record TripDetailVO(
        Long id,
        String title,
        Integer days,
        long spotCount,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        List<SpotVO> spots
) {
}
