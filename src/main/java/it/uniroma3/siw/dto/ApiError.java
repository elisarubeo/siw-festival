package it.uniroma3.siw.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String message, Map<String, String> fieldErrors) {

    public ApiError(String message) {
        this(message, null);
    }
}
