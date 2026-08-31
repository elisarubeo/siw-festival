package it.uniroma3.siw.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Screening;
import it.uniroma3.siw.model.ScreeningStatus;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    boolean existsByTheaterId(Long theaterId);

    boolean existsByMovieId(Long movieId);

    boolean existsByFestivalId(Long festivalId);

    /* Serve a impedire che un film venga tolto da un festival in cui ha
       ancora delle proiezioni programmate. */
    boolean existsByFestivalIdAndMovieId(Long festivalId, Long movieId);

    /* Le proiezioni della stessa sala nello stesso giorno, escluse quelle
       annullate: sono quelle con cui la nuova proiezione potrebbe accavallarsi.
       Non serve @Query: il nome basta a Spring Data per costruirla, e cosi'
       i tipi dei parametri li controlla il compilatore. */
    List<Screening> findByTheaterIdAndDateAndStatusNot(Long theaterId,
                                                       LocalDate date,
                                                       ScreeningStatus status);

    /* Variante per la modifica: esclude la proiezione che si sta spostando,
       altrimenti risulterebbe sempre in conflitto con se stessa. */
    List<Screening> findByTheaterIdAndDateAndStatusNotAndIdNot(Long theaterId,
                                                               LocalDate date,
                                                               ScreeningStatus status,
                                                               Long id);
}
