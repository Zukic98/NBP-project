package ba.unsa.etf.suds.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Pomoćna klasa za generisanje, parsiranje i validaciju JWT tokena.
 * <p>
 * Tokeni se potpisuju algoritmom <strong>HMAC-SHA-256</strong> koristeći tajni
 * ključ iz konfiguracije ({@code jwt.secret}). Svaki token sadrži sljedeće
 * claim-ove:
 * </p>
 * <ul>
 *   <li>{@code sub} (subject) — {@code userId.toString()}, identifikator korisnika;</li>
 *   <li>{@code user_id} — numerički ID korisnika ({@code Long});</li>
 *   <li>{@code role_name} — naziv uloge (npr. {@code "INSPEKTOR"}, {@code "SEF_STANICE"});</li>
 *   <li>{@code stanica_id} — ID policijske stanice kojoj korisnik pripada ({@code Long});</li>
 *   <li>{@code iat} — vrijeme izdavanja tokena (issued-at);</li>
 *   <li>{@code exp} — vrijeme isteka tokena (expiration), podrazumijevano 24h.</li>
 * </ul>
 * <p>
 * Podrazumijevani rok trajanja je {@code 86400000} ms (24 sata), a tajni ključ
 * se čita iz {@code jwt.secret} u {@code application.yml} s fallback vrijednošću.
 * </p>
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    /**
     * Inicijalizuje JwtUtil s tajnim ključem i rokom trajanja tokena.
     *
     * @param secret       tajni ključ za HMAC-SHA-256 potpisivanje, čita se iz
     *                     {@code jwt.secret} u {@code application.yml}
     * @param expirationMs trajanje tokena u milisekundama (podrazumijevano 86400000 = 24h)
     */
    public JwtUtil(
            @Value("${jwt.secret:suds-default-secret-key-koja-mora-biti-dovoljno-dugacka-256bit}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generiše potpisani JWT token s korisničkim podacima.
     * <p>
     * Token sadrži claim-ove: {@code sub} (userId), {@code user_id},
     * {@code role_name}, {@code stanica_id}, {@code iat} i {@code exp}.
     * Potpis se vrši HMAC-SHA-256 algoritmom.
     * </p>
     *
     * @param userId    jedinstveni identifikator korisnika
     * @param roleName  naziv uloge korisnika (npr. {@code "INSPEKTOR"})
     * @param stanicaId ID policijske stanice kojoj korisnik pripada
     * @return kompaktni JWT string u formatu {@code header.payload.signature}
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

    /**
     * Parsira i verificira JWT token te vraća sve claim-ove.
     *
     * @param token JWT string koji se parsira
     * @return {@link Claims} objekt sa svim claim-ovima iz tokena
     * @throws io.jsonwebtoken.JwtException ako token nije validan ili je istekao
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Izvlači korisnički ID iz {@code sub} claim-a tokena.
     *
     * @param token JWT string
     * @return korisnički ID kao string
     */
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Izvlači naziv uloge iz {@code role_name} claim-a tokena.
     *
     * @param token JWT string
     * @return naziv uloge (npr. {@code "INSPEKTOR"})
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role_name", String.class);
    }

    /**
     * Izvlači ID stanice iz {@code stanica_id} claim-a tokena.
     *
     * @param token JWT string
     * @return ID policijske stanice
     */
    public Long extractStanicaId(String token) {
        return extractClaims(token).get("stanica_id", Long.class);
    }

    /**
     * Provjerava je li token istekao na osnovu {@code exp} claim-a.
     *
     * @param token JWT string
     * @return {@code true} ako je token istekao, {@code false} inače
     */
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * Provjerava je li token sintaksno ispravan i nije istekao.
     * <p>
     * Za razliku od {@link #isTokenExpired(String)}, ova metoda hvata sve
     * iznimke parsiranja i vraća {@code false} umjesto da ih propagira.
     * </p>
     *
     * @param token JWT string
     * @return {@code true} ako je token validan i aktivan, {@code false} inače
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * Validira token provjeravajući da li odgovara zadanom korisniku i nije istekao.
     *
     * @param token  JWT string
     * @param userId očekivani korisnički ID
     * @return {@code true} ako token pripada korisniku i nije istekao
     */
    public boolean validateToken(String token, Long userId) {
        final String extractedUserId = extractUserId(token);
        return (extractedUserId.equals(userId.toString()) && !isTokenExpired(token));
    }
}
