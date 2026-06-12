package com.shopsphere.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(String userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        return createToken(claims, userId);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    public String getEmailFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("email");
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload(); // getBody() → getPayload()
    }
    // public Boolean validateToken(String token) {
    // try {
    // Jwts.parserBuilder()
    // .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
    // .build()
    // .parseClaimsJws(token);
    // return true;
    // } catch (Exception e) {
    // return false;
    // }
    // }

    // private Claims getAllClaimsFromToken(String token) {
    // return Jwts.parserBuilder()
    // .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes()))
    // .build()
    // .parseClaimsJws(token)
    // .getBody();
    // }
}
