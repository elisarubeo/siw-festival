package it.uniroma3.siw.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import it.uniroma3.siw.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Legge l'header "Authorization: Bearer <token>" e, se il token e' valido,
 * popola il SecurityContext con l'identita' dell'utente.
 *
 * Estende OncePerRequestFilter per la garanzia che il nome promette: senza,
 * un forward interno (per esempio verso /error) rieseguirebbe il filtro.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        /* Se il token c'e' ed e' valido si autentica; in tutti gli altri casi
           NON si blocca la richiesta e non si scrive un 401 qui. Il filtro
           constata soltanto, e la richiesta prosegue "anonima": chi decide se
           serviva o meno un'autenticazione e' la catena di sicurezza, sulla
           base delle regole in SecurityConfiguration. E' cosi' che le GET
           pubbliche sulle recensioni continuano a funzionare senza token. */
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = this.jwtService.extractUsername(token);
            String role = this.jwtService.extractRole(token);

            if (username != null) {
                /* Le authorities si ricostruiscono dal claim del token, senza
                   interrogare il database: e' il punto dell'approccio
                   stateless. Il ruolo e' quello scritto in Credentials
                   ("DEFAULT" oppure "ADMIN"), non "ROLE_..." — coerente con
                   le hasAuthority(...) usate nel resto dell'applicazione. */
                List<SimpleGrantedAuthority> authorities = role == null
                        ? List.of()
                        : List.of(new SimpleGrantedAuthority(role));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            return header.substring(PREFIX.length());
        }
        return null;
    }
}
