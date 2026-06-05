# Checklist Postman

- [ ] Importar `docs/postman/SmartLogix.postman_collection.json`.
- [ ] Importar `docs/postman/SmartLogix.postman_environment.json`.
- [ ] Seleccionar environment `SmartLogix Local`.
- [ ] Confirmar `base_url = http://localhost:8080`.
- [ ] Ejecutar `Auth public/Register` si se necesita usuario nuevo.
- [ ] Ejecutar `Auth public/Login`.
- [ ] Verificar que se guarden `token` y `companyId`.
- [ ] Ejecutar una ruta protegida de `Inventory`.
- [ ] Ejecutar una ruta protegida de `Order`.
- [ ] Ejecutar una ruta protegida de `Shipping`.
- [ ] Ejecutar una ruta protegida de `Notification`.
- [ ] Ejecutar un caso 400 con body incompleto.
- [ ] Ejecutar un caso 404 con identificador inexistente.
- [ ] Tomar captura del resultado del Collection Runner.

No pegar tokens reales en documentos. Si se captura pantalla, ocultar el valor completo del JWT.
