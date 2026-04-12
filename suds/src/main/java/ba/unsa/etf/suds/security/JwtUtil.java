package ba.unsa.etf.suds.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret:suds-default-secret-key-koja-mora-biti-dovoljno-dugacka-256bit}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generisanje JWT tokena sa korisničkim informacijama.
     */
    public String generateToken(Long userId, String roleName, Long stanicaId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("user_id", userId)
                .claim("role_name", roleName)
                .claim("stanica_id", stanicaId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role_name", String.class);
    }

    public Long extractStanicaId(String token) {
        return extractClaims(token).get("stanica_id", Long.class);
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    public boolean validateToken(String token, Long userId) {
        final String extractedUserId = extractUserId(token);
        return (extractedUserId.equals(userId.toString()) && !isTokenExpired(token));
    }
}
