package com.example.bankcards.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@Slf4j
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-time-min}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token){
        try {
            return Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * jwtExpiration))
                .signWith(getSignKey())
                .compact();
    }

    public boolean validateToken(final String token) {
        String subToken = token;
        if(token.startsWith("Bearer ")){
            subToken = token.substring(7);
        }
        try {
            Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(subToken);
            return true;
        } catch (ExpiredJwtException ex) {
            log.error("JWT expired", ex);
            throw ex;
        } catch (MalformedJwtException ex) {
            log.error("JWT is invalid", ex);
            throw ex;
        } catch (JwtException ex) {
            log.error("JWT is not supported", ex);
            throw ex;
        } catch (IllegalArgumentException ex) {
            log.error("Exception inside JWT Service", ex);
            throw ex;
        }

    }
}
