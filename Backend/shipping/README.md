# ms-shipping

Microservicio de envios, rutas y calculo de carrier con Strategy.

- Puerto: `8084`
- Swagger directo: `http://localhost:8084/swagger-ui.html`
- Gateway: `http://localhost:8080/smartlogix/shipping`
- Variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `EUREKA_URI`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`

Endpoints principales:

- `GET /shipments`
- `GET /shipments?deliveryStatus=`
- `GET /shipments/{id}`
- `GET /shipments/tracking/{tracking_number}`
- `POST /shipments`
- `PATCH /shipments/{id}/status`
- `DELETE /shipments/{id}`
- `GET /routes`
- `GET /routes?status=`
- `GET /routes/{id}`
- `POST /routes`
- `POST /routes/generate-proposal`
- `PATCH /routes/{id}/status`
- `DELETE /routes/{id}`

Los endpoints de estado `PATCH` reciben el enum como JSON string, por ejemplo `"DISPATCHED"` para envios o `"IN_PROGRESS"` para rutas.

Comandos:

```bash
mvn spring-boot:run
mvn clean test
mvn clean verify
```

Cobertura: `target/site/jacoco/index.html`.
