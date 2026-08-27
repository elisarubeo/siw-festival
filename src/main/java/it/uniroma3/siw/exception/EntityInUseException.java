package it.uniroma3.siw.exception;

/**
 * Segnala che un'entita' non puo' essere eliminata perche' altre entita'
 * dipendono da lei (per esempio un regista che ha dei film).
 */
public class EntityInUseException extends RuntimeException {

    public EntityInUseException(String message) {
        super(message);
    }
}
