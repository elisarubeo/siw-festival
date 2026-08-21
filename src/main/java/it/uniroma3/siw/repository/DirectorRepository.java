package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Director;

public interface DirectorRepository extends JpaRepository<Director, Long> {
}
