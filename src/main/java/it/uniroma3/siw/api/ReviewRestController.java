package it.uniroma3.siw.api;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.uniroma3.siw.dto.ReviewDto;
import it.uniroma3.siw.dto.ReviewRequest;
import it.uniroma3.siw.dto.ReviewStatsDto;
import it.uniroma3.siw.service.ReviewService;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api")
public class ReviewRestController {

    private final ReviewService reviewService;

    public ReviewRestController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/movies/{movieId}/reviews")
    public List<ReviewDto> list(@PathVariable Long movieId) {
        return this.reviewService.findByMovieId(movieId);
    }

    @GetMapping("/movies/{movieId}/reviews/stats")
    public ReviewStatsDto stats(@PathVariable Long movieId) {
        return this.reviewService.statsForMovie(movieId);
    }

    @PostMapping("/movies/{movieId}/reviews")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReviewDto> create(@PathVariable Long movieId,
                                            @Valid @RequestBody ReviewRequest request,
                                            Authentication authentication) {

        ReviewDto creata = this.reviewService.create(movieId, authentication.getName(), request);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/reviews/{id}")
                .buildAndExpand(creata.id())
                .toUri();

        return ResponseEntity.created(location).body(creata);
    }

    @PutMapping("/reviews/{reviewId}")
    @SecurityRequirement(name = "bearerAuth")
    public ReviewDto update(@PathVariable Long reviewId,
                            @Valid @RequestBody ReviewRequest request,
                            Authentication authentication) {
        return this.reviewService.update(reviewId, authentication.getName(), request);
    }

    @DeleteMapping("/reviews/{reviewId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable Long reviewId,
                                       Authentication authentication) {
        this.reviewService.delete(reviewId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
