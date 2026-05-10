# SmartLogix — Plataforma SaaS de Logística

Plataforma fullstack para gestión logística de PYMEs. Backend de microservicios con Java 21 + Spring Boot 3.2.5, orquestado con Docker Compose. Frontend en desarrollo.

---

## Tabla de Contenidos

- [Stack Tecnológico](#stack-tecnológico)
- [Estructura del Repositorio](#estructura-del-repositorio)
- [Mapa de Microservicios](#mapa-de-microservicios)
- [Arquitectura](#arquitectura)
- [Inicio Rápido](#inicio-rápido)
- [Variables de Entorno](#variables-de-entorno)
- [Documentación API (Swagger)](#documentación-api-swagger)
- [Sistema de Roles (ms-users)](#sistema-de-roles-ms-users)
- [Direcciones Geográficas (ms-order)](#direcciones-geográficas-ms-order)
- [Pruebas Unitarias](#pruebas-unitarias)
- [Convenciones de Código](#convenciones-de-código)

---

## Stack Tecnológico

### Backend

| Tecnología | Versión | Uso |
| :--- | :--- | :--- |
| Java | 21 (LTS) | Lenguaje principal |
| Spring Boot | 3.2.5 | Framework base |
| Spring Cloud | 2023.0.1 | Gateway y Discovery |
| Netflix Eureka | — | Service Discovery |
| Spring Cloud Gateway | — | API Gateway / Enrutador |
| PostgreSQL | 15 | Base de datos (por servicio) |
| RabbitMQ | 3.13 | Mensajería asíncrona |
| MapStruct | 1.5.5.Final | Mapeo de DTOs |
| Lombok | — | Reducción de boilerplate |
| Docker | — | Contenedores |
| springdoc-openapi | 2.5.0 | Documentación Swagger |

### Frontend

> En desarrollo — se agregará a este repositorio bajo `Frontend/`.

---

## Estructura del Repositorio

```
SmartLogix/
├── Backend/          ← microservicios Java
│   ├── eureka-server/
│   ├── api-gateway/
│   ├── ms-order/
│   ├── ms-inventory/
│   ├── ms-users/
│   ├── ms-shipping/
│   ├── ms-notification/
│   ├── ms-auth/
│   └── docker-compose-local.yml
└── Frontend/         ← en desarrollo
```

---

## Mapa de Microservicios

Todos los servicios se registran en **Eureka** (`8761`) y son accesibles vía **API Gateway** (`8080`).

| Servicio | Puerto Directo | Base de Datos | Responsabilidad |
| :--- | :---: | :--- | :--- |
| `eureka-server` | `8761` | — | Service Discovery |
| `api-gateway` | `8080` | — | Enrutador y punto de entrada |
| `ms-inventory` | `8081` | `smartlogix_inventory` | Stock, productos y bodegas |
| `ms-order` | `8082` | `orderdb` | Órdenes de compra |
| `ms-users` | `8083` | `db_users` | PYMEs, perfiles y transportistas |
| `ms-shipping` | `8084` | `shipping_db` | Entregas, rutas y flotas |
| `ms-notification` | `8085` | `notificationdb` | Alertas y correos |
| `ms-auth` | `8086` | `authdb` | JWT y autenticación |

---

## Arquitectura

```
Cliente / Frontend
        │
        ▼
┌───────────────────┐
│   API Gateway     │  :8080  ← entrada única
│  (Spring Cloud)   │
└────────┬──────────┘
         │  enruta a
┌────────▼──────────────────────────────────────────────┐
│                   Eureka Server  :8761                 │
│              (registro de servicios)                   │
└───┬───────┬───────┬───────┬───────┬───────┬───────────┘
    │       │       │       │       │       │
  Order  Inventory Users Shipping Notif.  Auth
  :8082   :8081   :8083   :8084   :8085  :8086
    │       │               │       │
    └───────┴───────────────┴───────┘
                    │
              RabbitMQ :5672
         (eventos asíncronos - Saga)
```

**Patrones implementados:**
- **Saga (Coreografía):** `Order` → `Inventory` → `Shipping` → `Notification` vía RabbitMQ
- **Database-per-service:** cada microservicio tiene su propia BD PostgreSQL
- **Soft Delete:** borrado lógico por cambio de estado, sin `DELETE` físico
- **Strategy:** cálculo de rutas según tipo de transportista (DHL, local)
- **Circuit Breaker:** Resilience4j en llamadas a APIs externas (OSRM, Nominatim)

---

## Inicio Rápido

### Prerequisitos

- Docker Desktop instalado y corriendo
- Git

### 1. Clonar y configurar entorno

```bash
git clone <repo-url>
cd SmartLogix/Backend

# Copiar template de variables de entorno
cp .env.example .env
# Editar .env con tus credenciales (ver sección Variables de Entorno)
```

### 2. Levantar el stack completo, incluido el frontend

Desde la raiz del repositorio:

```bash
docker compose up -d --build
```

Esto levanta en un solo comando:

- Frontend Vite: http://localhost:5173
- API Gateway: http://localhost:8080
- Eureka: http://localhost:8761
- RabbitMQ Management: http://localhost:15672
- Todos los microservicios y sus bases PostgreSQL

### Backend solamente

```bash
cd Backend
docker compose -f docker-compose-local.yml up -d
```

### 3. Verificar que todos los servicios estén corriendo

```bash
docker compose -f docker-compose-local.yml ps
```

Todos deben mostrar estado `Up`.

### 4. Rebuild con cambios de código

```bash
docker compose -f docker-compose-local.yml up -d --build
```

### 5. Detener el stack

```bash
docker compose -f docker-compose-local.yml down
```

---

## Variables de Entorno

Crear `Backend/.env` basado en `Backend/.env.example`:

```env
# JWT — generar con: openssl rand -hex 32
JWT_SECRET=<tu-secret-seguro>

# Passwords de bases de datos (por servicio)
SHIPPING_DB_PASSWORD=<password>
INVENTORY_DB_PASSWORD=<password>
ORDER_DB_PASSWORD=<password>
NOTIFICATION_DB_PASSWORD=<password>
AUTH_DB_PASSWORD=<password>
USERS_DB_PASSWORD=<password>

# RabbitMQ
RABBITMQ_USER=guest
RABBITMQ_PASS=guest

# SMTP para notificaciones por email
SMTP_HOST=sandbox.smtp.mailtrap.io
SMTP_PORT=2525
SMTP_USER=<tu-user-mailtrap>
SMTP_PASS=<tu-pass-mailtrap>
MAIL_FROM=noreply@smartlogix.cl
```

> **IMPORTANTE:** Nunca commitear el archivo `.env`. Ya está incluido en `.gitignore`.

---

## Documentación API (Swagger)

El **API Gateway agrega todos los servicios en un único Swagger UI**:

> ### http://localhost:8080/swagger-ui.html

Desde ahí se puede cambiar entre servicios con el selector desplegable:
`ms-auth` · `ms-order` · `ms-inventory` · `ms-shipping` · `ms-notification` · `ms-users`

Cada servicio también expone su Swagger de forma independiente:

| Servicio | Swagger UI directo |
| :--- | :--- |
| Inventario | http://localhost:8081/swagger-ui/index.html |
| Órdenes | http://localhost:8082/swagger-ui/index.html |
| Usuarios | http://localhost:8083/swagger-ui/index.html |
| Envíos | http://localhost:8084/swagger-ui/index.html |
| Notificaciones | http://localhost:8085/swagger-ui/index.html |
| Autenticación | http://localhost:8086/swagger-ui/index.html |
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Management | http://localhost:15672 `guest / guest` |

Todos los endpoints siguen la convención `/smartlogix/{servicio}/{módulo}`.

---

## Sistema de Roles (`ms-users`)

`ms-users` implementa un catálogo de roles y una tabla join `user_role` entre perfiles y roles.

### Roles disponibles

| Rol | Descripción |
| :--- | :--- |
| `ADMIN` | Propietario/administrador de la empresa |
| `OPERATOR` | Operador logístico |
| `DRIVER` | Conductor/transportista |
| `VIEWER` | Acceso solo lectura |

> Los roles se inicializan automáticamente al arrancar el servicio (`DataInitializer`). No se crean por API.

### Endpoints de Roles

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `/smartlogix/users/roles` | Lista el catálogo completo de roles |
| `POST` | `/smartlogix/users/profiles/company/{id}/admin` | Registra empresa — crea perfil con rol `ADMIN` automático |
| `POST` | `/smartlogix/users/profiles/company/{id}` | Admin crea empleado — roles explícitos en el body |
| `PUT` | `/smartlogix/users/profiles/{id}/roles` | Actualiza roles de un perfil existente |

### Flujo de negocio

```
Registro empresa
  POST /smartlogix/users/companies
  └─▶ POST /smartlogix/users/profiles/company/{id}/admin
        body: { authId, firstName, lastName }
        → rol ADMIN asignado automáticamente

Admin crea empleado
  POST /smartlogix/users/profiles/company/{id}
  body: { authId, firstName, lastName, "roles": ["OPERATOR"] }
  → roles explícitos, validados contra catálogo en BD

Admin reasigna roles
  PUT /smartlogix/users/profiles/{profileId}/roles
  body: ["DRIVER", "VIEWER"]
```

---

## Direcciones Geográficas (`ms-order`)

Las órdenes creadas desde el frontend usan un modelo de dirección estructurado en vez de texto libre, compuesto por una calle y una referencia a la comuna oficial chilena.

### Modelo de datos

```
Pais (1)
 └── Region (16 regiones de Chile)
       └── Comuna (346 comunas)
             └── Order.street (texto libre: "Av. Providencia 1234")
```

Las tablas `pais`, `region` y `comuna` se pueblan automáticamente al **primer arranque del contenedor** mediante `data.sql` con `INSERT ... ON CONFLICT DO NOTHING` — idempotente en arranques sucesivos.

### Endpoints para selectores del frontend

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `/smartlogix/order/regiones` | Lista las 16 regiones de Chile |
| `GET` | `/smartlogix/order/comunas?regionId={id}` | Lista las comunas de una región |

### Crear orden (POST /smartlogix/order/orders)

El campo `shippingAddress` de texto libre fue reemplazado por:

```json
{
  "customerName": "Juan Pérez",
  "customerEmail": "juan@example.com",
  "street": "Av. Providencia 1234",
  "comunaId": 13123,
  "items": [...]
}
```

La respuesta incluye los campos desglosados:

```json
{
  "street": "Av. Providencia 1234",
  "comunaId": 13123,
  "comunaNombre": "Providencia",
  "regionNombre": "Región Metropolitana de Santiago"
}
```

> Los eventos internos de RabbitMQ (`OrderEvent.shippingAddress`) siguen usando una cadena derivada con el formato `"{calle}, {comuna}, {región}, Chile"`, compatible con la geocodificación Nominatim usada por `ms-shipping`.

---

## Pruebas Unitarias

Los tests se corren por servicio. Requieren Java 21 y Maven instalados localmente (no necesitan Docker).

### Correr todos los tests de un servicio

```bash
# Inventario (10 tests)
cd Backend/inventory && mvn clean test

# Órdenes (13 tests)
cd Backend/order && mvn clean test

# Envíos (24 tests — ShipmentService + RouteService)
cd Backend/shipping && mvn clean test

# Notificaciones (13 tests — NotificationService + OrderEventListener)
cd Backend/notification && mvn clean test

# Usuarios (11 tests — CompanyService + UserProfileService)
cd Backend/users && mvn clean test
```

### Correr todos de una vez (desde Backend/)

```bash
cd Backend
for service in inventory order shipping notification users; do
  echo "=== Testing $service ==="
  (cd $service && mvn clean test -q)
done
```

### Cobertura actual

| Servicio | Tests | Clases cubiertas |
| :--- | :---: | :--- |
| `ms-inventory` | 10 | `InventoryReservationService` |
| `ms-order` | 13 | `OrderService` |
| `ms-shipping` | 24 | `ShipmentService`, `RouteService` |
| `ms-notification` | 13 | `NotificationService`, `OrderEventListener` |
| `ms-users` | 11 | `CompanyService`, `UserProfileService` |
| **Total** | **71** | |

---

## Convenciones de Código

| Regla | Detalle |
| :--- | :--- |
| **Rutas API** | `/smartlogix/{servicio}/{módulo}` — nunca `/api/...` |
| **Respuestas HTTP** | Siempre `MessageResponse<T>` con `statusCode`, `message`, `data` |
| **IDs de entidades de negocio** | UUID (`String`) — nunca `Long` o `Integer` |
| **IDs de tablas de referencia** | Integer con ID natural (ej. comunas/regiones usan IDs oficiales SINIM) |
| **Borrado** | Soft delete por cambio de estado — prohibido `repository.delete()` |
| **Mapeo DTOs** | Solo MapStruct `@Mapper(componentModel = "spring")` — prohibido mapeo manual |
| **Cascade** | Solo `PERSIST` y `MERGE` — prohibido `ALL` y `REMOVE` |
| **Secretos** | Variables de entorno — prohibido hardcodear en `application.yml` o compose |

---

## Actualizaciones Recientes

- Frontend operativo en `Frontend/smartlogix-app` con React, Vite, TypeScript, Zustand y Vitest.
- El modulo de inventario ahora opera por empresa autenticada: productos, bodegas y stock se filtran con `X-Company-Id` inyectado por el API Gateway desde el JWT.
- Productos y bodegas pueden crearse y editarse desde el frontend. El backend ignora `companyId` enviado en el body y usa siempre la empresa autenticada.
- El seed Prisma vive en `Backend/prisma` y puede poblar usuarios demo, productos, bodegas y stock local.

### Carga de Datos Demo

```bash
cd Backend/prisma
npm install
npm run seed:all
```

Variables usadas por Prisma:

```env
USERS_DATABASE_URL=postgresql://postgres:postgres@localhost:5437/db_users?schema=public
INVENTORY_DATABASE_URL=postgresql://postgres:postgres@localhost:5433/smartlogix_inventory?schema=public
```

Frontend local:

```bash
cd Frontend/smartlogix-app
npm install
npm run dev
```

URL: http://localhost:5173
