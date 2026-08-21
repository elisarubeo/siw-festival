package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.repository.FestivalRepository;

@Service
public class FestivalService {

    private FestivalRepository festivalRepository;

    public FestivalService(FestivalRepository festivalRepository) {
        this.festivalRepository = festivalRepository;
    }

    public Optional<Festival> findById(Long id) {
        return festivalRepository.findById(id);
    }

    public List<Festival> findAll() {
        return festivalRepository.findAll();
    }
}
