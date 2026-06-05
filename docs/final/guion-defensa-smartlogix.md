# SmartLogix - Guion de defensa oral

## Apertura

SmartLogix es una plataforma fullstack de logistica. Permite gestionar usuarios, empresas, productos, inventario, ordenes, envios, rutas y notificaciones. El sistema se construyo con frontend React, API Gateway/BFF y microservicios Spring Boot.

## 1. Arquitectura

El frontend no llama directamente a los microservicios. Todas las solicitudes pasan por `Backend/api-gateway`, que centraliza rutas, CORS, JWT y Swagger.

Los microservicios se registran en Eureka:

- auth
- users
- inventory
- order
- shipping
- notification

La separacion permite mantener dominios independientes y escalar servicios de forma aislada.

## 2. Persistencia

Se aplica Database per Service. Cada servicio tiene su propia base PostgreSQL y sus propias entidades JPA.

Esto evita acoplamiento por base compartida. Cuando un servicio necesita coordinarse con otro, usa REST o eventos RabbitMQ.

## 3. Flujo principal

El flujo mas importante es la creacion de una orden:

1. El usuario crea la orden desde React.
2. El Gateway enruta la solicitud a `order`.
3. `order` guarda la orden y publica un evento.
4. `inventory` intenta reservar stock.
5. Si hay stock, se publica reserva confirmada.
6. `shipping` crea envio y ruta.
7. `notification` registra y envia notificaciones.

Este flujo muestra integracion sincronica y asincronica.

## 4. Seguridad

`auth` emite JWT. El Gateway valida el token en rutas protegidas y propaga informacion como `X-Company-Id` hacia los servicios.

Para la entrega se incluyen solo `.env.example`. Los `.env` reales no deben subirse ni empaquetarse.

## 5. Pruebas

Backend usa JUnit 5, Mockito, Spring Boot Test, MockMvc y JaCoCo. Frontend usa Vitest, Testing Library y coverage V8.

La cobertura global de lineas queda sobre 60% en los servicios medidos. La tabla final esta en `docs/evidencias/04_pruebas_cobertura/cobertura-final.md`.

## 6. Despliegue

Docker Compose levanta frontend, gateway, Eureka, microservicios, PostgreSQL por servicio y RabbitMQ.

Comando principal:

```bash
docker compose up -d --build
```

URLs para demo:

- Frontend: `http://localhost:5173`
- Gateway: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Eureka: `http://localhost:8761`
- RabbitMQ: `http://localhost:15672`

## Preguntas frecuentes

### Por que microservicios

Porque el dominio logistico esta dividido en responsabilidades independientes. Esto reduce acoplamiento y permite evolucionar servicios por separado.

### Por que API Gateway/BFF

Porque simplifica el frontend, centraliza seguridad, CORS, rutas y documentacion Swagger.

### Por que RabbitMQ

Porque el flujo de ordenes requiere coordinar reservas, envios y notificaciones sin acoplar servicios con llamadas sincronicas en cadena.

### Por que JPA

Porque integra entidades, repositories y transacciones con Spring Boot, reduciendo boilerplate.

### Que falta para produccion

Migraciones versionadas, health checks, observabilidad centralizada y seguridad interna mas estricta entre microservicios.

## Cierre

SmartLogix demuestra una arquitectura fullstack completa: frontend funcional, gateway, microservicios, persistencia por servicio, eventos, pruebas, cobertura y despliegue reproducible.
