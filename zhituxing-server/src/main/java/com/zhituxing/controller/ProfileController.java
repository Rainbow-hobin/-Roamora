package com.zhituxing.controller;

import com.zhituxing.common.PageResult;
import com.zhituxing.common.Result;
import com.zhituxing.dto.*;
import com.zhituxing.security.CurrentUser;
import com.zhituxing.service.ProfileService;
import com.zhituxing.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final TripService tripService;

    /** 8.1 获取个人资料 */
    @GetMapping
    public Result<ProfileVO> me() {
        return Result.ok(profileService.me(CurrentUser.id()));
    }

    /** 8.2 更新个人资料 */
    @PatchMapping
    public Result<ProfileVO> update(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.ok(profileService.update(CurrentUser.id(), request));
    }

    /** 8.3 我的行程 */
    @GetMapping("/trips")
    public Result<List<TripVO>> myTrips() {
        return Result.ok(tripService.list(CurrentUser.id()));
    }

    /** 8.5 我的评论 */
    @GetMapping("/comments")
    public Result<PageResult<CommentVO>> myComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(profileService.myComments(CurrentUser.id(), page, pageSize));
    }

    /** 8.6 我的笔记 */
    @GetMapping("/notes")
    public Result<PageResult<NoteVO>> myNotes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(profileService.myNotes(CurrentUser.id(), page, pageSize));
    }

    /** 8.7 我的收藏 */
    @GetMapping("/favorites")
    public Result<List<SpotVO>> favorites() {
        return Result.ok(profileService.favorites(CurrentUser.id()));
    }

    /** 8.7 收藏景点 */
    @PostMapping("/favorites")
    public Result<Void> addFavorite(@RequestBody Map<String, Long> body) {
        profileService.addFavorite(CurrentUser.id(), body.get("spotId"));
        return Result.ok();
    }

    /** 8.7 取消收藏 */
    @DeleteMapping("/favorites/{spotId}")
    public Result<Void> removeFavorite(@PathVariable Long spotId) {
        profileService.removeFavorite(CurrentUser.id(), spotId);
        return Result.ok();
    }
}
