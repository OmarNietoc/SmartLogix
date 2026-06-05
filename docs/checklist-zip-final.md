# Checklist ZIP/RAR final

## Estructura esperada

```text
SmartLogix_entrega_final/
  01_documentacion/
  02_frontend/
  03_bff_api_gateway/
  04_microservicios/
  05_base_datos/
  06_pruebas/
  07_evidencias/
  08_postman_openapi/
  repositorios.txt
  README_ENTREGA.md
```

## Debe incluir

- [x] Codigo fuente frontend.
- [x] Codigo fuente de API Gateway.
- [x] Codigo fuente de microservicios.
- [x] `pom.xml` disponibles; usar Maven del sistema en servicios sin wrapper.
- [x] `package.json` y `package-lock.json`.
- [x] `application.yml` o `application.properties` sin secretos reales.
- [x] `.env.example`.
- [x] Documentacion en `docs/`.
- [x] Diagramas Mermaid.
- [x] Postman Collection y Environment.
- [x] Scripts de generacion y empaquetado.
- [x] `repositorios.txt`.
- [x] `README_ENTREGA.md`.

## Debe excluir

- [x] `.git/`.
- [x] `.env` reales.
- [x] `**/.env`.
- [x] `node_modules/`.
- [x] `target/`.
- [x] `dist/`.
- [x] `coverage/`.
- [x] `logs/`.
- [x] `*.log`.
- [x] `.tools/`.
- [x] ZIP/RAR previos.
- [x] Archivos temporales.

## Validacion antes de comprimir

```bash
git status --short
docker compose config
cd Backend && docker compose -f docker-compose-local.yml config
cd ../Frontend/smartlogix-app && npm run build && npm run test:coverage
```

Ejecutar `mvn clean verify` en cada servicio backend antes de capturar cobertura.

## Generacion automatica

```powershell
scripts/generate-docs.ps1
scripts/package-final.ps1
```

El ZIP queda en `dist/SmartLogix_entrega_final.zip`.
