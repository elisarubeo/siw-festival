package it.uniroma3.siw.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.exception.BusinessRuleException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.model.Movie;
import it.uniroma3.siw.model.Screening;
import it.uniroma3.siw.model.ScreeningStatus;
import it.uniroma3.siw.model.Theater;
import it.uniroma3.siw.repository.FestivalRepository;
import it.uniroma3.siw.repository.MovieRepository;
import it.uniroma3.siw.repository.ScreeningRepository;
import it.uniroma3.siw.repository.TheaterRepository;

@Service
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final FestivalRepository festivalRepository;
    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;

    public ScreeningService(ScreeningRepository screeningRepository,
                            FestivalRepository festivalRepository,
                            MovieRepository movieRepository,
                            TheaterRepository theaterRepository) {
        this.screeningRepository = screeningRepository;
        this.festivalRepository = festivalRepository;
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Screening> findById(Long id) {
        return screeningRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Screening> findAll() {
        return screeningRepository.findAll();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Screening schedule(Long festivalId, Long movieId, Long theaterId,
                              LocalDate date, LocalTime time) {

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun festival con id " + festivalId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + movieId));
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna sala con id " + theaterId));

        validate(festival, movieId, theaterId, date, time, null); //ancora non esiste la proiezione, quindi screeningIdToIgnore=null

        Screening screening = new Screening();
        screening.setFestival(festival);
        screening.setMovie(movie);
        screening.setTheater(theater);
        screening.setDate(date);
        screening.setTime(time);
        screening.setStatus(ScreeningStatus.SCHEDULED);

        return screeningRepository.save(screening);
    }

    private void validate(Festival festival, Long movieId, Long theaterId, LocalDate date, LocalTime time, Long screeningIdToIgnore) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + movieId));
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna sala con id " + theaterId));
        // 1. il film deve partecipare al festival
        if (!movie.getFestivals().contains(festival)) {
            throw new BusinessRuleException(movie.getTitle()
                    + " non partecipa a " + festival.getName()
                    + ": aggiungilo prima ai film del festival.");
        }

        // 2. la data deve cadere nel periodo del festival
        if (date.isBefore(festival.getStartDate()) || date.isAfter(festival.getEndDate())) {
            throw new BusinessRuleException("La data non rientra nel periodo del festival ("
                    + festival.getStartDate() + " - " + festival.getEndDate() + ").");
        }

        // 3. la sala deve essere libera per tutta la durata del film
        checkTheaterIsFree(theater, date, time, movie.getDuration(), screeningIdToIgnore);

    }

    private void checkTheaterIsFree(Theater theater, LocalDate date, LocalTime start,
                                    int durationMinutes, Long screeningIdToIgnore) {

        LocalDateTime inizio = date.atTime(start);
        LocalDateTime fine = inizio.plusMinutes(durationMinutes);

        LocalDate primoGiorno = date.minusDays(1);
        LocalDate ultimoGiorno = date.plusDays(1);

        List<Screening> candidate;
        // sto creando una nuova proiezione, quindi non devo escludere nessuna proiezione esistente
        if (screeningIdToIgnore == null) {
            candidate = screeningRepository.findByTheaterIdAndDateBetweenAndStatusNot(
                    theater.getId(), primoGiorno, ultimoGiorno, ScreeningStatus.CANCELLED);
        }
        // sto modificando una proiezione esistente, quindi devo escludere la proiezione stessa
        else {
            candidate = screeningRepository.findByTheaterIdAndDateBetweenAndStatusNotAndIdNot(
                    theater.getId(), primoGiorno, ultimoGiorno,
                    ScreeningStatus.CANCELLED, screeningIdToIgnore);
        }

        for (Screening other : candidate) {
            LocalDateTime altroInizio = other.getDate().atTime(other.getTime());
            LocalDateTime altraFine = altroInizio.plusMinutes(other.getMovie().getDuration());

            if (inizio.isBefore(altraFine) && altroInizio.isBefore(fine)) {
                throw new BusinessRuleException("La sala " + theater.getName()
                        + " è occupata il " + other.getDate()
                        + " da " + other.getTime() + " a " + altraFine.toLocalTime()
                        + " (" + other.getMovie().getTitle() + ").");
            }
        }
    }

    @Transactional
    public Long removeScreening(Long id){
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna proiezione con id " + id));
        screeningRepository.delete(screening);
        return screening.getFestival().getId();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long reschedule(Long screeningId, Long movieId, Long theaterId, LocalDate date, LocalTime time) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna proiezione con id " + screeningId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + movieId));
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna sala con id " + theaterId));
        validate(screening.getFestival(), movieId, theaterId, date, time, screeningId);
        screening.setMovie(movie);
        screening.setTheater(theater);
        screening.setDate(date);
        screening.setTime(time);
        return screening.getFestival().getId();
    }

    @Transactional
    public Long cancelScreening(Long id) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna proiezione con id " + id));
        screening.setStatus(ScreeningStatus.CANCELLED);

        return screening.getFestival().getId();
    }

    @Transactional(readOnly = true)
    public List<Screening> findFutureScreenings(LocalDate date) {
        return screeningRepository.findByDateGreaterThanEqualOrderByDateAscTimeAsc(date);
    }
}
