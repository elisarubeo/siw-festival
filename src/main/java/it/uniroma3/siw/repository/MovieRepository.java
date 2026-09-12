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

    /* ------------------------------------------------------------------
       Ricerca dei film per titolo, genere o regista (bonus §13 della traccia).

       Il pattern arriva dal service gia' in minuscolo e circondato da %:
       prepararlo una volta sola li' evita di ripetere lower(...) e concat(...)
       su ogni ramo della OR, e lascia qui una query piu' leggibile.

       Il cognome del regista non ha un ramo suo: si confronta "nome cognome"
       concatenato, cosi' la stessa condizione copre "sorrentino", "paolo" e
       "paolo sorrentino" senza doverli distinguere.

       m.genre puo' essere null e in quel caso il confronto non e' vero ne'
       falso ma sconosciuto: la riga semplicemente non corrisponde a quel ramo,
       che e' il comportamento voluto.

       join fetch m.director: l'elenco stampa il regista di ogni film, e senza
       il fetch partirebbe una query per film — le stesse N+1 misurate da
       FetchStrategyBenchmarkTest. L'alias d serve anche alla clausola where. */
    @Query("select m from Movie m join fetch m.director d "
         + "where lower(m.title) like :pattern "
         + "or lower(m.genre) like :pattern "
         + "or lower(concat(d.name, ' ', d.surname)) like :pattern "
         + "order by m.title")
    List<Movie> search(@Param("pattern") String pattern);

    /* L'elenco completo, con il regista gia' caricato: e' la stessa pagina
       quando il campo di ricerca e' vuoto, e deve costare le stesse query. */
    @Query("select m from Movie m join fetch m.director order by m.title")
    List<Movie> findAllFetchDirector();

    /* I film che NON partecipano ancora a un certo festival: sono quelli da
       proporre nella select. "not member of" e' l'operatore JPQL per
       l'appartenenza a una collezione; con i soli nomi dei metodi derivati
       non si esprime, quindi la query si scrive a mano. */
    @Query("select m from Movie m where :festivalId not in "
         + "(select f.id from m.festivals f) order by m.title")
    List<Movie> findNotInFestival(@Param("festivalId") Long festivalId);

    /* ------------------------------------------------------------------
       I film di un festival con il relativo regista, in due modi diversi.

       Servono al caso d'uso della pagina di dettaglio di un festival, che per
       ogni film mostra anche il regista: navigando l'associazione LAZY
       partirebbe una query per ogni film (problema delle N+1). Entrambi i
       metodi qui sotto lo evitano caricando tutto con una sola query; sono
       anche le due strategie messe a confronto da FetchStrategyBenchmarkTest.
       ------------------------------------------------------------------ */

    /* Strategia 1: JOIN FETCH esplicito nella query.
       "join fetch m.director" dice a Hibernate di caricare il regista INSIEME
       al film; "join m.festivals f" (senza fetch) serve solo a filtrare, i
       festival non interessa portarli in memoria. */
    @Query("select distinct m from Movie m join fetch m.director "
         + "join m.festivals f where f.id = :festivalId order by m.title")
    List<Movie> findByFestivalIdFetchDirector(@Param("festivalId") Long festivalId);

    /* Strategia 2: stesso risultato con un EntityGraph.
       La query resta derivata dal nome del metodo e l'annotazione dichiara
       quali associazioni caricare subito: si cambia cosa portare in memoria
       senza riscrivere la query. Hibernate genera un LEFT JOIN invece
       dell'INNER JOIN del fetch esplicito — differenza irrilevante qui, dato
       che director e' optional = false, ma non in generale. */
    @EntityGraph(attributePaths = "director")
    List<Movie> findByFestivals_IdOrderByTitleAsc(Long festivalId);
}
