package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Director;

public interface DirectorRepository extends JpaRepository<Director, Long> {

    boolean existsByNameAndSurname(String name, String surname);

    /* Variante per la modifica: esclude il regista che si sta modificando,
       altrimenti un salvataggio senza cambi di nome risulterebbe duplicato. */
    boolean existsByNameAndSurnameAndIdNot(String name, String surname, Long id);
}
