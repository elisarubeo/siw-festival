package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /* JOIN FETCH: senza, l'accesso a review.user (LAZY) farebbe partire una
       query per ogni recensione — il problema delle N+1. Cosi' ne basta una.
       ORDER BY: senza una clausola esplicita l'ordine delle righe non e'
       garantito da SQL, e la lista potrebbe presentarsi in ordine diverso a
       ogni chiamata. Le recensioni piu' recenti vanno per prime. */
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
