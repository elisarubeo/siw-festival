package it.uniroma3.siw.dto;

/**
 * Risposta di POST /api/auth/login.
 *
 * Oltre al token restituisce username e ruolo perche' al frontend servono
 * subito: lo username per capire quali recensioni sono dell'utente collegato,
 * il ruolo per decidere cosa mostrare. Sono comunque leggibili dentro il
 * token, ma cosi' React non deve decodificarlo.
 */
public record LoginResponse(String token, String username, String role) {
}
