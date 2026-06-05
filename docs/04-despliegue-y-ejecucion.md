# Despliegue y ejecucion

## Requisitos

- Docker y Docker Compose.
- Java 21 para ejecucion local backend.
- Maven para ejecutar tests si no se usa wrapper.
- Node 22 para frontend local.

## Variables de entorno

Plantillas seguras:

- `Backend/.env.example`
- `Backend/users/.env.example`
- `Backend/prisma/.env.example`
- `Frontend/smartlogix-app/.env.example`

No incluir `.env` reales en GitHub ni en el ZIP/RAR final.

## Levantar todo

Desde la raiz:

```bash
docker compose up -d --build
```

Incluye:

- Frontend.
- API Gateway/BFF.
- Eureka.
- Microservicios backend.
- PostgreSQL por servicio.
- RabbitMQ.

## Levantar solo backend

```bash
cd Backend
docker compose -f docker-compose-local.yml up -d --build
```

## URLs

| Recurso | URL |
|---|---|
| Frontend | `http://localhost:5173` |
| API Gateway/BFF | `http://localhost:8080` |
| Swagger Gateway | `http://localhost:8080/swagger-ui.html` |
| Eureka | `http://localhost:8761` |
| RabbitMQ Management | `http://localhost:15672` |

## Validacion Compose

```bash
docker compose config
cd Backend
docker compose -f docker-compose-local.yml config
```

## Detener

```bash
docker compose down
```

Para borrar volumenes locales:

```bash
docker compose down -v
```

`down -v` elimina bases de datos locales.

## CI/CD

La configuracion existe en `.github/workflows/ci.yml` y valida:

- Backend con `mvn -B clean verify`.
- Frontend con `npm ci`, lint, coverage y build.
- Docker Compose config.
- Build Docker.

## Riesgos actuales

- Los microservicios exponen puertos directos para demo local.
- Los Dockerfiles backend construyen imagen con tests omitidos; la calidad se valida por CI y `mvn clean verify`.
- No hay health checks HTTP por Actuator en microservicios.
