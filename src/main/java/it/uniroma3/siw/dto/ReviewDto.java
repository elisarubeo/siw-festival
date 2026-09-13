package it.uniroma3.siw.dto;

import java.time.LocalDate;

import it.uniroma3.siw.model.Review;

public record ReviewDto(
        Long id,
        String text,
        Integer rating,
        LocalDate reviewDate,
        Long authorId,
        String authorName,
        String authorSurname) {

    public static ReviewDto from(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getText(),
                review.getRating(),
                review.getReviewDate(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getUser().getSurname());
    }
}
