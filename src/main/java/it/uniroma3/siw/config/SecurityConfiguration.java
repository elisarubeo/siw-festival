package it.uniroma3.siw.config;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import it.uniroma3.siw.model.Credentials;
import jakarta.servlet.http.HttpServletResponse;

/**
 * L'applicazione ha DUE frontend con due modi di autenticarsi diversi, quindi
 * due catene di filtri distinte:
 *
 *   - la parte Thymeleaf lavora con sessione, cookie e token CSRF, come sempre;
 *   - la parte REST consumata da React e' stateless e si autentica con un JWT.
 *
 * A decidere quale catena gestisce una richiesta e' securityMatcher, valutato
 * nell'ordine indicato da @Order: la catena API intercetta /api/**, tutto il
 * resto ricade sulla catena web.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final DataSource dataSource;

    public SecurityConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        manager.setUsersByUsernameQuery(
                "SELECT username, password, true AS enabled FROM credentials WHERE username = ?");
        manager.setAuthoritiesByUsernameQuery(
                "SELECT username, role FROM credentials WHERE username = ?");
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Serve all'endpoint POST /api/auth/login per verificare username e
     * password prima di emettere il token. La catena Thymeleaf non ne ha
     * bisogno esplicitamente perche' formLogin se lo costruisce da sola.
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /**
     * CORS per lo sviluppo: in produzione il build di React finisce dentro
     * src/main/resources/static, quindi l'origine e' la stessa e questa
     * configurazione non entra mai in gioco. Serve solo quando il dev server
     * di Vite gira su :5173 e chiama :8080 senza passare dal proxy.
     *
     * Va definita come bean CorsConfigurationSource e non come WebMvcConfigurer:
     * la catena di Spring Security gira PRIMA di Spring MVC, quindi le
     * richieste di preflight (OPTIONS) verrebbero respinte con 401 senza mai
     * arrivare a MVC. Con questo bean piu' .cors(...) sulla catena, e' la
     * sicurezza stessa a rispondere al preflight.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // il JWT viaggia nell'header Authorization: senza questo verrebbe scartato
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    /* ==================================================================
       CATENA 1 — API REST (React)
       ================================================================== */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity httpSecurity,
                                              JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        // questa catena gestisce SOLO /api/**; il resto passa alla catena web
        httpSecurity.securityMatcher("/api/**");

        httpSecurity.cors(Customizer.withDefaults());

        /* Niente CSRF: il token CSRF protegge dalle richieste che il browser
           invia automaticamente grazie al cookie di sessione. Qui non ci sono
           cookie — il JWT va allegato a mano a ogni chiamata — quindi
           l'attacco che il CSRF previene non e' possibile. */
        httpSecurity.csrf(csrf -> csrf.disable());

        // nessuna sessione lato server: ogni richiesta si autentica da sola
        httpSecurity.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        httpSecurity.authorizeHttpRequests(authorize -> {
            // il login deve essere raggiungibile senza essere gia' autenticati
            authorize.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll();

            /* Le letture sono pubbliche: il §4.1 della traccia chiede che le
               recensioni siano visibili a chiunque, anche senza login. */
            authorize.requestMatchers(HttpMethod.GET, "/api/**").permitAll();

            // scritture (POST, PUT, DELETE): serve un token valido
            authorize.anyRequest().authenticated();
        });

        /* Il filtro va PRIMA di UsernamePasswordAuthenticationFilter: deve
           popolare il SecurityContext prima che le regole qui sopra vengano
           valutate, altrimenti ogni richiesta risulterebbe anonima. */
        httpSecurity.addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        /* Senza queste due righe una richiesta senza token riceve 403 Forbidden
           invece di 401 Unauthorized: non essendoci un meccanismo di login su
           questa catena, Spring Security ripiega sul 403. La differenza conta
           per React, che sul 401 sa di dover mandare l'utente al login, mentre
           il 403 significa "sei autenticato ma non ti e' permesso".
           Il corpo e' JSON, come per ogni altra risposta di /api. */
        httpSecurity.exceptionHandling(exception -> {
            exception.authenticationEntryPoint((request, response, authException) ->
                    writeJsonError(response, HttpStatus.UNAUTHORIZED,
                            "Autenticazione richiesta: allega un token JWT valido."));
            exception.accessDeniedHandler((request, response, deniedException) ->
                    writeJsonError(response, HttpStatus.FORBIDDEN,
                            "Non hai i permessi per questa operazione."));
        });

        return httpSecurity.build();
    }

    /* ==================================================================
       CATENA 2 — pagine Thymeleaf
       ================================================================== */
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity httpSecurity) throws Exception {

        /* ATTENZIONE ALL'ORDINE: vince la prima regola che corrisponde alla
           richiesta, quindi le regole piu' specifiche vanno prima di quelle
           generiche. Le rotte di amministrazione stanno sopra le pubbliche,
           altrimenti "/festivals/**" lascerebbe passare anche "/festivals/new". */
        httpSecurity.authorizeHttpRequests(authorize -> {

            // risorse statiche: sempre accessibili
            authorize.requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll();

            /* Il build di React viene servito da qui: sono file statici come
               gli altri, e le pagine dell'app decidono da sole cosa mostrare
               a chi non ha fatto login. */
            authorize.requestMatchers("/reviews", "/reviews/**").permitAll();

            /* Swagger: servono tutti e tre i percorsi. /swagger-ui.html e' il
               redirect iniziale, /swagger-ui/** sono i file dell'interfaccia,
               /v3/api-docs/** e' la specifica che l'interfaccia scarica per
               disegnare la pagina. Dimenticandone uno, Swagger si apre vuoto. */
            authorize.requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                    "/v3/api-docs", "/v3/api-docs/**").permitAll();

            /* /error deve restare accessibile: quando una richiesta produce un
               404 o un 500, Spring la inoltra internamente qui. Se fosse
               protetta, ogni errore diventerebbe un redirect al login. */
            authorize.requestMatchers("/error").permitAll();

            // funzionalita' riservate all'amministratore
            authorize.requestMatchers("/admin/**").hasAuthority(Credentials.ADMIN_ROLE);

            /* Registi e sale sono interamente riservati all'amministratore:
               non essendoci pagine pubbliche su questi percorsi, basta una
               regola sola per ciascuno, valida per qualunque metodo HTTP. */
            authorize.requestMatchers("/directors/**").hasAuthority(Credentials.ADMIN_ROLE);
            authorize.requestMatchers("/theaters/**").hasAuthority(Credentials.ADMIN_ROLE);

            /* Festival e film hanno pagine pubbliche, quindi qui si elencano
               solo i percorsi di amministrazione. Le POST sono tutte
               amministrative: creazione, modifica ed eliminazione. */
            authorize.requestMatchers(HttpMethod.GET,
                    "/festivals/new", "/festivals/*/edit",
                    "/movies/new", "/movies/*/edit",
                    "/screenings/*/edit").hasAuthority(Credentials.ADMIN_ROLE);

            /* Tutte le POST su queste risorse sono amministrative: creazione,
               modifica, eliminazione, annullamento. Sulle proiezioni la GET
               resta invece pubblica, perche' il programma lo consultano tutti. */
            authorize.requestMatchers(HttpMethod.POST,
                    "/festivals/**", "/movies/**", "/screenings/**")
                    .hasAuthority(Credentials.ADMIN_ROLE);

            /* Nota: "/api/**" non compare piu' in questo elenco. Le richieste
               a /api non arrivano mai qui, se le prende la catena 1. */
            authorize.requestMatchers(HttpMethod.GET,
                    "/", "/index", "/register", "/login",
                    "/festivals", "/festivals/**",
                    "/movies", "/movies/**",
                    "/screenings", "/screenings/**").permitAll();
            authorize.requestMatchers(HttpMethod.POST, "/register").permitAll();

            // tutto il resto richiede un utente autenticato
            authorize.anyRequest().authenticated();
        });

        httpSecurity.formLogin(form -> {
            form.loginPage("/login").permitAll();
            form.defaultSuccessUrl("/", true);
            form.failureUrl("/login?error=true");
        });

        httpSecurity.logout(logout -> {
            logout.logoutUrl("/logout");
            logout.logoutSuccessUrl("/");
            logout.invalidateHttpSession(true);
            logout.deleteCookies("JSESSIONID");
            logout.clearAuthentication(true);
            logout.permitAll();
        });

        return httpSecurity.build();
    }

    /**
     * Scrive a mano un corpo di errore JSON.
     *
     * Questi due casi vengono gestiti dai filtri di Spring Security, che girano
     * PRIMA di Spring MVC: qui ApiExceptionHandler non e' ancora entrato in
     * gioco, quindi la risposta va composta direttamente sulla HttpServletResponse.
     * La forma del corpo e' la stessa di ApiError, cosi' React ha sempre un solo
     * campo da leggere.
     */
    private void writeJsonError(HttpServletResponse response, HttpStatus status, String message)
            throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
