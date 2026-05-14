# Billing CA deploy

Expected server layout:

```text
/opt/billing-ca
  backend/
    plan-mng-web-*.jar
    config/
      application.yml
    logs/
  frontend/
    dist/
      index.html
  deploy/
    backend/
      config/
        application.yml
    backend.Dockerfile
    frontend.Dockerfile
    docker-compose.yml
    .env
    nginx.conf
```

Build artifacts before copying to the server:

```bash
cd backend
mvn clean package -DskipTests

cd ../frontend
npm ci
npm run build -- --outDir dist --emptyOutDir
```

Copy `backend/plan-mng-web/target/plan-mng-web-*.jar` into `/opt/billing-ca/backend/`.
Copy `frontend/dist` into `/opt/billing-ca/frontend/dist`.
Copy `deploy/backend/config/application.yml` into `/opt/billing-ca/backend/config/application.yml` if you want an external backend config file.

Before running, edit `/opt/billing-ca/deploy/.env` and replace the `change_me_*` values. Database variables are named `DB_*`; `docker-compose.yml` maps them to the `MYSQL_*` variables required by the MySQL image.

Run on the server:

```bash
cd /opt/billing-ca/deploy
mkdir -p ../backend/config ../backend/logs
docker compose --env-file .env up -d --build
```

Backend config files in `/opt/billing-ca/backend/config` are mounted read-only to `/app/config`.
Backend logs are written to `/opt/billing-ca/backend/logs`.
