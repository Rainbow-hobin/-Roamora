package com.zhituxing.repository;

import com.zhituxing.entity.TripSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripSpotRepository extends JpaRepository<TripSpot, Long> {

    List<TripSpot> findByTripIdOrderByDayNoAscSortNoAsc(Long tripId);

    void deleteByTripId(Long tripId);
}
