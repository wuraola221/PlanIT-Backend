
package PlanIT.PlanIT.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
@Slf4j

public class JwtUtil {
    private static final String SECRET_KEY = "dZxUKezvBXKPT3/OSxL+NZZuaP1aCEHgmwxh/xOCUlTFIRyqgP8NyRPwaEi09xfQNkStRyOBjJnhYY8XBGq8HA=="; // Use environment variable in production
    private static final long JWT_EXPIRATION = 3600000;
    private final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, String role, String fullName) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("username", email);
        claims.put("role", role);
        claims.put("name", fullName);
        claims.put("permissions", role);
        claims.put("sub", email);

        return generateToken(claims, email);
    }

    public String generateToken(Map<String, Object> extraClaims, String email) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email)) && !isTokenExpired(token);
    }

    public long getExpirationTime() {
        return JWT_EXPIRATION / 1000; // Return in seconds
    }

    public void invalidateToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            invalidatedTokens.add(token);
            log.info("Token invalidated successfully");
        }
    }
}