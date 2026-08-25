package it.uniroma3.siw.service;
import it.uniroma3.siw.model.*;
import it.uniroma3.siw.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ScreeningService {
    private final ScreeningRepository screeningRepository;

    public ScreeningService(ScreeningRepository screeningRepository) {
        this.screeningRepository = screeningRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Screening> findById(Long id) {
        return screeningRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Screening> findAll() {
        return screeningRepository.findAll();
    }

}