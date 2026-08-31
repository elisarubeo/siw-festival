package it.uniroma3.siw.exception;

/**
 * Segnala che un'operazione viola una regola di dominio: per esempio
 * programmare una proiezione in una sala gia' occupata.
 * Diversa da una violazione di validazione, che riguarda il singolo campo.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
