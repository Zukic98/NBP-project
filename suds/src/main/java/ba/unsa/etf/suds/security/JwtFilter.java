package ba.unsa.etf.suds.security;

import ba.unsa.etf.suds.repository.UposlenikRepository; 
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Spring Security filter koji se izvršava <strong>jednom po zahtjevu</strong>
 * ({@link OncePerRequestFilter}) i obavlja JWT autentifikaciju.
 * <p>
 * Tok obrade svakog zahtjeva:
 * </p>
 * <ol>
 *   <li>Čita {@code Authorization} zaglavlje i provjerava {@code Bearer } prefiks;</li>
 *   <li>Provjerava je li token na crnoj listi ({@code CRNA_LISTA_TOKENA}) —
 *       ako jeste, vraća HTTP 401 i prekida lanac filtera;</li>
 *   <li>Parsira token putem {@link JwtUtil} i izvlači {@code userId} i {@code role_name};</li>
 *   <li>Ako {@link org.springframework.security.core.context.SecurityContextHolder}
 *       još nema autentifikaciju, kreira
 *       {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}
 *       s ulogom u formatu {@code ROLE_<role_name>} i upisuje ga u kontekst;</li>
 *   <li>Prosljeđuje zahtjev dalje u lancu filtera.</li>
 * </ol>
 * <p>
 * Zahtjevi bez {@code Authorization} zaglavlja prolaze kroz filter bez
 * postavljanja autentifikacije — Spring Security ih dalje tretira kao anonimne.
 * </p>
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UposlenikRepository uposlenikRepository; 

    /**
     * Kreira filter uz injekciju JWT pomoćne klase i repozitorija za provjeru crne liste.
     *
     * @param jwtUtil              pomoćna klasa za parsiranje i validaciju JWT tokena
     * @param uposlenikRepository  repozitorij koji provjerava tablicu {@code CRNA_LISTA_TOKENA}
     */
    public JwtFilter(JwtUtil jwtUtil, UposlenikRepository uposlenikRepository) {
        this.jwtUtil = jwtUtil;
        this.uposlenikRepository = uposlenikRepository;
    }

    /**
     * Glavna logika filtera — izvršava se jednom po HTTP zahtjevu.
     * <p>
     * Parsira Bearer token iz {@code Authorization} zaglavlja, provjerava
     * crnu listu tokena ({@code CRNA_LISTA_TOKENA}), te popunjava
     * {@link SecurityContextHolder} ako token nije poništen.
     * </p>
     *
     * @param request     dolazni HTTP zahtjev
     * @param response    odlazni HTTP odgovor
     * @param filterChain ostatak lanca filtera
     * @throws ServletException ako dođe do greške u obradi servleta
     * @throws IOException      ako dođe do I/O greške
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (uposlenikRepository.jeLiTokenUcrnojListi(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("Token je poništen. Molimo prijavite se ponovo.");
                return; 
            }

            try {
                String userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);
                System.out.println("Logovan User: " + userId + " sa ulogom: ROLE_" + role);
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                
            }
        }

        filterChain.doFilter(request, response);
    }
}