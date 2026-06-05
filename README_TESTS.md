# SmartLogix - Guia de Tests Unitarios y Cobertura

Este documento resume frameworks, comandos y metricas reales auditadas desde los reportes existentes. Las cifras corresponden a reportes JaCoCo/Vitest generados por comandos de build y no deben reemplazarse por estimaciones manuales.

## 1. Frameworks

| Componente | Framework | Reporte |
|---|---|---|
| Backend Spring Boot | JUnit 5, Mockito, Spring Boot Test, MockMvc | `Backend/<servicio>/target/site/jacoco/index.html` |
| Frontend React | Vitest, React Testing Library, jsdom | `Frontend/smartlogix-app/coverage/index.html` |

## 2. Comandos backend

Ejecutar por microservicio:

```bash
cd Backend/auth
mvn clean verify
```

Servicios:

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

Reporte JaCoCo:

```text
Backend/<servicio>/target/site/jacoco/index.html
Backend/<servicio>/target/site/jacoco/jacoco.xml
```

## 3. Comandos frontend

```bash
cd Frontend/smartlogix-app
npm ci
npm run test
npm run test:coverage
npm run build
```

Reporte Vitest:

```text
Frontend/smartlogix-app/coverage/index.html
Frontend/smartlogix-app/coverage/lcov.info
```

## 4. Tests existentes y reforzados

| Componente | Tests destacados |
|---|---|
| api-gateway | `AuthenticationFilterTest`, `JwtUtilTest`, `RouteValidatorTest` |
| auth | `AuthServiceTest`, `AuthControllerTest`, `JwtUtilTest`, `ChileanRutValidatorTest` |
| inventory | `ProductServiceTest`, `InventoryServiceTest`, `InventoryReservationServiceTest`, `WarehouseServiceTest`, `InventoryMapperTest`, `OrderEventConsumerTest`, controllers |
| order | `OrderServiceTest`, `OrderControllerTest`, `OrderMapperTest`, `OrderEventConsumersTest` |
| shipping | `ShipmentServiceTest`, `RouteServiceTest`, `RoutingApiServiceTest`, `ShippingMapperTest`, `ReservationConfirmedConsumerTest`, `ShippingEventPublisherTest`, controllers |
| notification | `NotificationServiceTest`, `EmailServiceTest`, `OrderEventListenerTest`, `OrderStatusListenersTest`, controller |
| users | tests de `CompanyService`, `UserProfileService`, carriers, integrations, roles, controllers y mappers |
| frontend | servicios API, auth, stores y componentes principales |

## 5. Tests agregados en Prioridad 1

| Servicio | Archivo | Motivo |
|---|---|---|
| auth | `Backend/auth/src/test/java/com/smartlogix/auth_service/exception/GlobalExceptionHandlerTest.java` | Cubrir contrato de errores |
| auth | `Backend/auth/src/test/java/com/smartlogix/auth_service/security/CustomUserDetailsServiceTest.java` | Cubrir carga de usuario Spring Security |
| users | `Backend/users/src/test/java/com/smartlogix/users/exception/GlobalExceptionHandlerTest.java` | Cubrir contrato de errores |
| inventory | `Backend/inventory/src/test/java/com/smartlogix/inventory/exception/GlobalExceptionHandlerTest.java` | Cubrir contrato de errores |
| order | `Backend/order/src/test/java/com/smartlogix/order/exception/GlobalExceptionHandlerTest.java` | Cubrir contrato de errores |
| shipping | `Backend/shipping/src/test/java/com/smartlogix/shipping/exception/GlobalExceptionHandlerTest.java` | Cubrir contrato de errores |
| notification | `Backend/notification/src/test/java/com/smartlogix/notification/exception/GlobalExceptionHandlerTest.java` | Cubrir contrato de errores |

## 6. Tests agregados o reforzados en Fase 2

| Servicio | Archivo | Comportamiento cubierto |
|---|---|---|
| users | `Backend/users/src/test/java/com/smartlogix/users/controller/UsersControllerTest.java` | Controllers principales con MockMvc: companies, profiles, roles, carriers e integrations |
| users | `Backend/users/src/test/java/com/smartlogix/users/mapper/UsersMapperTest.java` | Mapeos entidad/DTO de companies, profiles, carriers e integrations |
| inventory | `Backend/inventory/src/test/java/com/smartlogix/inventory/mapper/InventoryMapperTest.java` | Mappers de products, warehouses, inventory, movements y reservations |
| inventory | `Backend/inventory/src/test/java/com/smartlogix/inventory/service/WarehouseServiceTest.java` | Consultas, creacion, validaciones, actualizacion y baja logica de bodegas |
| inventory | `Backend/inventory/src/test/java/com/smartlogix/inventory/event/OrderEventConsumerTest.java` | Reserva de stock, compensacion y publicacion de eventos RabbitMQ |
| order | `Backend/order/src/test/java/com/smartlogix/order/mapper/OrderMapperTest.java` | Mapeo de orden con comuna, region e items |
| order | `Backend/order/src/test/java/com/smartlogix/order/event/OrderEventConsumersTest.java` | Transiciones por eventos de reserva, despacho y entrega |
| shipping | `Backend/shipping/src/test/java/com/smartlogix/shipping/mapper/ShippingMapperTest.java` | Mapeo de shipment y route, estados y campos anidados |
| shipping | `Backend/shipping/src/test/java/com/smartlogix/shipping/event/ReservationConfirmedConsumerTest.java` | Creacion de envio desde reserva confirmada y errores de repositorio |
| shipping | `Backend/shipping/src/test/java/com/smartlogix/shipping/event/ShippingEventPublisherTest.java` | Publicacion de eventos `order.shipped` y `order.delivered` |
| shipping | `Backend/shipping/src/test/java/com/smartlogix/shipping/service/RoutingApiServiceTest.java` | Fallback local cuando no hay coordenadas o falla el servicio externo |
| notification | `Backend/notification/src/test/java/com/smartlogix/notification/listener/OrderStatusListenersTest.java` | Emails y notificaciones por estados confirmed, shipped, delivered y rejected |
| eureka-server | `Backend/eureka-server/src/test/java/com/smartlogix/eureka/EurekaServerApplicationTest.java` | Bootstrap Spring Boot/Eureka y delegacion de `main` |

## 7. Cobertura real registrada localmente

Metricas leidas desde reportes JaCoCo generados con `mvn clean verify`. El criterio academico se evalua sobre cobertura global de lineas por servicio backend. Antes de entregar, regenerar reportes y usar `docs/evidencias/04_pruebas_cobertura/cobertura-final.md` como tabla final auditada.

| Componente | Lineas antes Fase 2 | Lineas finales | Metodos finales | Ramas finales | Estado frente al 60% | Reporte |
|---|---:|---:|---:|---:|---|---|
| users | 28.83% | 97.46% | 100.00% | 82.14% | Cumple lineas | `Backend/users/target/site/jacoco/index.html` |
| inventory | 33.56% | 87.58% | 84.68% | 67.59% | Cumple lineas | `Backend/inventory/target/site/jacoco/index.html` |
| order | 47.15% | 98.28% | 95.38% | 100.00% | Cumple lineas | `Backend/order/target/site/jacoco/index.html` |
| shipping | 51.83% | 82.44% | 75.00% | 75.96% | Cumple lineas | `Backend/shipping/target/site/jacoco/index.html` |
| notification | 54.11% | 90.78% | 82.81% | 100.00% | Cumple lineas | `Backend/notification/target/site/jacoco/index.html` |
| eureka-server | 0.00% | 66.67% | 50.00% | n/a | Cumple lineas; componente de infraestructura | `Backend/eureka-server/target/site/jacoco/index.html` |
| api-gateway | 66.67% | 87.18% | 100.00% | 70.00% | Cumple lineas | `Backend/api-gateway/target/site/jacoco/index.html` |
| auth | 76.33% | 82.67% | 93.55% | 42.86% | Cumple lineas | `Backend/auth/target/site/jacoco/index.html` |
| frontend | 79.77% | 79.77% | 70.68% | 56.07% | Cumple lineas/funciones | `Frontend/smartlogix-app/coverage/index.html` |

## 8. Observaciones tecnicas

La cobertura de lineas backend funcional queda sobre 60% en todos los servicios revisados e integrados. La cobertura de ramas tambien queda sobre 60% en la mayoria de servicios; `auth` queda bajo 60% en ramas, pero cumple el umbral academico configurado por lineas.

`Backend/eureka-server/pom.xml` ya excluia `**/*Application.class` del check JaCoCo antes de esta fase. Aun asi, se agrego una prueba de bootstrap que valida las anotaciones `@SpringBootApplication`, `@EnableEurekaServer` y la delegacion de `main` hacia `SpringApplication.run`. Eureka debe presentarse como infraestructura de descubrimiento, no como microservicio de negocio.

## 9. Acciones pendientes recomendadas

1. Subir cobertura de ramas en services con reglas de transicion y handlers de errores.
2. Mantener los reportes HTML JaCoCo generados y adjuntarlos como evidencia.
3. Evitar excluir clases adicionales de JaCoCo sin justificacion escrita.
4. Para defensa oral, explicar que el umbral academico se valida sobre lineas globales y que ramas quedan como mejora continua.

## 10. Evidencia recomendada para entrega

Guardar evidencia en `docs/evidencias/04_pruebas_cobertura/`:

- Captura de resumen JaCoCo.
- Captura de `Frontend/smartlogix-app/coverage/index.html`.
- Captura de comando `mvn clean verify`.
- Captura de comando `npm run test:coverage`.

No versionar `target/` ni `coverage/`; ambos estan ignorados y deben usarse solo como fuente para capturas.
