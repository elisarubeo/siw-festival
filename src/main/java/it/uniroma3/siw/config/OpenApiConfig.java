package it.uniroma3.siw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Descrizione delle API REST per Swagger UI (/swagger-ui.html).
 *
 * springdoc scopre da solo i @RestController: questa classe serve solo ad
 * aggiungere il titolo e — soprattutto — a dichiarare lo schema di sicurezza,
 * che e' cio' che fa comparire il bottone "Authorize" nell'interfaccia.
 * Da li' si incolla il JWT una volta sola e resta agganciato a tutte le
 * chiamate successive, senza bisogno di curl.
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SIW Festival API")
                        .version("v1")
                        .description("API REST per festival, film e recensioni. "
                                + "Le letture sono pubbliche; per scrivere una recensione "
                                + "serve un token ottenuto da POST /api/auth/login."))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
