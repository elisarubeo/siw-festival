package it.uniroma3.siw.exception;

/**
 * Il file caricato non è un'immagine utilizzabile: formato non ammesso,
 * file vuoto o nome non valido.
 *
 * È distinta da BusinessRuleException perché non riguarda una regola del
 * dominio (un festival, un film, una recensione) ma il file in sé: chi la
 * riceve deve rimandare l'utente alla form per caricarne un altro, non
 * comunicargli che l'operazione era impossibile.
 */
public class InvalidImageException extends RuntimeException {

    public InvalidImageException(String message) {
        super(message);
    }
}
