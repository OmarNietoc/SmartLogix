# Presentacion final

Guion tecnico de 15 minutos:

1. Contexto: SmartLogix centraliza ordenes, inventario, envios, rutas, usuarios y notificaciones para operacion logistica.
2. Arquitectura: frontend React consume API Gateway, gateway enruta a microservicios registrados en Eureka.
3. Persistencia: database per service con PostgreSQL y JPA/Hibernate para independencia y escalabilidad.
4. Flujo principal: crear orden, publicar evento, reservar stock, generar envio/ruta y notificar.
5. Mensajeria: RabbitMQ permite coreografia Saga sin acoplar servicios por llamadas sincronas.
6. Seguridad: auth emite JWT y gateway protege endpoints de negocio.
7. Pruebas: JUnit/Mockito y Vitest, JaCoCo/V8 con meta >=60%.
8. CI/CD: GitHub Actions ejecuta tests, cobertura, build frontend y validacion Docker.
9. Operacion: Docker Compose levanta infraestructura y servicios.
10. Escalabilidad: cada servicio puede desplegarse y escalarse por separado.

Preguntas esperadas:

- Por que microservicios: separa dominios y permite evolucionar servicios independientemente.
- Por que JPA: reduce boilerplate, integra repositorios y gestiona entidades transaccionales.
- Por que Gateway: entrada unica, seguridad, rutas y simplificacion para frontend.
- Por que Eureka: discovery dinamico para evitar URLs fijas entre servicios.
- Por que RabbitMQ: eventos asincronos para resiliencia y bajo acoplamiento.
- Como aseguran cobertura: `mvn clean verify` ejecuta JaCoCo check y frontend usa `npm run test:coverage`.
- Como escalar: replicar servicios stateless detras del gateway y mantener base independiente por servicio.
