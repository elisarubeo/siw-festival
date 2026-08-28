package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.exception.EntityInUseException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Movie;
import it.uniroma3.siw.repository.FestivalRepository;
import it.uniroma3.siw.repository.MovieRepository;
import it.uniroma3.siw.repository.ScreeningRepository;

@Service
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;

    public FestivalService(FestivalRepository festivalRepository,
                           MovieRepository movieRepository,
                           ScreeningRepository screeningRepository) {
        this.festivalRepository = festivalRepository;
        this.movieRepository = movieRepository;
        this.screeningRepository = screeningRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Festival> findById(Long id) {
        return festivalRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Festival> findAll() {
        return festivalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsByNameAndYear(String name, Integer year) {
        return festivalRepository.existsByNameAndYear(name, year);
    }

    @Transactional(readOnly = true)
    public boolean existsByNameAndYearExcluding(String name, Integer year, Long id) {
        return festivalRepository.existsByNameAndYearAndIdNot(name, year, id);
    }

    @Transactional
    public Festival save(Festival festival) {
        return festivalRepository.save(festival);
    }

    @Transactional(readOnly = true)
    public List<Movie> findMoviesNotInFestival(Long festivalId) {
        return movieRepository.findNotInFestival(festivalId);
    }

    @Transactional
    public void addMovie(Long festivalId, Long movieId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun festival con id " + festivalId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + movieId));

        // gia' associato: non succede niente
        if (movie.getFestivals().contains(festival)) {
            return;
        }

        /* Movie.festivals e' il LATO PROPRIETARIO: e' questa riga che fa
           scrivere a Hibernate la riga nella tabella di join. */
        movie.getFestivals().add(festival);

        /* Il lato inverso si aggiorna solo per coerenza dell'oggetto in
           memoria: senza, il festival appena letto non mostrerebbe il film. */
        festival.getMovies().add(movie);

    }

    @Transactional
    public void removeMovie(Long festivalId, Long movieId) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun festival con id " + festivalId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + movieId));

        /* Resterebbero proiezioni di un film che non partecipa
           piu' al festival. */
        if (screeningRepository.existsByFestivalIdAndMovieId(festivalId, movieId)) {
            throw new EntityInUseException("Non è possibile togliere "
                    + movie.getTitle() + " da " + festival.getName()
                    + ": ha delle proiezioni programmate in questo festival.");
        }

        movie.getFestivals().remove(festival);
        festival.getMovies().remove(movie);
    }

    @Transactional
    public Festival update(Long id, Festival data) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun festival con id " + id));
        festival.setName(data.getName());
        festival.setCity(data.getCity());
        festival.setYear(data.getYear());
        festival.setStartDate(data.getStartDate());
        festival.setEndDate(data.getEndDate());
        festival.setDescription(data.getDescription());
        return festival;
    }

    @Transactional
    public void delete(Long id) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun festival con id " + id));

        if (screeningRepository.existsByFestivalId(id)) {
            throw new EntityInUseException("Non è possibile eliminare "
                    + festival.getName()
                    + ": ha un programma di proiezioni. Elimina prima le proiezioni.");
        }

        /* Festival e' il lato inverso della ManyToMany con Movie: Hibernate non
           ripulisce da solo le righe di movie_festival, quindi l'eliminazione
           fallirebbe per violazione di chiave esterna. */
        if (movieRepository.existsByFestivals_Id(id)) {
            throw new EntityInUseException("Non è possibile eliminare "
                    + festival.getName()
                    + ": ci sono film che vi partecipano. Rimuovili prima dal festival.");
        }

        festivalRepository.delete(festival);
    }
}
