# Evidencia 01 - Arquitectura

## Que evidencia debe ir aqui

- Captura del diagrama de arquitectura de microservicios.
- Captura del diagrama de despliegue local.
- Captura del diagrama de secuencia de creacion de orden.
- Captura de Eureka con servicios registrados.
- Captura de Swagger Gateway abierto en `http://localhost:8080/swagger-ui.html`.

## Como generarla

1. Levantar el sistema con `docker compose up -d --build` o levantar backend con `cd Backend && docker compose -f docker-compose-local.yml up -d --build`.
2. Abrir `http://localhost:8761` y capturar la lista de servicios.
3. Abrir `http://localhost:8080/swagger-ui.html` y capturar el selector de APIs `ms-*`.
4. Convertir los Mermaid desde `docs/diagrams/*.mmd` con Mermaid Live Editor, extension de VS Code o `mmdc`.

## Archivo o captura a usar

- Fuente: `docs/diagrams/arquitectura-microservicios.mmd`.
- Fuente: `docs/diagrams/despliegue.mmd`.
- Fuente: `docs/diagrams/secuencia-creacion-orden.mmd`.
- Si se genera localmente: guardar PNG/SVG en esta carpeta y en `docs/final/diagrams/`.

## Por que sirve para la evaluacion

Demuestra que el proyecto tiene frontend, API Gateway/BFF, Eureka, microservicios, RabbitMQ y persistencia separada por servicio. Es la evidencia principal para defender la arquitectura solicitada.
