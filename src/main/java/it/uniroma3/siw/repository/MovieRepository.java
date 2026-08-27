package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitleAndYear(String title, Integer year);

    boolean existsByTitleAndYearAndIdNot(String title, Integer year, Long id);

    /* Serve a sapere se un regista ha film, senza caricare la collezione. */
    boolean existsByDirectorId(Long directorId);

    /* L'underscore attraversa l'associazione: "il film ha, tra i suoi
       festival, quello con questo id". */
    boolean existsByFestivals_Id(Long festivalId);
}
