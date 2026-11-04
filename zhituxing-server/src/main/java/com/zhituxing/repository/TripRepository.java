package com.zhituxing.repository;

import com.zhituxing.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Trip> findByUserId(Long userId, Pageable pageable);

    long countByUserId(Long userId);
}
