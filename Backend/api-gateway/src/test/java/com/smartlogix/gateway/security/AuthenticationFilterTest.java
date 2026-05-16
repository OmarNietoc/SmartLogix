package com.smartlogix.gateway.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthenticationFilterTest {

    private final RouteValidator routeValidator = new RouteValidator();
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final AuthenticationFilter filterFactory = new AuthenticationFilter();

    AuthenticationFilterTest() {
        ReflectionTestUtils.setField(filterFactory, "validator", routeValidator);
        ReflectionTestUtils.setField(filterFactory, "jwtUtil", jwtUtil);
    }

    @Test
    void apply_skipsAuthenticationForOpenEndpoint() {
        GatewayFilter filter = filterFactory.apply(new AuthenticationFilter.Config());
        GatewayFilterChain chain = exchange -> Mono.empty();
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/smartlogix/auth/login"));

        filter.filter(exchange, chain).block();

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void apply_rejectsSecuredEndpointWithoutAuthorizationHeader() {
        GatewayFilter filter = filterFactory.apply(new AuthenticationFilter.Config());
        GatewayFilterChain chain = exchange -> Mono.empty();
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/smartlogix/order/orders"));

        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Missing authorization header");
    }

    @Test
    void apply_validTokenAddsCompanyHeaderBeforeForwarding() {
        GatewayFilter filter = filterFactory.apply(new AuthenticationFilter.Config());
        Claims claims = mock(Claims.class);
        when(claims.get("companyId", String.class)).thenReturn("company-1");
        when(jwtUtil.getClaims("jwt-token")).thenReturn(claims);

        final ServerWebExchange[] forwarded = new ServerWebExchange[1];
        GatewayFilterChain chain = exchange -> {
            forwarded[0] = exchange;
            return Mono.empty();
        };
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/smartlogix/order/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer jwt-token"));

        filter.filter(exchange, chain).block();

        verify(jwtUtil).validateToken("jwt-token");
        assertThat(forwarded[0].getRequest().getHeaders().getFirst("X-Company-Id")).isEqualTo("company-1");
    }
}
