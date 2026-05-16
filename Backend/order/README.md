# ms-order

Microservicio de ordenes, items y catalogo geografico de regiones/comunas.

- Puerto: `8082`
- Swagger directo: `http://localhost:8082/swagger-ui.html`
- Gateway: `http://localhost:8080/smartlogix/order`
- Variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `EUREKA_URI`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`

Endpoints principales:

- `GET/POST /orders`
- `GET /orders/{id}`
- `PUT /orders/{id}/status`
- `GET /regiones`
- `GET /comunas?regionId=`

Comandos:

```bash
mvn spring-boot:run
mvn clean test
mvn clean verify
```

Cobertura: `target/site/jacoco/index.html`.
