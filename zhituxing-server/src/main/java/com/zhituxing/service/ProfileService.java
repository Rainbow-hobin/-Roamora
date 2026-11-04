package com.zhituxing.service;

import com.zhituxing.common.BusinessException;
import com.zhituxing.common.ErrorCode;
import com.zhituxing.common.PageResult;
import com.zhituxing.dto.*;
import com.zhituxing.entity.Comment;
import com.zhituxing.entity.Favorite;
import com.zhituxing.entity.Note;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final NoteRepository noteRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final SpotRepository spotRepository;
    private final PostRepository postRepository;

    public ProfileVO me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        ProfileStats stats = new ProfileStats(
                tripRepository.findByUserIdOrderByCreatedAtDesc(userId).size(),
                noteRepository.countByUserId(userId),
                commentRepository.countByUserId(userId));
        String activity = latestActivity(userId);
        return new ProfileVO(user.getName(), user.getPhone(), user.getAvatar(),
                user.getSignature(), user.getRole(), stats, activity);
    }

    @Transactional
    public ProfileVO update(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (request.name() != null && !request.name().isBlank()
                && !request.name().equals(user.getName())) {
            if (userRepository.existsByName(request.name())) {
                throw new BusinessException(ErrorCode.CONFLICT, "用户名已被占用");
            }
            user.setName(request.name());
        }
        if (request.avatar() != null) {
            user.setAvatar(request.avatar());
        }
        if (request.signature() != null) {
            user.setSignature(request.signature());
        }
        userRepository.save(user);
        return me(userId);
    }

    private String latestActivity(Long userId) {
        var page = commentRepository.findByUserIdOrderByDateDesc(
                userId, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "date")));
        if (page.getContent().isEmpty()) {
            return "";
        }
        Comment latest = page.getContent().get(0);
        String content = latest.getContent();
        if (content.length() > 12) {
            content = content.substring(0, 12) + "…";
        }
        return "最近评论了「" + content + "」";
    }

    public List<SpotVO> favorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream()
                .map(f -> spotRepository.findById(f.getSpotId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(s -> SpotVO.from(s, false))
                .toList();
    }

    @Transactional
    public void addFavorite(Long userId, Long spotId) {
        spotRepository.findById(spotId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (favoriteRepository.existsByUserIdAndSpotId(userId, spotId)) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setSpotId(spotId);
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long userId, Long spotId) {
        favoriteRepository.deleteByUserIdAndSpotId(userId, spotId);
    }

    public PageResult<CommentVO> myComments(Long userId, int page, int pageSize) {
        Page<Comment> comments = commentRepository.findByUserIdOrderByDateDesc(
                userId, PageRequest.of(Math.max(page - 1, 0), pageSize));
        return new PageResult<>(comments.map(c -> {
            User author = userRepository.findById(c.getUserId()).orElse(null);
            String postTitle = c.getPostId() == null ? null
                    : postRepository.findById(c.getPostId()).map(Post::getTitle).orElse(null);
            return new CommentVO(c.getId(), postTitle,
                    author != null ? author.getName() : "未知用户",
                    c.getRating(), c.getContent(), c.getDate(), c.getStatus());
        }).getContent(), comments.getTotalElements(), page, pageSize);
    }

    public PageResult<NoteVO> myNotes(Long userId, int page, int pageSize) {
        Page<Note> notes = noteRepository.findByUserId(
                userId, PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResult<>(notes.map(n -> new NoteVO(n.getId(), n.getTitle(), n.getContent(), n.getCreatedAt()))
                .getContent(), notes.getTotalElements(), page, pageSize);
    }
}
