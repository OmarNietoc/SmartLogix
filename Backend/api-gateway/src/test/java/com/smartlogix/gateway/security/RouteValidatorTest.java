package com.smartlogix.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

class RouteValidatorTest {

    private final RouteValidator routeValidator = new RouteValidator();

    @Test
    void isSecured_allowsAuthAndSwaggerEndpoints() {
        assertThat(routeValidator.isSecured.test(
                MockServerHttpRequest.get("/smartlogix/auth/login").build())).isFalse();
        assertThat(routeValidator.isSecured.test(
                MockServerHttpRequest.get("/swagger-ui.html").build())).isFalse();
    }

    @Test
    void isSecured_securesBusinessEndpoints() {
        assertThat(routeValidator.isSecured.test(
                MockServerHttpRequest.get("/smartlogix/order/orders").build())).isTrue();
    }
}
