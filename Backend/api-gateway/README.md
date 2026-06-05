# api-gateway

Gateway REST de SmartLogix con Spring Cloud Gateway, validacion JWT y routing por Eureka. En la entrega academica se documenta como BFF/API Gateway porque es la unica entrada HTTP usada por el frontend, centraliza CORS, autenticacion, rutas y Swagger agregado.

- Puerto: `8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Variables: `SERVER_PORT`, `EUREKA_URI`, `JWT_SECRET`

Rutas principales:

- `/smartlogix/auth/**`
- `/smartlogix/users/**`
- `/smartlogix/inventory/**`
- `/smartlogix/order/**`
- `/smartlogix/shipping/**`
- `/smartlogix/notification/**`
- `/api-docs/ms-auth`
- `/api-docs/ms-users`
- `/api-docs/ms-inventory`
- `/api-docs/ms-order`
- `/api-docs/ms-shipping`
- `/api-docs/ms-notification`

Rutas publicas sin Bearer token:

- `/smartlogix/auth/register`
- `/smartlogix/auth/login`
- `/swagger-ui.html`
- `/api-docs/**`

Rutas protegidas:

- `/smartlogix/users/**`
- `/smartlogix/inventory/**`
- `/smartlogix/order/**`
- `/smartlogix/shipping/**`
- `/smartlogix/notification/**`

El filtro `AuthenticationFilter` valida el JWT y agrega `X-Company-Id` al request enviado al microservicio destino.

Comandos:

```bash
mvn spring-boot:run
mvn clean test
mvn clean verify
```

Cobertura: `target/site/jacoco/index.html`.
