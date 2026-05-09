# 🚀 Quick Start Guide - Triển khai trên Linux

## ⚡ Triển khai nhanh nhất (5 phút)

### 1. Yêu cầu
```bash
# Cập nhật Ubuntu
sudo apt update && sudo apt upgrade -y

# Cài Docker
curl -fsSL https://get.docker.com -o get-docker.sh | bash
sudo apt install -y docker-compose

# Thêm user vào group docker (tùy chọn - để dùng mà không cần sudo)
sudo usermod -aG docker $USER
newgrp docker
```

### 2. Triển khai

```bash
# Clone project
cd /opt && git clone <repository-url> ttjobs
cd ttjobs

# Tạo file cấu hình
cp .env.example .env

# Chỉnh sửa password (QUAN TRỌNG)
nano .env
# Đổi: POSTGRES_PASSWORD=your_secure_password_123

# Chạy deployment script
chmod +x deploy.sh
./deploy.sh deploy

# Hoặc manual
docker-compose build
docker-compose up -d
```

### 3. Truy cập

```
Backend:    http://localhost:8080
API Docs:   http://localhost:8080/swagger-ui.html
AI Service: http://localhost:8000
```

---

## 🎯 3 Phương pháp Triển khai

### Phương pháp A: Docker Compose (⭐ Khuyến cáo)

**Ưu điểm:**
- ✅ Nhanh & dễ
- ✅ Isolation đầy đủ
- ✅ Dễ scale
- ✅ Dễ backup

**Nhược điểm:**
- ❌ Tốn RAM hơn

```bash
docker-compose up -d
docker-compose logs -f
```

### Phương pháp B: Manual Installation

**Ưu điểm:**
- ✅ Kiểm soát đầy đủ
- ✅ Tốn ít tài nguyên

**Nhược điểm:**
- ❌ Phức tạp hơn
- ❌ Dễ lỗi hơn

```bash
# Cài Java 17
sudo apt install -y openjdk-17-jdk

# Cài PostgreSQL
sudo apt install -y postgresql

# Cài Python & AI Service
sudo apt install -y python3 python3-pip
pip install -r ai-service/requirements.txt

# Build & chạy backend
cd backend && ./mvnw clean package

# Chạy các service riêng lẻ
```

### Phương pháp C: Kubernetes (K8s)

**Ưu điểm:**
- ✅ Production-grade
- ✅ Auto-scaling
- ✅ Self-healing

**Nhược điểm:**
- ❌ Phức tạp
- ❌ Tốn tài nguyên

```bash
# Chuyển Docker Compose sang K8s
kompose convert -f docker-compose.yml

# Deploy
kubectl apply -f .
```

---

## 📋 Checklist Triển khai

```bash
# 1. Kiểm tra Docker
docker --version      # ✓ v20.10+
docker-compose --version  # ✓ v1.29+

# 2. Clone & chuẩn bị
git clone <repo> && cd <folder>
cp .env.example .env
# Edit .env với password mạnh

# 3. Build & start
docker-compose build
docker-compose up -d

# 4. Chờ ~30s rồi kiểm tra
docker-compose ps

# 5. Test API
curl http://localhost:8080/actuator/health
curl http://localhost:8000/health

# 6. Xem logs nếu có lỗi
docker-compose logs -f
```

---

## 🔧 Lệnh Thường Dùng

```bash
# Xem status
docker-compose ps

# Xem logs
docker-compose logs -f             # Tất cả
docker-compose logs -f backend     # Backend
docker-compose logs -f ai-service  # AI
docker-compose logs -f postgres    # Database

# Dừng services
docker-compose stop

# Khởi động lại
docker-compose restart

# Xóa hết (cẩn thận!)
docker-compose down -v

# Truy cập database
docker exec -it ttjobs-postgres psql -U postgres -d ttjobs

# Backup database
docker exec ttjobs-postgres pg_dump -U postgres ttjobs | gzip > backup_$(date +%s).sql.gz

# Xem logs chi tiết
docker-compose logs backend | tail -50
```

---

## 🐛 Troubleshooting

### Backend không start

```bash
# Kiểm tra logs
docker-compose logs backend

# Nguyên nhân phổ biến:
# 1. Database chưa ready - chờ thêm vài giây
# 2. Password sai - kiểm tra .env vs docker-compose.yml
# 3. Port 8080 bị chiếm - sudo lsof -i :8080

# Khắc phục
docker-compose restart backend
```

### Database connection error

```bash
# Kiểm tra PostgreSQL
docker-compose logs postgres

# Test kết nối
docker exec -it ttjobs-postgres psql -U postgres

# Reset database
docker-compose down -v
docker-compose up -d postgres
# Chờ service healthy rồi start backend
```

### Out of Memory

```bash
# Edit docker-compose.yml
# backend:
#   environment:
#     JAVA_OPTS: "-Xmx2048m -Xms1024m"

docker-compose restart backend
```

### AI Service lỗi

```bash
# Kiểm tra model files
ls -la ai-service/models/
ls -la ai-service/cv-job-matcher/

# Nếu thiếu, download hoặc rebuild
docker-compose build --no-cache ai-service
```

---

## 🔐 Security Tips

### 1. Change Passwords
```bash
# Edit .env
POSTGRES_PASSWORD=<strong_password>
# Thay đổi password cho tất cả services
```

### 2. Firewall Setup
```bash
sudo ufw enable
sudo ufw allow 22/tcp      # SSH
sudo ufw allow 80/tcp      # HTTP
sudo ufw allow 443/tcp     # HTTPS
```

### 3. Use Reverse Proxy (Nginx)
```bash
sudo apt install nginx
# Configure https + ssl
```

### 4. Regular Backups
```bash
# Cron job hàng ngày
0 2 * * * docker exec ttjobs-postgres pg_dump -U postgres ttjobs | gzip > /backups/db_$(date +\%Y\%m\%d).sql.gz
```

---

## 📊 Monitoring

### Xem resource usage
```bash
docker stats
docker-compose stats
```

### Xem health
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8000/health
```

---

## 📞 Cần Help?

1. Xem chi tiết: [LINUX_DEPLOYMENT.md](LINUX_DEPLOYMENT.md)
2. Xem logs: `docker-compose logs -f`
3. Kiểm tra port: `sudo netstat -tlnp`
4. Kiểm tra disk: `df -h`
5. Kiểm tra RAM: `free -h`

---

## 🎓 Bước tiếp theo

- [ ] Test API endpoints
- [ ] Configure SSL/HTTPS
- [ ] Setup backup strategy
- [ ] Setup monitoring
- [ ] Deploy frontend
- [ ] Scale services (nếu cần)

Chúc mừng! 🎉
