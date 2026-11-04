package com.zhituxing.controller;

import com.zhituxing.common.Result;
import com.zhituxing.dto.TripCreateRequest;
import com.zhituxing.dto.TripDetailVO;
import com.zhituxing.dto.TripVO;
import com.zhituxing.security.CurrentUser;
import com.zhituxing.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    /** 6.1 我的行程列表 */
    @GetMapping
    public Result<List<TripVO>> list() {
        return Result.ok(tripService.list(CurrentUser.id()));
    }

    /** 6.2 行程详情 */
    @GetMapping("/{id}")
    public Result<TripDetailVO> detail(@PathVariable Long id) {
        return Result.ok(tripService.detail(CurrentUser.id(), id));
    }

    /** 6.3 创建行程 */
    @PostMapping
    public Result<TripVO> create(@Valid @RequestBody TripCreateRequest request) {
        return Result.ok(tripService.create(CurrentUser.id(), request));
    }

    /** 6.4 更新行程 */
    @PutMapping("/{id}")
    public Result<TripVO> update(@PathVariable Long id,
                                 @Valid @RequestBody TripCreateRequest request) {
        return Result.ok(tripService.update(CurrentUser.id(), id, request));
    }

    /** 6.5 删除行程 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tripService.delete(CurrentUser.id(), id);
        return Result.ok();
    }
}
