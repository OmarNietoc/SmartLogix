# Checklist Swagger/OpenAPI

- [ ] Levantar Eureka, Gateway y microservicios.
- [ ] Abrir `http://localhost:8080/swagger-ui.html`.
- [ ] Confirmar que aparezcan APIs `ms-auth`, `ms-users`, `ms-inventory`, `ms-order`, `ms-shipping` y `ms-notification`.
- [ ] Abrir `/api-docs/ms-auth`.
- [ ] Abrir `/api-docs/ms-users`.
- [ ] Abrir `/api-docs/ms-inventory`.
- [ ] Abrir `/api-docs/ms-order`.
- [ ] Abrir `/api-docs/ms-shipping`.
- [ ] Abrir `/api-docs/ms-notification`.
- [ ] Probar login desde Swagger o mostrarlo en Postman.
- [ ] Probar un endpoint protegido con Bearer token.
- [ ] Capturar un error 400 o 404 controlado.

Evidencia tecnica: el Gateway configura estas rutas en `Backend/api-gateway/src/main/resources/application.yml`.
