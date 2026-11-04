package com.zhituxing.controller;

import com.zhituxing.common.PageResult;
import com.zhituxing.common.Result;
import com.zhituxing.dto.CommentCreateRequest;
import com.zhituxing.dto.CommentVO;
import com.zhituxing.dto.SpotVO;
import com.zhituxing.entity.Destination;
import com.zhituxing.repository.DestinationRepository;
import com.zhituxing.security.CurrentUser;
import com.zhituxing.service.SpotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SpotController {

    private final SpotService spotService;
    private final DestinationRepository destinationRepository;

    /** 4.1 景点列表（公开） */
    @GetMapping("/spots")
    public Result<PageResult<SpotVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(spotService.list(keyword, city, category, sort, page, pageSize));
    }

    /** 4.2 景点详情（公开） */
    @GetMapping("/spots/{id}")
    public Result<SpotVO> detail(@PathVariable Long id) {
        return Result.ok(spotService.detail(id));
    }

    /** 4.3 热门目的地（公开） */
    @GetMapping("/destinations")
    public Result<Map<String, List<Destination>>> destinations() {
        return Result.ok(Map.of("list", destinationRepository.findAllByOrderByIdAsc()));
    }

    /** 4.4 景点评论列表（公开） */
    @GetMapping("/spots/{id}/comments")
    public Result<PageResult<CommentVO>> comments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(spotService.comments(id, page, pageSize));
    }

    /** 4.4 发表景点评论（需登录） */
    @PostMapping("/spots/{id}/comments")
    public Result<CommentVO> addComment(@PathVariable Long id,
                                       @Valid @RequestBody CommentCreateRequest request) {
        return Result.ok(spotService.addComment(CurrentUser.id(), id, request));
    }
}
