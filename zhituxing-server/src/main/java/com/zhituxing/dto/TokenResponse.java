package com.zhituxing.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn, UserVO user) {
}
