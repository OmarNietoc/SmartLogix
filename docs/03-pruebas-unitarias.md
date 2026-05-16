# Pruebas Unitarias

Estrategia: tests unitarios con JUnit 5 y Mockito en servicios, validadores, mappers con logica, listeners y utilidades. Para frontend se usa Vitest, Testing Library y coverage provider `v8`.

Comandos backend:

```bash
cd Backend/inventory && mvn clean verify
cd Backend/order && mvn clean verify
cd Backend/shipping && mvn clean verify
cd Backend/notification && mvn clean verify
cd Backend/users && mvn clean verify
cd Backend/auth && mvn clean verify
cd Backend/api-gateway && mvn clean verify
cd Backend/eureka-server && mvn clean verify
```

Comandos frontend:

```bash
cd Frontend/smartlogix-app
npm ci
npm run test:coverage
npm run build
```

Reportes:

- Backend: `target/site/jacoco/index.html` por servicio.
- Frontend: `Frontend/smartlogix-app/coverage/index.html`.

| Componente | Comando | Tipo de pruebas | Meta |
| --- | --- | --- | --- |
| auth | `mvn clean verify` | servicio, JWT, RUT, contexto | >=60% lineas |
| users | `mvn clean verify` | servicios de compania/perfil | >=60% lineas |
| inventory | `mvn clean verify` | servicios producto/reserva | >=60% lineas |
| order | `mvn clean verify` | servicio, validaciones, estado | >=60% lineas |
| shipping | `mvn clean verify` | servicios, estrategia, eventos | >=60% lineas |
| notification | `mvn clean verify` | servicio y listeners | >=60% lineas |
| api-gateway | `mvn clean verify` | JWT y rutas seguras | >=60% lineas |
| frontend | `npm run test:coverage` | API services y componentes | >=60% lineas |
