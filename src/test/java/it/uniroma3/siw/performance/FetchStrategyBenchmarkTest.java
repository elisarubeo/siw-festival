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

@SpringBootTest
@Transactional
class FetchStrategyBenchmarkTest {

    private static final int FILM_DI_PROVA = Integer.getInteger("bench.movies", 200);

    private static final boolean MOSTRA_SQL = Boolean.getBoolean("bench.sql");

    @DynamicPropertySource
    static void configuraShowSql(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.show-sql", () -> MOSTRA_SQL);
    }

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
            movie.getFestivals().add(festival);
            entityManager.persist(movie);
        }

        entityManager.flush();
        entityManager.clear();

        this.festivalId = festival.getId();
    }

    @Test
    @DisplayName("Confronto fra strategie di accesso ai dati: LAZY, JOIN FETCH, EntityGraph")
    void confrontoStrategieDiAccesso() {

        stampaIntestazione();

        riscaldamento();

        Misura lazy = misura("Strategia 1: LAZY (navigazione delle associazioni)", this::conLazy);
        Misura joinFetch = misura("Strategia 2: JOIN FETCH (query JPQL esplicita)", this::conJoinFetch);
        Misura entityGraph = misura("Strategia 3: EntityGraph (@EntityGraph sul repository)", this::conEntityGraph);

        stampaRiepilogo(lazy, joinFetch, entityGraph);

        assertEquals(FILM_DI_PROVA, lazy.film(), "film caricati dalla strategia LAZY");
        assertEquals(lazy.film(), joinFetch.film(), "JOIN FETCH deve caricare gli stessi film");
        assertEquals(lazy.film(), entityGraph.film(), "EntityGraph deve caricare gli stessi film");
        assertEquals(lazy.registi(), joinFetch.registi(), "e gli stessi registi");
        assertEquals(lazy.registi(), entityGraph.registi(), "e gli stessi registi");

        assertTrue(joinFetch.query() < lazy.query(),
                "JOIN FETCH deve eseguire meno query della navigazione LAZY");
        assertTrue(entityGraph.query() < lazy.query(),
                "EntityGraph deve eseguire meno query della navigazione LAZY");
    }

    private Risultato conLazy() {
        Festival festival = entityManager.find(Festival.class, this.festivalId);

        Set<String> registi = new HashSet<>();
        int film = 0;
        for (Movie movie : festival.getMovies()) {
            registi.add(movie.getDirector().getSurname());
            film++;
        }
        return new Risultato(film, registi.size());
    }

    private Risultato conJoinFetch() {
        List<Movie> film = this.movieRepository.findByFestivalIdFetchDirector(this.festivalId);

        Set<String> registi = new HashSet<>();
        for (Movie movie : film) {
            registi.add(movie.getDirector().getSurname());
        }
        return new Risultato(film.size(), registi.size());
    }

    private Risultato conEntityGraph() {
        List<Movie> film = this.movieRepository.findByFestivals_IdOrderByTitleAsc(this.festivalId);

        Set<String> registi = new HashSet<>();
        for (Movie movie : film) {
            registi.add(movie.getDirector().getSurname());
        }
        return new Risultato(film.size(), registi.size());
    }

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

    private Statistics statistiche() {
        Statistics statistiche = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class)
                .getStatistics();
        statistiche.setStatisticsEnabled(true);
        return statistiche;
    }

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

    private record Risultato(int film, int registi) {
    }

    private record Misura(String nome, int film, int registi, long query, long entita,
                          long millisecondi) {
    }
}
