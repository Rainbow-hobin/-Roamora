package com.zhituxing.repository;

import com.zhituxing.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserId(Long userId);

    Optional<Favorite> findByUserIdAndSpotId(Long userId, Long spotId);

    boolean existsByUserIdAndSpotId(Long userId, Long spotId);

    void deleteByUserIdAndSpotId(Long userId, Long spotId);
}
