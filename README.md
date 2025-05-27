# 🧵 Handmade Shop App

Ứng dụng bán đồ handmade được xây dựng bằng **Java Android (Android Studio)** với backend sử dụng **Spring Boot**, **JWT**, **MySQL**, và tích hợp **chatbot AI**, **Zapier** và **Ngrok** để tăng trải nghiệm người dùng.

## 🔧 Công nghệ sử dụng

### 📱 Android App (Client)
- Java + Android Studio
- XML thiết kế giao diện
- JWT Token Authentication
- Retrofit2 để kết nối API
- Ngrok (test API trên thiết bị thật)
- Chatbot AI tích hợp API
- Firebase (tùy chọn, nếu có dùng thông báo)

### ☕ Backend (Server)
- Spring Boot
- Spring Security + JWT
- MySQL Database
- RESTful API
- Zapier Webhook (tự động hóa)

---

## 🚀 Tính năng người dùng

- ✅ Đăng ký tài khoản
- 🔐 Đăng nhập bằng JWT token
- 🔁 Quên mật khẩu (gửi mã OTP qua email)
- 🛍️ Xem danh sách sản phẩm handmade
- 🔍 Tìm kiếm sản phẩm theo tên hoặc loại
- 🛒 Thêm sản phẩm vào giỏ hàng và đặt mua
- 💳 Thanh toán (giả lập)
- 📦 Theo dõi đơn hàng
- ⭐ Đánh giá sản phẩm đã mua
- 🤖 Nhắn tin với chatbot để tư vấn, hỗ trợ

---

## ⚙️ Cài đặt và triển khai
Backend (Spring Boot)
Cấu hình application.properties:

properties
Copy
Edit
spring.datasource.url=jdbc:mysql://localhost:3306/handmade
spring.datasource.username=root
spring.datasource.password=your_password
jwt.secret=your_jwt_secret
Chạy backend:

bash

./mvnw spring-boot:run
Dùng Ngrok để tạo URL public cho Android kết nối:ngrok http 8080

Android App
Mở trong Android Studio

🔐 Bảo mật
Sử dụng JWT để xác thực người dùng

Mật khẩu được mã hóa bằng BCrypt

Xác thực và phân quyền cơ bản qua token

⚙️ Tự động hóa với Zapier
Tích hợp Zapier để gửi thông báo tự động qua email

Có thể mở rộng gửi thông tin đơn hàng đến Google Sheets, Gmail, Slack...

🤖 Chatbot AI
Chatbot tích hợp API từ AI (OpenAI, DialogFlow hoặc Rasa)

Giúp tư vấn sản phẩm, giải đáp thắc mắc của người dùng

Có thể mở rộng hỗ trợ giọng nói hoặc giao diện trò chuyện nâng cao

📸 Hình ảnh giao diện
(Thêm ảnh chụp màn hình Android App: đăng nhập, xem sản phẩm, giỏ hàng, chatbot, đánh giá,...)

📄 License
Dự án dành cho mục đích học tập, không sử dụng thương mại.

👨‍💻 Tác giả
Họ tên: Nguyễn Thị Hồng Nhung-22110391
        Liên Huệ Tiên-22110433

Trường: Đại học Sư phạm Kỹ thuật TPHCM
GVHD: Th.S Nguyễn Hữu Trung


### Clone repo
git bash
git clone https://github.com/iamtien-cmd/Project_mobile.git





