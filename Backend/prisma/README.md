# SmartLogix Prisma Seed

Seed local para poblar datos demo en las bases PostgreSQL usadas por Docker Compose.

## Uso

```bash
cd Backend/prisma
npm install
npm run seed:all
```

El seed usa:

- `USERS_DATABASE_URL`: `postgresql://postgres:postgres@localhost:5437/db_users?schema=public`
- `INVENTORY_DATABASE_URL`: `postgresql://postgres:postgres@localhost:5433/smartlogix_inventory?schema=public`

## Credenciales demo del frontend

Estas credenciales son validadas por el frontend hasta que `ms-auth` implemente endpoints reales:

- `admin@smartlogix.cl` / `demo1234`
- `operador@smartlogix.cl` / `demo1234`
- `conductor@smartlogix.cl` / `demo1234`

El backend actual no tiene tabla de credenciales en `ms-auth`; el seed crea perfiles en `ms-users` con `auth_id` igual al correo.
