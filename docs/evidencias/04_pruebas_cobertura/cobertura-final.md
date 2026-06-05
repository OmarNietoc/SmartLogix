# Cobertura final

Fuente inicial: reportes locales `jacoco.csv` y Surefire existentes antes de la validacion final de Fase 3. Si al ejecutar nuevamente cambia algun valor, actualizar esta tabla con los reportes regenerados.

| Servicio | Cobertura lineas | Estado | Ruta reporte | Comando usado | Tests | Observacion |
|---|---:|---|---|---|---:|---|
| auth | 82.67% | Cumple | `Backend/auth/target/site/jacoco/index.html` | `cd Backend/auth && mvn clean verify` | 18 | Servicio de autenticacion y JWT |
| users | 97.46% | Cumple | `Backend/users/target/site/jacoco/index.html` | `cd Backend/users && mvn clean verify` | 69 | Empresas, perfiles, roles e integraciones |
| inventory | 87.58% | Cumple | `Backend/inventory/target/site/jacoco/index.html` | `cd Backend/inventory && mvn clean verify` | 85 | Productos, bodegas, stock y reservas |
| order | 98.28% | Cumple | `Backend/order/target/site/jacoco/index.html` | `cd Backend/order && mvn clean verify` | 57 | Ordenes, items y eventos de saga |
| shipping | 82.44% | Cumple | `Backend/shipping/target/site/jacoco/index.html` | `cd Backend/shipping && mvn clean verify` | 80 | Envios, rutas y fallback de ruteo |
| notification | 90.78% | Cumple | `Backend/notification/target/site/jacoco/index.html` | `cd Backend/notification && mvn clean verify` | 49 | Notificaciones y correo |
| eureka-server | 66.67% | Cumple | `Backend/eureka-server/target/site/jacoco/index.html` | `cd Backend/eureka-server && mvn clean verify` | 2 | Infraestructura de discovery |
| api-gateway | 87.18% | Cumple | `Backend/api-gateway/target/site/jacoco/index.html` | `cd Backend/api-gateway && mvn clean verify` | 6 | Gateway/BFF, JWT y rutas |
| frontend | 79.77% | Cumple | `Frontend/smartlogix-app/coverage/index.html` | `cd Frontend/smartlogix-app && npm run test:coverage` | 26 | Vitest con coverage provider v8 |

Reportes que deben capturarse para la entrega:

- `Backend/users/target/site/jacoco/index.html`
- `Backend/inventory/target/site/jacoco/index.html`
- `Backend/order/target/site/jacoco/index.html`
- `Backend/shipping/target/site/jacoco/index.html`
- `Backend/notification/target/site/jacoco/index.html`
- `Backend/eureka-server/target/site/jacoco/index.html`
- `Backend/auth/target/site/jacoco/index.html`
- `Backend/api-gateway/target/site/jacoco/index.html`
- `Frontend/smartlogix-app/coverage/index.html`

No agregar los reportes HTML al repositorio si `.gitignore` los excluye. Usarlos como evidencia visual externa o capturas dentro del ZIP/RAR final solo si la pauta lo solicita expresamente.
