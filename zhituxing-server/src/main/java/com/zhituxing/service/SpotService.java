package com.zhituxing.service;

import com.zhituxing.common.BusinessException;
import com.zhituxing.common.ErrorCode;
import com.zhituxing.common.PageResult;
import com.zhituxing.dto.CommentCreateRequest;
import com.zhituxing.dto.CommentVO;
import com.zhituxing.dto.SpotVO;
import com.zhituxing.entity.Comment;
import com.zhituxing.entity.Spot;
import com.zhituxing.entity.User;
import com.zhituxing.repository.CommentRepository;
import com.zhituxing.repository.SpotRepository;
import com.zhituxing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpotService {

    private final SpotRepository spotRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public PageResult<SpotVO> list(String keyword, String city, String category, String sort,
                                   int page, int pageSize) {
        Sort sortBy = "rating".equals(sort)
                ? Sort.by(Sort.Direction.DESC, "rating")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Page<Spot> spots = spotRepository.search(blank(keyword), blank(city), blank(category), "上架",
                PageRequest.of(Math.max(page - 1, 0), pageSize, sortBy));
        return new PageResult<>(spots.map(s -> SpotVO.from(s, false)).getContent(),
                spots.getTotalElements(), page, pageSize);
    }

    public SpotVO detail(Long id) {
        Spot spot = spotRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        return SpotVO.from(spot, true);
    }

    public PageResult<CommentVO> comments(Long spotId, int page, int pageSize) {
        spotRepository.findById(spotId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        Page<Comment> comments = commentRepository.findBySpotIdAndStatusOrderByDateDesc(
                spotId, "通过", PageRequest.of(Math.max(page - 1, 0), pageSize));
        return new PageResult<>(comments.map(this::toCommentVO).getContent(),
                comments.getTotalElements(), page, pageSize);
    }

    @Transactional
    public CommentVO addComment(Long userId, Long spotId, CommentCreateRequest request) {
        spotRepository.findById(spotId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        Comment comment = new Comment();
        comment.setSpotId(spotId);
        comment.setUserId(userId);
        comment.setContent(request.content());
        comment.setRating(request.rating());
        comment.setStatus("待审");
        commentRepository.save(comment);
        return toCommentVO(comment);
    }

    private CommentVO toCommentVO(Comment comment) {
        User author = userRepository.findById(comment.getUserId()).orElse(null);
        return new CommentVO(comment.getId(), "景点评论",
                author != null ? author.getName() : "未知用户",
                comment.getRating(), comment.getContent(), comment.getDate(), comment.getStatus());
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
