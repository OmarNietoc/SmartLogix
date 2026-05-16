# ms-shipping

Microservicio de envios, rutas y calculo de carrier con Strategy.

- Puerto: `8084`
- Swagger directo: `http://localhost:8084/swagger-ui.html`
- Gateway: `http://localhost:8080/smartlogix/shipping`
- Variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `EUREKA_URI`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`

Endpoints principales:

- `GET/POST /shipments`
- `GET /shipments/{id}`
- `PATCH /shipments/{id}/status`
- `GET/POST /routes`

Comandos:

```bash
mvn spring-boot:run
mvn clean test
mvn clean verify
```

Cobertura: `target/site/jacoco/index.html`.
