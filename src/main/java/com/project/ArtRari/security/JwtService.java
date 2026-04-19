package com.project.ArtRari.security;

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
    @Value("${jwt.secret}") //todo поменять структуру константы в апл проп?
    private String secret;
    @Value("${jwt.expirationMs}")
    private int expirationMs; //todo продлевать срок токена при активности на сайте

    private Key getSecret() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return Jwts.builder().setSubject(userDetails.getUsername()).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(SignatureAlgorithm.HS256,getSecret())
                .compact();
    }

    public String getNameFromJwt(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecret())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
