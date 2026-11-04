package com.zhituxing.repository;

import com.zhituxing.entity.Captcha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaptchaRepository extends JpaRepository<Captcha, Long> {

    Optional<Captcha> findByCaptchaKey(String captchaKey);
}
