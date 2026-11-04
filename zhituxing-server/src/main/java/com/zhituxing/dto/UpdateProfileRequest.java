package com.zhituxing.dto;

public record UpdateProfileRequest(
        String name,
        String avatar,
        String signature
) {
}
