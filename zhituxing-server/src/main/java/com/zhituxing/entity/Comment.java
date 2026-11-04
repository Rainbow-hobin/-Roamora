package com.zhituxing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_post", columnList = "post_id"),
        @Index(name = "idx_comment_spot", columnList = "spot_id")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 帖子评论：post_id 与 spot_id 二选一 */
    @Column(name = "post_id")
    private Long postId;

    /** 景点评论 */
    @Column(name = "spot_id")
    private Long spotId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500)
    private String content;

    /** 景点评论评分（1-5） */
    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    /** 通过 / 待审 / 删除 */
    @Column(nullable = false, length = 8)
    private String status = "待审";

    @CreationTimestamp
    @Column(name = "date", updatable = false)
    private LocalDateTime date;
}
