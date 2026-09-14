package it.uniroma3.siw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitleAndYear(String title, Integer year);

    boolean existsByTitleAndYearAndIdNot(String title, Integer year, Long id);

    boolean existsByDirectorId(Long directorId);

    boolean existsByFestivals_Id(Long festivalId);

    @Query("select m from Movie m join fetch m.director d "
         + "where lower(m.title) like :pattern "
         + "or lower(m.genre) like :pattern "
         + "or lower(concat(d.name, ' ', d.surname)) like :pattern "
         + "order by m.title")
    List<Movie> search(@Param("pattern") String pattern);

    @Query("select m from Movie m join fetch m.director order by lower(m.title)")
    List<Movie> findAllFetchDirector();

    @Query("select m from Movie m where :festivalId not in "
         + "(select f.id from m.festivals f) order by m.title")
    List<Movie> findNotInFestival(@Param("festivalId") Long festivalId);

    @Query("select distinct m from Movie m join fetch m.director "
         + "join m.festivals f where f.id = :festivalId order by m.title")
    List<Movie> findByFestivalIdFetchDirector(@Param("festivalId") Long festivalId);

    @EntityGraph(attributePaths = "director")
    List<Movie> findByFestivals_IdOrderByTitleAsc(Long festivalId);
}
