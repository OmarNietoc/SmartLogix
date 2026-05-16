package com.smartlogix.auth_service.security;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    @Test
    void generateToken_includesSubjectCompanyAndExpiration() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "smartlogix-secret-key-for-tests-1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);

        String token = jwtUtil.generateToken("admin@smartlogix.cl", "company-1");

        var claims = Jwts.parserBuilder()
                .setSigningKey("smartlogix-secret-key-for-tests-1234567890".getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
        assertThat(claims.getSubject()).isEqualTo("admin@smartlogix.cl");
        assertThat(claims.get("companyId")).isEqualTo("company-1");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }
}
