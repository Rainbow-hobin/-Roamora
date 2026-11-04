package com.zhituxing.service;

import com.zhituxing.common.BusinessException;
import com.zhituxing.common.ErrorCode;
import com.zhituxing.common.PageResult;
import com.zhituxing.dto.BatchReviewRequest;
import com.zhituxing.dto.CommentVO;
import com.zhituxing.dto.SpotCreateRequest;
import com.zhituxing.dto.SpotVO;
import com.zhituxing.dto.UserVO;
import com.zhituxing.entity.Comment;
import com.zhituxing.entity.Post;
import com.zhituxing.entity.Spot;
import com.zhituxing.entity.User;
import com.zhituxing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SpotRepository spotRepository;
    private final CommentRepository commentRepository;
    private final TripRepository tripRepository;
    private final PostRepository postRepository;

    // ============ 数据看板 ============

    public Map<String, Object> dashboard(int range) {
        int days = (range == 30) ? 30 : 7;
        Map<String, Object> data = new LinkedHashMap<>();

        List<Map<String, Object>> stats = new ArrayList<>();
        long pendingAuditSpots = spotRepository.findAll().stream()
                .filter(s -> "待审核".equals(s.getAuditStatus())).count();
        long pendingReviewCount = commentRepository.findAll().stream()
                .filter(c -> "待审".equals(c.getStatus())).count();
        stats.add(stat("总用户", userRepository.count(), "+12% 本月"));
        stats.add(stat("景点数", spotRepository.count(), pendingAuditSpots + " 个待审核"));
        stats.add(stat("评论数", commentRepository.count(), pendingReviewCount + " 条待审核"));
        data.put("stats", stats);

        List<Integer> trend = new ArrayList<>(Collections.nCopies(days, 0));
        List<Comment> comments = commentRepository.findAll(Sort.by(Sort.Direction.DESC, "date"));
        LocalDate today = LocalDate.now();
        for (Comment c : comments) {
            LocalDate d = c.getDate() == null ? null : c.getDate().toLocalDate();
            if (d != null && !d.isBefore(today.minusDays(days - 1L)) && !d.isAfter(today)) {
                int index = (int) (d.until(today, java.time.temporal.ChronoUnit.DAYS));
                trend.set(days - 1 - index, trend.get(days - 1 - index) + 1);
            }
        }
        data.put("trend", trend);
        data.put("range", range);

        List<Map<String, Object>> share = new ArrayList<>();
        Map<String, Long> categoryCount = new LinkedHashMap<>();
        for (Spot s : spotRepository.findAll()) {
            categoryCount.merge(s.getCategory(), 1L, Long::sum);
        }
        long total = categoryCount.values().stream().mapToLong(Long::longValue).sum();
        if (total > 0) {
            for (Map.Entry<String, Long> e : categoryCount.entrySet()) {
                share.add(Map.of("name", e.getKey(),
                        "value", Math.round(e.getValue() * 100.0 / total)));
            }
        }
        data.put("categoryShare", share);

        Map<String, Integer> pending = new LinkedHashMap<>();
        pending.put("reviews", (int) commentRepository.findAll()
                .stream().filter(c -> "待审".equals(c.getStatus())).count());
        data.put("pending", pending);
        return data;
    }

    private Map<String, Object> stat(String label, long value, String trend) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("label", label);
        map.put("value", value);
        map.put("trend", trend);
        return map;
    }

    // ============ 景点管理 ============

    public PageResult<SpotVO> spots(String keyword, String city, String category,
                                    String auditStatus, int page, int pageSize) {
        Page<Spot> spots = spotRepository.adminSearch(
                blank(keyword), blank(city), blank(category), blank(auditStatus),
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResult<>(spots.map(s -> SpotVO.from(s, true)).getContent(),
                spots.getTotalElements(), page, pageSize);
    }

    @Transactional
    public SpotVO createSpot(SpotCreateRequest request) {
        Spot spot = new Spot();
        applySpot(spot, request);
        spot.setStatus("上架");
        spot.setAuditStatus("待审核");
        spotRepository.save(spot);
        return SpotVO.from(spot, true);
    }

    @Transactional
    public SpotVO updateSpot(Long id, SpotCreateRequest request) {
        Spot spot = spotRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        applySpot(spot, request);
        spotRepository.save(spot);
        return SpotVO.from(spot, true);
    }

    @Transactional
    public void deleteSpot(Long id) {
        spotRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        spotRepository.deleteById(id);
    }

    @Transactional
    public SpotVO updateSpotStatus(Long id, String status) {
        Spot spot = spotRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!Set.of("上架", "下架").contains(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态仅支持：上架/下架");
        }
        spot.setStatus(status);
        spotRepository.save(spot);
        return SpotVO.from(spot, true);
    }

    private void applySpot(Spot spot, SpotCreateRequest request) {
        if (request.price() == null || request.price().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "价格必须大于 0");
        }
        spot.setName(request.name());
        spot.setCity(request.city());
        spot.setCategory(request.category());
        spot.setPrice(request.price());
        spot.setDescription(request.description());
        spot.setCover(request.cover());
        spot.setAddress(request.address());
        spot.setOpeningHours(request.openingHours());
    }

    // ============ 用户管理 ============

    public PageResult<UserVO> users(String keyword, String status, int page, int pageSize) {
        Page<User> users = userRepository.adminSearch(
                blank(keyword), blank(status),
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResult<>(users.map(u -> new UserVO(u.getId(), u.getName(), u.getPhone(),
                u.getAvatar(), u.getSignature(), u.getRole(), u.getStatus(),
                tripRepository.countByUserId(u.getId()), u.getCreatedAt())).getContent(),
                users.getTotalElements(), page, pageSize);
    }

    @Transactional
    public UserVO updateUserStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!Set.of("正常", "禁用").contains(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态仅支持：正常/禁用");
        }
        user.setStatus(status);
        userRepository.save(user);
        return UserVO.from(user);
    }

    // ============ 评论管理 ============

    public PageResult<CommentVO> reviews(String status, String keyword, int page, int pageSize) {
        Page<Comment> comments = commentRepository.adminSearch(
                blank(status), blank(keyword),
                PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "date")));
        return new PageResult<>(comments.map(this::toCommentVO).getContent(),
                comments.getTotalElements(), page, pageSize);
    }

    @Transactional
    public CommentVO updateReviewStatus(Long id, String status) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!Set.of("通过", "删除").contains(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态仅支持：通过/删除");
        }
        comment.setStatus(status);
        commentRepository.save(comment);
        return toCommentVO(comment);
    }

    @Transactional
    public void batchReviews(BatchReviewRequest request) {
        if (!Set.of("通过", "删除").contains(request.status())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态仅支持：通过/删除");
        }
        List<Comment> comments = commentRepository.findAllById(request.ids());
        comments.forEach(c -> c.setStatus(request.status()));
        commentRepository.saveAll(comments);
    }

    private CommentVO toCommentVO(Comment comment) {
        User author = userRepository.findById(comment.getUserId()).orElse(null);
        String postTitle;
        if (comment.getPostId() != null) {
            postTitle = postRepository.findById(comment.getPostId()).map(Post::getTitle).orElse("未知帖子");
        } else if (comment.getSpotId() != null) {
            postTitle = spotRepository.findById(comment.getSpotId()).map(s -> s.getName() + "（景点评论）").orElse("景点评论");
        } else {
            postTitle = "景点评论";
        }
        return new CommentVO(comment.getId(), postTitle,
                author != null ? author.getName() : "未知用户",
                comment.getRating(), comment.getContent(), comment.getDate(), comment.getStatus());
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
