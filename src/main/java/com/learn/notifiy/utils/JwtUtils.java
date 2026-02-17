package com.learn.notifiy.utils;

import com.learn.notifiy.enums.AuthTokenType;
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
import java.util.HashMap;
import java.util.Map;

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
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + accessTokenExpirationInMs))
                .signWith(getAccessKey())
                .compact();
    }

    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .claim("type", "REFRESH")
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
            return null;

        return token.split(" ")[1];
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, getAccessKey(), AuthTokenType.ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, getRefreshKey(), AuthTokenType.REFRESH);
    }

    public boolean validateToken(String token, Key key, AuthTokenType tokenType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token).getPayload();

            String type = claims.get("type", String.class);

            return type.equals(tokenType.name());
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
