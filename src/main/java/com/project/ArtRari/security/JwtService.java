package com.project.ArtRari.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService{
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expirationMs}")
    private int expirationMs;

    private Key getSecret() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("id", userDetails.getId())
                .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(SignatureAlgorithm.HS256,getSecret())
                .compact();
    }

    private Claims getAllClaimsFromJwt(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecret())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            getAllClaimsFromJwt(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getIdFromJwt(String token) {
        return getAllClaimsFromJwt(token).get("id", Long.class);
    }

    public String getNameFromJwt(String token) {
        return getAllClaimsFromJwt(token).getSubject();
    }

    public String getRoleFromJwt(String token) {
        return getAllClaimsFromJwt(token).get("role", String.class);
    }

    public long getRemainingTimeFromJwt(String token) {
        Date expiration = getAllClaimsFromJwt(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
