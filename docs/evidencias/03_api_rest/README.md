# Evidencia 03 - API REST

## Que evidencia debe ir aqui

- Captura de Swagger UI del Gateway.
- Captura del login exitoso y token JWT.
- Captura de endpoint protegido con `Authorization: Bearer <token>`.
- Captura de creacion o consulta de orden.
- Captura de consulta de inventario.
- Captura de consulta de notificaciones.
- Captura de error controlado 400 o 404.
- Captura de la coleccion Postman ejecutada.

## Como generarla

1. Levantar backend y Gateway.
2. Abrir `http://localhost:8080/swagger-ui.html`.
3. Importar `docs/postman/SmartLogix.postman_collection.json`.
4. Importar `docs/postman/SmartLogix.postman_environment.json`.
5. Ejecutar `Auth public/Login` para guardar `token` y `companyId`.
6. Ejecutar requests protegidos usando el environment.

## Archivo o captura a usar

- `docs/api-endpoints.md`.
- `docs/postman/SmartLogix.postman_collection.json`.
- `docs/postman/SmartLogix.postman_environment.json`.
- Capturas de Postman Runner y Swagger UI.

## Por que sirve para la evaluacion

Demuestra contratos REST reales, seguridad con JWT, documentacion OpenAPI agregada desde Gateway y pruebas manuales reproducibles.
