package com.zhituxing.controller;

import com.zhituxing.common.PageResult;
import com.zhituxing.common.Result;
import com.zhituxing.dto.CommentCreateRequest;
import com.zhituxing.dto.CommentVO;
import com.zhituxing.dto.PostCreateRequest;
import com.zhituxing.dto.PostVO;
import com.zhituxing.security.CurrentUser;
import com.zhituxing.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /** 7.1 帖子列表（公开） */
    @GetMapping("/posts")
    public Result<PageResult<PostVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(postService.list(page, pageSize));
    }

    /** 7.2 帖子详情（公开） */
    @GetMapping("/posts/{id}")
    public Result<PostVO> detail(@PathVariable Long id) {
        return Result.ok(postService.detail(id));
    }

    /** 7.3 发布帖子 */
    @PostMapping("/posts")
    public Result<PostVO> create(@Valid @RequestBody PostCreateRequest request) {
        return Result.ok(postService.create(CurrentUser.id(), request));
    }

    /** 7.4 删除帖子 */
    @DeleteMapping("/posts/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(CurrentUser.id(), id);
        return Result.ok();
    }

    /** 7.5 帖子评论列表（公开） */
    @GetMapping("/posts/{id}/comments")
    public Result<PageResult<CommentVO>> comments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(postService.comments(id, page, pageSize));
    }

    /** 7.6 发表评论 */
    @PostMapping("/posts/{id}/comments")
    public Result<CommentVO> addComment(@PathVariable Long id,
                                       @Valid @RequestBody CommentCreateRequest request) {
        return Result.ok(postService.addComment(CurrentUser.id(), id, request));
    }

    /** 7.7 删除评论 */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        postService.deleteComment(CurrentUser.id(), id);
        return Result.ok();
    }
}
