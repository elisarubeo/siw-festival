package it.uniroma3.siw.exception;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/* basePackages e' indispensabile: senza, questo gestore cattura anche le
   eccezioni delle chiamate /api — comprese quelle sollevate quando nessun
   controller corrisponde all'indirizzo — e restituisce una pagina HTML dove
   React si aspetta del JSON. Le API hanno il loro ApiExceptionHandler. */
@ControllerAdvice(basePackages = "it.uniroma3.siw.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException e, Model model) {
        model.addAttribute("errorMessage", "Pagina non trovata");
        return "error/404";
    }

    /**
     * File che non e' un'immagine utilizzabile.
     *
     * Nell'uso normale non si arriva qui: la form controlla il file prima di
     * salvare e mostra l'errore accanto al campo. Questa e' la rete per le
     * richieste che quel controllo non lo passano, per esempio inviate a mano.
     */
    @ExceptionHandler(InvalidImageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidImage(InvalidImageException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

    /**
     * Transazione annullata dal database per un conflitto di serializzazione.
     *
     * Succede quando due amministratori programmano o spostano una proiezione
     * nella stessa sala nello stesso istante: ScreeningService lavora a
     * livello SERIALIZABLE proprio per impedire che passino entrambe, e
     * PostgreSQL ne fa fallire una al commit.
     *
     * Non e' un guasto ma il meccanismo che garantisce la consistenza, quindi
     * non merita un 500: l'operazione non e' stata eseguita e ripeterla e'
     * sicuro. Al secondo tentativo il controllo vede la proiezione dell'altro
     * e risponde con il normale messaggio di sala occupata.
     */
    @ExceptionHandler(ConcurrencyFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConcurrencyFailure(ConcurrencyFailureException e, Model model) {
        model.addAttribute("errorMessage",
                "Un'altra operazione sulla stessa sala è stata completata nello stesso "
                + "istante, e questa è stata annullata per non creare sovrapposizioni. "
                + "Riprova: nulla è stato modificato.");
        return "error/409";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpectedException(Exception e, Model model) {
        model.addAttribute("errorMessage","Si è verificato un errore. Riprovare più tardi.");
        return "error/500";
    }

}
