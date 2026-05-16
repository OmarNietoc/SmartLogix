# ms-inventory

Microservicio de productos, bodegas, stock, movimientos y reservas.

- Puerto: `8081`
- Swagger directo: `http://localhost:8081/swagger-ui.html`
- Gateway: `http://localhost:8080/smartlogix/inventory`
- Variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `EUREKA_URI`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`

Endpoints principales:

- `GET/POST /products`
- `GET/POST /warehouses`
- `GET/POST /stocks`
- `PATCH /stocks/{id}/increase`
- `POST /reservations`

Comandos:

```bash
mvn spring-boot:run
mvn clean test
mvn clean verify
```

Cobertura: `target/site/jacoco/index.html`.
