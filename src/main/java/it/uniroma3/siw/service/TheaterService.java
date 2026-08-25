package it.uniroma3.siw.service;
import it.uniroma3.siw.model.*;
import it.uniroma3.siw.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TheaterService {
    private final TheaterRepository theaterRepository;

    public TheaterService(TheaterRepository theaterRepository) {
        this.theaterRepository = theaterRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Theater> findById(Long id) {
        return theaterRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Theater> findAll() {
        return theaterRepository.findAll();
    }
}