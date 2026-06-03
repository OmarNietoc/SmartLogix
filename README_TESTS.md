# SmartLogix — Guía de Tests Unitarios

## 1. Overview

| Componente | Framework | Cobertura objetivo | Tests existentes |
|---|---|---|---|
| api-gateway | JUnit 5 + Mockito | 60% | AuthenticationFilterTest, JwtUtilTest, RouteValidatorTest |
| auth | JUnit 5 + Mockito / @WebMvcTest | 60% | AuthServiceTest, AuthControllerTest, JwtUtilTest, ChileanRutValidatorTest |
| inventory | JUnit 5 + Mockito / @WebMvcTest | 60% | InventoryServiceTest, InventoryReservationServiceTest, ProductServiceTest, InventoryControllerTest, ProductControllerTest |
| notification | JUnit 5 + Mockito / @WebMvcTest | 60% | NotificationServiceTest, OrderEventListenerTest, NotificationControllerTest, EmailServiceTest |
| order | JUnit 5 + Mockito / @WebMvcTest | 60% | OrderServiceTest, OrderControllerTest |
| shipping | JUnit 5 + Mockito / @WebMvcTest | 60% | RouteServiceTest, ShipmentServiceTest, RouteControllerTest, ShipmentControllerTest |
| users | JUnit 5 + Mockito | 60% | CompanyServiceTest, UserProfileServiceTest, ExternalCarrierServiceTest, MarketplaceIntegrationServiceTest, RoleServiceTest |
| Frontend | Vitest + React Testing Library | 60% líneas/funciones, 50% ramas | authService, api, Auth, authServiceApi, RegionComunaSelector, smartlogixService, CreateOrder, useAuthStore |

Todos los tests de backend usan H2 (base de datos en memoria) cuando se requiere contexto de Spring. Los unit tests con Mockito no levantan contexto de aplicación.

---

## 2. Cómo correr los tests

### Backend (por cada microservicio)

```bash
# Situarse en el directorio del microservicio
cd Backend/auth        # o inventory, order, shipping, notification, users, api-gateway

# Correr todos los tests
mvn test

# Correr un test específico
mvn test -Dtest=AuthServiceTest

# Correr con reporte de cobertura JaCoCo
mvn test jacoco:report

# Ver reporte HTML
# Abrir: target/site/jacoco/index.html
```

### Frontend

```bash
cd Frontend/smartlogix-app

# Correr todos los tests (modo CI, sin watch)
npm test

# Correr con cobertura
npm run test:coverage

# Ver reporte HTML de cobertura
# Abrir: coverage/index.html
```

---

## 3. Cómo generar reportes de cobertura

### Backend — JaCoCo

JaCoCo está configurado en todos los `pom.xml` de los microservicios. Se activa automáticamente con `mvn test jacoco:report`.

Reporte generado en: `target/site/jacoco/index.html`

Para generar todos los reportes a la vez desde la raíz:

```bash
# Requiere que cada microservicio sea un módulo Maven o correrlo individualmente
cd Backend/auth && mvn test jacoco:report
cd ../inventory && mvn test jacoco:report
cd ../order && mvn test jacoco:report
cd ../shipping && mvn test jacoco:report
cd ../notification && mvn test jacoco:report
cd ../users && mvn test jacoco:report
cd ../api-gateway && mvn test jacoco:report
```

### Frontend — Vitest v8

La cobertura usa `@vitest/coverage-v8` configurado en `vitest.config.ts`:

- Provider: `v8`
- Umbrales: 60% líneas, funciones y statements; 50% ramas
- Archivos incluidos: `src/services/**`, `src/components/**`, `src/pages/Auth.tsx`

```bash
npm run test:coverage
# Reporte en: coverage/index.html
```

---

## 4. Resultados de cobertura (llenar post-ejecución)

| Microservicio | Líneas | Funciones | Ramas | Statements | ¿Cumple 60%? |
|---|---|---|---|---|---|
| api-gateway | — | — | — | — | — |
| auth | — | — | — | — | — |
| inventory | — | — | — | — | — |
| notification | — | — | — | — | — |
| order | — | — | — | — | — |
| shipping | — | — | — | — | — |
| users | — | — | — | — | — |
| Frontend | — | — | — | — | — |

> Ejecutar `mvn test jacoco:report` en cada microservicio y `npm run test:coverage` en el frontend para obtener los porcentajes reales.

---

## 5. Patrones de diseño en tests

| Patrón | Dónde se prueba | Archivo de test |
|---|---|---|
| **Strategy** | ShippingCalculationStrategy (DhlStrategy vs LocalCarrierStrategy) | `shipping/RouteServiceTest.java` |
| **Repository** | Acceso a datos via Spring Data JPA en todos los servicios | `*ServiceTest.java` (todos los microservicios) |
| **Facade** | Services encapsulan lógica de repos, mappers y eventos | `OrderServiceTest.java`, `NotificationServiceTest.java` |
| **Observer / Event-Driven** | Listeners RabbitMQ en notification | `OrderEventListenerTest.java` |
| **Circuit Breaker** | EmailService con Resilience4j fallback | `EmailServiceTest.java` |
| **JWT / Token** | Generación y validación de tokens en auth y gateway | `JwtUtilTest.java` (auth y api-gateway) |

---

## 6. Convenciones del proyecto

### Backend
- Anotación unit: `@ExtendWith(MockitoExtension.class)` + `@InjectMocks` + `@Mock`
- Anotación controller: `@WebMvcTest(XController.class)` + `@Import(SecurityConfig.class)` + `@MockBean XService`
- Estructura: `// arrange` → `// act` → `// assert`
- Paquete del test = paquete de la clase bajo test

### Frontend
- Tests en: `src/__tests__/`
- Mocks con `vi.mock()` para servicios y API
- `vi.clearAllMocks()` en `beforeEach`
- Setup global: `src/__tests__/setup.ts` importa `@testing-library/jest-dom`
