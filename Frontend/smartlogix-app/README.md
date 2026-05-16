# SmartLogix Frontend

Aplicacion React + Vite + TypeScript para operar SmartLogix desde el API Gateway.

- Puerto: `5173`
- Variable principal: `VITE_API_URL=http://localhost:8080`
- API base: `http://localhost:8080`

Comandos:

```bash
npm ci
npm run dev
npm run lint
npm run test
npm run test:coverage
npm run build
```

Cobertura: `coverage/index.html`.

Flujos cubiertos: login/registro, dashboard operativo, creacion de ordenes, inventario, stock, envios y rutas.
