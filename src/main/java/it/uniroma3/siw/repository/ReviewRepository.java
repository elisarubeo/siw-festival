package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.uniroma3.siw.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
