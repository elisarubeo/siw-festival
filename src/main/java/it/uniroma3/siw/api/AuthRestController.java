package it.uniroma3.siw.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.dto.ApiError;
import it.uniroma3.siw.dto.LoginRequest;
import it.uniroma3.siw.dto.LoginResponse;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.JwtService;
import jakarta.validation.Valid;

/**
 * Login della parte REST: verifica le credenziali ed emette un JWT.
 *
 * E' l'unico endpoint sotto /api raggiungibile senza token — e' il punto in cui
 * il token si ottiene.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CredentialsService credentialsService;

    public AuthRestController(AuthenticationManager authenticationManager,
                              JwtService jwtService,
                              CredentialsService credentialsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.credentialsService = credentialsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            /* La verifica della password non si fa a mano: se ne occupa
               l'AuthenticationManager, che carica le credenziali dal database
               e confronta l'hash BCrypt. Se non corrispondono lancia
               BadCredentialsException. */
            Authentication authentication = this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(), request.password()));

            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(Credentials.DEFAULT_ROLE);

            String token = this.jwtService.generateToken(authentication.getName(), role);

            /* Il ponte username -> User: nel modello Credentials punta a User,
               ma non viceversa, quindi si passa sempre da qui. Una query in
               piu' al solo momento del login. */
            Long userId = this.credentialsService.getCredentials(authentication.getName())
                    .map(credentials -> credentials.getUser().getId())
                    .orElse(null);

            return ResponseEntity.ok(
                    new LoginResponse(token, authentication.getName(), userId, role));

        } catch (BadCredentialsException e) {
            /* 401 e non 500: credenziali sbagliate e' un esito previsto del
               caso d'uso, non un guasto del server. */
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("Username o password non corretti"));
        }
    }
}
