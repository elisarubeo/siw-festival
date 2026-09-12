# Analisi sperimentale dell'accesso ai dati (traccia §8)

Script richiesto dal §8.2 della traccia: confronta più strategie di accesso ai
dati sullo stesso caso d'uso e sugli stessi dati, riportando query SQL eseguite,
tempo e oggetti caricati.

**File:** `src/test/java/it/uniroma3/siw/performance/FetchStrategyBenchmarkTest.java`

## Come si esegue

PostgreSQL deve essere avviato, come per l'applicazione (il test usa lo stesso
database configurato in `application.properties`).

```bash
./mvnw test -Dtest=FetchStrategyBenchmarkTest
```

Parametri opzionali:

| Parametro | Effetto |
|---|---|
| `-Dbench.movies=500` | quanti film mettere nel festival di prova (default 200) |
| `-Dbench.sql=true` | stampa anche l'SQL generato da Hibernate, per leggere le query una per una |

I dati di prova li crea il test stesso e la transazione viene annullata alla
fine (`@Transactional` su una classe di test fa rollback): **il database resta
esattamente com'era**, verificato dopo l'esecuzione.

## Caso d'uso misurato

«Caricare tutti i film di un festival con i relativi registi.»

Non è un esempio costruito per l'occasione: è quello che fa la pagina di
dettaglio di un festival, `templates/festivals/show.html`, che per ogni film
stampa nome e cognome del regista (`movie.director.name`). `Movie.director` è
`@ManyToOne(fetch = LAZY)`, quindi navigandolo film per film parte una query per
ciascun regista.

Tre strategie a confronto, stesso risultato:

1. **LAZY** — `festival.getMovies()` e poi `movie.getDirector()` per ciascun film;
2. **JOIN FETCH** — `MovieRepository.findByFestivalIdFetchDirector(...)`, query JPQL con `join fetch m.director`;
3. **EntityGraph** — `MovieRepository.findByFestivals_IdOrderByTitleAsc(...)`, query derivata dal nome del metodo più `@EntityGraph(attributePaths = "director")`.

## Output (esecuzione reale, 200 film)

```
========================================================================
=== Test accesso ai dati — film di un festival con i relativi registi ===
Dataset: 1 festival, 200 film, 200 registi (un regista per film)
Stesso caso d'uso e stessi dati per tutte e tre le strategie.

Strategia 1: LAZY (navigazione delle associazioni)
  Film caricati:     200
  Registi letti:     200
  Query SQL:         202
  Entità caricate:   401
  Tempo:             26 ms

Strategia 2: JOIN FETCH (query JPQL esplicita)
  Film caricati:     200
  Registi letti:     200
  Query SQL:         1
  Entità caricate:   400
  Tempo:             3 ms

Strategia 3: EntityGraph (@EntityGraph sul repository)
  Film caricati:     200
  Registi letti:     200
  Query SQL:         1
  Entità caricate:   400
  Tempo:             3 ms

Riepilogo
                  query SQL      tempo
  LAZY                  202       26 ms
  JOIN FETCH              1        3 ms
  ENTITY GRAPH            1        3 ms
```

Con `-Dbench.movies=500` la strategia LAZY passa a **502 query**, le altre due
restano a **1**: è la dimostrazione che il numero di query cresce con i dati
(N+1) invece di restare costante.

## Come si leggono i numeri

- **202 = 1 + 1 + 200.** Una query per il festival (`find`), una per la
  collezione `festival.movies`, poi una per ogni regista. È il problema delle
  N+1 query.
- **401 entità contro 400.** La strategia LAZY carica anche l'entità `Festival`,
  le altre due partono direttamente dai film filtrandoli per id del festival.
  Gli oggetti utili sono gli stessi: 200 film + 200 registi.
- **Il tempo conta meno del numero di query.** Su un database locale ogni query
  costa pochissimo; su un database remoto, dove ogni round trip vale
  millisecondi, 202 andate e ritorni contro 1 cambiano l'ordine di grandezza. Il
  numero di query è la misura strutturale, il tempo solo la sua conseguenza su
  questa macchina.
- **JOIN FETCH ed EntityGraph sono equivalenti in effetto, diversi in forma.**
  Il fetch esplicito genera un `inner join`, l'EntityGraph un `left join` (lo si
  vede con `-Dbench.sql=true`): differenza irrilevante qui, perché
  `director` è `optional = false`, ma non in generale — con un'associazione
  facoltativa l'inner join perderebbe le righe senza associato.

## Accortezze nella misura (da saper spiegare)

- `entityManager.clear()` prima di ogni strategia: senza, la seconda troverebbe
  tutto nella cache di primo livello e risulterebbe istantanea per un motivo che
  non ha niente a che vedere con il fetch.
- Un giro di riscaldamento prima delle misure: la prima esecuzione paga la
  compilazione delle query e la preparazione degli statement JDBC.
- Le statistiche vengono dalle `Statistics` di Hibernate, accese solo dentro il
  test: `getPrepareStatementCount()` conta gli statement SQL realmente inviati,
  `getEntityLoadCount()` le entità materializzate.
- Nella strategia LAZY si legge `getSurname()` e non `getId()`: su un proxy
  l'id è già noto e non farebbe partire nessuna query — sarebbe una misura falsa.
- Un regista diverso per ogni film: è il caso peggiore per le N+1 e rende il
  confronto leggibile (con registi ripetuti, Hibernate ne riuserebbe alcuni dalla
  cache di primo livello e il conteggio dipenderebbe dai dati).

## §8.1 — Strategie di fetch adottate nel modello

| Associazione | Strategia | Motivo |
|---|---|---|
| `Movie.director` | `ManyToOne` LAZY | il regista serve solo in alcune pagine; dove serve per molti film si usa il JOIN FETCH |
| `Movie.festivals` / `Festival.movies` | `ManyToMany` LAZY (default) | collezioni potenzialmente grandi, quasi mai necessarie insieme all'entità |
| `Movie.screenings`, `Festival.screenings`, `Theater.screenings`, `Director.movies` | `OneToMany` LAZY (default) | caricare una collezione a ogni lettura dell'entità sarebbe spreco puro |
| `Review.movie`, `Review.user` | `ManyToOne` LAZY | la lista delle recensioni di un film ha bisogno solo dell'autore, ed è caricato con `JOIN FETCH` nella query del repository |
| `Screening.festival`, `Screening.movie`, `Screening.theater` | `ManyToOne` LAZY | il programma delle proiezioni mostra film e sala: caso candidato a un secondo JOIN FETCH |

Nessuna associazione è EAGER: l'EAGER si porta dietro i dati a ogni caricamento
dell'entità, anche quando non servono, e non si può disattivare per singola
query — mentre il contrario (LAZY + fetch mirato dove serve) si decide query per
query. È la ragione per cui il default JPA `EAGER` sui `@ManyToOne` è stato
sovrascritto ovunque.

Un secondo esempio di N+1 già risolto in produzione è in
`ReviewRepository.findByMovieId`: `JOIN FETCH r.user` evita una query per ogni
recensione quando la API REST costruisce i `ReviewDto`.

## Nota su `open-in-view`

`spring.jpa.open-in-view` non è impostato, quindi vale il default `true`: la
sessione Hibernate resta aperta durante il rendering della vista. È il motivo per
cui `festivals/show.html` riesce a navigare `movie.director` fuori dal metodo
`@Transactional` del service senza `LazyInitializationException` — ed è anche il
motivo per cui, in quella pagina, le N query partono durante il rendering del
template. Con `open-in-view=false` la stessa pagina darebbe
`LazyInitializationException`, e il caricamento andrebbe fatto nel service con
una delle due strategie di fetch.

## Se all'esame viene chiesto di modificare qualcosa

- **Aggiungere una quarta strategia** (per esempio `@BatchSize` sul regista, o
  `Hibernate.initialize`): si scrive un metodo `conXxx()` accanto agli altri e si
  aggiunge una riga `misura("Strategia 4: ...", this::conXxx)`.
- **Cambiare il caso d'uso** (per esempio il programma delle proiezioni con film
  e sale): stessa struttura, cambia solo cosa si carica nei tre metodi.
- **Portare la correzione in produzione**: la pagina di dettaglio del festival
  può usare `movieRepository.findByFestivalIdFetchDirector(festivalId)` in
  `FestivalService`, passando al template la lista dei film già completa invece
  di far navigare `festival.movies` a Thymeleaf. Il test serve proprio a
  giustificare quella scelta con dei numeri.
- **Passare `Movie.director` a EAGER** e rieseguire: la strategia LAZY scende a 2
  query, ma il regista verrebbe caricato in *ogni* lettura di un film, anche
  dove non serve (elenco film, form, API). È il classico rimedio peggiore del
  male, e il test lo rende verificabile in un minuto.
