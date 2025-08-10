# MongoDB Setup Guide - SSS Social Network

## 1. Cài đặt MongoDB

### Windows
1. Tải MongoDB Community Server từ: https://www.mongodb.com/try/download/community
2. Chạy installer và làm theo hướng dẫn
3. Chọn "Complete" installation
4. Cài đặt MongoDB Compass (GUI tool) nếu muốn

### macOS
```bash
# Sử dụng Homebrew
brew tap mongodb/brew
brew install mongodb-community

# Khởi động MongoDB
brew services start mongodb/brew/mongodb-community
```

### Linux (Ubuntu)
```bash
# Import MongoDB public GPG key
wget -qO - https://www.mongodb.org/static/pgp/server-7.0.asc | sudo apt-key add -

# Tạo list file cho MongoDB
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list

# Cập nhật package database
sudo apt-get update

# Cài đặt MongoDB
sudo apt-get install -y mongodb-org

# Khởi động MongoDB
sudo systemctl start mongod
sudo systemctl enable mongod
```

## 2. Cấu hình MongoDB

### Tạo database và user
1. Kết nối vào MongoDB:
```bash
mongosh
```

2. Tạo database và user:
```javascript
// Chuyển sang admin database
use admin

// Tạo user admin
db.createUser({
  user: "admin",
  pwd: "password123",
  roles: [
    { role: "userAdminAnyDatabase", db: "admin" },
    { role: "readWriteAnyDatabase", db: "admin" }
  ]
})

// Tạo database cho ứng dụng
use sss_user_db

// Tạo user cho database
db.createUser({
  user: "sss_user",
  pwd: "sss_password",
  roles: [
    { role: "readWrite", db: "sss_user_db" }
  ]
})

// Thoát
exit
```

## 3. Cấu hình ứng dụng

### Cập nhật application.yml
File `src/main/resources/application.yml` đã được cấu hình sẵn với MongoDB URI:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://admin:password123@localhost:27017/sss_user_db?authSource=admin&retryWrites=true&w=majority
      auto-index-creation: true
```

### Thay đổi cấu hình nếu cần
Nếu bạn muốn sử dụng user khác, cập nhật trong `application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://sss_user:sss_password@localhost:27017/sss_user_db?authSource=admin&retryWrites=true&w=majority
```

## 4. Chạy ứng dụng

### Khởi động MongoDB
```bash
# Windows
net start MongoDB

# macOS
brew services start mongodb/brew/mongodb-community

# Linux
sudo systemctl start mongod
```

### Chạy ứng dụng Spring Boot
```bash
./gradlew bootRun
```

## 5. Kiểm tra kết nối

### Sử dụng MongoDB Compass
1. Mở MongoDB Compass
2. Kết nối với URI: `mongodb://admin:password123@localhost:27017/sss_user_db?authSource=admin`
3. Kiểm tra database `sss_user_db` và collection `users`

### Sử dụng mongosh
```bash
mongosh "mongodb://admin:password123@localhost:27017/sss_user_db?authSource=admin"

# Kiểm tra collections
show collections

# Xem dữ liệu users
db.users.find()

# Xem users theo loại
db.users.find({userType: "CHARACTER"})
db.users.find({userType: "REAL_USER"})
db.users.find({userType: "ADMIN"})
```

## 6. Users mặc định

Khi chạy ứng dụng lần đầu, hệ thống sẽ tự động tạo:

### Admin User
- **Username:** admin
- **Password:** admin123
- **Email:** admin@sss.com
- **Type:** ADMIN

### Real User
- **Username:** user
- **Password:** user123
- **Email:** user@sss.com
- **Type:** REAL_USER
- **Bio:** Fan cuồng anime và manga! 🎌

### Character Users
- **Naruto Uzumaki** (username: naruto, password: character123)
- **Son Goku** (username: goku, password: character123)
- **Monkey D. Luffy** (username: luffy, password: character123)

## 7. Cấu trúc dữ liệu

### User Types
- **REAL_USER:** Người dùng thật
- **CHARACTER:** Nhân vật truyện tranh/anime
- **ADMIN:** Quản trị viên hệ thống

### Fields cho Character Users
- `characterName`: Tên nhân vật
- `animeMangaSource`: Nguồn gốc (anime/manga nào)
- `characterDescription`: Mô tả nhân vật
- `avatarUrl`: Ảnh đại diện
- `coverImageUrl`: Ảnh bìa
- `characterStatus`: Trạng thái nhân vật

### Fields cho Real Users
- `bio`: Tiểu sử người dùng
- `profilePictureUrl`: Ảnh đại diện
- `dateOfBirth`: Ngày sinh
- `gender`: Giới tính
- `location`: Vị trí địa lý

## 8. Troubleshooting

### Lỗi kết nối
- Kiểm tra MongoDB đã chạy chưa
- Kiểm tra port 27017 có bị block không
- Kiểm tra URI trong application.yml

### Lỗi authentication
- Đảm bảo đã tạo user trong MongoDB
- Kiểm tra authSource trong URI

### Lỗi database không tồn tại
- MongoDB sẽ tự động tạo database khi ứng dụng chạy
- Hoặc tạo thủ công: `use sss_user_db`
