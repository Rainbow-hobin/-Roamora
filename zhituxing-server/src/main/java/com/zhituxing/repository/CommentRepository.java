package com.zhituxing.repository;

import com.zhituxing.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findBySpotIdOrderByDateDesc(Long spotId, Pageable pageable);

    Page<Comment> findBySpotIdAndStatusOrderByDateDesc(Long spotId, String status, Pageable pageable);

    Page<Comment> findByPostIdOrderByDateDesc(Long postId, Pageable pageable);

    Page<Comment> findByPostIdAndStatusOrderByDateDesc(Long postId, String status, Pageable pageable);

    List<Comment> findByPostId(Long postId);

    org.springframework.data.domain.Page<Comment> findByUserIdOrderByDateDesc(Long userId,
            org.springframework.data.domain.Pageable pageable);

    long countByPostIdAndStatus(Long postId, String status);

    long countByUserId(Long userId);

    @Query("""
            select c from Comment c
            where (:status is null or c.status = :status)
              and (:keyword is null or c.content like concat('%', :keyword, '%'))
            """)
    Page<Comment> adminSearch(@Param("status") String status,
                              @Param("keyword") String keyword,
                              Pageable pageable);
}
