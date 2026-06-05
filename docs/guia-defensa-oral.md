# Guia de defensa oral SmartLogix

## Discurso tecnico recomendado

SmartLogix se organiza como una arquitectura de microservicios porque el dominio logistico se divide en responsabilidades independientes: autenticacion, usuarios, inventario, ordenes, despacho y notificaciones. El frontend consume solo el API Gateway/BFF, que centraliza seguridad, CORS, rutas y documentacion OpenAPI. Cada microservicio persiste sus datos con JPA en una base PostgreSQL propia y se integra con otros servicios mediante REST y eventos RabbitMQ.

## Preguntas probables y respuestas

### Por que microservicios?

Porque el sistema tiene dominios funcionales independientes. Separar auth, users, inventory, order, shipping y notification reduce acoplamiento, permite escalar servicios criticos y mejora la mantenibilidad.

### Donde esta el BFF?

Esta en `Backend/api-gateway`. Es un BFF/API Gateway: el frontend consume una sola entrada HTTP, el Gateway valida JWT, aplica CORS, enruta a microservicios y agrega Swagger. No es un BFF con agregaciones complejas de vista, pero cumple el rol de frontera backend para frontend.

### Como se comunica el frontend con backend?

El frontend centraliza llamadas en `Frontend/smartlogix-app/src/services/api.ts` y usa `VITE_API_URL`. Las rutas apuntan al Gateway en `http://localhost:8080/smartlogix/...`.

### Como se comunican los microservicios?

Por REST para solicitudes directas y RabbitMQ para eventos del flujo principal. Al crear una orden, order publica evento, inventory reserva stock, shipping crea envio/ruta y notification informa al cliente.

### Como funciona la persistencia?

Cada microservicio tiene JPA/Hibernate y PostgreSQL propio. Esto aplica Database per Service. El servicio order ademas carga regiones y comunas desde `Backend/order/src/main/resources/data.sql`.

### Como se mide cobertura?

Backend usa JaCoCo por microservicio. Frontend usa Vitest coverage v8. Los reportes estan en `target/site/jacoco/index.html` y `Frontend/smartlogix-app/coverage/index.html`.

### Que patrones aplicaron realmente?

MVC, Repository, Service Layer, DTO/Mapper, Dependency Injection, API Gateway/BFF, Database per Service, Saga coreografiada con RabbitMQ, Strategy en shipping y Circuit Breaker en integraciones externas.

### Cual es la principal limitacion actual?

La cobertura global de varios servicios esta bajo 60% y faltan mejoras productivas como migraciones Flyway/Liquibase, Actuator health checks y seguridad interna entre microservicios si se despliega fuera de un entorno local controlado.

### Como se ejecuta el sistema?

Con Docker Compose desde la raiz:

```bash
docker compose up -d --build
```

Luego se revisa frontend en `http://localhost:5173`, Gateway en `http://localhost:8080`, Swagger en `/swagger-ui.html` y Eureka en `http://localhost:8761`.

## Checklist antes de exponer

- [ ] Ejecutar Postman login y guardar token.
- [ ] Mostrar Swagger Gateway.
- [ ] Mostrar diagrama arquitectura.
- [ ] Explicar flujo de orden.
- [ ] Mostrar JPA repositories y entidades.
- [ ] Mostrar tests y cobertura.
- [ ] Reconocer honestamente brechas de cobertura global.
- [ ] Explicar plan de mejora.
