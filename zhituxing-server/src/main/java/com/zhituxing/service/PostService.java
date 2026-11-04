package com.zhituxing.service;

import com.zhituxing.common.BusinessException;
import com.zhituxing.common.ErrorCode;
import com.zhituxing.common.PageResult;
import com.zhituxing.dto.CommentCreateRequest;
import com.zhituxing.dto.CommentVO;
import com.zhituxing.dto.PostCreateRequest;
import com.zhituxing.dto.PostVO;
import com.zhituxing.entity.Comment;
import com.zhituxing.entity.Post;
import com.zhituxing.entity.User;
import com.zhituxing.repository.CommentRepository;
import com.zhituxing.repository.PostRepository;
import com.zhituxing.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public PageResult<PostVO> list(int page, int pageSize) {
        Page<Post> posts = postRepository.findAll(PageRequest.of(Math.max(page - 1, 0), pageSize,
                Sort.by(Sort.Direction.DESC, "date")));
        return new PageResult<>(posts.map(this::toVO).getContent(),
                posts.getTotalElements(), page, pageSize);
    }

    @Transactional
    public PostVO detail(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        post.setViews(post.getViews() + 1);
        postRepository.save(post);
        return toVO(post);
    }

    @Transactional
    public PostVO create(Long userId, PostCreateRequest request) {
        Post post = new Post();
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setAuthorId(userId);
        post.setBoard(request.board());
        post.setImages(toJson(request.images()));
        post.setViews(0);
        post.setCommentsCount(0);
        postRepository.save(post);
        return toVO(post);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        User user = userRepository.findById(userId).orElse(null);
        boolean isAdmin = user != null && "admin".equals(user.getRole());
        if (!post.getAuthorId().equals(userId) && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        commentRepository.deleteAll(commentRepository.findByPostId(id));
        postRepository.delete(post);
    }

    public PageResult<CommentVO> comments(Long postId, int page, int pageSize) {
        postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        Page<Comment> comments = commentRepository.findByPostIdAndStatusOrderByDateDesc(
                postId, "通过", PageRequest.of(Math.max(page - 1, 0), pageSize));
        return new PageResult<>(comments.map(this::toCommentVO).getContent(),
                comments.getTotalElements(), page, pageSize);
    }

    @Transactional
    public CommentVO addComment(Long userId, Long postId, CommentCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(request.content());
        comment.setStatus("待审");
        commentRepository.save(comment);
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);
        return toCommentVO(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        User user = userRepository.findById(userId).orElse(null);
        boolean isAdmin = user != null && "admin".equals(user.getRole());
        if (!comment.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        commentRepository.delete(comment);
        if (comment.getPostId() != null) {
            postRepository.findById(comment.getPostId()).ifPresent(p -> {
                p.setCommentsCount(Math.max(p.getCommentsCount() - 1, 0));
                postRepository.save(p);
            });
        }
    }

    private PostVO toVO(Post post) {
        User author = userRepository.findById(post.getAuthorId()).orElse(null);
        List<String> images = fromJson(post.getImages());
        return new PostVO(post.getId(), post.getTitle(),
                author != null ? author.getName() : "未知用户",
                post.getBoard(), post.getViews(), Math.toIntExact(commentRepository.countByPostIdAndStatus(post.getId(), "通过")),
                images.isEmpty() ? null : images.get(0),
                images, post.getContent(), post.getDate());
    }

    private CommentVO toCommentVO(Comment comment) {
        User author = userRepository.findById(comment.getUserId()).orElse(null);
        String postTitle = comment.getPostId() == null ? null
                : postRepository.findById(comment.getPostId()).map(Post::getTitle).orElse(null);
        return new CommentVO(comment.getId(), postTitle,
                author != null ? author.getName() : "未知用户",
                comment.getRating(), comment.getContent(), comment.getDate(), comment.getStatus());
    }

    private String toJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
