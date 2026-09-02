package it.uniroma3.siw.exception;

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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpectedException(Exception e, Model model) {
        model.addAttribute("errorMessage","Si è verificato un errore. Riprovare più tardi.");
        return "error/500";
    }

}
