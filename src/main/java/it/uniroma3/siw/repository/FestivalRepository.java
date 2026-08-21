package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Festival;

public interface FestivalRepository extends JpaRepository<Festival, Long> {
}
