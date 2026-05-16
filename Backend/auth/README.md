# ms-auth

Microservicio de registro, login y emision de JWT.

- Puerto: `8086`
- Swagger directo: `http://localhost:8086/swagger-ui.html`
- Gateway: `http://localhost:8080/smartlogix/auth`
- Variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `EUREKA_URI`

Endpoints principales:

- `POST /register`
- `POST /login`

Comandos:

```bash
mvn spring-boot:run
mvn clean test
mvn clean verify
```

Cobertura: `target/site/jacoco/index.html`.
