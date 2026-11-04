package com.zhituxing.security;

import com.zhituxing.common.BusinessException;
import com.zhituxing.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从安全上下文获取当前登录用户
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UserPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    public static Long id() {
        return principal().getId();
    }
}
