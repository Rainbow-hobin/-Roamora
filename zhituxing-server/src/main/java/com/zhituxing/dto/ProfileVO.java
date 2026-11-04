package com.zhituxing.dto;

public record ProfileVO(
        String name,
        String phone,
        String avatar,
        String signature,
        String role,
        ProfileStats stats,
        String activity
) {
}
