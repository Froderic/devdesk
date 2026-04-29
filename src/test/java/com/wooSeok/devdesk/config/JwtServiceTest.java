package com.wooSeok.devdesk.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        Field secretField = JwtService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b");

        Field expirationField = JwtService.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, 86400000L);
    }

    @Test
    void generateToken_andExtractEmail_success() {
        String token = jwtService.generateToken("test@devdesk.com", "ADMIN");

        assertThat(token).isNotNull();
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@devdesk.com");
    }

    @Test
    void generateToken_andExtractRole_success() {
        String token = jwtService.generateToken("test@devdesk.com", "ADMIN");

        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateToken("test@devdesk.com", "ADMIN");

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("invalid.token.string")).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() throws Exception {
        Field expirationField = JwtService.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, -1000L);

        String expiredToken = jwtService.generateToken("test@devdesk.com", "ADMIN");

        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }
}