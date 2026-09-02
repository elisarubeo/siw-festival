package it.uniroma3.siw.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Genera e verifica i JSON Web Token usati dalla parte REST consumata da React.
 *
 * Il token e' firmato con HMAC-SHA256: il server non memorizza nulla, gli basta
 * ricalcolare la firma con la propria chiave segreta per sapere se il token e'
 * autentico. E' questo che rende l'autenticazione stateless.
 *
 * ATTENZIONE: il payload e' codificato in Base64URL, non cifrato. Chiunque
 * intercetti il token puo' leggerne il contenuto (provare per credere su
 * jwt.io). La firma garantisce l'integrita', non la riservatezza: dentro ci
 * vanno username e ruolo, mai la password o dati sensibili.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMillis) {
        /* HMAC-SHA256 richiede una chiave di almeno 256 bit: se la stringa in
           application.properties fosse piu' corta di 32 caratteri, jjwt
           rifiuterebbe la chiave gia' all'avvio dell'applicazione. */
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    /**
     * Costruisce un token per un utente che ha appena superato il login.
     * Il "subject" e' lo username: e' il dato che il filtro riuserra' per
     * ricostruire l'identita' a ogni richiesta successiva.
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + this.expirationMillis);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(this.key)
                .compact();
    }

    /**
     * Verifica firma e scadenza e restituisce lo username contenuto nel token,
     * oppure null se il token e' assente, manomesso o scaduto.
     *
     * Restituisce null invece di propagare l'eccezione perche' chi chiama e' il
     * filtro, che davanti a un token non valido non deve interrompere la
     * richiesta: si limita a non autenticare nessuno e lascia decidere alla
     * catena di sicurezza.
     */
    public String extractUsername(String token) {
        Claims claims = parseClaims(token);
        return claims == null ? null : claims.getSubject();
    }

    public String extractRole(String token) {
        Claims claims = parseClaims(token);
        return claims == null ? null : claims.get("role", String.class);
    }

    public boolean isValid(String token) {
        return parseClaims(token) != null;
    }

    private Claims parseClaims(String token) {
        try {
            /* parseSignedClaims fallisce se la firma non corrisponde o se la
               data di scadenza e' passata: entrambi i casi finiscono qui. */
            return Jwts.parser()
                    .verifyWith(this.key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
