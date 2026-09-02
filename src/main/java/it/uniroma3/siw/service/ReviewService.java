package it.uniroma3.siw.service;
import it.uniroma3.siw.dto.ReviewDto;
import it.uniroma3.siw.dto.ReviewRequest;
import it.uniroma3.siw.dto.ReviewStatsDto;
import it.uniroma3.siw.exception.BusinessRuleException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.*;
import it.uniroma3.siw.repository.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private ReviewRepository reviewRepository;
    private MovieRepository movieRepository;
    private CredentialsService credentialsService;

    public ReviewService(ReviewRepository reviewRepository,
                         MovieRepository movieRepository,
                         CredentialsService credentialsService) {
        this.reviewRepository = reviewRepository;
        this.movieRepository = movieRepository;
        this.credentialsService = credentialsService;
    }

    private User getUserByUsername(String username) {
        Credentials credentials = credentialsService.getCredentials(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nessun utente registrato con username " + username));
        return credentials.getUser();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> findByMovieId(Long movieId) {
        List<Review> reviews = reviewRepository.findByMovieId(movieId);
        return reviews.stream()
                .map(ReviewDto::from)
                .toList();
    }

    @Transactional
    public ReviewDto create(Long movieId, String username, ReviewRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + movieId));

        User user = getUserByUsername(username);

        if (reviewRepository.existsByMovieIdAndUserId(movieId, user.getId())) {
            throw new BusinessRuleException("Hai già recensito questo film.");
        }

        Review review = new Review();
        review.setText(request.text());
        review.setRating(request.rating());
        review.setReviewDate(LocalDate.now());
        review.setMovie(movie);
        review.setUser(user);

        Review salvata = reviewRepository.save(review);

        return ReviewDto.from(salvata);
    }

    @Transactional
    public ReviewDto update(Long reviewId, String username, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nessuna recensione con id " + reviewId));

        User user = getUserByUsername(username);

        if (!user.getId().equals(review.getUser().getId())) {
            throw new AccessDeniedException("Non puoi modificare la recensione di un altro utente.");
        }

        review.setText(request.text());
        review.setRating(request.rating());

        return ReviewDto.from(review);
    }

    @Transactional
    public void delete(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nessuna recensione con id " + reviewId));

        User user = getUserByUsername(username);

        if (!user.getId().equals(review.getUser().getId())) {
            throw new AccessDeniedException("Non puoi eliminare la recensione di un altro utente.");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public ReviewStatsDto statsForMovie(Long movieId){
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Nessun film con id " + movieId);
        }

        Double averageRating = reviewRepository.findAverageRatingByMovieId(movieId);
        Long reviewCount = reviewRepository.countReviewsByMovieId(movieId);
        Map<Integer, Long> ratingDistribution = new LinkedHashMap<>();

        // Inizializzo la distribuzione delle valutazioni con valori predefiniti (0) per ogni rating da 1 a 5
        for(int i = 1; i <= 5; i++){
            ratingDistribution.put((Integer) i , (long) 0);
        }

        List<Object[]> distributionData = reviewRepository.countReviewsByMovieIdGroupedByRating(movieId);
        for(Object[] row : distributionData){
            ratingDistribution.put((Integer) row[0], (Long) row[1]);
        }

        return new ReviewStatsDto(averageRating, reviewCount, ratingDistribution);
    }


}