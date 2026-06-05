# SmartLogix Prisma Seed

Herramienta auxiliar para poblar datos demo en bases PostgreSQL locales usadas por Docker Compose.

Este directorio no reemplaza la persistencia principal del backend. Los microservicios productivos usan JPA/Hibernate con Spring Data repositories. Prisma se usa solo como utilidad local de seed para datos de apoyo.

## Tecnologia

- Node.js
- Prisma
- PostgreSQL

## Variables de entorno

Usar `Backend/prisma/.env.example` como plantilla. No versionar `Backend/prisma/.env` real.

| Variable | Uso |
|---|---|
| `USERS_DATABASE_URL` | Conexion Prisma a `db_users` |
| `INVENTORY_DATABASE_URL` | Conexion Prisma a `smartlogix_inventory` |

Ejemplo seguro:

```env
USERS_DATABASE_URL=postgresql://postgres:<password>@localhost:5437/db_users?schema=public
INVENTORY_DATABASE_URL=postgresql://postgres:<password>@localhost:5433/smartlogix_inventory?schema=public
```

## Uso local

```bash
cd Backend/prisma
npm install
npm run seed:all
```

## Relacion con autenticacion

El microservicio real de autenticacion existe en `Backend/auth` y expone:

- `POST /smartlogix/auth/register`
- `POST /smartlogix/auth/login`

El registro crea credenciales en `ms-auth` y coordina la empresa/perfil admin con `ms-users`. Por lo tanto, las credenciales del sistema no deben documentarse como validadas solo por frontend.

## Alcance

- Puede crear datos demo de usuarios/inventario para pruebas locales.
- No debe usarse para justificar la persistencia principal ante la evaluacion.
- No debe incluir secretos ni archivos `.env` reales en el ZIP/RAR final.
