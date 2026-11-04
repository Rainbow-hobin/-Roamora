package com.zhituxing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "posts", indexes = @Index(name = "idx_post_author", columnList = "author_id"))
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    /** 景点分享 / 人文地理 / 出行攻略 */
    @Column(length = 16)
    private String board;

    /** 配图 URL 列表，JSON 数组字符串 */
    @Column(columnDefinition = "TEXT")
    private String images;

    @Column(nullable = false)
    private Integer views = 0;

    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount = 0;

    @CreationTimestamp
    @Column(name = "date", updatable = false)
    private LocalDateTime date;
}
