# SmartLogix - README de entrega academica

## Nombre del proyecto

SmartLogix.

## Objetivo

Plataforma fullstack de logistica para gestionar autenticacion, usuarios, inventario, ordenes, envios, rutas y notificaciones mediante frontend React, API Gateway/BFF y microservicios Spring Boot.

## Arquitectura

El frontend consume solo el API Gateway/BFF. El Gateway enruta a microservicios registrados en Eureka. Los servicios se integran por REST y eventos RabbitMQ. Cada microservicio persiste en su propia base PostgreSQL.

## Componentes

| Componente | Ruta | Responsabilidad |
|---|---|---|
| Frontend | `Frontend/smartlogix-app` | Interfaz React/Vite/TypeScript |
| API Gateway/BFF | `Backend/api-gateway` | Entrada unica, CORS, JWT, rutas y Swagger |
| Eureka | `Backend/eureka-server` | Service discovery |
| auth | `Backend/auth` | Registro, login y JWT |
| users | `Backend/users` | Empresas, usuarios, roles e integraciones |
| inventory | `Backend/inventory` | Productos, bodegas, stock y reservas |
| order | `Backend/order` | Ordenes y eventos de saga |
| shipping | `Backend/shipping` | Envios, rutas y tracking |
| notification | `Backend/notification` | Notificaciones y correo |

## Tecnologias

Java 21, Spring Boot 3, Spring Cloud Gateway, Eureka, Spring Data JPA, PostgreSQL, RabbitMQ, React, Vite, TypeScript, Vitest, JaCoCo, Docker Compose y Postman.

## Ejecutar frontend

```bash
cd Frontend/smartlogix-app
npm ci
npm run dev
```

URL: `http://localhost:5173`.

## Ejecutar backend

```bash
cd Backend/auth
mvn spring-boot:run
```

Repetir por servicio si se ejecuta manualmente. Para demo completa se recomienda Docker Compose.

## Ejecutar Docker

Todo el sistema:

```bash
docker compose up -d --build
```

Solo backend:

```bash
cd Backend
docker compose -f docker-compose-local.yml up -d --build
```

URLs principales:

- Frontend: `http://localhost:5173`
- Gateway: `http://localhost:8080`
- Swagger Gateway: `http://localhost:8080/swagger-ui.html`
- Eureka: `http://localhost:8761`
- RabbitMQ: `http://localhost:15672`

## Ejecutar pruebas

Backend:

```bash
cd Backend/auth && mvn clean verify
cd ../users && mvn clean verify
cd ../inventory && mvn clean verify
cd ../order && mvn clean verify
cd ../shipping && mvn clean verify
cd ../notification && mvn clean verify
cd ../eureka-server && mvn clean verify
cd ../api-gateway && mvn clean verify
```

Frontend:

```bash
cd Frontend/smartlogix-app
npm run test:coverage
npm run build
```

## Reportes

- JaCoCo backend: `Backend/<servicio>/target/site/jacoco/index.html`.
- Vitest frontend: `Frontend/smartlogix-app/coverage/index.html`.
- Tabla final: `docs/evidencias/04_pruebas_cobertura/cobertura-final.md`.

## Postman

- Collection: `docs/postman/SmartLogix.postman_collection.json`.
- Environment: `docs/postman/SmartLogix.postman_environment.json`.

## Diagramas

- Mermaid: `docs/diagrams/*.mmd`.
- Instrucciones de conversion: `docs/final/README_GENERAR_PDFS.md`.

## PDFs

Fuentes Markdown finales:

- `docs/final/informe-final-smartlogix.md`
- `docs/final/guion-defensa-smartlogix.md`
- `docs/final/README_GENERAR_PDFS.md`

Para generar Markdown compilado, HTML o PDF si `pandoc` esta disponible:

```bash
scripts/generate-docs.ps1
```

o:

```bash
bash scripts/generate-docs.sh
```

## Empaquetado ZIP/RAR

Para preparar un ZIP seguro de entrega:

```powershell
scripts/package-final.ps1
```

o:

```bash
bash scripts/package-final.sh
```

Salida esperada:

```text
dist/SmartLogix_entrega_final.zip
```

El empaquetado excluye `.git`, `.env` reales, `node_modules`, `target`, `dist`, `coverage`, logs y paquetes previos.

## Enlaces GitHub

Ver `repositorios.txt`.

## Seguridad

No incluir `.env` reales, tokens, passwords, claves JWT, credenciales SMTP ni secretos en GitHub ni en el ZIP/RAR. Usar solo `.env.example`.
