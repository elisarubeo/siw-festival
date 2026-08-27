package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public MovieService(MovieRepository movieRepository, ScreeningRepository screeningRepository) {
        this.movieRepository = movieRepository;
        this.screeningRepository = screeningRepository;
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

        movieRepository.delete(movie);
    }
}
