package it.uniroma3.siw.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import it.uniroma3.siw.model.Credentials;

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
    public SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {

        /* ATTENZIONE ALL'ORDINE: vince la prima regola che corrisponde alla
           richiesta, quindi le regole piu' specifiche vanno prima di quelle
           generiche. Le rotte di amministrazione stanno sopra le pubbliche,
           altrimenti "/festivals/**" lascerebbe passare anche "/festivals/new". */
        httpSecurity.authorizeHttpRequests(authorize -> {

            // risorse statiche: sempre accessibili
            authorize.requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll();

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
                    "/movies/new", "/movies/*/edit").hasAuthority(Credentials.ADMIN_ROLE);
            authorize.requestMatchers(HttpMethod.POST, "/festivals/**", "/movies/**")
                    .hasAuthority(Credentials.ADMIN_ROLE);

            // funzionalita' pubbliche
            authorize.requestMatchers(HttpMethod.GET,
                    "/", "/index", "/register", "/login",
                    "/festivals", "/festivals/**",
                    "/movies", "/movies/**",
                    "/screenings", "/api/**").permitAll();
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
    
}
