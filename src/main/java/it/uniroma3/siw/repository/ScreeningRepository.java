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

    /* Le proiezioni della stessa sala nell'intervallo di giorni indicato,
       escluse quelle annullate: sono quelle con cui la nuova proiezione
       potrebbe accavallarsi.

       L'intervallo e' di piu' giorni e non del solo giorno della proiezione
       perche' un film che inizia prima di mezzanotte finisce il giorno dopo:
       filtrando sulla sola data, le sovrapposizioni a cavallo della mezzanotte
       sfuggirebbero. Gli estremi li calcola il service.

       Between in Spring Data e' inclusivo su entrambi gli estremi. */
    List<Screening> findByTheaterIdAndDateBetweenAndStatusNot(Long theaterId,
                                                              LocalDate from,
                                                              LocalDate to,
                                                              ScreeningStatus status);

    /* Variante per la modifica: esclude la proiezione che si sta spostando */
    List<Screening> findByTheaterIdAndDateBetweenAndStatusNotAndIdNot(Long theaterId,
                                                                      LocalDate from,
                                                                      LocalDate to,
                                                                      ScreeningStatus status,
                                                                      Long id);

    List<Screening> findByDateGreaterThanEqualOrderByDateAscTimeAsc(LocalDate date);
                                    
}
