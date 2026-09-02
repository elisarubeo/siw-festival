package it.uniroma3.siw.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corpo della richiesta di POST /api/auth/login.
 *
 * E' un record e non un'entita': i dati che entrano dall'API hanno una forma
 * propria, indipendente dal modello di dominio.
 */
public record LoginRequest(
        @NotBlank(message = "Lo username è obbligatorio") String username,
        @NotBlank(message = "La password è obbligatoria") String password) {
}
