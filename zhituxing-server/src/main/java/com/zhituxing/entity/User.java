package com.zhituxing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "uk_phone", columnList = "phone", unique = true),
        @Index(name = "uk_name", columnList = "name", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = false, length = 11)
    private String phone;

    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 255)
    private String avatar;

    @Column(length = 255)
    private String signature;

    @Column(nullable = false, length = 16)
    private String role = "user";

    /** 正常 / 禁用 */
    @Column(nullable = false, length = 8)
    private String status = "正常";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
