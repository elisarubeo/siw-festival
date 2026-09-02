package it.uniroma3.siw.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import it.uniroma3.siw.dto.ApiError;

/**
 * Gestore degli errori per i soli controller REST.
 *
 * Serve perche' GlobalExceptionHandler restituisce NOMI DI VISTE Thymeleaf
 * ("error/404"): applicato a una chiamata /api, farebbe arrivare ad axios una
 * pagina HTML al posto del JSON atteso. Qui invece ogni eccezione diventa un
 * ResponseEntity con corpo ApiError.
 *
 * I controller REST stanno in it.uniroma3.siw.api e quelli Thymeleaf in
 * it.uniroma3.siw.controller: due package disgiunti, cosi' ciascun gestore
 * copre esattamente i suoi e non c'e' modo che si sovrappongano.
 */
@RestControllerAdvice(basePackages = "it.uniroma3.siw.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    /** Film o recensione che non esiste. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(e.getMessage()));
    }

    /**
     * Violazione di una regola di dominio: per esempio una seconda recensione
     * dello stesso utente per lo stesso film.
     *
     * 409 Conflict e non 400: la richiesta e' ben formata, e' lo stato del
     * sistema a renderla impossibile.
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(e.getMessage()));
    }

    @ExceptionHandler({DuplicateResourceException.class, EntityInUseException.class})
    public ResponseEntity<ApiError> handleConflict(RuntimeException e) {
        String message = e.getMessage() != null ? e.getMessage()
                : "L'operazione non è possibile nello stato attuale.";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(message));
    }

    /**
     * Rete di sicurezza del database: se due richieste arrivano insieme, il
     * controllo applicativo puo' passarle entrambe ma il vincolo
     * uk_review_user_movie ne blocca una. La traduciamo nello stesso 409 del
     * controllo applicativo, cosi' il frontend vede un solo comportamento.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("L'operazione viola un vincolo di integrità dei dati."));
    }

    /** L'utente sta cercando di modificare o cancellare qualcosa che non è suo. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("Non hai i permessi per questa operazione."));
    }

    /**
     * Fallimento di @Valid sul corpo della richiesta: voto fuori da 1-5, testo
     * vuoto. Oltre al messaggio generico restituisce l'elenco campo -> errore,
     * cosi' il form React puo' evidenziare il campo giusto.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(new ApiError("Dati non validi.", fieldErrors));
    }

    /**
     * Rete finale: qualunque altra eccezione diventa un 500 con corpo JSON.
     * Il messaggio originale NON viene esposto — potrebbe contenere dettagli
     * interni — ma va stampato nei log, altrimenti diventa impossibile capire
     * cosa sia successo.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("Si è verificato un errore. Riprovare più tardi."));
    }
}
