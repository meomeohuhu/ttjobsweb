# Triển khai TTJobs bằng Nginx (Linux)

Mô hình triển khai:
- Nginx serve frontend React build tĩnh.
- Nginx reverse proxy API `/api/*` và websocket `/ws/*` sang backend Spring Boot.
- Backend gọi AI service nội bộ qua `TTJOBS_AI_BASE_URL`.

## 1. Chuẩn bị máy Linux

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y docker.io docker-compose-plugin
sudo usermod -aG docker $USER
newgrp docker
```

## 2. Chuẩn bị source

```bash
git clone <repo-url> ttjobs
cd ttjobs
cp .env.example .env
nano .env
```

Thiết lập tối thiểu trong `.env`:

```env
POSTGRES_DB=ttjobs
POSTGRES_USER=postgres
POSTGRES_PASSWORD=doi_password_manh_o_day
```

## 3. Build và chạy stack Nginx

```bash
docker compose -f docker-compose.nginx.yml up -d --build
```

## 4. Kiểm tra trạng thái

```bash
docker compose -f docker-compose.nginx.yml ps
curl -I http://localhost/
curl -s http://localhost/api/actuator/health || curl -s http://localhost/actuator/health
```

Truy cập:
- App: `http://<server-ip>/`
- Swagger: `http://<server-ip>/swagger-ui/index.html`

## 5. Lệnh vận hành thường dùng

```bash
# Xem logs
docker compose -f docker-compose.nginx.yml logs -f nginx
docker compose -f docker-compose.nginx.yml logs -f backend

# Restart stack
docker compose -f docker-compose.nginx.yml restart

# Stop
docker compose -f docker-compose.nginx.yml down

# Stop + xóa volume DB (cẩn thận)
docker compose -f docker-compose.nginx.yml down -v
```

## 6. Bật HTTPS (khuyến nghị production)

Khi có domain thật, thay `server_name` trong cấu hình Nginx và dùng certbot ở host:

```bash
sudo apt install -y certbot
# nếu dùng nginx host cài native thì dùng certbot --nginx
```

Nếu bạn muốn, mình có thể tạo luôn bản cấu hình `ttjobs-ssl.conf` + hướng dẫn gắn chứng chỉ Let's Encrypt theo domain của bạn.
