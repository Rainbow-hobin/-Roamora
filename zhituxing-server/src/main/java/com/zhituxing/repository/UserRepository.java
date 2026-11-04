package com.zhituxing.repository;

import com.zhituxing.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByName(String name);

    boolean existsByPhone(String phone);

    boolean existsByName(String name);

    @Query("""
            select u from User u
            where (:keyword is null or u.name like concat('%', :keyword, '%')
                   or u.phone like concat('%', :keyword, '%'))
              and (:status is null or u.status = :status)
            """)
    Page<User> adminSearch(@Param("keyword") String keyword,
                           @Param("status") String status,
                           Pageable pageable);
}
