# Pruebas Unitarias y Cobertura

SmartLogix usa pruebas unitarias y de componentes ligeros en backend y frontend. La meta academica es demostrar al menos 60% de cobertura. En Fase 2 se reforzo cobertura backend con pruebas de controllers, services, mappers, listeners RabbitMQ y bootstrap de infraestructura, sin modificar funcionalidad principal.

## Frameworks

| Capa | Frameworks |
|---|---|
| Backend | JUnit 5, Mockito, Spring Boot Test, MockMvc, JaCoCo |
| Frontend | Vitest, React Testing Library, jsdom, coverage provider `v8` |

## Comandos backend

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

Reporte JaCoCo por servicio:

```text
Backend/<servicio>/target/site/jacoco/index.html
Backend/<servicio>/target/site/jacoco/jacoco.xml
```

## Comandos frontend

```bash
cd Frontend/smartlogix-app
npm ci
npm run test:coverage
npm run build
```

Reporte frontend:

```text
Frontend/smartlogix-app/coverage/index.html
Frontend/smartlogix-app/coverage/lcov.info
```

## Cobertura real posterior a Fase 2

| Componente | Lineas antes Fase 2 | Lineas despues Fase 2 | Metodos despues | Ramas despues | Estado frente al 60% |
|---|---:|---:|---:|---:|---|
| users | 28.83% | 86.58% | 77.71% | 17.13% | Cumple en lineas |
| inventory | 33.56% | 76.17% | 62.67% | 12.72% | Cumple en lineas |
| order | 47.15% | 77.55% | 65.25% | 22.38% | Cumple en lineas |
| shipping | 51.83% | 70.63% | 63.13% | 9.52% | Cumple en lineas |
| notification | 54.11% | 72.50% | 55.44% | 10.13% | Cumple en lineas |
| eureka-server | 0.00% | 66.67% | 50.00% | n/a | Cumple en lineas; infraestructura |
| api-gateway | 66.67% | 66.67% | 70.59% | 70.00% | Cumple en lineas; sin cambios en Fase 2 |
| auth | 76.33% | 76.33% | 53.63% | 5.39% | Cumple en lineas; sin cambios en Fase 2 |
| frontend | 79.77% | 79.77% | 70.68% | 56.07% | Cumple lineas/funciones; sin cambios en Fase 2 |

Estas cifras provienen de:

- `Backend/*/target/site/jacoco/jacoco.xml`
- `Frontend/smartlogix-app/coverage/lcov.info`

## Tests agregados en Prioridad 1

Se agregaron pruebas de bajo riesgo para handlers de errores y seguridad:

- `Backend/auth/src/test/java/com/smartlogix/auth_service/exception/GlobalExceptionHandlerTest.java`
- `Backend/auth/src/test/java/com/smartlogix/auth_service/security/CustomUserDetailsServiceTest.java`
- `Backend/users/src/test/java/com/smartlogix/users/exception/GlobalExceptionHandlerTest.java`
- `Backend/inventory/src/test/java/com/smartlogix/inventory/exception/GlobalExceptionHandlerTest.java`
- `Backend/order/src/test/java/com/smartlogix/order/exception/GlobalExceptionHandlerTest.java`
- `Backend/shipping/src/test/java/com/smartlogix/shipping/exception/GlobalExceptionHandlerTest.java`
- `Backend/notification/src/test/java/com/smartlogix/notification/exception/GlobalExceptionHandlerTest.java`

## Tests agregados o reforzados en Fase 2

| Servicio | Archivos principales | Objetivo |
|---|---|
| users | `UsersControllerTest`, `UsersMapperTest` | Cubrir controllers principales y mapeos entidad/DTO |
| inventory | `InventoryMapperTest`, `WarehouseServiceTest`, `OrderEventConsumerTest` | Cubrir mappers, servicio de bodegas y flujo RabbitMQ de reserva/compensacion |
| order | `OrderMapperTest`, `OrderEventConsumersTest` | Cubrir mapeo de orden y transiciones por eventos |
| shipping | `ShippingMapperTest`, `ReservationConfirmedConsumerTest`, `ShippingEventPublisherTest`, `RoutingApiServiceTest` | Cubrir mapeos, creacion de despacho, publicacion RabbitMQ y fallback de rutas |
| notification | `OrderStatusListenersTest` | Cubrir listeners de confirmed, shipped, delivered y rejected |
| eureka-server | `EurekaServerApplicationTest` | Validar anotaciones de bootstrap y delegacion a `SpringApplication.run` |

## Observaciones tecnicas

La cobertura de lineas backend queda sobre 60% en todos los servicios medidos. La cobertura de ramas sigue siendo baja en varios servicios porque JaCoCo contabiliza DTOs, configuraciones, builders Lombok y bifurcaciones generadas por frameworks. Para nota maxima, conviene mantener la evidencia HTML y explicar que Fase 2 priorizo lineas globales y comportamiento de negocio observable.

`eureka-server` es infraestructura de descubrimiento. Su `pom.xml` ya excluia `**/*Application.class` del check JaCoCo antes de esta fase; aun asi, el reporte HTML queda sobre 60% con pruebas de bootstrap. En defensa oral se debe explicar que Eureka no contiene reglas de negocio ni endpoints funcionales del dominio SmartLogix.

## Plan posterior para mejorar calidad de pruebas

1. Aumentar cobertura de ramas en validaciones y transiciones invalidas.
2. Agregar pruebas de integracion controladas para repositorios JPA con H2/Testcontainers si el tiempo lo permite.
3. Separar reporte academico global y thresholds por capa en una configuracion JaCoCo comun.
4. Guardar reportes HTML JaCoCo, capturas de consola y esta documentacion como evidencia de entrega.
