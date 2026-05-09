# Hướng dẫn Triển khai Dự án TTJobs trên Linux

## 📋 Yêu cầu Hệ thống

### Phần cứng tối thiểu:
- **CPU**: 2 cores
- **RAM**: 4GB (8GB khuyến cáo)
- **Storage**: 20GB (để lưu trữ các image Docker và dữ liệu)
- **Network**: Kết nối Internet ổn định

### Phần mềm bắt buộc:
- Linux (Ubuntu 20.04 LTS hoặc cao hơn khuyến cáo)
- Docker 20.10+
- Docker Compose 1.29+
- Git

---

## 🚀 Phương pháp 1: Triển khai với Docker Compose (Khuyến cáo)

### Bước 1: Chuẩn bị môi trường

```bash
# Cập nhật hệ thống
sudo apt update && sudo apt upgrade -y

# Cài đặt Docker
sudo apt install -y docker.io docker-compose

# Thêm user vào nhóm docker (tùy chọn, để chạy mà không cần sudo)
sudo usermod -aG docker $USER
newgrp docker

# Xác minh cài đặt
docker --version
docker-compose --version
```

### Bước 2: Clone và chuẩn bị dự án

```bash
# Clone repository
cd /opt
sudo git clone <repository-url> ttjobs
cd ttjobs

# Cấp quyền truy cập (nếu cần)
sudo chown -R $USER:$USER /opt/ttjobs

# Tạo file .env từ template
cp .env.example .env
```

### Bước 3: Cấu hình biến môi trường (.env)

```bash
nano .env
```

Sửa các giá trị:

```ini
# PostgreSQL Configuration
POSTGRES_DB=ttjobs
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password_123  # Đổi thành password mạnh

# Spring Boot Configuration (tùy chọn)
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ttjobs
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_secure_password_123
```

### Bước 4: Xây dựng và chạy containers

```bash
# Xây dựng image Docker
docker-compose build

# Khởi động tất cả services
docker-compose up -d

# Kiểm tra trạng thái
docker-compose ps

# Xem logs
docker-compose logs -f

# Để xem logs của service cụ thể
docker-compose logs -f backend
docker-compose logs -f ai-service
docker-compose logs -f postgres
```

### Bước 5: Xác minh triển khai

```bash
# Kiểm tra Backend API
curl -s http://localhost:8080/actuator/health | jq

# Kiểm tra AI Service
curl -s http://localhost:8000/health

# Kiểm tra Database
docker exec ttjobs-postgres psql -U postgres -d ttjobs -c "SELECT 1"
```

### Truy cập ứng dụng:
- **Backend API**: http://localhost:8080
- **API Docs (Swagger)**: http://localhost:8080/swagger-ui.html
- **AI Service**: http://localhost:8000
- **Frontend**: http://localhost:5173 (nếu chạy riêng) hoặc http://localhost:3000 (nếu serve qua backend)

---

## 🐳 Phương pháp 2: Triển khai Manual (Không dùng Docker)

### Bước 1: Cài đặt Java 17

```bash
# Cài Java 17
sudo apt install -y openjdk-17-jdk openjdk-17-jre

# Xác minh
java -version
```

### Bước 2: Cài đặt PostgreSQL

```bash
# Cài PostgreSQL 16
sudo apt install -y postgresql postgresql-contrib

# Khởi động service
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Tạo database và user
sudo -u postgres psql << EOF
CREATE USER ttjobs_user WITH PASSWORD 'your_secure_password_123';
CREATE DATABASE ttjobs OWNER ttjobs_user;
GRANT ALL PRIVILEGES ON DATABASE ttjobs TO ttjobs_user;
EOF
```

### Bước 3: Cài đặt Python và AI Service

```bash
# Cài Python 3.9+
sudo apt install -y python3.11 python3-pip python3-venv

# Tạo virtual environment
cd /opt/ttjobs/ai-service
python3 -m venv venv
source venv/bin/activate

# Cài dependencies
pip install -r requirements.txt
```

### Bước 4: Cấu hình Backend

```bash
# Chỉnh sửa application.properties
cd /opt/ttjobs/backend
cp src/main/resources/application-secret.example.properties src/main/resources/application-secret.properties

# Chỉnh sửa file cấu hình
nano src/main/resources/application.properties
```

Thêm hoặc cập nhật:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ttjobs
spring.datasource.username=ttjobs_user
spring.datasource.password=your_secure_password_123
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
ttjobs.ai.base-url=http://localhost:8000
```

### Bước 5: Build Backend

```bash
cd /opt/ttjobs/backend
chmod +x mvnw
./mvnw clean package -DskipTests

# Hoặc dùng Maven (nếu cài đặt)
# mvn clean package -DskipTests
```

### Bước 6: Chạy services

**Terminal 1 - AI Service:**
```bash
cd /opt/ttjobs/ai-service
source venv/bin/activate
python app.py  # Hoặc: gunicorn app:app --bind 0.0.0.0:8000
```

**Terminal 2 - Backend:**
```bash
cd /opt/ttjobs/backend
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

**Terminal 3 - Frontend:**
```bash
cd /opt/ttjobs/frontend
npm install
npm run dev  # Để development hoặc npm run build && npm run preview
```

---

## 📦 Phương pháp 3: Triển khai với Systemd Services (Production)

### Tạo service cho Backend

```bash
sudo nano /etc/systemd/system/ttjobs-backend.service
```

Thêm nội dung:

```ini
[Unit]
Description=TTJobs Backend Service
After=network.target postgresql.service

[Service]
Type=simple
User=ttjobs
WorkingDirectory=/opt/ttjobs/backend
ExecStart=/usr/bin/java -Xmx1024m -Xms512m -jar target/backend-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=ttjobs-backend

[Install]
WantedBy=multi-user.target
```

### Tạo service cho AI Service

```bash
sudo nano /etc/systemd/system/ttjobs-ai.service
```

```ini
[Unit]
Description=TTJobs AI Service
After=network.target

[Service]
Type=simple
User=ttjobs
WorkingDirectory=/opt/ttjobs/ai-service
Environment="PATH=/opt/ttjobs/ai-service/venv/bin"
ExecStart=/opt/ttjobs/ai-service/venv/bin/python app.py
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=ttjobs-ai

[Install]
WantedBy=multi-user.target
```

### Kích hoạt và chạy services

```bash
# Reload systemd daemon
sudo systemctl daemon-reload

# Kích hoạt services tự động start
sudo systemctl enable ttjobs-backend
sudo systemctl enable ttjobs-ai

# Khởi động services
sudo systemctl start ttjobs-backend
sudo systemctl start ttjobs-ai

# Kiểm tra trạng thái
sudo systemctl status ttjobs-backend
sudo systemctl status ttjobs-ai

# Xem logs
sudo journalctl -u ttjobs-backend -f
sudo journalctl -u ttjobs-ai -f
```

---

## 🔒 Bảo mật Production

### 1. Cài đặt Nginx Reverse Proxy

```bash
sudo apt install -y nginx

# Tạo file cấu hình
sudo nano /etc/nginx/sites-available/ttjobs
```

```nginx
upstream backend {
    server localhost:8080;
}

upstream ai_service {
    server localhost:8000;
}

server {
    listen 80;
    server_name your_domain.com;

    # Chuyển hướng HTTP sang HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your_domain.com;

    ssl_certificate /etc/letsencrypt/live/your_domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your_domain.com/privkey.pem;

    location / {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ai/ {
        proxy_pass http://ai_service;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 2. Cài đặt SSL với Let's Encrypt

```bash
sudo apt install -y certbot python3-certbot-nginx

# Tạo certificate
sudo certbot certonly --nginx -d your_domain.com

# Auto renew
sudo systemctl enable certbot.timer
```

### 3. Cầu hình Firewall

```bash
sudo ufw enable
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
```

---

## 📊 Monitoring và Logging

### 1. Xem logs với Docker Compose

```bash
# Logs tất cả services
docker-compose logs -f

# Logs service cụ thể
docker-compose logs -f backend --tail 100
```

### 2. Cài đặt Monitoring Stack

```bash
# Sử dụng Prometheus + Grafana
docker-compose -f docker-compose.monitoring.yml up -d
```

### 3. Backup Database

```bash
# Tạo backup hàng ngày
mkdir -p /opt/ttjobs/backups

# Script backup
cat > /opt/ttjobs/backup.sh << 'EOF'
#!/bin/bash
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
docker exec ttjobs-postgres pg_dump -U postgres ttjobs | gzip > /opt/ttjobs/backups/ttjobs_$TIMESTAMP.sql.gz
echo "Backup created: ttjobs_$TIMESTAMP.sql.gz"
EOF

chmod +x /opt/ttjobs/backup.sh

# Thêm vào crontab để backup tự động
# 0 2 * * * /opt/ttjobs/backup.sh
```

---

## 🔧 Troubleshooting

### 1. Backend không kết nối được database

```bash
# Kiểm tra kết nối
docker exec ttjobs-backend curl -s http://localhost:8080/actuator/health

# Kiểm tra logs
docker-compose logs backend
```

### 2. Out of Memory

```bash
# Tăng heap size cho Java (backend)
docker-compose.yml:
  backend:
    environment:
      JAVA_OPTS: "-Xmx2048m -Xms1024m"
```

### 3. AI Service không khởi động

```bash
# Kiểm tra model files
ls -la ai-service/models/
ls -la ai-service/cv-job-matcher/

# Rebuild
docker-compose build --no-cache ai-service
```

### 4. Port bị chiếm dụng

```bash
# Kiểm tra port
sudo lsof -i :8080
sudo lsof -i :5432

# Giết process (nếu cần)
sudo kill -9 <PID>
```

---

## 📈 Scale-up (Tùy chọn)

### Sử dụng Docker Swarm

```bash
# Khởi tạo swarm
docker swarm init

# Deploy với stack
docker stack deploy -c docker-compose.yml ttjobs

# Scale service
docker service scale ttjobs_backend=3
```

### Sử dụng Kubernetes

```bash
# Tạo deployment files từ docker-compose
kompose convert -f docker-compose.yml

# Deploy trên Kubernetes
kubectl apply -f .

# Scale replica
kubectl scale deployment ttjobs-backend --replicas=3
```

---

## ✅ Checklist Triển khai

- [ ] Cài đặt Docker và Docker Compose
- [ ] Clone repository
- [ ] Cấu hình file .env
- [ ] Build Docker images
- [ ] Khởi động containers
- [ ] Xác minh kết nối services
- [ ] Cấu hình Nginx/SSL
- [ ] Cấu hình Firewall
- [ ] Thiết lập Backup
- [ ] Cấu hình Monitoring
- [ ] Test ứng dụng đầu cuối

---

## 📞 Hỗ trợ

Để xem chi tiết hơn, tham khảo:
- Backend: [backend/HELP.md](backend/HELP.md)
- AI Service: [ai-service/README.md](ai-service/README.md)
