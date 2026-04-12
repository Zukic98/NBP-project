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

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UposlenikRepository uposlenikRepository; 

    public JwtFilter(JwtUtil jwtUtil, UposlenikRepository uposlenikRepository) {
        this.jwtUtil = jwtUtil;
        this.uposlenikRepository = uposlenikRepository;
    }

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