package ba.unsa.etf.suds.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "OvoJeMojaTajnaSifraZaNBPProjekat2026!"; 
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    
    private final int EXPIRATION_TIME = 86400000; // 24h

    public String generateToken(Long userId, String roleName, Long stanicaId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("role_name", roleName);
        claims.put("stanica_id", stanicaId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role_name", String.class);
    }

    public Long extractStanicaId(String token) {
        return extractAllClaims(token).get("stanica_id", Long.class);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, Long userId) {
        final String extractedUserId = extractUserId(token);
        return (extractedUserId.equals(userId.toString()) && !isTokenExpired(token));
    }
}