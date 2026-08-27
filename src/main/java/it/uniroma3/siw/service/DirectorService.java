package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.exception.EntityInUseException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Director;
import it.uniroma3.siw.repository.DirectorRepository;
import it.uniroma3.siw.repository.MovieRepository;

@Service
public class DirectorService {

    private final DirectorRepository directorRepository;
    private final MovieRepository movieRepository;

    public DirectorService(DirectorRepository directorRepository, MovieRepository movieRepository) {
        this.directorRepository = directorRepository;
        this.movieRepository = movieRepository;
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

    @Transactional(readOnly = true)
    public boolean existsByNameAndSurnameExcluding(String name, String surname, Long id) {
        return directorRepository.existsByNameAndSurnameAndIdNot(name, surname, id);
    }

    @Transactional
    public Director save(Director director) {
        return directorRepository.save(director);
    }

    /* Carica l'entita' gestita e ne aggiorna i soli campi modificabili.
       Non si salva l'oggetto arrivato dalla form: le sue collezioni sono
       vuote e sovrascriverebbero le associazioni esistenti. */
    @Transactional
    public Director update(Long id, Director data) {
        Director director = directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun regista con id " + id));
        director.setName(data.getName());
        director.setSurname(data.getSurname());
        director.setNationality(data.getNationality());
        director.setBirthDate(data.getBirthDate());
        return director;
    }

    @Transactional
    public void delete(Long id) {
        Director director = directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun regista con id " + id));

        /* Movie.director e' obbligatorio: cancellare un regista con film
           lascerebbe quei film senza regista, e il database rifiuterebbe
           l'operazione con un errore di chiave esterna. */
        if (movieRepository.existsByDirectorId(id)) {
            throw new EntityInUseException("Non è possibile eliminare "
                    + director.getName() + " " + director.getSurname()
                    + ": ha dei film associati. Elimina prima i suoi film.");
        }

        directorRepository.delete(director);
    }
}
