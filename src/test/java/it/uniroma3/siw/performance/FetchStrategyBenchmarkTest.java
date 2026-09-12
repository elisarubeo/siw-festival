package it.uniroma3.siw.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import it.uniroma3.siw.model.Director;
import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Movie;
import it.uniroma3.siw.repository.MovieRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Analisi sperimentale dell'accesso ai dati (traccia §8.2).
 *
 * CASO D'USO MISURATO: "caricare tutti i film di un festival con i relativi
 * registi". Non e' un esempio inventato per il test: e' esattamente cio' che
 * fa la pagina di dettaglio di un festival (templates/festivals/show.html),
 * che per ogni film stampa nome e cognome del regista.
 *
 * L'associazione Movie.director e' ManyToOne LAZY: navigandola film per film
 * Hibernate emette una query per ciascun regista — il problema delle N+1.
 * Il test confronta tre modi di ottenere lo STESSO risultato:
 *
 *   1. LAZY         — navigazione delle associazioni (1 + 1 + N query)
 *   2. JOIN FETCH   — una query sola, scritta a mano in JPQL
 *   3. EntityGraph  — una query sola, dichiarata con un'annotazione
 *
 * COME SI ESEGUE (serve PostgreSQL avviato, come per l'applicazione):
 *
 *   ./mvnw test -Dtest=FetchStrategyBenchmarkTest
 *
 * Parametri, tutti opzionali:
 *   -Dbench.movies=500   quanti film mettere nel festival di prova (default 200)
 *   -Dbench.sql=true     stampa anche l'SQL generato da Hibernate, per leggere
 *                        le query una per una invece dei soli totali
 *
 * I dati di prova vengono creati dal test stesso e la transazione viene
 * annullata alla fine (@Transactional su una classe di test fa rollback):
 * il database resta esattamente com'era.
 */
@SpringBootTest
@Transactional
class FetchStrategyBenchmarkTest {

    /** Quanti film mettere nel festival di prova. */
    private static final int FILM_DI_PROVA = Integer.getInteger("bench.movies", 200);

    /** true se e' stato passato -Dbench.sql=true: si vuole vedere anche l'SQL. */
    private static final boolean MOSTRA_SQL = Boolean.getBoolean("bench.sql");

    /**
     * L'applicazione ha show-sql attivo, utile in sviluppo ma qui coprirebbe
     * l'output del confronto con centinaia di righe di SQL. Si spegne di
     * default e si riaccende con -Dbench.sql=true, senza toccare il codice:
     * durante l'orale serve per mostrare le query che Hibernate genera davvero.
     */
    @DynamicPropertySource
    static void configuraShowSql(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.show-sql", () -> MOSTRA_SQL);
    }

    /**
     * Il livello del logger si imposta a mano e non fra le proprieta' dinamiche
     * del test: Spring Boot applica le logging.level all'avvio del contesto,
     * quindi una proprieta' dichiarata li' verrebbe comunque sovrascritta da
     * quella di application.properties (che tiene org.hibernate.SQL a debug).
     * Va chiamato DOPO l'avvio del contesto — da qui, cioe' dal @BeforeEach —
     * altrimenti l'avvio rimette il livello di prima.
     */
    private static void configuraLogSql() {
        Level livello = MOSTRA_SQL ? Level.DEBUG : Level.WARN;
        for (String logger : List.of("org.hibernate.SQL", "org.hibernate.orm.jdbc.bind",
                "org.hibernate.type.descriptor.sql")) {
            if (LoggerFactory.getLogger(logger) instanceof ch.qos.logback.classic.Logger logback) {
                logback.setLevel(livello);
            }
        }
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MovieRepository movieRepository;

    private Long festivalId;

    /* ==================================================================
       DATI DI PROVA
       ================================================================== */

    /**
     * Crea un festival con FILM_DI_PROVA film, ciascuno con un regista
     * diverso: un regista per film e' il caso peggiore per le N+1, ed e'
     * quello che rende il confronto leggibile.
     *
     * I dati vengono creati qui e non presi da import.sql perche' le tre
     * strategie devono lavorare sullo STESSO insieme di dati (requisito 2
     * della traccia) e perche' con i dieci film dei dati di prova la
     * differenza fra una query e undici non si vedrebbe.
     */
    @BeforeEach
    void preparaDataset() {
        configuraLogSql();

        Festival festival = new Festival();
        festival.setName("Festival di prova (benchmark)");
        festival.setCity("Roma");
        festival.setYear(2026);
        festival.setStartDate(LocalDate.of(2026, 1, 1));
        festival.setEndDate(LocalDate.of(2026, 1, 10));
        entityManager.persist(festival);

        for (int i = 0; i < FILM_DI_PROVA; i++) {
            Director director = new Director();
            director.setName("Nome" + i);
            director.setSurname("Cognome" + i);
            director.setNationality("Italiana");
            director.setBirthDate(LocalDate.of(1960 + (i % 40), 1, 1));
            entityManager.persist(director);

            Movie movie = new Movie();
            movie.setTitle(String.format("Film di prova %04d", i));
            movie.setYear(2000 + (i % 25));
            movie.setDuration(90 + (i % 60));
            movie.setGenre("Drammatico");
            movie.setCountry("Italia");
            movie.setDirector(director);
            /* Movie e' il lato proprietario della ManyToMany: e' da qui che
               si scrive la riga nella tabella di join movie_festival. */
            movie.getFestivals().add(festival);
            entityManager.persist(movie);
        }

        /* flush: manda gli INSERT al database, altrimenti le query delle
           strategie non troverebbero nulla.
           clear: svuota il persistence context, altrimenti le entita' appena
           create resterebbero in memoria e la prima strategia non eseguirebbe
           nessuna query — misurerebbe la cache di primo livello, non il
           database. */
        entityManager.flush();
        entityManager.clear();

        this.festivalId = festival.getId();
    }

    /* ==================================================================
       IL TEST
       ================================================================== */

    @Test
    @DisplayName("Confronto fra strategie di accesso ai dati: LAZY, JOIN FETCH, EntityGraph")
    void confrontoStrategieDiAccesso() {

        stampaIntestazione();

        /* Giro a vuoto: la primissima esecuzione paga la compilazione delle
           query e la preparazione degli statement JDBC, costi che non
           riguardano la strategia e falserebbero il confronto. */
        riscaldamento();

        Misura lazy = misura("Strategia 1: LAZY (navigazione delle associazioni)", this::conLazy);
        Misura joinFetch = misura("Strategia 2: JOIN FETCH (query JPQL esplicita)", this::conJoinFetch);
        Misura entityGraph = misura("Strategia 3: EntityGraph (@EntityGraph sul repository)", this::conEntityGraph);

        stampaRiepilogo(lazy, joinFetch, entityGraph);

        /* Le tre strategie devono restituire lo stesso risultato: cambia come
           i dati vengono caricati, non quali. */
        assertEquals(FILM_DI_PROVA, lazy.film(), "film caricati dalla strategia LAZY");
        assertEquals(lazy.film(), joinFetch.film(), "JOIN FETCH deve caricare gli stessi film");
        assertEquals(lazy.film(), entityGraph.film(), "EntityGraph deve caricare gli stessi film");
        assertEquals(lazy.registi(), joinFetch.registi(), "e gli stessi registi");
        assertEquals(lazy.registi(), entityGraph.registi(), "e gli stessi registi");

        /* Il punto della misura: le due strategie di fetch tolgono le N query
           aggiuntive sui registi. */
        assertTrue(joinFetch.query() < lazy.query(),
                "JOIN FETCH deve eseguire meno query della navigazione LAZY");
        assertTrue(entityGraph.query() < lazy.query(),
                "EntityGraph deve eseguire meno query della navigazione LAZY");
    }

    /* ==================================================================
       LE TRE STRATEGIE
       Stesso caso d'uso, stessi dati, stesso risultato: cambia solo il modo
       in cui i film e i loro registi arrivano dal database.
       ================================================================== */

    /**
     * Come fa oggi la pagina di dettaglio del festival: si carica il festival
     * e si naviga festival.getMovies(), poi per ogni film il suo regista.
     *
     * Query attese: 1 per il festival + 1 per la collezione dei film + N per
     * i registi, uno alla volta.
     */
    private Risultato conLazy() {
        Festival festival = entityManager.find(Festival.class, this.festivalId);

        Set<String> registi = new HashSet<>();
        int film = 0;
        for (Movie movie : festival.getMovies()) {
            /* getSurname() e non getId(): su un proxy LAZY l'id e' gia' noto e
               non farebbe partire nessuna query. E' leggendo un attributo vero
               che il proxy si inizializza — ed e' quello che il template fa
               quando stampa il nome del regista. */
            registi.add(movie.getDirector().getSurname());
            film++;
        }
        return new Risultato(film, registi.size());
    }

    /**
     * Una sola query: i registi arrivano insieme ai film, gia' inizializzati.
     */
    private Risultato conJoinFetch() {
        List<Movie> film = this.movieRepository.findByFestivalIdFetchDirector(this.festivalId);

        Set<String> registi = new HashSet<>();
        for (Movie movie : film) {
            registi.add(movie.getDirector().getSurname());
        }
        return new Risultato(film.size(), registi.size());
    }

    /**
     * Stesso effetto del JOIN FETCH, ma dichiarato con un'annotazione sulla
     * query derivata dal nome del metodo.
     */
    private Risultato conEntityGraph() {
        List<Movie> film = this.movieRepository.findByFestivals_IdOrderByTitleAsc(this.festivalId);

        Set<String> registi = new HashSet<>();
        for (Movie movie : film) {
            registi.add(movie.getDirector().getSurname());
        }
        return new Risultato(film.size(), registi.size());
    }

    /* ==================================================================
       MISURAZIONE
       ================================================================== */

    /**
     * Esegue una strategia con il persistence context vuoto e le statistiche
     * azzerate, e ne riporta query eseguite, entita' caricate e tempo.
     *
     * Il clear() prima di ogni misura e' la parte che conta: senza, la seconda
     * strategia troverebbe tutto nella cache di primo livello e risulterebbe
     * istantanea per un motivo che non ha nulla a che vedere con il fetch.
     */
    private Misura misura(String nome, Supplier<Risultato> strategia) {
        entityManager.clear();

        Statistics statistiche = statistiche();
        statistiche.clear();

        long inizio = System.nanoTime();
        Risultato risultato = strategia.get();
        long millisecondi = (System.nanoTime() - inizio) / 1_000_000;

        Misura misura = new Misura(nome, risultato.film(), risultato.registi(),
                statistiche.getPrepareStatementCount(),
                statistiche.getEntityLoadCount(),
                millisecondi);

        stampa(misura);
        return misura;
    }

    private void riscaldamento() {
        entityManager.clear();
        conLazy();
        entityManager.clear();
        conJoinFetch();
        entityManager.clear();
        conEntityGraph();
    }

    /**
     * Le statistiche di Hibernate sono spente di default: si accendono qui,
     * solo per il test, senza toccare application.properties.
     * getPrepareStatementCount() conta gli statement SQL realmente inviati al
     * database, getEntityLoadCount() le entita' materializzate.
     */
    private Statistics statistiche() {
        Statistics statistiche = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class)
                .getStatistics();
        statistiche.setStatisticsEnabled(true);
        return statistiche;
    }

    /* ==================================================================
       OUTPUT
       ================================================================== */

    private void stampaIntestazione() {
        System.out.println();
        System.out.println("=".repeat(72));
        System.out.println("=== Test accesso ai dati — film di un festival con i relativi registi ===");
        System.out.printf("Dataset: 1 festival, %d film, %d registi (un regista per film)%n",
                FILM_DI_PROVA, FILM_DI_PROVA);
        System.out.println("Stesso caso d'uso e stessi dati per tutte e tre le strategie.");
    }

    private void stampa(Misura misura) {
        System.out.println();
        System.out.println(misura.nome());
        System.out.printf("  Film caricati:     %d%n", misura.film());
        System.out.printf("  Registi letti:     %d%n", misura.registi());
        System.out.printf("  Query SQL:         %d%n", misura.query());
        System.out.printf("  Entità caricate:   %d%n", misura.entita());
        System.out.printf("  Tempo:             %d ms%n", misura.millisecondi());
    }

    private void stampaRiepilogo(Misura... misure) {
        System.out.println();
        System.out.println("Riepilogo");
        System.out.printf("  %-14s %10s %10s%n", "", "query SQL", "tempo");
        for (Misura misura : misure) {
            System.out.printf("  %-14s %10d %8d ms%n", etichetta(misura), misura.query(),
                    misura.millisecondi());
        }
        System.out.println();
        System.out.println("La strategia LAZY esegue 1 query per il festival, 1 per la collezione");
        System.out.println("dei film e 1 per ogni regista: e' il problema delle N+1 query. Al");
        System.out.println("crescere dei film il numero di query cresce con loro, mentre con");
        System.out.println("JOIN FETCH ed EntityGraph resta 1.");
        System.out.println("=".repeat(72));
        System.out.println();
    }

    private String etichetta(Misura misura) {
        if (misura.nome().contains("LAZY")) {
            return "LAZY";
        }
        return misura.nome().contains("JOIN FETCH") ? "JOIN FETCH" : "ENTITY GRAPH";
    }

    /** Cosa ha prodotto una strategia: serve a verificare che siano equivalenti. */
    private record Risultato(int film, int registi) {
    }

    /** Il risultato di una strategia piu' i suoi numeri. */
    private record Misura(String nome, int film, int registi, long query, long entita,
                          long millisecondi) {
    }
}
