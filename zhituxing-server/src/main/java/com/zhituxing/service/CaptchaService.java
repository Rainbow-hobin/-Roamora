package com.zhituxing.service;

import com.zhituxing.common.BusinessException;
import com.zhituxing.common.ErrorCode;
import com.zhituxing.dto.CaptchaResponse;
import com.zhituxing.entity.Captcha;
import com.zhituxing.repository.CaptchaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CaptchaRepository captchaRepository;

    @Transactional
    public CaptchaResponse generate() {
        String code = randomCode(4);
        String key = UUID.randomUUID().toString().replace("-", "");
        Captcha captcha = new Captcha();
        captcha.setCaptchaKey(key);
        captcha.setCode(code);
        captcha.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        captchaRepository.save(captcha);
        return new CaptchaResponse(key, render(code));
    }

    @Transactional
    public void verify(String key, String code) {
        Captcha captcha = captchaRepository.findByCaptchaKey(key)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAPTCHA_ERROR));
        captchaRepository.delete(captcha);
        if (captcha.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
        }
        if (!captcha.getCode().equalsIgnoreCase(code.trim())) {
            throw new BusinessException(ErrorCode.CAPTCHA_ERROR);
        }
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String render(String code) {
        int width = 120;
        int height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xF0, 0xC0, 0x60));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(0x00, 0x3A, 0x5C));
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        for (int i = 0; i < code.length(); i++) {
            g.drawString(String.valueOf(code.charAt(i)), 14 + i * 24, 28 + RANDOM.nextInt(4));
        }
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(0x00, 0x90, 0xA0));
            g.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height),
                    RANDOM.nextInt(width), RANDOM.nextInt(height));
        }
        g.dispose();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
