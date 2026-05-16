# Despliegue y ejecucion

Requisitos:

- Docker y Docker Compose.
- Java 21 para ejecucion local backend.
- Node 22 para frontend local.

Levantar todo:

```bash
docker compose up -d --build
```

Levantar solo backend:

```bash
cd Backend
docker compose -f docker-compose-local.yml up -d --build
```

URLs:

- Frontend: http://localhost:5173
- API Gateway: http://localhost:8080
- Eureka: http://localhost:8761
- Swagger Gateway: http://localhost:8080/swagger-ui.html
- RabbitMQ Management: http://localhost:15672

Detener:

```bash
docker compose down
```

Limpiar volumenes de test/local:

```bash
docker compose down -v
```

Para solo validar Compose:

```bash
docker compose config
cd Backend && docker compose -f docker-compose-local.yml config
```
