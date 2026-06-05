# Preguntas y respuestas de defensa

## Por que microservicios?

Porque el dominio logistico se divide naturalmente en autenticacion, usuarios, inventario, ordenes, despacho y notificaciones. Separar responsabilidades reduce acoplamiento y permite evolucionar cada componente con su propia base de datos.

## Que es el BFF/API Gateway?

Es la frontera backend consumida por el frontend. En SmartLogix esta en `Backend/api-gateway`: centraliza CORS, validacion JWT, rutas `/smartlogix/**` y Swagger agregado. No contiene reglas de negocio del dominio.

## Por que Eureka?

Eureka permite service discovery. El Gateway enruta a `lb://ms-*`, por lo que no necesita conocer host y puerto fisico de cada instancia.

## Por que PostgreSQL por servicio?

Porque aplica Database per Service. Cada microservicio administra sus entidades y repositories, evitando joins entre dominios y reduciendo acoplamiento de datos.

## Como se comunica el frontend con backend?

El frontend usa servicios TypeScript y `VITE_API_URL` para llamar solo al Gateway en `http://localhost:8080/smartlogix/...`. Las rutas protegidas usan Bearer token.

## Como se usa RabbitMQ?

RabbitMQ coordina eventos del flujo principal: order publica creacion de orden, inventory reserva stock, shipping crea despacho y notification informa al usuario.

## Que patrones se aplicaron?

MVC, Service Layer, Repository, DTO/Mapper, Dependency Injection, API Gateway/BFF, Database per Service, Saga coreografiada, Strategy en shipping y Circuit Breaker en integraciones externas.

## Como se mide cobertura?

Backend usa JaCoCo con `mvn clean verify` por microservicio. Frontend usa Vitest coverage con `npm run test:coverage`. Los reportes se abren en `target/site/jacoco/index.html` y `coverage/index.html`.

## Que limitaciones existen?

No hay migraciones versionadas con Flyway/Liquibase, falta observabilidad centralizada, algunos puertos internos se exponen para demo local y la seguridad entre microservicios deberia endurecerse para produccion.

## Que se mejoraria en produccion?

Migraciones versionadas, secretos administrados por vault/variables seguras, health checks Actuator, trazabilidad distribuida, logs centralizados, metricas, CI/CD con despliegue y perfiles por ambiente.

## Que hizo cada integrante?

Usar `docs/defensa/resumen-por-integrante.md` como fuente. Cada integrante debe explicar una parte tecnica concreta, mostrar una ruta real del proyecto y mencionar una prueba o evidencia asociada.
