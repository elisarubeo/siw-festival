package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Theater;

public interface TheaterRepository extends JpaRepository<Theater, Long> {
    boolean existsByNameAndAddress(String name, String address);
}
