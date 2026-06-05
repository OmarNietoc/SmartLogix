# Endpoints API SmartLogix

Todas las rutas se consumen por API Gateway/BFF:

```text
http://localhost:8080
```

Rutas publicas:

- `POST /smartlogix/auth/register`
- `POST /smartlogix/auth/login`
- `/swagger-ui.html`
- `/api-docs/ms-*`

Rutas protegidas:

- `/smartlogix/users/**`
- `/smartlogix/inventory/**`
- `/smartlogix/order/**`
- `/smartlogix/shipping/**`
- `/smartlogix/notification/**`

Para rutas protegidas se usa:

```http
Authorization: Bearer <token>
```

El Gateway valida el JWT y agrega `X-Company-Id` al request enviado al microservicio.

## Auth

| Metodo | Ruta | Body |
|---|---|---|
| `POST` | `/smartlogix/auth/register` | `email`, `password`, `companyName`, `taxId`, `firstName`, `lastName`, `contactEmail`, `phone` |
| `POST` | `/smartlogix/auth/login` | `email`, `password` |

## Users

| Metodo | Ruta | Body/parametros |
|---|---|---|
| `POST` | `/smartlogix/users/companies` | `taxId`, `name`, `contactEmail`, `phone` |
| `GET` | `/smartlogix/users/companies` | - |
| `POST` | `/smartlogix/users/profiles/company/{companyId}/admin` | `authId`, `firstName`, `lastName`, `roles` |
| `POST` | `/smartlogix/users/profiles/company` | `authId`, `firstName`, `lastName`, `roles` |
| `PUT` | `/smartlogix/users/profiles/{profileId}/roles` | array JSON de roles |
| `GET` | `/smartlogix/users/profiles/company` | empresa desde JWT |
| `GET` | `/smartlogix/users/roles` | - |
| `POST` | `/smartlogix/users/carriers/company/{companyId}` | `name`, `contactEmail`, `phone` |
| `GET` | `/smartlogix/users/carriers/company/{companyId}` | - |
| `POST` | `/smartlogix/users/integrations/company/{companyId}` | `platformName`, `webhookSecret`, `active` |
| `GET` | `/smartlogix/users/integrations/company/{companyId}` | - |

## Inventory

| Metodo | Ruta | Body/parametros |
|---|---|---|
| `GET` | `/smartlogix/inventory/products` | - |
| `GET` | `/smartlogix/inventory/products/{id}` | - |
| `GET` | `/smartlogix/inventory/products/sku/{sku}` | - |
| `POST` | `/smartlogix/inventory/products` | `sku`, `name`, `price`, `status` |
| `PUT` | `/smartlogix/inventory/products/{id}` | `sku`, `name`, `price`, `status` |
| `DELETE` | `/smartlogix/inventory/products/{id}` | - |
| `GET` | `/smartlogix/inventory/warehouses` | query opcional `type` |
| `GET` | `/smartlogix/inventory/warehouses/{id}` | - |
| `POST` | `/smartlogix/inventory/warehouses` | `name`, `locationAddress`, `type`, `status` |
| `PUT` | `/smartlogix/inventory/warehouses/{id}` | `name`, `locationAddress`, `type`, `status` |
| `DELETE` | `/smartlogix/inventory/warehouses/{id}` | - |
| `GET` | `/smartlogix/inventory/stocks` | query opcional `productId`, `warehouseId` |
| `GET` | `/smartlogix/inventory/stocks/{id}` | - |
| `POST` | `/smartlogix/inventory/stocks` | `productId`, `warehouseId`, `stockAvailable` |
| `PATCH` | `/smartlogix/inventory/stocks/{id}/increase` | `quantity`, `reason` |
| `PATCH` | `/smartlogix/inventory/stocks/{id}/decrease` | `quantity`, `reason` |
| `GET` | `/smartlogix/inventory/stocks/{id}/movements` | - |
| `GET` | `/smartlogix/inventory/reservations` | query opcional `orderId`, `status` |
| `GET` | `/smartlogix/inventory/reservations/{id}` | - |
| `POST` | `/smartlogix/inventory/reservations` | `orderId`, `productId`, `warehouseId`, `quantity`, `companyId` |
| `PATCH` | `/smartlogix/inventory/reservations/{id}/compensate` | - |
| `PATCH` | `/smartlogix/inventory/reservations/{id}/confirm-output` | - |

## Order

| Metodo | Ruta | Body/parametros |
|---|---|---|
| `POST` | `/smartlogix/order/orders` | `customerName`, `customerEmail`, `street`, `comunaId`, `items` |
| `GET` | `/smartlogix/order/orders` | - |
| `GET` | `/smartlogix/order/orders/{id}` | - |
| `PUT` | `/smartlogix/order/orders/{id}/status` | `{ "status": "CONFIRMED" }` |
| `GET` | `/smartlogix/order/regiones` | - |
| `GET` | `/smartlogix/order/comunas?regionId=13` | query `regionId` |

## Shipping

| Metodo | Ruta | Body/parametros |
|---|---|---|
| `GET` | `/smartlogix/shipping/shipments` | query opcional `deliveryStatus` |
| `GET` | `/smartlogix/shipping/shipments/{id}` | - |
| `GET` | `/smartlogix/shipping/shipments/tracking/{tracking_number}` | - |
| `POST` | `/smartlogix/shipping/shipments` | `orderId`, `customerName`, `customerEmail`, `shippingAddress`, `latitude`, `longitude`, `deliveryStatus` |
| `PATCH` | `/smartlogix/shipping/shipments/{id}/status` | JSON string, por ejemplo `"DISPATCHED"` |
| `DELETE` | `/smartlogix/shipping/shipments/{id}` | - |
| `GET` | `/smartlogix/shipping/routes` | query opcional `status` |
| `POST` | `/smartlogix/shipping/routes` | `carrierId`, `originAddress`, `shipmentIds`, `optimizeRoute` |
| `POST` | `/smartlogix/shipping/routes/generate-proposal` | `originAddress`, `shipmentIds` |
| `GET` | `/smartlogix/shipping/routes/{id}` | - |
| `PATCH` | `/smartlogix/shipping/routes/{id}/status` | JSON string, por ejemplo `"IN_PROGRESS"` |
| `DELETE` | `/smartlogix/shipping/routes/{id}` | - |

## Notification

| Metodo | Ruta | Body/parametros |
|---|---|---|
| `POST` | `/smartlogix/notification/notifications` | `orderId`, `recipient`, `subject`, `message` |
| `GET` | `/smartlogix/notification/notifications` | - |
| `GET` | `/smartlogix/notification/notifications/unread` | - |
| `PATCH` | `/smartlogix/notification/notifications/{id}/read` | - |
| `GET` | `/smartlogix/notification/notifications/{id}` | - |
| `GET` | `/smartlogix/notification/notifications/order/{orderId}` | - |

## Postman

- Coleccion: `docs/postman/SmartLogix.postman_collection.json`
- Environment: `docs/postman/SmartLogix.postman_environment.json`

El request de login guarda `token` y `companyId` en el environment si la respuesta es exitosa.
