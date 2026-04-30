# ms-inventory / inventary - SmartLogix

Microservicio Spring Boot encargado de la gestión de inventario de SmartLogix. Fue ampliado siguiendo la misma lógica de capas usada por `ms-shipping`: `controller`, `dto`, `mapper`, `model`, `repository`, `service`, `exception` y `enums`.

> Nota: la carpeta y el nombre de Eureka del proyecto original se mantienen como `inventary` / `ms-inventary` para no romper Docker Compose ni Eureka. Los endpoints REST se rectificaron a `/smartlogix/inventory/**`, igualando la convención usada por shipping: `/smartlogix/shipping/**`.

## Estructura implementada

- `Product`: catálogo de productos por empresa.
- `Warehouse`: bodegas o tiendas físicas.
- `Inventory`: stock disponible y reservado por producto/bodega.
- `InventoryReservation`: reservas idempotentes para el Patrón Saga.
- `InventoryMovement`: bitácora de movimientos `IN`, `OUT`, `RESERVED`, `COMPENSATED`.
- `MessageResponse<T>`: respuesta estándar igual a `ms-shipping`.
- `GlobalExceptionHandler`: manejo centralizado de errores.
- `MapStruct`: mapeo entre entidades y DTOs, siguiendo la lógica de shipping.

## Endpoints

### Productos

Base path: `/smartlogix/inventory/products`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/smartlogix/inventory/products?companyId={id}` | Lista productos, opcionalmente por empresa. |
| GET | `/smartlogix/inventory/products/{id}` | Obtiene producto por ID. |
| GET | `/smartlogix/inventory/products/sku/{sku}` | Obtiene producto por SKU. |
| POST | `/smartlogix/inventory/products` | Crea producto. |
| PUT | `/smartlogix/inventory/products/{id}` | Actualiza producto. |
| DELETE | `/smartlogix/inventory/products/{id}` | Elimina producto. |

Ejemplo POST:

```json
{
  "companyId": "company-001",
  "sku": "SKU-001",
  "name": "Caja de embalaje",
  "price": 12990.00
}
```

### Bodegas

Base path: `/smartlogix/inventory/warehouses`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/smartlogix/inventory/warehouses?companyId={id}&type=WAREHOUSE` | Lista bodegas, con filtros opcionales. |
| GET | `/smartlogix/inventory/warehouses/{id}` | Obtiene bodega por ID. |
| POST | `/smartlogix/inventory/warehouses` | Crea bodega. |
| PUT | `/smartlogix/inventory/warehouses/{id}` | Actualiza bodega. |
| DELETE | `/smartlogix/inventory/warehouses/{id}` | Elimina bodega. |

Ejemplo POST:

```json
{
  "companyId": "company-001",
  "name": "Bodega Central",
  "locationAddress": "Av. Principal 123, Santiago",
  "type": "WAREHOUSE"
}
```

### Inventario / stock

Base path: `/smartlogix/inventory/stocks`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/smartlogix/inventory/stocks?companyId={id}` | Lista inventario por empresa. |
| GET | `/smartlogix/inventory/stocks?productId={id}` | Lista stock por producto. |
| GET | `/smartlogix/inventory/stocks?warehouseId={id}` | Lista stock por bodega. |
| GET | `/smartlogix/inventory/stocks/{id}` | Obtiene una fila de inventario. |
| POST | `/smartlogix/inventory/stocks` | Crea stock inicial producto/bodega. |
| PATCH | `/smartlogix/inventory/stocks/{id}/increase` | Incrementa stock disponible. |
| PATCH | `/smartlogix/inventory/stocks/{id}/decrease` | Descuenta stock disponible. |
| GET | `/smartlogix/inventory/stocks/{id}/movements` | Lista movimientos auditables. |

Ejemplo POST:

```json
{
  "productId": "uuid-producto",
  "warehouseId": "uuid-bodega",
  "stockAvailable": 100
}
```

Ejemplo PATCH increase/decrease:

```json
{
  "quantity": 20,
  "reason": "Ingreso manual por reposición"
}
```

### Reservas Saga

Base path: `/smartlogix/inventory/reservations`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/smartlogix/inventory/reservations?orderId={id}` | Lista reservas por pedido. |
| GET | `/smartlogix/inventory/reservations?status=RESERVED` | Lista reservas por estado. |
| GET | `/smartlogix/inventory/reservations/{id}` | Obtiene reserva por ID. |
| POST | `/smartlogix/inventory/reservations` | Reserva stock para un pedido. |
| PATCH | `/smartlogix/inventory/reservations/{id}/compensate` | Compensa/cancela reserva y devuelve stock. |
| PATCH | `/smartlogix/inventory/reservations/{id}/confirm-output` | Confirma salida final desde stock reservado. |

Ejemplo POST:

```json
{
  "orderId": "order-001",
  "productId": "uuid-producto",
  "warehouseId": "uuid-bodega",
  "quantity": 2
}
```

## Flujo de reserva

1. `ms-orders` crea un pedido y solicita reserva.
2. `ms-inventory` valida stock disponible.
3. Si hay stock, descuenta `stockAvailable` y aumenta `stockReserved`.
4. Se crea `InventoryReservation` con estado `RESERVED`.
5. Si el pedido se cancela, `/compensate` devuelve stock disponible.
6. Si el pedido se confirma/despacha, `/confirm-output` descuenta definitivamente el stock reservado.

## Cambios en API Gateway

Se actualizó la ruta del gateway para inventario:

```yaml
- id: ms-inventary
  uri: lb://ms-inventary
  predicates:
    - Path=/smartlogix/inventory/**
```
