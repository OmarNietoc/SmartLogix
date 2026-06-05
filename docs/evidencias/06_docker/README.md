# Evidencia 06 - Docker

## Que evidencia debe ir aqui

- Captura de `docker compose config`.
- Captura de `cd Backend && docker compose -f docker-compose-local.yml config`.
- Captura de `docker compose ps` con contenedores levantados.
- Captura de RabbitMQ Management si se usa en la demo.

## Como generarla

```bash
docker compose config
cd Backend
docker compose -f docker-compose-local.yml config
```

Si Docker no esta disponible en el equipo de generacion, documentar el motivo y ejecutar en un equipo con Docker Desktop o Docker Engine.

## Archivo o captura a usar

- `docker-compose.yml`.
- `Backend/docker-compose-local.yml`.
- Capturas de consola.

## Por que sirve para la evaluacion

Valida que la entrega incluye infraestructura reproducible para frontend, Gateway, Eureka, microservicios, PostgreSQL por servicio y RabbitMQ.
