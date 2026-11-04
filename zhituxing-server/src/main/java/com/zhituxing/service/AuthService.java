package com.zhituxing.service;

import com.zhituxing.common.BusinessException;
import com.zhituxing.common.ErrorCode;
import com.zhituxing.dto.LoginRequest;
import com.zhituxing.dto.RegisterRequest;
import com.zhituxing.dto.TokenResponse;
import com.zhituxing.dto.UserVO;
import com.zhituxing.entity.User;
import com.zhituxing.repository.UserRepository;
import com.zhituxing.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;

    @Transactional
    public Long register(RegisterRequest request) {
        captchaService.verify(request.captchaKey(), request.captcha());
        if (userRepository.existsByPhone(request.phone())) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        if (userRepository.existsByName(request.account())) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        User user = new User();
        user.setName(request.account());
        user.setPhone(request.phone());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("user");
        user.setStatus("正常");
        userRepository.save(user);
        return user.getId();
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        captchaService.verify(request.captchaKey(), request.captcha());
        User user = userRepository.findByPhone(request.account())
                .or(() -> userRepository.findByName(request.account()))
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS);
        }
        if (!"正常".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }
        String token = jwtUtil.generate(user.getId(), user.getName(), user.getRole());
        return new TokenResponse(token, "Bearer", 7200, UserVO.from(user));
    }

    public UserVO me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        return UserVO.from(user);
    }
}
