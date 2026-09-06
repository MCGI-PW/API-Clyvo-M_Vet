package com.clyvo.veterinary.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class JwtUtil {
    private static final String SECRET = "clyvovet-super-secret-key-2026-must-be-at-least-256-bits-long-for-hs256";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    private final long expiration = 86400000; // 24h

    public String generateToken(UUID idConta, String tipoConta) {
        return Jwts.builder()
                .subject(idConta.toString())
                .claim("tipoConta", tipoConta)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try { Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token); return true; }
        catch (Exception e) { return false; }
    }

    public String extractIdConta(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public Date extractExpiration(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) key).build().parseSignedClaims(token).getPayload().getExpiration();
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao gerar hash do token", ex);
        }
    }
}
