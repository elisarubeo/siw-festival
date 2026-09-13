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
            Authentication authentication = this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(), request.password()));

            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(Credentials.DEFAULT_ROLE);

            String token = this.jwtService.generateToken(authentication.getName(), role);

            Long userId = this.credentialsService.getCredentials(authentication.getName())
                    .map(credentials -> credentials.getUser().getId())
                    .orElse(null);

            return ResponseEntity.ok(
                    new LoginResponse(token, authentication.getName(), userId, role));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError("Username o password non corretti"));
        }
    }
}
