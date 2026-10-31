
package com.creditcard.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtSecurityTest {

    private JwtEncoder encoder;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();

        String secret = java.util.Base64.getEncoder()
                .encodeToString(new byte[32]);

        SecretKey key = config.jwtSecretKey(secret);
        encoder = config.jwtEncoder(key);
        decoder = config.jwtDecoder(key, "credit-card-management");
    }

    private String createToken(String issuer, Instant issuedAt,
                               Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject("2")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("email", "login-test-002@example.com")
                .build();

        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        )).getTokenValue();
    }

    @Test
    void shouldAcceptValidToken() {
        Instant now = Instant.now();

        String token = createToken(
                "credit-card-management",
                now,
                now.plusSeconds(1800)
        );

        Jwt decoded = decoder.decode(token);

        assertEquals("2", decoded.getSubject());
    }

    @Test
    void shouldRejectExpiredToken() {
        Instant now = Instant.now();

        String token = createToken(
                "credit-card-management",
                now.minusSeconds(600),
                now.minusSeconds(300)
        );

        assertThrows(JwtException.class,
                () -> decoder.decode(token));
    }

    @Test
    void shouldRejectIncorrectIssuer() {
        Instant now = Instant.now();

        String token = createToken(
                "unknown-issuer",
                now,
                now.plusSeconds(1800)
        );

        assertThrows(JwtException.class,
                () -> decoder.decode(token));
    }

    @Test
    void shouldRejectTamperedToken() {
        Instant now = Instant.now();

        String token = createToken(
                "credit-card-management",
                now,
                now.plusSeconds(1800)
        );

        String[] parts = token.split("\\.");

        // Alter the signature without signing the token again.
        char original = parts[2].charAt(0);
        char replacement = original == 'A' ? 'B' : 'A';

        parts[2] = replacement + parts[2].substring(1);

        String tamperedToken = String.join(".", parts);

        assertThrows(JwtException.class,
                () -> decoder.decode(tamperedToken));
    }
}
