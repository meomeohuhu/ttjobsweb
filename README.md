# TTJobs

TTJobs is a Spring Boot + React job platform with recruiter/candidate flows, admin management, forum/community, realtime messaging, AI matching, and 1-1 interview rooms.

Detailed Vietnamese project documentation: [PROJECT_DOCUMENTATION.md](PROJECT_DOCUMENTATION.md)

## Local Development

Requirements:

- Java 17
- Node.js 20+
- PostgreSQL
- Redis optional for realtime broker
- Python 3.11 if running `ai-service` locally

Common commands:

```bash
# Backend
cd backend
./mvnw spring-boot:run

# Frontend
cd frontend
npm install
npm run dev

# Frontend build check
npm run build

# Backend test check
cd backend
./mvnw test
```

## Production Deploy

Use the normalized production compose file:

```bash
cp .env.example .env
# edit .env and replace every CHANGE_ME value
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

Production checklist:

- Set `JWT_SECRET` to a random value of at least 32 ASCII characters.
- Set `POSTGRES_PASSWORD` to a strong password.
- Set `TTJOBS_APP_BASE_URL` and `TTJOBS_CORS_ALLOWED_ORIGINS` to the public HTTPS domain.
- Keep `VITE_ENABLE_DEMO_FALLBACK=false`.
- Keep `TTJOBS_EMAIL_ENABLED=false` until SMTP credentials are valid.
- Configure `CLOUDINARY_*` before using image upload in production.
- Serve the site through HTTPS. Browser camera/microphone APIs for interview rooms require a secure context.
- Add a TURN server later if WebRTC users are often on different networks/NAT.

## Key URLs

- Frontend: `http://localhost:5173` in development
- Backend health: `/actuator/health`
- API base: `/api`
- WebSocket endpoint: `/ws`
- Interview room route: `/interviews/:interviewId/room`

## Deployment Files

- `.env.example`: complete environment template
- `docker-compose.prod.yml`: production-oriented local build stack
- `docker-compose.images.yml`: image-based stack for prebuilt images
- `deploy/nginx/ttjobs.conf`: frontend, API, and WebSocket reverse proxy
