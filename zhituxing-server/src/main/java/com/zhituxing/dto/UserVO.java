package com.zhituxing.dto;

import com.zhituxing.entity.User;

import java.time.LocalDateTime;

public record UserVO(
        Long id,
        String name,
        String phone,
        String avatar,
        String signature,
        String role,
        String status,
        Long trips,
        LocalDateTime createdAt
) {
    public static UserVO from(User user) {
        return new UserVO(user.getId(), user.getName(), user.getPhone(), user.getAvatar(),
                user.getSignature(), user.getRole(), user.getStatus(), null, user.getCreatedAt());
    }
}
