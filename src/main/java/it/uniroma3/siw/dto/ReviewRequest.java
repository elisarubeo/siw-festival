package it.uniroma3.siw.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(

        @NotBlank(message = "Il testo della recensione non può essere vuoto")
        @Size(max = 2000, message = "La recensione non può superare i 2000 caratteri")
        String text,

        @NotNull(message = "Il voto è obbligatorio")
        @Min(value = 1, message = "Il voto minimo è 1")
        @Max(value = 5, message = "Il voto massimo è 5")
        Integer rating) {
}
