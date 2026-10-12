# Guía de despliegue — TechStore

Proyecto full stack: **backend Spring Boot 4 (API REST + JWT)** y **frontend Angular 21 (SPA)**.

```
┌────────────────────┐        HTTPS + JWT        ┌─────────────────────┐
│  Frontend (SPA)    │  ───────────────────────► │  Backend (API REST) │
│  Vercel / Netlify  │  ◄─────────────────────── │  Render / Railway   │
└────────────────────┘        JSON               └──────────┬──────────┘
                                                            │ JDBC
                                                 ┌──────────▼──────────┐
                                                 │  PostgreSQL (Neon / │
                                                 │  Railway / Render)  │
                                                 └─────────────────────┘
```

---

## 0. Requisitos previos

- Cuenta en **GitHub** (código subido).
- Cuenta en un host backend: **Render** o **Railway**.
- Cuenta en un host frontend: **Vercel** o **Netlify**.
- Una base **PostgreSQL** gestionada: **Neon**, **Railway** o **Render Postgres**.
- Localmente: Java 21 y Docker (opcional, para probar la imagen).

---

## 1. Base de datos PostgreSQL

1. Crea una instancia PostgreSQL (Neon/ Railway / Render).
2. Copia la cadena de conexión JDBC. Ejemplos:
   - Neon: `jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/techstore?sslmode=require`
   - Railway: `jdbc:postgresql://containers-us-west-1.railway.app:5432/railway`
3. Anota `usuario` y `password`.
4. (Opcional) Ejecuta el script `docs/sql/techstore_schema.sql` en esa base para precargar el esquema y los datos demo. Si no, Hibernate creará el esquema automáticamente (`DDL_AUTO=update`).

> Credenciales demo que crea `DataInitializer` al primer arranque:
> `admin@techstore.com / Admin123*` y `cliente@techstore.com / Cliente123*`.

---

## 2. Backend en Render (Docker)

El repositorio incluye un **Dockerfile** multi-etapa (JDK 21 → JRE).

1. En Render: **New → Web Service → Build and deploy from a Git repository**.
2. Selecciona el repositorio de TechStore.
3. Configuración:
   - **Environment**: `Docker`
   - **Dockerfile Path**: `./Dockerfile`
   - **Health Check Path**: `/actuator/health`
   - **Instance Type**: Free (o el que prefieras)
4. **Variables de entorno** (Environment → Add Environment Variable):

   | Nombre | Valor |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DB_URL` | `jdbc:postgresql://...:5432/techstore?sslmode=require` |
   | `DB_USER` | tu usuario de PostgreSQL |
   | `DB_PASSWORD` | tu contraseña de PostgreSQL |
   | `DDL_AUTO` | `update` |
   | `JWT_SECRET` | cadena aleatoria de 32+ caracteres |
   | `JWT_EXPIRATION` | `86400000` |
   | `CORS_ORIGINS` | `https://tu-frontend.vercel.app` (se completa en el paso 3) |

5. Deploy. Render construye la imagen y expone la API en `https://tu-servicio.onrender.com`.
6. Verifica: `https://tu-servicio.onrender.com/actuator/health` → `{"status":"UP"}` y `https://tu-servicio.onrender.com/swagger-ui.html`.

### Alternativa: Railway
- **New Project → Deploy from GitHub repo** (Railway detecta el Dockerfile).
- Agrega las mismas variables de entorno.
- Railway asigna el puerto vía `PORT`; la app ya lo lee (`server.port=${PORT:8080}`).

### Alternativa: build manual de la imagen
```bash
docker build -t techstore-backend .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:postgresql://host:5432/techstore" \
  -e DB_USER=postgres -e DB_PASSWORD=secret \
  -e JWT_SECRET="cambia-esto-en-produccion-por-algo-largo" \
  techstore-backend
```

> ⚠️ Genera un `JWT_SECRET` real (no el de ejemplo). La app no arranca con el valor por defecto en `prod` si lo cambias; usa >= 32 caracteres para HS256.

---

## 3. Frontend en Vercel

Incluye `frontend/vercel.json` con el *rewrite* de SPA.

1. **Import Project → GitHub** y selecciona el repositorio.
2. **Root Directory**: `frontend`.
3. Framework Preset: **Angular** (o **Other**). Build command `npm run build`, Output `dist/frontend/browser` (ya viene en `vercel.json`).
4. Antes del deploy, edita `frontend/src/environments/environment.prod.ts` con la URL real del backend:
   ```ts
   export const environment = {
     production: true,
     apiUrl: 'https://tu-servicio.onrender.com/api'
   };
   ```
5. Deploy → obtienes `https://tu-frontend.vercel.app`.
6. **Vuelve a Render** y actualiza `CORS_ORIGINS` con esa URL, luego redeploy del backend.

### Alternativa: Netlify
- Incluye `frontend/netlify.toml` (build + redirect SPA).
- **Add new site → Import from Git**.
- **Base directory**: `frontend`; el resto lo toma de `netlify.toml`.

---

## 4. Orden correcto de despliegue

1. Crear PostgreSQL.
2. Desplegar **backend** (con `CORS_ORIGINS` temporal `http://localhost:4200`).
3. Verificar health + Swagger.
4. Desplegar **frontend** con la `apiUrl` del backend.
5. Actualizar `CORS_ORIGINS` en el backend con el dominio del frontend y redeploy.

---

## 5. Variables de entorno — resumen

### Backend
| Variable | Obligatoria | Descripción |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Sí | `prod` |
| `DB_URL` | Sí | URL JDBC de PostgreSQL |
| `DB_USER` / `DB_PASSWORD` | Sí | Credenciales de la base |
| `JWT_SECRET` | Sí | Clave HS256 (32+ caracteres) |
| `JWT_EXPIRATION` | No | ms de validez del token (def. 86400000) |
| `DDL_AUTO` | No | `update` (def.) |
| `CORS_ORIGINS` | Sí | Dominio(s) del frontend, separados por coma |
| `PORT` | No | Puerto (lo inyecta el host) |

### Frontend
| Archivo | Variable | Descripción |
|---|---|---|
| `environment.prod.ts` | `apiUrl` | URL base del backend (`https://host/api`) |

---

## 6. Prueba post-despliegue (checklist)

- [ ] `GET /actuator/health` → `UP`.
- [ ] `POST /api/auth/login` con `admin@techstore.com / Admin123*` → devuelve `token`.
- [ ] Swagger UI abre y el botón **Authorize** acepta el token.
- [ ] El frontend carga el catálogo (sin errores de CORS en la consola).
- [ ] Login desde el frontend redirige al panel acorde al rol.
- [ ] Crear un pedido de prueba y verlo en **Mis pedidos**.

---

## 7. Notas y solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| Error CORS en el navegador | `CORS_ORIGINS` no incluye el dominio del frontend | Actualiza la variable y redeploy del backend |
| 401 tras login | `JWT_SECRET` distinto entre arranques | Fija `JWT_SECRET` en el entorno |
| `password authentication failed` | Credenciales PG incorrectas | Revisa `DB_USER`/`DB_PASSWORD`/`DB_URL` |
| La SPA da 404 al recargar rutas | Falta el rewrite | Usa `vercel.json` / `netlify.toml` incluidos |
| El backend tarda en despertar | Plan free de Render | Espera el arranque en frío (~50 s) |

> Los secretos **nunca** se suben al repositorio: todo se inyecta por variables de entorno.
