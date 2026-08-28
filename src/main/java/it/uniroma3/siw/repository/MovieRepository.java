package it.uniroma3.siw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitleAndYear(String title, Integer year);

    boolean existsByTitleAndYearAndIdNot(String title, Integer year, Long id);

    boolean existsByDirectorId(Long directorId);

    boolean existsByFestivals_Id(Long festivalId);

    /* I film che NON partecipano ancora a un certo festival: sono quelli da
       proporre nella select. "not member of" e' l'operatore JPQL per
       l'appartenenza a una collezione; con i soli nomi dei metodi derivati
       non si esprime, quindi la query si scrive a mano. */
    @Query("select m from Movie m where :festivalId not in "
         + "(select f.id from m.festivals f) order by m.title")
    List<Movie> findNotInFestival(@Param("festivalId") Long festivalId);
}
