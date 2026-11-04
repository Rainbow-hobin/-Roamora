package com.zhituxing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "spots", indexes = {
        @Index(name = "idx_city", columnList = "city"),
        @Index(name = "idx_category", columnList = "category")
})
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 32)
    private String city;

    /** 自然风光 / 历史文化 */
    @Column(nullable = false, length = 16)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(length = 255)
    private String cover;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String address;

    @Column(length = 32)
    private String openingHours;

    /** 上架 / 下架 */
    @Column(nullable = false, length = 8)
    private String status = "上架";

    /** 待审核 / 已通过 */
    @Column(nullable = false, length = 8)
    private String auditStatus = "已通过";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
