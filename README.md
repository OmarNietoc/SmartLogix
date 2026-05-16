# SmartLogix

SmartLogix es una plataforma fullstack de logistica para gestionar autenticacion, usuarios, inventario, ordenes, envios, rutas y notificaciones. La arquitectura usa Java 21, Spring Boot 3.2.5, Spring Cloud Gateway, Eureka, PostgreSQL por microservicio, RabbitMQ y frontend React/Vite/TypeScript.

## Estructura

```text
SmartLogix/
  Backend/
    api-gateway/
    eureka-server/
    auth/
    users/
    inventory/
    order/
    shipping/
    notification/
    docker-compose-local.yml
  Frontend/
    smartlogix-app/
  docs/
  docker-compose.yml
```

## Ejecucion

```bash
docker compose up -d --build
```

Solo backend:

```bash
cd Backend
docker compose -f docker-compose-local.yml up -d --build
```

Frontend local:

```bash
cd Frontend/smartlogix-app
npm ci
npm run dev
```

Backend local por servicio:

```bash
cd Backend/inventory
mvn spring-boot:run
```

## URLs

- Frontend: http://localhost:5173
- API Gateway: http://localhost:8080
- Eureka: http://localhost:8761
- Swagger Gateway: http://localhost:8080/swagger-ui.html
- RabbitMQ: http://localhost:15672

## Pruebas y cobertura

Backend:

```bash
cd Backend/auth && mvn clean verify
cd Backend/users && mvn clean verify
cd Backend/inventory && mvn clean verify
cd Backend/order && mvn clean verify
cd Backend/shipping && mvn clean verify
cd Backend/notification && mvn clean verify
cd Backend/api-gateway && mvn clean verify
cd Backend/eureka-server && mvn clean verify
```

Cada reporte JaCoCo queda en `target/site/jacoco/index.html`.

Frontend:

```bash
cd Frontend/smartlogix-app
npm ci
npm run test
npm run test:coverage
npm run build
```

El reporte frontend queda en `Frontend/smartlogix-app/coverage/index.html`.

## CI/CD y calidad

GitHub Actions esta en `.github/workflows/ci.yml` y ejecuta:

- Tests y JaCoCo por microservicio.
- Lint, Vitest coverage y build frontend.
- `docker compose config` y build de imagenes.

SonarQube/SonarCloud usa `sonar-project.properties`. Ejecucion local:

```bash
sonar-scanner
```

No se incluyen tokens ni secretos.

## Documentacion

- [Arquitectura](docs/01-arquitectura.md)
- [Persistencia](docs/02-persistencia.md)
- [Pruebas unitarias](docs/03-pruebas-unitarias.md)
- [Despliegue y ejecucion](docs/04-despliegue-y-ejecucion.md)
- [Presentacion final](docs/05-presentacion-final.md)
- [Postman Collection](docs/postman/SmartLogix.postman_collection.json)

## Variables de entorno

Usa `Backend/.env.example` como plantilla. No subir `.env`, claves JWT, credenciales SMTP ni passwords reales al repositorio.
