#!/bin/bash

################################
# TTJobs Linux Deployment Script
# Hỗ trợ triển khai nhanh dự án
################################

set -e

# Màu sắc cho output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Hàm in logs
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Kiểm tra yêu cầu hệ thống
check_requirements() {
    log_info "Kiểm tra yêu cầu hệ thống..."

    # Kiểm tra Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker không được cài đặt"
        echo "Cài đặt Docker: curl -fsSL https://get.docker.com -o get-docker.sh | sh"
        exit 1
    fi
    log_success "Docker: $(docker --version)"

    # Kiểm tra Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose không được cài đặt"
        echo "Cài đặt Docker Compose: sudo apt install -y docker-compose"
        exit 1
    fi
    log_success "Docker Compose: $(docker-compose --version)"

    # Kiểm tra Git
    if ! command -v git &> /dev/null; then
        log_error "Git không được cài đặt"
        exit 1
    fi
    log_success "Git: $(git --version)"

    log_success "Tất cả yêu cầu hệ thống đã được kiểm tra"
}

# Chuẩn bị dự án
prepare_project() {
    log_info "Chuẩn bị dự án..."

    # Tạo file .env nếu chưa tồn tại
    if [ ! -f .env ]; then
        if [ -f .env.example ]; then
            cp .env.example .env
            log_success "Tạo file .env từ template"
            
            log_warning "Vui lòng chỉnh sửa file .env trước khi chạy script"
            log_info "nano .env"
            exit 0
        else
            log_error "Không tìm thấy .env.example"
            exit 1
        fi
    fi

    log_success "File .env đã tồn tại"
}

# Build Docker images
build_images() {
    log_info "Xây dựng Docker images..."
    
    docker-compose build --no-cache
    
    log_success "Build hoàn tất"
}

# Khởi động services
start_services() {
    log_info "Khởi động services..."

    docker-compose up -d

    # Chờ services khởi động
    log_info "Chờ services khởi động..."
    sleep 10

    log_success "Services đã được khởi động"
}

# Kiểm tra health status
check_health() {
    log_info "Kiểm tra trạng thái services..."

    # Kiểm tra PostgreSQL
    if docker-compose exec -T postgres pg_isready -U postgres &> /dev/null; then
        log_success "PostgreSQL: Healthy"
    else
        log_error "PostgreSQL: Unhealthy"
    fi

    # Kiểm tra Backend
    if curl -s http://localhost:8080/actuator/health &> /dev/null; then
        log_success "Backend: Healthy"
    else
        log_warning "Backend: Chưa ready (đang khởi động)"
    fi

    # Kiểm tra AI Service
    if curl -s http://localhost:8000/health &> /dev/null; then
        log_success "AI Service: Healthy"
    else
        log_warning "AI Service: Chưa ready (đang khởi động)"
    fi

    log_success "Kiểm tra health hoàn tất"
}

# Hiển thị thông tin truy cập
show_access_info() {
    log_info "═══════════════════════════════════════"
    log_info "🎉 Triển khai thành công!"
    log_info "═══════════════════════════════════════"
    echo ""
    echo "📍 Các endpoint có sẵn:"
    echo "  • Backend API: http://localhost:8080"
    echo "  • API Documentation: http://localhost:8080/swagger-ui.html"
    echo "  • AI Service: http://localhost:8000"
    echo "  • PostgreSQL: localhost:5432"
    echo ""
    echo "📋 Lệnh hữu ích:"
    echo "  • Xem logs: docker-compose logs -f"
    echo "  • Dừng services: docker-compose down"
    echo "  • Xem trạng thái: docker-compose ps"
    echo ""
    echo "🔧 Troubleshooting:"
    echo "  • Xem logs backend: docker-compose logs backend"
    echo "  • Xem logs ai-service: docker-compose logs ai-service"
    echo "  • Xem logs postgres: docker-compose logs postgres"
    echo ""
}

# Main menu
show_menu() {
    echo ""
    echo "TTJobs Linux Deployment"
    echo "========================================"
    echo "1. Kiểm tra yêu cầu hệ thống"
    echo "2. Chuẩn bị dự án"
    echo "3. Build images"
    echo "4. Khởi động services"
    echo "5. Kiểm tra trạng thái"
    echo "6. Triển khai đầy đủ (1-5)"
    echo "7. Dừng services"
    echo "8. Xem logs"
    echo "9. Reset (xóa containers & volumes)"
    echo "0. Thoát"
    echo "========================================"
}

# Hàm xem logs
view_logs() {
    echo ""
    echo "Chọn service để xem logs:"
    echo "1. Tất cả"
    echo "2. Backend"
    echo "3. AI Service"
    echo "4. PostgreSQL"
    echo "5. Quay lại"
    read -p "Lựa chọn: " log_choice
    
    case $log_choice in
        1) docker-compose logs -f ;;
        2) docker-compose logs -f backend ;;
        3) docker-compose logs -f ai-service ;;
        4) docker-compose logs -f postgres ;;
        5) ;;
        *) log_error "Lựa chọn không hợp lệ" ;;
    esac
}

# Hàm reset
reset_deployment() {
    read -p "⚠️  Xác nhận xóa containers và volumes? (yes/no): " confirm
    
    if [ "$confirm" = "yes" ]; then
        log_warning "Đang xóa containers và volumes..."
        docker-compose down -v
        log_success "Reset hoàn tất"
    else
        log_info "Hủy bỏ"
    fi
}

# Main script
main() {
    # Kiểm tra có phải đang ở root directory
    if [ ! -f "docker-compose.yml" ]; then
        log_error "Script phải được chạy từ root directory của dự án"
        log_info "Thực hiện: cd /path/to/ttjobs && bash deploy.sh"
        exit 1
    fi

    # Kiểm tra Docker daemon
    if ! docker ps &> /dev/null; then
        log_error "Docker daemon không chạy hoặc bạn không có quyền truy cập"
        log_info "Hãy thêm user vào group docker: sudo usermod -aG docker \$USER"
        exit 1
    fi

    while true; do
        show_menu
        read -p "Lựa chọn (0-9): " choice

        case $choice in
            1) check_requirements ;;
            2) prepare_project ;;
            3) build_images ;;
            4) start_services && check_health ;;
            5) check_health ;;
            6) 
                check_requirements
                prepare_project
                build_images
                start_services
                check_health
                show_access_info
                ;;
            7) 
                log_warning "Dừng services..."
                docker-compose down
                log_success "Services đã dừng"
                ;;
            8) view_logs ;;
            9) reset_deployment ;;
            0) 
                log_info "Thoát"
                exit 0
                ;;
            *)
                log_error "Lựa chọn không hợp lệ"
                ;;
        esac
    done
}

# Chạy main nếu không có argument
if [ $# -eq 0 ]; then
    main
else
    case $1 in
        check) check_requirements ;;
        prepare) prepare_project ;;
        build) build_images ;;
        start) start_services && check_health ;;
        health) check_health ;;
        deploy) 
            check_requirements
            prepare_project
            build_images
            start_services
            check_health
            show_access_info
            ;;
        stop) docker-compose down ;;
        logs) docker-compose logs -f ;;
        reset) reset_deployment ;;
        *)
            echo "Sử dụng:"
            echo "  $0                 # Chế độ interactive"
            echo "  $0 check          # Kiểm tra yêu cầu"
            echo "  $0 prepare        # Chuẩn bị dự án"
            echo "  $0 build          # Build images"
            echo "  $0 start          # Khởi động services"
            echo "  $0 health         # Kiểm tra health"
            echo "  $0 deploy         # Triển khai đầy đủ"
            echo "  $0 stop           # Dừng services"
            echo "  $0 logs           # Xem logs"
            echo "  $0 reset          # Reset deployment"
            exit 1
            ;;
    esac
fi
