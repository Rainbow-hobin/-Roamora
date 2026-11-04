package com.zhituxing.service;

import com.zhituxing.common.BusinessException;
import com.zhituxing.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final Set<String> ALLOWED_TYPES = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    @Value("${zhituxing.upload.dir}")
    private String uploadDir;

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上传文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片文件不能超过 5MB");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED_TYPES.contains(ext)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持 jpg/png/webp 格式");
        }
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(dateDir);
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            file.transferTo(dir.resolve(filename).toFile());
            return "/uploads/" + dateDir + "/" + filename;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
