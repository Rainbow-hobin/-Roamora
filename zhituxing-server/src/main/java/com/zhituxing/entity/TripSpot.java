package com.zhituxing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "trip_spots", indexes = {
        @Index(name = "uk_trip_spot", columnList = "trip_id, spot_id", unique = true)
})
public class TripSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "day_no", nullable = false)
    private Integer dayNo = 1;

    @Column(name = "sort_no", nullable = false)
    private Integer sortNo = 0;
}
