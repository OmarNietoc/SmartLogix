# Evidencia 02 - Persistencia

## Que evidencia debe ir aqui

- Capturas de entidades JPA por microservicio.
- Capturas de repositories Spring Data JPA.
- Captura del diagrama ER simplificado.
- Captura de las bases PostgreSQL levantadas en Docker.
- Captura de `Backend/docker-compose-local.yml` mostrando una base por servicio.

## Como generarla

1. Abrir `docs/02-persistencia.md` y revisar la tabla de bases.
2. Convertir `docs/diagrams/er-simplificado.mmd` a PNG/SVG.
3. Ejecutar `docker compose ps` si Docker esta disponible.
4. Abrir carpetas `Backend/<servicio>/src/main/java/.../model` y `repository` para mostrar JPA.

## Archivo o captura a usar

- `docs/diagrams/er-simplificado.mmd`.
- `Backend/docker-compose-local.yml`.
- `Backend/*/src/main/resources/application.yml`.
- `Backend/*/src/main/java/com/smartlogix/*/model`.
- `Backend/*/src/main/java/com/smartlogix/*/repository`.

## Por que sirve para la evaluacion

Permite evidenciar el patron Database per Service, el uso real de JPA/Hibernate y la independencia de datos entre microservicios.
