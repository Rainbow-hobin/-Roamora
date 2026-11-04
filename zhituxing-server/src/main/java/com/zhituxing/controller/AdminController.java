package com.zhituxing.controller;

import com.zhituxing.common.PageResult;
import com.zhituxing.common.Result;
import com.zhituxing.dto.*;
import com.zhituxing.security.CurrentUser;
import com.zhituxing.security.UserPrincipal;
import com.zhituxing.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理后台接口（需管理员角色，由 Security 统一校验 ROLE_ADMIN）
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /** 9.1 数据看板统计 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(
            @RequestParam(defaultValue = "7") int range) {
        return Result.ok(adminService.dashboard(range));
    }

    // ============ 9.2 景点管理 ============

    @GetMapping("/spots")
    public Result<PageResult<SpotVO>> spots(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String auditStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(adminService.spots(keyword, city, category, auditStatus, page, pageSize));
    }

    @PostMapping("/spots")
    public Result<SpotVO> createSpot(@Valid @RequestBody SpotCreateRequest request) {
        return Result.ok(adminService.createSpot(request));
    }

    @PutMapping("/spots/{id}")
    public Result<SpotVO> updateSpot(@PathVariable Long id,
                                     @Valid @RequestBody SpotCreateRequest request) {
        return Result.ok(adminService.updateSpot(id, request));
    }

    @DeleteMapping("/spots/{id}")
    public Result<Void> deleteSpot(@PathVariable Long id) {
        adminService.deleteSpot(id);
        return Result.ok();
    }

    @PutMapping("/spots/{id}/status")
    public Result<SpotVO> updateSpotStatus(@PathVariable Long id,
                                           @Valid @RequestBody StatusRequest request) {
        return Result.ok(adminService.updateSpotStatus(id, request.status()));
    }

    // ============ 9.4 用户管理 ============

    @GetMapping("/users")
    public Result<PageResult<UserVO>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(adminService.users(keyword, status, page, pageSize));
    }

    @PutMapping("/users/{id}/status")
    public Result<UserVO> updateUserStatus(@PathVariable Long id,
                                           @Valid @RequestBody StatusRequest request) {
        return Result.ok(adminService.updateUserStatus(id, request.status()));
    }

    // ============ 9.5 评论管理 ============

    @GetMapping("/reviews")
    public Result<PageResult<CommentVO>> reviews(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(adminService.reviews(status, keyword, page, pageSize));
    }

    @PutMapping("/reviews/{id}/status")
    public Result<CommentVO> updateReviewStatus(@PathVariable Long id,
                                                @Valid @RequestBody StatusRequest request) {
        return Result.ok(adminService.updateReviewStatus(id, request.status()));
    }

    @PostMapping("/reviews/batch")
    public Result<Void> batchReviews(@Valid @RequestBody BatchReviewRequest request) {
        adminService.batchReviews(request);
        return Result.ok();
    }
}
