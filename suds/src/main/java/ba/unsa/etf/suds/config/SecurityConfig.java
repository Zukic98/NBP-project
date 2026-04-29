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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
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
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
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
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        return username -> {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException("Use JWT");
        };
    }
}