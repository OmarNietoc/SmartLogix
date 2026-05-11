package com.smartlogix.gateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/smartlogix/auth/register",
            "/smartlogix/auth/login",
            "/api-docs",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-resources",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
