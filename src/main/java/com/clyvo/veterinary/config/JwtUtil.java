package com.clyvo.veterinary.config;
import com.clyvo.veterinary.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long expiration = 86400000; // 1 day

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("id", user.getId().toString());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try { Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token); return true; }
        catch (Exception e) { return false; }
    }

    public String extractUsername(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token).getPayload().getSubject();
    }
    
    public Claims extractAllClaims(String token) {
        return (Claims) Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token).getPayload();
    }
}
