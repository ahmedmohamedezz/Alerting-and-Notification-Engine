package com.learn.notifiy.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class JwtUtilsTest {
    private JwtUtils jwtUtils;
    private final String accessSecret = "accessSecret12300321tercesSseccaaccessSecret12300321tercesSsecca";
    private final String refreshSecret = "refreshSecret12300321tercesHserferaccessrefreshSecret12300321tercesHserferaccess";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(accessSecret, refreshSecret, 60000L, 120000L);
    }

    @Test
    @DisplayName("Should generate a valid access token and extract the correct subject")
    void generateAccessToken_Success() {
        String email = "dev@example.com";

        String token = jwtUtils.generateAccessToken(email);

        assertThat(token).isNotNull();
        assertThatCode(() -> jwtUtils.validateAccessToken(token)).doesNotThrowAnyException();
        assertThat(jwtUtils.getSubjectFromAccessToken(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("Should fail validation if Access Token is signed with wrong key")
    void validateAccessToken_WrongKey() {
        // Create a token using the REFRESH secret, then try to validate as ACCESS
        String tamperedToken = jwtUtils.generateRefreshToken("hacker@example.com");

        // This should throw a SignatureException because validateAccessToken uses the AccessKey
        assertThatThrownBy(() -> jwtUtils.validateAccessToken(tamperedToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Should successfully refresh and return a new Access Token")
    void refresh_Success() {
        String email = "user@example.com";
        String refreshToken = jwtUtils.generateRefreshToken(email);

        String newAccessToken = jwtUtils.refresh(refreshToken);

        assertThat(newAccessToken).isNotNull();
        assertThatCode(() -> jwtUtils.validateAccessToken(newAccessToken)).doesNotThrowAnyException();
        assertThat(jwtUtils.getSubjectFromAccessToken(newAccessToken)).isEqualTo(email);
    }

    @Test
    @DisplayName("Should throw exception when trying to refresh using an Access Token")
    void refresh_FailWithAccessToken() {
        String accessToken = jwtUtils.generateAccessToken("user@example.com");

        // refreshing expects a token signed with refreshSecret
        assertThatThrownBy(() -> jwtUtils.refresh(accessToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Should fail when token is expired")
    void validateToken_Expired() throws InterruptedException {
        // Set up a utility with 1ms expiration
        JwtUtils fastExpireUtils = new JwtUtils(accessSecret, refreshSecret, 1L, 1L);
        String token = fastExpireUtils.generateAccessToken("expired@example.com");

        // Wait 10ms to ensure it's expired
        Thread.sleep(10);

        assertThatThrownBy(() -> fastExpireUtils.validateAccessToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
