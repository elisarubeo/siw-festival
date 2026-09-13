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

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity httpSecurity,
                                              JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        httpSecurity.securityMatcher("/api/**");

        httpSecurity.cors(Customizer.withDefaults());

        httpSecurity.csrf(csrf -> csrf.disable());

        httpSecurity.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        httpSecurity.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll();

            authorize.requestMatchers(HttpMethod.GET, "/api/**").permitAll();

            authorize.anyRequest().authenticated();
        });

        httpSecurity.addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

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

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests(authorize -> {

            authorize.requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll();

            authorize.requestMatchers(HttpMethod.GET, "/uploads/**").permitAll();

            authorize.requestMatchers("/reviews", "/reviews/**").permitAll();

            authorize.requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                    "/v3/api-docs", "/v3/api-docs/**").permitAll();

            authorize.requestMatchers("/error").permitAll();

            authorize.requestMatchers("/admin/**").hasAuthority(Credentials.ADMIN_ROLE);

            authorize.requestMatchers("/directors/**").hasAuthority(Credentials.ADMIN_ROLE);
            authorize.requestMatchers("/theaters/**").hasAuthority(Credentials.ADMIN_ROLE);

            authorize.requestMatchers(HttpMethod.GET,
                    "/festivals/new", "/festivals/*/edit",
                    "/movies/new", "/movies/*/edit",
                    "/screenings/*/edit").hasAuthority(Credentials.ADMIN_ROLE);

            authorize.requestMatchers(HttpMethod.POST,
                    "/festivals/**", "/movies/**", "/screenings/**")
                    .hasAuthority(Credentials.ADMIN_ROLE);

            authorize.requestMatchers(HttpMethod.GET,
                    "/", "/index", "/register", "/login",
                    "/festivals", "/festivals/**",
                    "/movies", "/movies/**",
                    "/screenings", "/screenings/**").permitAll();
            authorize.requestMatchers(HttpMethod.POST, "/register").permitAll();

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

    private void writeJsonError(HttpServletResponse response, HttpStatus status, String message)
            throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
