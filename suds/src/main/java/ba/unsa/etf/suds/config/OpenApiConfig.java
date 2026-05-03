package ba.unsa.etf.suds.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguracija SpringDoc OpenAPI (Swagger UI) za SUDS aplikaciju.
 * <p>
 * Registruje globalni {@code bearerAuth} JWT sigurnosni shemu koji se
 * automatski propagira na sve operacije u Swagger UI-u. Korisnik može
 * unijeti JWT token putem dugmeta <em>Authorize</em> i sve naredne
 * zahtjeve Swagger UI šalje s odgovarajućim {@code Authorization: Bearer ...}
 * zaglavljem.
 * </p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    /**
     * Kreira prilagođenu {@link OpenAPI} konfiguraciju s JWT sigurnosnom shemom.
     * <p>
     * Definiše:
     * <ul>
     *   <li>Metapodatke API-ja (naslov, verzija, opis);</li>
     *   <li>Globalni sigurnosni zahtjev {@code bearerAuth} koji se primjenjuje
     *       na sve endpoint-e;</li>
     *   <li>HTTP Bearer sigurnosnu shemu s formatom {@code JWT} — Swagger UI
     *       prikazuje polje za unos tokena u dijalogu <em>Authorize</em>.</li>
     * </ul>
     * </p>
     *
     * @return konfigurisana {@link OpenAPI} instanca
     */
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("SUDS API - Sistem Upravljanja Dokaznim Sredstvima")
                        .version("1.0")
                        .description("API dokumentacija sa JWT autorizacijom"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}