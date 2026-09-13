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
    public List<Movie> search(String testo) {
        if (testo == null || testo.isBlank()) {
            return movieRepository.findAllFetchDirector();
        }
        return movieRepository.search("%" + testo.trim().toLowerCase() + "%");
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

        if (screeningRepository.existsByMovieId(id)) {
            throw new EntityInUseException("Non è possibile eliminare "
                    + movie.getTitle()
                    + ": ci sono proiezioni programmate. Elimina prima le proiezioni.");
        }

        String locandina = movie.getPosterFilename();
        movieRepository.delete(movie);

        cancellaDopoLaTransazione(locandina, null);
    }

    @Transactional
    public void updatePoster(Long id, MultipartFile file) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + id));

        String precedente = movie.getPosterFilename();
        String nuovo = imageStorageService.store(file);
        movie.setPosterFilename(nuovo);

        cancellaDopoLaTransazione(precedente, nuovo);
    }

    @Transactional
    public void removePoster(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + id));

        String precedente = movie.getPosterFilename();
        movie.setPosterFilename(null);

        cancellaDopoLaTransazione(precedente, null);
    }

    private void cancellaDopoLaTransazione(String seCommit, String seRollback) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
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
