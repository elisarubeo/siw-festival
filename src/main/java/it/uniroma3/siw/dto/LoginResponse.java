package it.uniroma3.siw.dto;

/**
 * Risposta di POST /api/auth/login.
 *
 * Oltre al token restituisce i dati che al frontend servono subito: il ruolo
 * per decidere cosa mostrare, e userId per riconoscere le recensioni
 * dell'utente collegato confrontandolo con ReviewDto.authorId.
 *
 * userId NON sta nel token: il payload di un JWT e' leggibile da chiunque, e
 * per quanto un id non sia un segreto, il principio e' mettere nel token solo
 * cio' che serve ad autenticare. Qui viaggia nel corpo della risposta.
 */
public record LoginResponse(String token, String username, Long userId, String role) {
}
