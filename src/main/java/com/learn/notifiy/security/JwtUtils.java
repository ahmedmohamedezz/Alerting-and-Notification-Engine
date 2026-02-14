package com.learn.notifiy.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.access.key}")
    private String accessKey;

    @Value("${jwt.refresh.key}")
    private String refreshKey;

    @Value("${jwt.expiration.access.ms}")
    private Long accessTokenExpirationInMs;

    @Value("${jwt.expiration.refresh.ms}")
    private Long refreshTokenExpirationInMs;

    public String generateTokenFromSubject(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + accessTokenExpirationInMs))
                .signWith(getAccessKey())
                .compact();
    }

    public String extractTokenFromRequestHeaders(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer "))
            return null;

        return token.split(" ")[1];
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) getAccessKey())
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (ExpiredJwtException e) {
            logger.error("Expired token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            // invalid token structure
            logger.error("Invalid token: {}", e.getMessage());
        } catch (SignatureException e) {
            // tampered / signed with another key
            logger.error("Tampered token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            // unsupported alg
            logger.error("Unsupported token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // Empty token
            logger.error("Claims not found in token: {}", e.getMessage());
        }

        return false;
    }

    public String getSubjectFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getAccessKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }


    // utilities
    private Key getAccessKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
    }

    private Key getRefreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
    }
}
