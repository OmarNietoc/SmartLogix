# SmartLogix - Informe final

## Resumen ejecutivo

SmartLogix es una plataforma fullstack de logistica orientada a gestion de usuarios, empresas, productos, inventario, ordenes, rutas, envios y notificaciones. La solucion combina frontend React/Vite, API Gateway/BFF, microservicios Spring Boot, PostgreSQL por servicio y mensajeria RabbitMQ.

## Objetivo del sistema

El objetivo es centralizar el flujo logistico principal de una orden:

1. Registrar o autenticar usuarios.
2. Crear empresas, perfiles y roles.
3. Administrar productos, bodegas e inventario.
4. Crear ordenes con items y destino geografico.
5. Reservar stock.
6. Generar envio y ruta.
7. Notificar cambios de estado.

## Arquitectura

La arquitectura se separa por dominios funcionales:

| Capa | Componente | Responsabilidad |
|---|---|---|
| Presentacion | `Frontend/smartlogix-app` | Interfaz React/Vite/TypeScript |
| Entrada backend | `Backend/api-gateway` | Gateway/BFF, CORS, JWT, rutas y Swagger |
| Discovery | `Backend/eureka-server` | Registro y descubrimiento de servicios |
| Dominio | `auth`, `users`, `inventory`, `order`, `shipping`, `notification` | Reglas de negocio por contexto |
| Infraestructura | PostgreSQL y RabbitMQ | Persistencia y eventos asincronos |

El frontend consume solo el API Gateway. El Gateway enruta a servicios registrados en Eureka mediante rutas `lb://ms-*`. La comunicacion sincronica se usa para operaciones REST y la asincronica para el flujo de ordenes, reservas, envios y notificaciones.

## Patrones aplicados

- API Gateway/BFF como entrada unica para frontend.
- MVC en controladores REST.
- Service Layer para reglas de negocio.
- Repository con Spring Data JPA.
- DTO/Mapper para contratos de entrada y salida.
- Database per Service para independencia de datos.
- Saga coreografiada con eventos RabbitMQ.
- Strategy en calculo de envios.

## Persistencia

Cada microservicio tiene su propia base PostgreSQL:

| Servicio | Base |
|---|---|
| auth | `authdb` |
| users | `db_users` |
| inventory | `smartlogix_inventory` |
| order | `orderdb` |
| shipping | `shipping_db` |
| notification | `notificationdb` |

Las entidades principales estan en paquetes `model` y se acceden mediante repositories JPA. El servicio `order` incluye `data.sql` para datos geograficos de pais, regiones y comunas.

## API REST y contratos

La evidencia de endpoints esta en:

- `docs/api-endpoints.md`
- `docs/postman/SmartLogix.postman_collection.json`
- `docs/postman/SmartLogix.postman_environment.json`

El Gateway expone Swagger/OpenAPI agregado desde `http://localhost:8080/swagger-ui.html`.

## Flujo principal

1. El usuario crea una orden desde el frontend.
2. El frontend envia la solicitud al Gateway.
3. El Gateway enruta hacia `order`.
4. `order` guarda la orden y publica el evento de creacion.
5. `inventory` reserva stock y publica confirmacion o rechazo.
6. `shipping` genera envio/ruta cuando hay reserva confirmada.
7. `notification` registra y envia notificaciones por estado.

## Pruebas y cobertura

La estrategia de pruebas cubre servicios, controllers, mappers, listeners/eventos, seguridad y frontend.

Backend:

```bash
mvn clean verify
```

Frontend:

```bash
npm run test:coverage
npm run build
```

La tabla auditada esta en `docs/evidencias/04_pruebas_cobertura/cobertura-final.md`. La cobertura global de lineas registrada supera el 60% en los servicios medidos.

## Despliegue

La demo local se ejecuta con Docker Compose:

```bash
docker compose up -d --build
```

URLs principales:

| Recurso | URL |
|---|---|
| Frontend | `http://localhost:5173` |
| API Gateway | `http://localhost:8080` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| Eureka | `http://localhost:8761` |
| RabbitMQ | `http://localhost:15672` |

## Seguridad de entrega

El ZIP/RAR final no debe incluir `.env` reales, tokens, passwords, claves JWT, credenciales SMTP, `target`, `coverage`, `dist`, `node_modules`, `.git` ni logs. Solo deben incluirse plantillas `.env.example`.

## Riesgos y mejoras

- Reemplazar `ddl-auto` por migraciones Flyway/Liquibase en produccion.
- Agregar health checks HTTP por servicio con Actuator.
- Endurecer seguridad interna entre microservicios fuera de entorno local.
- Mejorar observabilidad centralizada.
- Subir cobertura de ramas donde sea necesario para una metrica mas estricta.

## Conclusion

SmartLogix cumple con una entrega fullstack basada en microservicios, persistencia independiente por servicio, integracion REST/eventos, gateway centralizado, pruebas automatizadas y documentacion operativa. La solucion es defendible academicamente porque conecta arquitectura, implementacion, pruebas y despliegue con evidencias concretas del repositorio.
