package com.zhituxing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PostVO(
        Long id,
        String title,
        String author,
        String board,
        int views,
        int comments,
        String cover,
        List<String> images,
        String content,
        LocalDateTime date
) {
}
