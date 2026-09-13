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

    boolean existsByFestivalIdAndMovieId(Long festivalId, Long movieId);

    List<Screening> findByTheaterIdAndDateBetweenAndStatusNot(Long theaterId,
                                                              LocalDate from,
                                                              LocalDate to,
                                                              ScreeningStatus status);

    List<Screening> findByTheaterIdAndDateBetweenAndStatusNotAndIdNot(Long theaterId,
                                                                      LocalDate from,
                                                                      LocalDate to,
                                                                      ScreeningStatus status,
                                                                      Long id);

    List<Screening> findByDateGreaterThanEqualOrderByDateAscTimeAsc(LocalDate date);

}
