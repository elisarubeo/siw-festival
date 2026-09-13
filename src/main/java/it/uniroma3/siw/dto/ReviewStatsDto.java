package it.uniroma3.siw.dto;

import java.util.Map;

public record ReviewStatsDto(
        Double averageRating,
        Long totalReviews,
        Map<Integer, Long> ratingDistribution) {
}
