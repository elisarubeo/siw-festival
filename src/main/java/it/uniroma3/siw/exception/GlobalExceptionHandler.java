package it.uniroma3.siw.exception;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackages = "it.uniroma3.siw.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException e, Model model) {
        model.addAttribute("errorMessage", "Pagina non trovata");
        return "error/404";
    }

    @ExceptionHandler(InvalidImageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidImage(InvalidImageException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

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
