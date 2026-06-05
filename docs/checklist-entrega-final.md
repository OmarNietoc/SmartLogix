# Checklist de entrega final SmartLogix

## Obligatorio para cumplir la pauta

- [x] README general actualizado.
- [x] README por componente actualizado.
- [x] Frontend identificado y ejecutable.
- [x] API Gateway/BFF identificado y ejecutable.
- [x] Microservicios backend identificados.
- [x] Integracion REST documentada.
- [x] Persistencia JPA documentada.
- [x] Pruebas unitarias ejecutables.
- [x] Reportes de cobertura documentados.
- [x] Swagger/OpenAPI y Postman Collection documentados.
- [x] Diagramas de arquitectura disponibles en Mermaid.
- [x] `repositorios.txt` con enlace GitHub.

## Recomendado para nota maxima

- [x] Diagrama de arquitectura de microservicios.
- [x] Diagrama de despliegue.
- [x] Diagrama de secuencia del flujo principal.
- [x] Diagrama entidad-relacion simplificado.
- [x] Tabla de endpoints reales.
- [x] Postman environment con variables.
- [x] Informe de pruebas con metricas reales.
- [x] Guia de ejecucion paso a paso.
- [x] Guia de defensa oral.
- [x] Capturas funcionales o demo documentada.

## Extra 150%

- [ ] GitHub Actions verde.
- [x] Sonar configurado.
- [x] Docker Compose completo.
- [x] Swagger agregado desde Gateway.
- [ ] Reportes HTML JaCoCo y Vitest adjuntos como capturas de entrega.
- [ ] Health checks HTTP por servicio.
- [ ] Perfiles `local/test/prod`.
- [ ] ADRs de decisiones arquitectonicas.
- [x] Checklist por integrante para defensa.

## Archivos que deben ir en el ZIP/RAR

```text
README.md
README_TESTS.md
repositorios.txt
docker-compose.yml
Backend/
Frontend/
docs/
docs/postman/SmartLogix.postman_collection.json
docs/postman/SmartLogix.postman_environment.json
Backend/.env.example
Backend/users/.env.example
Backend/prisma/.env.example
Frontend/smartlogix-app/.env.example
```

## Archivos que NO deben ir en el ZIP/RAR

```text
.env
*.env.local
node_modules/
target/
dist/
build/
coverage/
.tools/
*.log
```

Si existen archivos `.env` reales en el workspace local, no imprimirlos ni adjuntarlos. Usar solo `.env.example`.

## Comandos finales de validacion

```bash
docker compose config
docker compose up -d --build
docker compose ps
```

```bash
cd Frontend/smartlogix-app
npm ci
npm run test:coverage
npm run build
```

```bash
cd Backend/auth && mvn clean verify
cd Backend/users && mvn clean verify
cd Backend/inventory && mvn clean verify
cd Backend/order && mvn clean verify
cd Backend/shipping && mvn clean verify
cd Backend/notification && mvn clean verify
cd Backend/api-gateway && mvn clean verify
cd Backend/eureka-server && mvn clean verify
```
