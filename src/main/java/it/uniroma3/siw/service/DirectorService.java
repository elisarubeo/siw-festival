package it.uniroma3.siw.service;
import it.uniroma3.siw.model.*;
import it.uniroma3.siw.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

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
}