# TTJobs Quick Start On Linux

## 1. Prepare Server

Install Docker and Docker Compose plugin:

```bash
sudo apt update
sudo apt install -y ca-certificates curl git
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker "$USER"
newgrp docker
docker compose version
```

## 2. Clone And Configure

```bash
git clone <your-repo-url> ttjobs
cd ttjobs
cp .env.example .env
nano .env
```

Replace at minimum:

```env
POSTGRES_PASSWORD=...
JWT_SECRET=...
TTJOBS_PUBLIC_URL=https://your-domain.com
TTJOBS_APP_BASE_URL=https://your-domain.com
TTJOBS_CORS_ALLOWED_ORIGINS=https://your-domain.com
VITE_ENABLE_DEMO_FALLBACK=false
```

Keep email disabled until SMTP is ready:

```env
TTJOBS_EMAIL_ENABLED=false
```

## 3. Check AI Model Folders

Expected paths:

```text
ai-service/models/cv-jd-matcher
ai-service/cv-job-matcher
```

Optional category classifier:

```text
ai-service/models/category-classifier
```

If the optional category classifier is missing, `/ai/predict` degrades, but `/ai/predict-match` can still work if `cv-jd-matcher` exists.

## 4. Start Production Stack

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
docker compose -f docker-compose.prod.yml ps
```

Check logs:

```bash
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml logs -f nginx
docker compose -f docker-compose.prod.yml logs -f ai-service
```

Health check:

```bash
curl -i http://localhost/actuator/health
curl -i http://localhost/
```

Nginx intentionally blocks public `/actuator/**`, `/openapi.yaml`, and Swagger routes. Container health checks call backend internally.

## 5. HTTPS Requirement

Interview camera/microphone requires HTTPS on production domains. Put this stack behind one of:

- Cloudflare proxy with SSL
- Nginx Proxy Manager
- Caddy
- A host-level Nginx with Let's Encrypt

If using another reverse proxy in front of the included Nginx, forward:

```text
/      -> ttjobs-frontend:80
/api   -> ttjobs-frontend:80
/ws    -> ttjobs-frontend:80 with WebSocket upgrade
```

## 6. Useful Commands

Restart:

```bash
docker compose -f docker-compose.prod.yml restart
```

Update:

```bash
git pull
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

Backup PostgreSQL:

```bash
docker exec ttjobs-postgres pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" > ttjobs-backup.sql
```

Stop:

```bash
docker compose -f docker-compose.prod.yml down
```

Do not run `down -v` unless you intentionally want to delete database volumes.
