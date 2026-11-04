package com.zhituxing.controller;

import com.zhituxing.common.Result;
import com.zhituxing.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /** 10.1 图片上传（需登录） */
    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(Map.of("url", fileService.upload(file)));
    }
}
