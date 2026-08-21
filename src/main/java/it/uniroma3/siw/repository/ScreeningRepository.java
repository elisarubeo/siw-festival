package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Screening;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
}
