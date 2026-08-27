package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Director;
import it.uniroma3.siw.repository.DirectorRepository;

@Service
public class DirectorService {

    private final DirectorRepository directorRepository;

    public DirectorService(DirectorRepository directorRepository) {
        this.directorRepository = directorRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Director> findById(Long id) {
        return directorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Director> findAll() {
        return directorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsByNameAndSurname(String name, String surname) {
        return directorRepository.existsByNameAndSurname(name, surname);
    }

    @Transactional
    public Director save(Director director) {
        return directorRepository.save(director);
    }
}
