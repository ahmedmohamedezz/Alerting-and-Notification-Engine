package com.learn.notifiy.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
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

    public String generateAccessToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + accessTokenExpirationInMs))
                .signWith(getAccessKey())
                .compact();
    }

    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + refreshTokenExpirationInMs))
                .signWith(getRefreshKey())
                .compact();
    }

    public Map<String, String> getTokens(String subject) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", generateAccessToken(subject));
        tokens.put("refreshToken", generateRefreshToken(subject));
        return tokens;
    }

    public String extractTokenFromRequestHeaders(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer "))
            throw new RuntimeException("Couldn't extract token. invalid, or missing");

        return token.split(" ")[1];
    }

    public void validateAccessToken(String token) {
        validateToken(token, getAccessKey());
    }

    public void validateRefreshToken(String token) {
        validateToken(token, getRefreshKey());
    }

    public String getSubjectFromAccessToken(String token) {
        return getSubjectFromToken(token, getAccessKey());
    }

    public String getSubjectFromRefreshToken(String token) {
        return getSubjectFromToken(token, getRefreshKey());
    }

    public String refresh(String refreshToken) {
        return generateAccessToken(getSubjectFromRefreshToken(refreshToken));
    }

    // utilities
    private Key getAccessKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
    }

    private Key getRefreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
    }

    private void validateToken(String token, Key key) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token).getPayload();

        } catch (JwtException e) {
            logger.error("Expired token: {}", e.getMessage());
            // re-throw exception
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Token validation failed. " + e.getMessage());
        }
    }

    private String getSubjectFromToken(String token, Key key) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
