package com.zhituxing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CommentVO(
        Long id,
        String post,
        String author,
        BigDecimal rating,
        String content,
        LocalDateTime date,
        String status
) {
}
