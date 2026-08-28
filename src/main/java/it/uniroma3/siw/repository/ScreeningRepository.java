package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Screening;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    boolean existsByTheaterId(Long theaterId);

    boolean existsByMovieId(Long movieId);

    boolean existsByFestivalId(Long festivalId);

    /* Serve a impedire che un film venga tolto da un festival in cui ha
       ancora delle proiezioni programmate. */
    boolean existsByFestivalIdAndMovieId(Long festivalId, Long movieId);
}
