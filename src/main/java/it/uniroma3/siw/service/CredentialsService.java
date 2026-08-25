package it.uniroma3.siw.service;


import java.util.Optional;

import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.repository.CredentialsRepository;

@Service
public class CredentialsService {
    CredentialsRepository credentialsRepository;

    public CredentialsService(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    public Credentials getCredentials(Long id) {
        return credentialsRepository.findById(id).orElse(null);
    }

    public Optional<Credentials> getCredentials(String username) {
        return credentialsRepository.findByUsername(username);
    }

    public Credentials saveCredentials(Credentials credentials) {
        return credentialsRepository.save(credentials);
    }

}
