package it.uniroma3.siw.dto;

import java.util.Map;

/**
 * Statistiche delle recensioni di un film (bonus §13 della traccia).
 *
 * averageRating e' Double e non double perche' puo' essere null: la query di
 * aggregazione restituisce null quando il film non ha ancora recensioni, e un
 * primitivo darebbe NullPointerException alla conversione.
 *
 * ratingDistribution associa a ogni voto da 1 a 5 quante recensioni lo hanno
 * assegnato. La query con GROUP BY restituisce righe SOLO per i voti presenti:
 * riempire con 0 quelli assenti e' compito del service, non del database —
 * cosi' React riceve sempre tutte e cinque le voci e puo' disegnare le barre
 * senza doversi difendere dai valori mancanti.
 */
public record ReviewStatsDto(
        Double averageRating,
        Long totalReviews,
        Map<Integer, Long> ratingDistribution) {
}
