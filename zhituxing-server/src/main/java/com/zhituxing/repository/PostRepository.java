package com.zhituxing.repository;

import com.zhituxing.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    long countByAuthorId(Long authorId);
}
