package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Director;

public interface DirectorRepository extends JpaRepository<Director, Long> {

    /* Un regista e' identificato dalla coppia nome+cognome, non dai due campi
       separatamente: serve una sola query che li controlli insieme. */
    boolean existsByNameAndSurname(String name, String surname);
}
