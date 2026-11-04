package com.zhituxing.dto;

import com.zhituxing.entity.Spot;

import java.math.BigDecimal;

public record SpotVO(
        Long id,
        String name,
        String city,
        String category,
        BigDecimal price,
        BigDecimal rating,
        String cover,
        String summary,
        String status,
        String description,
        String address,
        String openingHours,
        String auditStatus
) {
    public static SpotVO from(Spot spot, boolean full) {
        String summary = spot.getDescription() == null || spot.getDescription().isBlank()
                ? spot.getName()
                : spot.getDescription().length() > 60
                ? spot.getDescription().substring(0, 60) + "…"
                : spot.getDescription();
        return new SpotVO(spot.getId(), spot.getName(), spot.getCity(), spot.getCategory(),
                spot.getPrice(), spot.getRating(), spot.getCover(), summary, spot.getStatus(),
                full ? spot.getDescription() : null,
                full ? spot.getAddress() : null,
                full ? spot.getOpeningHours() : null,
                spot.getAuditStatus());
    }
}
