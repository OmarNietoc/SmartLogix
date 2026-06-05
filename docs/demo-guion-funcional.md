# Guion de demo funcional SmartLogix

## 1. Levantar infraestructura

- Objetivo: iniciar bases PostgreSQL, RabbitMQ, Eureka, Gateway y microservicios.
- URL local: no aplica.
- Comando: `docker compose up -d --build`.
- Que debe observar el docente: contenedores iniciados sin errores criticos.
- Que decir: "La demo usa Docker Compose para reproducir la infraestructura completa con una base por servicio y RabbitMQ para eventos."

## 2. Abrir Eureka

- Objetivo: demostrar service discovery.
- URL local: `http://localhost:8761`.
- Comando: no aplica si Docker ya esta levantado.
- Que debe observar el docente: servicios `ms-auth`, `ms-users`, `ms-inventory`, `ms-order`, `ms-shipping`, `ms-notification` y `api-gateway`.
- Que decir: "Eureka permite que el Gateway resuelva servicios por nombre logico en vez de depender de URLs fijas."

## 3. Abrir Gateway Swagger

- Objetivo: mostrar contratos OpenAPI agregados desde el Gateway.
- URL local: `http://localhost:8080/swagger-ui.html`.
- Comando: no aplica.
- Que debe observar el docente: selector con APIs `ms-*`.
- Que decir: "El frontend y Postman consumen el Gateway; Swagger centraliza la documentacion tecnica de los servicios."

## 4. Abrir frontend

- Objetivo: mostrar la interfaz React.
- URL local: `http://localhost:5173`.
- Comando local alternativo: `cd Frontend/smartlogix-app && npm run dev`.
- Que debe observar el docente: aplicacion cargada y conectada al Gateway.
- Que decir: "El frontend usa `VITE_API_URL` para enviar todas las llamadas a `http://localhost:8080`."

## 5. Login

- Objetivo: obtener sesion y JWT.
- URL local: `http://localhost:5173`.
- Comando: no aplica.
- Que debe observar el docente: login exitoso y acceso a pantalla principal.
- Que decir: "El servicio auth emite JWT y el Gateway protege rutas de negocio con Bearer token."

## 6. Mostrar dashboard o pantalla principal

- Objetivo: evidenciar navegacion funcional.
- URL local: `http://localhost:5173`.
- Comando: no aplica.
- Que debe observar el docente: opciones operativas para ordenes, inventario, envios o rutas.
- Que decir: "La pantalla principal permite operar el flujo logistico desde una sola interfaz."

## 7. Crear o consultar orden

- Objetivo: demostrar el flujo principal del dominio.
- URL local: frontend o `http://localhost:8080/smartlogix/order/orders`.
- Comando Postman opcional: `POST /smartlogix/order/orders`.
- Que debe observar el docente: orden creada o listado de ordenes.
- Que decir: "Order persiste la orden y publica eventos para continuar la saga de reserva y despacho."

## 8. Mostrar impacto en inventario

- Objetivo: evidenciar reserva o consulta de stock.
- URL local: `http://localhost:8080/smartlogix/inventory/stocks`.
- Comando Postman opcional: `GET /smartlogix/inventory/stocks`.
- Que debe observar el docente: stock, reserva o movimiento asociado.
- Que decir: "Inventory gestiona stock y reservas; no comparte tablas con order, se coordina por eventos."

## 9. Mostrar flujo shipping

- Objetivo: mostrar despacho, tracking o rutas.
- URL local: `http://localhost:8080/smartlogix/shipping/shipments`.
- Comando Postman opcional: `GET /smartlogix/shipping/shipments`.
- Que debe observar el docente: shipment, tracking o ruta generada.
- Que decir: "Shipping aplica estrategia de carrier y usa circuit breaker/fallback para ruteo externo."

## 10. Mostrar notificacion

- Objetivo: demostrar feedback al cliente o usuario.
- URL local: `http://localhost:8080/smartlogix/notification/notifications`.
- Comando Postman opcional: `GET /smartlogix/notification/notifications`.
- Que debe observar el docente: notificaciones persistidas o correo preparado.
- Que decir: "Notification escucha eventos de orden y despacho para informar cambios de estado."

## 11. Mostrar Postman

- Objetivo: demostrar pruebas manuales reproducibles de API.
- URL local: no aplica.
- Comando: importar `docs/postman/SmartLogix.postman_collection.json` y environment.
- Que debe observar el docente: login, token guardado, requests protegidos y casos 400/404.
- Que decir: "Postman deja una evidencia independiente del frontend para validar contratos REST."

## 12. Mostrar reportes de cobertura

- Objetivo: evidenciar pruebas automatizadas.
- URL local: abrir archivos HTML locales.
- Comando: `mvn clean verify` por servicio y `npm run test:coverage`.
- Que debe observar el docente: reportes JaCoCo y Vitest coverage.
- Que decir: "La cobertura se mide por servicio; los reportes generados no se versionan porque `target/` y `coverage/` estan ignorados."

## 13. Explicar arquitectura

- Objetivo: conectar componentes y responsabilidades.
- URL local: no aplica.
- Comando: abrir `docs/01-arquitectura.md`.
- Que debe observar el docente: diagrama de arquitectura.
- Que decir: "SmartLogix separa dominios en microservicios y usa Gateway, Eureka y RabbitMQ para integracion."

## 14. Explicar persistencia

- Objetivo: defender Database per Service.
- URL local: no aplica.
- Comando: abrir `docs/02-persistencia.md`.
- Que debe observar el docente: tabla de bases y entidades JPA.
- Que decir: "Cada servicio tiene su propia base PostgreSQL y sus repositories JPA; no hay base monolitica compartida."

## 15. Explicar pruebas

- Objetivo: cerrar con calidad y riesgos.
- URL local: no aplica.
- Comando: abrir `README_TESTS.md` y `docs/evidencias/04_pruebas_cobertura/cobertura-final.md`.
- Que debe observar el docente: tabla de cobertura, rutas de reportes y comandos.
- Que decir: "Las pruebas cubren services, controllers, mappers, listeners y componentes frontend; las mejoras pendientes estan documentadas."
