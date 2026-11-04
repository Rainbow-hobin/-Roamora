package com.zhituxing.service;

import com.zhituxing.common.BusinessException;
import com.zhituxing.common.ErrorCode;
import com.zhituxing.dto.SpotVO;
import com.zhituxing.dto.TripCreateRequest;
import com.zhituxing.dto.TripDetailVO;
import com.zhituxing.dto.TripVO;
import com.zhituxing.entity.Spot;
import com.zhituxing.entity.Trip;
import com.zhituxing.entity.TripSpot;
import com.zhituxing.repository.SpotRepository;
import com.zhituxing.repository.TripRepository;
import com.zhituxing.repository.TripSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TripSpotRepository tripSpotRepository;
    private final SpotRepository spotRepository;

    public List<TripVO> list(Long userId) {
        List<Trip> trips = tripRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return trips.stream().map(this::toVO).toList();
    }

    public TripDetailVO detail(Long userId, Long id) {
        Trip trip = getOwned(userId, id);
        List<TripSpot> tripSpots = tripSpotRepository.findByTripIdOrderByDayNoAscSortNoAsc(id);
        List<SpotVO> spots = tripSpots.stream()
                .map(ts -> spotRepository.findById(ts.getSpotId()).map(s -> SpotVO.from(s, true)).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
        return new TripDetailVO(trip.getId(), trip.getTitle(), trip.getDays(), spots.size(),
                trip.getStartDate(), trip.getEndDate(), trip.getStatus(), spots);
    }

    @Transactional
    public TripVO create(Long userId, TripCreateRequest request) {
        Trip trip = new Trip();
        trip.setUserId(userId);
        trip.setTitle(request.title());
        trip.setDays(request.days());
        trip.setStartDate(request.startDate());
        if (request.startDate() != null) {
            trip.setEndDate(request.startDate().plusDays(request.days() - 1L));
        }
        trip.setStatus("计划中");
        tripRepository.save(trip);
        saveTripSpots(trip.getId(), request.spotIds(), request.days());
        return toVO(trip);
    }

    @Transactional
    public TripVO update(Long userId, Long id, TripCreateRequest request) {
        Trip trip = getOwned(userId, id);
        trip.setTitle(request.title());
        trip.setDays(request.days());
        trip.setStartDate(request.startDate());
        trip.setEndDate(request.startDate() == null ? null : request.startDate().plusDays(request.days() - 1L));
        tripRepository.save(trip);
        tripSpotRepository.deleteByTripId(id);
        saveTripSpots(id, request.spotIds(), request.days());
        return toVO(trip);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        getOwned(userId, id);
        tripSpotRepository.deleteByTripId(id);
        tripRepository.deleteById(id);
    }

    private void saveTripSpots(Long tripId, List<Long> spotIds, int days) {
        if (spotIds == null || spotIds.isEmpty()) {
            return;
        }
        List<TripSpot> list = new ArrayList<>();
        for (int i = 0; i < spotIds.size(); i++) {
            TripSpot ts = new TripSpot();
            ts.setTripId(tripId);
            ts.setSpotId(spotIds.get(i));
            ts.setDayNo(Math.min(i / Math.max(spotIds.size() / days, 1) + 1, days));
            ts.setSortNo(i);
            list.add(ts);
        }
        tripSpotRepository.saveAll(list);
    }

    private Trip getOwned(Long userId, Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!trip.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return trip;
    }

    private TripVO toVO(Trip trip) {
        long count = tripSpotRepository.findByTripIdOrderByDayNoAscSortNoAsc(trip.getId()).size();
        return new TripVO(trip.getId(), trip.getTitle(), trip.getDays(), count,
                trip.getStartDate(), trip.getEndDate(), trip.getStatus());
    }
}
