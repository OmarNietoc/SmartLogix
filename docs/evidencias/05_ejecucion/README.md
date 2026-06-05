# Evidencia 05 - Ejecucion

## Que evidencia debe ir aqui

- Captura de frontend en `http://localhost:5173`.
- Captura de login.
- Captura del dashboard o pantalla principal.
- Captura de creacion o consulta de orden.
- Captura del flujo de inventario, shipping y notificaciones.
- Captura de consola con servicios levantados.

## Como generarla

1. Ejecutar `docker compose up -d --build` desde la raiz, o levantar backend con `Backend/docker-compose-local.yml` y frontend con `npm run dev`.
2. Abrir `http://localhost:5173`.
3. Iniciar sesion.
4. Ejecutar el flujo descrito en `docs/demo-guion-funcional.md`.

## Archivo o captura a usar

- `docs/demo-guion-funcional.md`.
- Capturas del navegador.
- Capturas de consola con comandos ejecutados.

## Por que sirve para la evaluacion

Demuestra que el sistema no es solo documentacion: se ejecuta, permite operar el flujo principal y conecta frontend con Gateway y microservicios.
