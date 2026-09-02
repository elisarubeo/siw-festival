package it.uniroma3.siw.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Forma unica del corpo di risposta quando una chiamata /api fallisce.
 *
 * Avere sempre la stessa forma significa che lato React c'e' un solo punto da
 * leggere per mostrare un messaggio all'utente: error.response.data.message.
 *
 * fieldErrors e' valorizzato solo per gli errori di validazione (400) e
 * contiene coppie campo -> messaggio; negli altri casi e' null e Jackson lo
 * omette del tutto grazie a @JsonInclude.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String message, Map<String, String> fieldErrors) {

    public ApiError(String message) {
        this(message, null);
    }
}
