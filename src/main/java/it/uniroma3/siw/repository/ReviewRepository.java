package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.movie.id = :movieId "
         + "ORDER BY r.reviewDate DESC, r.id DESC")
    List<Review> findByMovieId(@Param("movieId") Long movieId);

    boolean existsByMovieIdAndUserId(Long movieId, Long userId);

    Optional<Review> findByMovieIdAndUserId(Long movieId, Long userId);

    @Query("SELECT avg(r.rating) FROM Review r WHERE r.movie.id = :movieId")
    Double findAverageRatingByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT count(r) FROM Review r WHERE r.movie.id = :movieId")
    Long countReviewsByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT r.rating, count(r) FROM Review r WHERE r.movie.id = :movieId GROUP BY r.rating")
    List<Object[]> countReviewsByMovieIdGroupedByRating(@Param("movieId") Long movieId);
}
