# ms-users

Microservicio responsable de empresas, perfiles de usuario, roles, transportistas externos e integraciones de marketplace de SmartLogix.

## Tecnologia

- Java 21
- Spring Boot 3.2.5
- Spring Web
- Spring Data JPA
- PostgreSQL
- Spring Cloud Eureka Client
- Springdoc OpenAPI
- MapStruct
- Lombok
- JaCoCo

## Puertos y URLs

- Puerto local: `8083`
- Swagger directo: `http://localhost:8083/swagger-ui.html`
- Gateway/BFF: `http://localhost:8080/smartlogix/users`
- OpenAPI via Gateway: `http://localhost:8080/api-docs/ms-users`

## Variables de entorno

| Variable | Uso |
|---|---|
| `DB_URL` | JDBC URL de PostgreSQL para `db_users` |
| `DB_USERNAME` | Usuario de base de datos |
| `DB_PASSWORD` | Password de base de datos |
| `SERVER_PORT` | Puerto HTTP del servicio |
| `EUREKA_URI` | URL del servidor Eureka |

Plantilla local recomendada: `Backend/users/.env.example`.

## Endpoints reales

Todas las rutas se publican a traves del Gateway con prefijo `http://localhost:8080`.

| Metodo | Ruta Gateway | Descripcion |
|---|---|---|
| `POST` | `/smartlogix/users/companies` | Crear empresa |
| `GET` | `/smartlogix/users/companies` | Listar empresas |
| `POST` | `/smartlogix/users/profiles/company/{companyId}/admin` | Crear perfil admin para una empresa |
| `POST` | `/smartlogix/users/profiles/company` | Crear perfil para la empresa autenticada |
| `PUT` | `/smartlogix/users/profiles/{profileId}/roles` | Asignar roles a un perfil |
| `GET` | `/smartlogix/users/profiles/company` | Listar perfiles de la empresa autenticada |
| `GET` | `/smartlogix/users/roles` | Listar roles disponibles |
| `POST` | `/smartlogix/users/carriers/company/{companyId}` | Crear transportista externo |
| `GET` | `/smartlogix/users/carriers/company/{companyId}` | Listar transportistas externos por empresa |
| `POST` | `/smartlogix/users/integrations/company/{companyId}` | Crear integracion marketplace |
| `GET` | `/smartlogix/users/integrations/company/{companyId}` | Listar integraciones marketplace por empresa |

Nota: cuando se accede por Gateway, las rutas protegidas requieren `Authorization: Bearer <token>`. El Gateway valida el JWT y propaga `X-Company-Id` al microservicio.

## Ejecucion

```bash
mvn spring-boot:run
mvn clean test
mvn clean verify
```

Con Docker Compose desde la raiz del backend:

```bash
cd Backend
docker compose -f docker-compose-local.yml up -d --build ms-users
```

## Pruebas y cobertura

```bash
mvn clean verify
```

Reporte JaCoCo:

```text
Backend/users/target/site/jacoco/index.html
Backend/users/target/site/jacoco/jacoco.xml
```
