# ms-inventory

Microservicio de productos, bodegas, stock, movimientos y reservas.

- Puerto: `8081`
- Swagger directo: `http://localhost:8081/swagger-ui.html`
- Gateway: `http://localhost:8080/smartlogix/inventory`
- Variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `EUREKA_URI`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`

Endpoints principales:

- `GET /products`
- `GET /products/{id}`
- `GET /products/sku/{sku}`
- `POST /products`
- `PUT /products/{id}`
- `DELETE /products/{id}`
- `GET /warehouses`
- `GET /warehouses?type=WAREHOUSE`
- `GET /warehouses/{id}`
- `POST /warehouses`
- `PUT /warehouses/{id}`
- `DELETE /warehouses/{id}`
- `GET /stocks`
- `GET /stocks?productId=&warehouseId=`
- `GET /stocks/{id}`
- `POST /stocks`
- `PATCH /stocks/{id}/increase`
- `PATCH /stocks/{id}/decrease`
- `GET /stocks/{id}/movements`
- `GET /reservations`
- `GET /reservations?orderId=&status=`
- `GET /reservations/{id}`
- `POST /reservations`
- `PATCH /reservations/{id}/compensate`
- `PATCH /reservations/{id}/confirm-output`

Las rutas se consumen normalmente por Gateway con Bearer token. El Gateway propaga `X-Company-Id` hacia este servicio.

Estados validos de reserva: `RESERVED`, `COMPENSATED`, `CANCELLED`.

Comandos:

```bash
mvn spring-boot:run
mvn clean test
mvn clean verify
```

Cobertura: `target/site/jacoco/index.html`.
