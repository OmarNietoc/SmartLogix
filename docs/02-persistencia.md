# Persistencia

La persistencia principal de SmartLogix se implementa con JPA/Hibernate y PostgreSQL por microservicio. No se usa una base monolitica compartida.

## Patron aplicado

SmartLogix aplica Database per Service:

- Cada microservicio tiene su propia base PostgreSQL.
- Las entidades de un servicio no son persistidas por otros servicios.
- La coordinacion entre dominios se realiza por REST y RabbitMQ, no por joins entre bases.

## Bases por servicio

| Servicio | Base | Puerto local | Evidencia |
|---|---|---:|---|
| auth | `authdb` | 5436 | `Backend/auth/src/main/resources/application.yml` |
| users | `db_users` | 5437 | `Backend/users/src/main/resources/application.yml` |
| inventory | `smartlogix_inventory` | 5433 | `Backend/inventory/src/main/resources/application.yml` |
| order | `orderdb` | 5434 | `Backend/order/src/main/resources/application.yml` |
| shipping | `shipping_db` | 5432 | `Backend/shipping/src/main/resources/application.yml` |
| notification | `notificationdb` | 5435 | `Backend/notification/src/main/resources/application.yml` |

Las bases y contenedores estan definidos en `Backend/docker-compose-local.yml`.

## Entidades principales

| Servicio | Entidades |
|---|---|
| auth | `UserCredential` |
| users | `Company`, `UserProfile`, `Role`, `ExternalCarrier`, `MarketplaceIntegration` |
| inventory | `Product`, `Warehouse`, `Inventory`, `InventoryMovement`, `InventoryReservation` |
| order | `Order`, `OrderItem`, `Pais`, `Region`, `Comuna` |
| shipping | `Shipment`, `Route` |
| notification | `Notification` |

## Repositories

Los servicios usan Spring Data JPA repositories en carpetas `repository`, por ejemplo:

- `Backend/inventory/src/main/java/com/smartlogix/inventory/repository`
- `Backend/order/src/main/java/com/smartlogix/order/repository`
- `Backend/users/src/main/java/com/smartlogix/users/repository`

## Datos de inicializacion

`Backend/order/src/main/resources/data.sql` carga datos geograficos de pais, regiones y comunas usados por el frontend al crear ordenes.

## Variables de conexion

| Variable | Uso |
|---|---|
| `DB_URL` | JDBC URL del PostgreSQL del servicio |
| `DB_USERNAME` | Usuario de base |
| `DB_PASSWORD` | Password local o de ambiente |

Las variables reales deben vivir en `.env` locales. Las plantillas seguras son:

- `Backend/.env.example`
- `Backend/users/.env.example`
- `Backend/prisma/.env.example`

## Prisma

`Backend/prisma` es solo una herramienta auxiliar de seed local. No reemplaza JPA ni los repositories Spring del backend.

## Riesgos y mejoras pendientes

- Actualmente no hay Flyway/Liquibase.
- Para produccion se recomienda reemplazar `ddl-auto: update` por migraciones versionadas.
- El diagrama ER simplificado esta en `docs/diagrams/er-simplificado.mmd`.
