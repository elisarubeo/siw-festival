package it.uniroma3.siw.exception;

/**
 * Segnala che una risorsa richiesta tramite id non esiste.
 * Viene tradotta in una risposta HTTP 404 da GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
