package it.uniroma3.siw.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Festival;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    boolean existsByNameAndYear(String name, Integer year);

    boolean existsByNameAndYearAndIdNot(String name, Integer year, Long id);

    /* Festival in corso o non ancora iniziati: i piu' imminenti per primi. */
    List<Festival> findByEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate date);

    /* Festival gia' conclusi: i piu' recenti per primi. */
    List<Festival> findByEndDateLessThanOrderByStartDateDesc(LocalDate date);
}
