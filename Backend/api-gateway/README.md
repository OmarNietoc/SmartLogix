# api-gateway

Gateway REST de SmartLogix con Spring Cloud Gateway, validacion JWT y routing por Eureka.

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

Comandos:

```bash
mvn spring-boot:run
mvn clean test
mvn clean verify
```

Cobertura: `target/site/jacoco/index.html`.
