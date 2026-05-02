package ba.unsa.etf.suds.config;

import ba.unsa.etf.suds.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Centralna Spring Security konfiguracija SUDS aplikacije.
 * <p>
 * Aktivira web sigurnost ({@code @EnableWebSecurity}) i metod-nivo sigurnost
 * ({@code @EnableMethodSecurity}) kako bi {@code @PreAuthorize} anotacije
 * na kontrolerima funkcionisale. Koristi isključivo JWT autentifikaciju —
 * nema HTTP sesija ni form-based prijave.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    /**
     * Kreira instancu konfiguracije uz injekciju JWT filtera.
     *
     * @param jwtFilter filter koji parsira i validira JWT token iz svakog zahtjeva
     */
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    /**
     * Konfiguriše glavni Spring Security filter chain:
     * <ul>
     *   <li>Onemogućuje CSRF (stateless API);</li>
     *   <li>Postavlja CORS za frontend na {@code http://localhost:5173};</li>
     *   <li>Stateless session policy (nema {@code HttpSession}-a);</li>
     *   <li>Whitelist javnih endpoint-a: {@code /api/auth/login},
     *       {@code /api/stanice/register}, {@code /api/adrese},
     *       Swagger UI i OpenAPI JSON putanje, te {@code /error};</li>
     *   <li>Endpoint-i za fotografije dokaza i osumnjičenih dostupni svim
     *       autentifikovanim korisnicima;</li>
     *   <li>Sve ostalo zahtijeva autentifikaciju;</li>
     *   <li>Ubacuje {@link JwtFilter} prije
     *       {@link UsernamePasswordAuthenticationFilter}.</li>
     * </ul>
     *
     * @param http Spring Security konfiguracijski builder
     * @return izgrađen {@link SecurityFilterChain}
     * @throws Exception ako konfiguracija ne uspije
     */
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Javni endpointi
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/stanice/register").permitAll()
                        .requestMatchers("/api/adrese").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Fotografije dokaza - svi autentifikovani mogu vidjeti
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/dokazi/*/fotografije").authenticated()
                        // Fotografije dokaza - samo određene uloge mogu dodavati
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/dokazi/*/fotografije").authenticated()

                        // Fotografije osumnjičenih - svi autentifikovani mogu vidjeti
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/osumnjiceni/*/fotografije").authenticated()
                        // Fotografije osumnjičenih - dodavanje, ažuriranje, brisanje
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/osumnjiceni/*/fotografije").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/osumnjiceni/*/fotografije/*").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/osumnjiceni/*/fotografije/*").authenticated()

                        // Sve ostalo zahtijeva autentifikaciju
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    /**
     * Kreira {@link BCryptPasswordEncoder} bean za hashiranje lozinki.
     *
     * @return instanca BCrypt enkodera sa podrazumijevanim faktorom snage (10)
     */
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    /**
     * Konfiguriše CORS politiku za cijelu aplikaciju.
     * <p>
     * Dozvoljava zahtjeve isključivo s frontend origin-a
     * {@code http://localhost:5173}, uz podršku za sve standardne HTTP metode
     * i zaglavlja {@code Authorization} i {@code Content-Type}.
     * Credentials (kolačići, Authorization zaglavlje) su dozvoljeni.
     * </p>
     *
     * @return {@link org.springframework.web.cors.CorsConfigurationSource} registrovan za sve putanje ({@code /**})
     */
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    /**
     * Stub {@link org.springframework.security.core.userdetails.UserDetailsService} bean.
     * <p>
     * SUDS koristi isključivo JWT autentifikaciju, pa standardni Spring Security
     * mehanizam učitavanja korisnika po korisničkom imenu nije potreban.
     * Svaki poziv baca {@link org.springframework.security.core.userdetails.UsernameNotFoundException}
     * kako bi se spriječilo nenamjerno korištenje form-based prijave.
     * </p>
     *
     * @return {@link org.springframework.security.core.userdetails.UserDetailsService} koji uvijek baca iznimku
     */
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        return username -> {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException("Use JWT");
        };
    }
}