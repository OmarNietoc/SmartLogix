# ms-users

Microservicio de companias, perfiles, roles, carriers e integraciones.

- Puerto: `8083`
- Swagger directo: `http://localhost:8083/swagger-ui.html`
- Gateway: `http://localhost:8080/smartlogix/users`
- Variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `EUREKA_URI`

Endpoints principales:

- `GET/POST /companies`
- `GET/POST /companies/{companyId}/profiles`
- `GET /roles`
- `GET/POST /external-carriers`
- `GET/POST /marketplace-integrations`

Comandos:

```bash
mvn spring-boot:run
mvn clean test
mvn clean verify
```

Cobertura: `target/site/jacoco/index.html`.
