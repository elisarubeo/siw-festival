package it.uniroma3.siw.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * I dati che l'utente scrive in una recensione: solo questi arrivano dal
 * client. Id, data e autore li decide il server — accettarli dall'esterno
 * significherebbe lasciar scegliere a chi chiama per conto di chi scrivere.
 *
 * Lo stesso record serve sia per la creazione (POST) sia per la modifica (PUT):
 * i campi modificabili sono gli stessi.
 */
public record ReviewRequest(

        @NotBlank(message = "Il testo della recensione non può essere vuoto")
        @Size(max = 2000, message = "La recensione non può superare i 2000 caratteri")
        String text,

        /* @NotNull e' indispensabile accanto a @Min e @Max: per la Bean
           Validation un valore null e' VALIDO per @Min/@Max, che si limitano a
           non pronunciarsi. Senza @NotNull, un JSON che omette "rating"
           passerebbe la validazione e arriverebbe al service con null.

           Integer e non int: con un primitivo il campo mancante diventerebbe 0
           e @NotNull non scatterebbe mai, perche' 0 non e' null. */
        @NotNull(message = "Il voto è obbligatorio")
        @Min(value = 1, message = "Il voto minimo è 1")
        @Max(value = 5, message = "Il voto massimo è 5")
        Integer rating) {
}
