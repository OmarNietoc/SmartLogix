# Evidencia 04 - Pruebas y cobertura

## Que evidencia debe ir aqui

- Capturas de `mvn clean verify` por microservicio.
- Capturas de resumen JaCoCo por servicio.
- Captura de `npm run test:coverage`.
- Captura de `Frontend/smartlogix-app/coverage/index.html`.
- Tabla final `cobertura-final.md`.

## Como generarla

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

## Archivo o captura a usar

- `Backend/auth/target/site/jacoco/index.html`.
- `Backend/users/target/site/jacoco/index.html`.
- `Backend/inventory/target/site/jacoco/index.html`.
- `Backend/order/target/site/jacoco/index.html`.
- `Backend/shipping/target/site/jacoco/index.html`.
- `Backend/notification/target/site/jacoco/index.html`.
- `Backend/eureka-server/target/site/jacoco/index.html`.
- `Backend/api-gateway/target/site/jacoco/index.html`.
- `Frontend/smartlogix-app/coverage/index.html`.

## Por que sirve para la evaluacion

Demuestra pruebas automatizadas, cobertura medible y cumplimiento del umbral academico. No versionar `target/` ni `coverage/`; solo usar esos reportes para capturas.
