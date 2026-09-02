package it.uniroma3.siw.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serve l'app React buildata da /reviews/**.
 *
 * Il problema da risolvere: il routing di React Router e' interamente lato
 * client, quindi se l'utente ricarica la pagina su /reviews/movies/1 il browser
 * chiede DAVVERO quell'indirizzo al server, che non ha nulla mappato li' e
 * risponderebbe 404.
 *
 * La soluzione non puo' essere un controller che inoltra tutto /reviews/** a
 * index.html: intercetterebbe anche /reviews/assets/index-abc.js, restituendo
 * HTML al posto del JavaScript. Qui invece il resolver controlla PRIMA se il
 * file richiesto esiste davvero: se esiste lo serve, altrimenti — ed e' il caso
 * delle rotte React — ripiega su index.html.
 */
@Configuration
public class ReactAppConfig implements WebMvcConfigurer {

    private static final String BASE = "/static/reviews/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/reviews/**")
                .addResourceLocations("classpath:" + BASE)
                .resourceChain(false)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location)
                            throws IOException {
                        /* Il controllo su vuoto e su "/" finale non e' un
                           dettaglio: per la richiesta a /reviews/ il path e'
                           la stringa vuota, e createRelative("") restituisce
                           la CARTELLA, che "esiste" ma non e' servibile — il
                           risultato sarebbe un 404 proprio sulla home dell'app. */
                        if (!resourcePath.isEmpty() && !resourcePath.endsWith("/")) {
                            Resource requested = location.createRelative(resourcePath);
                            if (requested.exists() && requested.isReadable()) {
                                return requested;
                            }
                        }
                        /* Non e' un file: e' una rotta di React Router.
                           Restituendo index.html l'app si carica e legge da
                           sola l'indirizzo per capire cosa mostrare. */
                        return new ClassPathResource(BASE + "index.html");
                    }
                });
    }
}
