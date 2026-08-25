package it.uniroma3.siw.repository;
import it.uniroma3.siw.model.Credentials;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    public Optional<Credentials> findByUsername(String username);

}
