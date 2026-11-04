package com.zhituxing.dto;

import java.time.LocalDateTime;

public record NoteVO(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt
) {
}
