CREATE TABLE IF NOT EXISTS career_guide_articles (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(160) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(120) NOT NULL,
    cover_image_url VARCHAR(500),
    reading_time_minutes INTEGER,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO career_guide_articles (slug, title, summary, content, category, cover_image_url, reading_time_minutes, featured, published_at, created_at, updated_at)
SELECT 'cv-thuyet-phuc-nha-tuyen-dung', 'Cách viết CV thuyết phục nhà tuyển dụng',
       'Cách sắp xếp CV, chọn từ khóa và trình bày thành tựu để hồ sơ dễ đi qua vòng sàng lọc đầu tiên.',
       'Chia CV thành 4 phần rõ ràng: thông tin cá nhân, mục tiêu nghề nghiệp, kinh nghiệm và kỹ năng.\n\nĐầu mỗi mô tả công việc, hãy nêu kết quả đo được thay vì chỉ liệt kê nhiệm vụ. Ví dụ: tăng doanh số, giảm thời gian xử lý, cải thiện tỉ lệ phản hồi.\n\nKhi ứng tuyển online, đồng bộ từ khóa trong mô tả với JD để hệ thống ATS dễ nhận diện hơn.',
       'Tìm việc', 'https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=1200&q=80',
       6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM career_guide_articles WHERE slug = 'cv-thuyet-phuc-nha-tuyen-dung'
);

INSERT INTO career_guide_articles (slug, title, summary, content, category, cover_image_url, reading_time_minutes, featured, published_at, created_at, updated_at)
SELECT 'phong-van-hieu-qua', 'Chuẩn bị phỏng vấn hiệu quả trong 48 giờ',
       'Lộ trình ôn tập nhanh để bạn trả lời gọn, đúng trọng tâm và tạo ấn tượng chuyên nghiệp.',
       'Trong 24 giờ đầu, hãy đọc lại JD, nghiên cứu công ty và liệt kê 5 câu hỏi phổ biến nhất cho vị trí.\n\nNgày kế tiếp, luyện trả lời theo cấu trúc ngắn: bối cảnh, hành động, kết quả. Với câu hỏi khó, đừng vòng vo; hãy nói rõ điều bạn làm được và điều bạn còn muốn học.\n\nKết thúc buổi chuẩn bị bằng việc kiểm tra trang phục, đường đi và thiết bị nếu phỏng vấn online.',
       'Phỏng vấn', 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80',
       5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM career_guide_articles WHERE slug = 'phong-van-hieu-qua'
);

INSERT INTO career_guide_articles (slug, title, summary, content, category, cover_image_url, reading_time_minutes, featured, published_at, created_at, updated_at)
SELECT 'chuyen-nganh-sang-it', 'Chuyển ngành sang IT bắt đầu từ đâu',
       'Bản đồ kỹ năng, lộ trình học và cách viết lại hồ sơ khi bạn chuyển từ ngành khác sang công nghệ.',
       'Bắt đầu từ một vai trò cụ thể thay vì nói chung chung về ngành IT. Frontend, backend, QA, data hay product đều có bộ kỹ năng khác nhau.\n\nHãy chọn một ngôn ngữ hoặc công cụ chính, làm 2 đến 3 dự án nhỏ, rồi viết lại CV theo hướng thể hiện khả năng giải quyết vấn đề.\n\nKhi chưa có kinh nghiệm chính thức, portfolio và mô tả dự án thực tế quan trọng hơn các danh sách khóa học.',
       'Chuyển ngành', 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80',
       7, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM career_guide_articles WHERE slug = 'chuyen-nganh-sang-it'
);

INSERT INTO career_guide_articles (slug, title, summary, content, category, cover_image_url, reading_time_minutes, featured, published_at, created_at, updated_at)
SELECT 'deal-luong-dung-cach', 'Deal lương đúng cách mà không mất lợi thế',
       'Một số nguyên tắc giúp bạn thương lượng lương, thưởng và phúc lợi rõ ràng hơn trong vòng offer.',
       'Khi nhận offer, đừng phản hồi ngay nếu bạn chưa nắm đủ dữ liệu. Hãy hỏi về lương cứng, thưởng, thời gian thử việc, phụ cấp và cơ hội review sau 3 đến 6 tháng.\n\nNếu muốn thương lượng, hãy đưa ra lý do dựa trên giá trị bạn mang lại: kinh nghiệm, kỹ năng hiếm, phạm vi trách nhiệm hoặc benchmark thị trường.\n\nMục tiêu không phải là "đòi thêm", mà là chốt một gói phù hợp với đóng góp và kỳ vọng của cả hai bên.',
       'Lương thưởng', 'https://images.unsplash.com/photo-1554224155-6726b3ff858f?auto=format&fit=crop&w=1200&q=80',
       4, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM career_guide_articles WHERE slug = 'deal-luong-dung-cach'
);
