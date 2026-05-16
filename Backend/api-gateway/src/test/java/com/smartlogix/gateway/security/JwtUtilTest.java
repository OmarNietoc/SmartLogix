package com.smartlogix.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class JwtUtilTest {

    private static final String SECRET = "smartlogix-secret-key-for-tests-1234567890";

    @Test
    void validateToken_acceptsTokenSignedWithPlainSecret() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        String token = tokenFor("admin@smartlogix.cl");

        assertThatCode(() -> jwtUtil.validateToken(token)).doesNotThrowAnyException();
        assertThat(jwtUtil.getClaims(token).getSubject()).isEqualTo("admin@smartlogix.cl");
    }

    private String tokenFor(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
}
