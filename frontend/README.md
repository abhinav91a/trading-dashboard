# React + Vite

## Run the dashboard locally

Sign-in requires PostgreSQL and the auth service on port 8085. From the
repository root, start PostgreSQL:

```bash
docker compose up -d postgres
```

On a new PostgreSQL data volume, this creates `auth_db` along with the other
service databases. If the volume was initialized before `auth_db` was added,
create it once by running `CREATE DATABASE auth_db;` while connected to the
`postgres` database.

In another shell, set a local JWT signing secret and start auth:

```bash
export JWT_SECRET="$(openssl rand -hex 32)"
cd services/auth-service
./mvnw spring-boot:run
```

Then start the frontend from `frontend/` with `npm run dev`. Its Vite proxy
sends `/api/auth` requests to `http://localhost:8085`.

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
