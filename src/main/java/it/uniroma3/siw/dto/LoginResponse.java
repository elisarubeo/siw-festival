package it.uniroma3.siw.dto;

public record LoginResponse(String token, String username, Long userId, String role) {
}
