package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.exception.DuplicateResourceException;
import it.uniroma3.siw.exception.EntityInUseException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Movie;
import it.uniroma3.siw.repository.MovieRepository;
import it.uniroma3.siw.repository.ScreeningRepository;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;
    private final ImageStorageService imageStorageService;

    public MovieService(MovieRepository movieRepository,
                        ScreeningRepository screeningRepository,
                        ImageStorageService imageStorageService) {
        this.movieRepository = movieRepository;
        this.screeningRepository = screeningRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public Optional<Movie> findById(Long id) {
        return movieRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long count() {
        return movieRepository.count();
    }

    @Transactional(readOnly = true)
    public boolean existsByTitleAndYear(String title, Integer year) {
        return movieRepository.existsByTitleAndYear(title, year);
    }

    @Transactional(readOnly = true)
    public boolean existsByTitleAndYearExcluding(String title, Integer year, Long id) {
        return movieRepository.existsByTitleAndYearAndIdNot(title, year, id);
    }

    @Transactional
    public Movie save(Movie movie) throws DuplicateResourceException {
        if (movieRepository.existsByTitleAndYear(movie.getTitle(), movie.getYear())) {
            throw new DuplicateResourceException();
        }
        return movieRepository.save(movie);
    }

    @Transactional
    public Movie update(Long id, Movie data) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + id));
        movie.setTitle(data.getTitle());
        movie.setYear(data.getYear());
        movie.setGenre(data.getGenre());
        movie.setCountry(data.getCountry());
        movie.setDuration(data.getDuration());
        movie.setDirector(data.getDirector());
        return movie;
    }

    @Transactional
    public void delete(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + id));

        /* Le recensioni hanno cascade + orphanRemoval e spariscono con il film;
           le righe di movie_festival le toglie Hibernate perche' Movie e' il
           lato proprietario della ManyToMany. Le proiezioni no: quelle
           bloccano l'eliminazione. */
        if (screeningRepository.existsByMovieId(id)) {
            throw new EntityInUseException("Non è possibile eliminare "
                    + movie.getTitle()
                    + ": ci sono proiezioni programmate. Elimina prima le proiezioni.");
        }

        String locandina = movie.getPosterFilename();
        movieRepository.delete(movie);

        /* Il file si cancella solo se l'eliminazione va davvero a buon fine:
           se la transazione tornasse indietro, il film resterebbe nel database
           con il riferimento a un'immagine che non esiste piu'. */
        cancellaDopoLaTransazione(locandina, null);
    }

    /* ==================================================================
       LOCANDINA
       ================================================================== */

    /**
     * Imposta (o sostituisce) la locandina di un film.
     *
     * Il file viene scritto su disco e nel database finisce solo il suo nome.
     */
    @Transactional
    public void updatePoster(Long id, MultipartFile file) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + id));

        String precedente = movie.getPosterFilename();
        String nuovo = imageStorageService.store(file);
        movie.setPosterFilename(nuovo);

        /* Qui sta il punto delicato di tutta la funzionalita': il file e' gia'
           sul disco, ma la riga del database viene scritta davvero solo al
           commit. Il filesystem non partecipa alla transazione, quindi i due
           mondi vanno riallineati a mano:
             - se si arriva al commit, e' il file PRECEDENTE a non servire piu';
             - se si torna indietro, e' quello NUOVO a essere di troppo.
           Cancellare subito il precedente significherebbe perderlo in caso di
           rollback, lasciando nel database un nome che non punta piu' a nulla. */
        cancellaDopoLaTransazione(precedente, nuovo);
    }

    /** Toglie la locandina a un film: riferimento nel database e file su disco. */
    @Transactional
    public void removePoster(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + id));

        String precedente = movie.getPosterFilename();
        movie.setPosterFilename(null);

        cancellaDopoLaTransazione(precedente, null);
    }

    /**
     * Rimanda la cancellazione di un file alla conclusione della transazione:
     * viene eliminato {@code seCommit} se la transazione e' confermata,
     * {@code seRollback} se viene annullata. Un argomento null significa
     * "niente da cancellare in quel caso".
     */
    private void cancellaDopoLaTransazione(String seCommit, String seRollback) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            /* Nessuna transazione in corso (metodo chiamato fuori da @Transactional):
               non c'e' un commit da attendere, si cancella subito. */
            imageStorageService.delete(seCommit);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                imageStorageService.delete(status == STATUS_COMMITTED ? seCommit : seRollback);
            }
        });
    }
}
