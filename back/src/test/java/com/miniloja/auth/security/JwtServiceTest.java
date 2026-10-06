package com.miniloja.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void shouldGenerateAndValidateToken_subjectRoundtrip() {
        JwtService jwt = new JwtService("test-secret", "test");

        String token = jwt.generateToken("user@example.com");

        var subject = jwt.validateAndGetSubject(token);

        assertTrue(subject.isPresent());
        assertEquals("user@example.com", subject.get());
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService jwt = new JwtService("test-secret", "test");

        Instant now = Instant.now();
        String token = jwt.generateToken("user@example.com", now.minusSeconds(100), Duration.ofSeconds(1));

        assertTrue(jwt.validateAndGetSubject(token).isEmpty());
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        JwtService jwtA = new JwtService("secret-a", "test");
        JwtService jwtB = new JwtService("secret-b", "test");

        String token = jwtA.generateToken("user@example.com");

        assertTrue(jwtA.validateAndGetSubject(token).isPresent());
        assertTrue(jwtB.validateAndGetSubject(token).isEmpty());
    }

    @Test
    void shouldFallbackToDefaultSecretInDevOrTestWhenNotConfigured() {
        JwtService jwtDev = new JwtService("", "dev");
        JwtService jwtTest = new JwtService("", "test");

        String tokenDev = jwtDev.generateToken("user@example.com");
        String tokenTest = jwtTest.generateToken("user@example.com");

        // se ambos usam o mesmo default, o token deve ser validável entre eles
        assertTrue(jwtDev.validateAndGetSubject(tokenTest).isPresent());
        assertTrue(jwtTest.validateAndGetSubject(tokenDev).isPresent());
    }

    @Test
    void shouldFailWithoutSecretOutsideDevOrTest() {
        boolean thrown = false;
        try {
            new JwtService("", "prod");
        } catch (IllegalStateException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }
}
