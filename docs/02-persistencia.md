# Persistencia

La persistencia se implementa con JPA/Hibernate y PostgreSQL por microservicio. Cada servicio mantiene su propio esquema y no comparte tablas con otros servicios.

Bases por servicio:

- auth: `authdb`, credenciales y roles JWT.
- users: `db_users`, companias, perfiles, roles, carriers e integraciones.
- inventory: `smartlogix_inventory`, productos, bodegas, stock, movimientos y reservas.
- order: `orderdb`, ordenes, items, paises, regiones y comunas.
- shipping: `shipping_db`, envios y rutas.
- notification: `notificationdb`, notificaciones y estado de entrega.

Entidades principales:

- auth: `UserCredential`.
- users: `Company`, `UserProfile`, `Role`, `ExternalCarrier`, `MarketplaceIntegration`.
- inventory: `Product`, `Warehouse`, `Inventory`, `InventoryMovement`, `InventoryReservation`.
- order: `Order`, `OrderItem`, `Pais`, `Region`, `Comuna`.
- shipping: `Shipment`, `Route`.
- notification: `Notification`.

`Backend/order/src/main/resources/data.sql` carga catalogo geografico de pais, regiones y comunas cuando el servicio inicializa la base.

Variables de conexion:

- `DB_URL`: JDBC URL del PostgreSQL del servicio.
- `DB_USERNAME`: usuario de base de datos.
- `DB_PASSWORD`: password del servicio. Debe venir desde `.env` local o variables del entorno, nunca hardcodeado.
