package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.exception.EntityInUseException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Theater;
import it.uniroma3.siw.repository.ScreeningRepository;
import it.uniroma3.siw.repository.TheaterRepository;

@Service
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final ScreeningRepository screeningRepository;

    public TheaterService(TheaterRepository theaterRepository, ScreeningRepository screeningRepository) {
        this.theaterRepository = theaterRepository;
        this.screeningRepository = screeningRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Theater> findById(Long id) {
        return theaterRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Theater> findAll() {
        return theaterRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsByNameAndAddress(String name, String address) {
        return theaterRepository.existsByNameAndAddress(name, address);
    }

    @Transactional(readOnly = true)
    public boolean existsByNameAndAddressExcluding(String name, String address, Long id) {
        return theaterRepository.existsByNameAndAddressAndIdNot(name, address, id);
    }

    @Transactional
    public Theater save(Theater theater) {
        return theaterRepository.save(theater);
    }

    @Transactional
    public Theater update(Long id, Theater data) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna sala con id " + id));
        theater.setName(data.getName());
        theater.setAddress(data.getAddress());
        theater.setCapacity(data.getCapacity());
        return theater;
    }

    @Transactional
    public void delete(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna sala con id " + id));

        /* Una proiezione si svolge sempre in una sala: eliminarla lascerebbe
           le proiezioni senza luogo. */
        if (screeningRepository.existsByTheaterId(id)) {
            throw new EntityInUseException("Non è possibile eliminare la sala "
                    + theater.getName()
                    + ": ci sono proiezioni programmate. Elimina prima le proiezioni.");
        }

        theaterRepository.delete(theater);
    }
}
