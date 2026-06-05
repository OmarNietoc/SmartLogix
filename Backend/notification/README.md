# ms-notification

Microservicio de notificaciones, listeners RabbitMQ y envio de correos.

- Puerto: `8085`
- Swagger directo: `http://localhost:8085/swagger-ui.html`
- Gateway: `http://localhost:8080/smartlogix/notification`
- Variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `EUREKA_URI`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`, `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASS`, `MAIL_FROM`

Endpoints principales:

- `POST /notifications`
- `GET /notifications`
- `GET /notifications/unread`
- `GET /notifications/{id}`
- `PATCH /notifications/{id}/read`
- `GET /notifications/order/{orderId}`

Body real para crear notificacion manual:

```json
{
  "orderId": "order-id",
  "recipient": "cliente@smartlogix.cl",
  "subject": "Orden creada",
  "message": "Tu orden fue creada exitosamente"
}
```

Comandos:

```bash
mvn spring-boot:run
mvn clean test
mvn clean verify
```

Cobertura: `target/site/jacoco/index.html`.
