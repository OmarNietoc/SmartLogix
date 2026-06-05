# Checklist de entrega final SmartLogix

## Obligatorio para cumplir la pauta

- [ ] README general actualizado.
- [ ] README por componente actualizado.
- [ ] Frontend identificado y ejecutable.
- [ ] API Gateway/BFF identificado y ejecutable.
- [ ] Al menos dos microservicios backend identificados.
- [ ] Integracion REST documentada.
- [ ] Persistencia JPA documentada.
- [ ] Pruebas unitarias ejecutables.
- [ ] Reportes de cobertura disponibles.
- [ ] Swagger/OpenAPI o Postman Collection.
- [ ] Diagramas de arquitectura.
- [ ] `repositorios.txt` con enlace GitHub.

## Recomendado para nota maxima

- [ ] Diagrama de arquitectura de microservicios.
- [ ] Diagrama de despliegue.
- [ ] Diagrama de secuencia del flujo principal.
- [ ] Diagrama entidad-relacion simplificado.
- [ ] Tabla de endpoints reales.
- [ ] Postman environment con variables.
- [ ] Informe de pruebas con metricas reales.
- [ ] Guia de ejecucion paso a paso.
- [ ] Guia de defensa oral.
- [ ] Capturas funcionales o demo documentada.

## Extra 150%

- [ ] GitHub Actions verde.
- [ ] Sonar configurado.
- [ ] Docker Compose completo.
- [ ] Swagger agregado desde Gateway.
- [ ] Reportes HTML JaCoCo y Vitest adjuntos.
- [ ] Health checks HTTP por servicio.
- [ ] Perfiles `local/test/prod`.
- [ ] ADRs de decisiones arquitectonicas.
- [ ] Checklist por integrante para defensa.

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

Archivos `.env` reales detectados en workspace:

- `Backend/.env`
- `Backend/users/.env`
- `Backend/prisma/.env`

No imprimir ni adjuntar esos archivos.

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
