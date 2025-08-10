# Hướng dẫn thiết lập Keycloak

## 1. Khởi động Keycloak

```bash
cd user
docker-compose up -d keycloak postgres
```

Keycloak sẽ khởi động tại: http://localhost:8080

## 2. Đăng nhập Keycloak Admin Console

- URL: http://localhost:8080/admin
- Username: `admin`
- Password: `admin123`

## 3. Tạo Realm

1. Click vào dropdown "master" ở góc trái trên
2. Click "Create Realm"
3. Nhập Realm name: `sss-realm`
4. Click "Create"

## 4. Tạo Client cho Frontend

1. Trong realm `sss-realm`, vào menu "Clients"
2. Click "Create client"
3. Điền thông tin:
   - **Client type**: OpenID Connect
   - **Client ID**: `sss-frontend`
   - **Name**: `SSS Frontend Application`
4. Click "Next"
5. Tại "Capability config":
   - **Client authentication**: OFF
   - **Authorization**: OFF
   - **Standard flow**: ON
   - **Direct access grants**: ON
6. Click "Next"
7. Tại "Login settings":
   - **Root URL**: `http://localhost:3000`
   - **Home URL**: `http://localhost:3000`
   - **Valid redirect URIs**: `http://localhost:3000/*`
   - **Valid post logout redirect URIs**: `http://localhost:3000/*`
   - **Web origins**: `http://localhost:3000`
8. Click "Save"

## 5. Tạo Client cho Backend (Resource Server)

1. Click "Create client"
2. Điền thông tin:
   - **Client type**: OpenID Connect
   - **Client ID**: `sss-backend`
   - **Name**: `SSS Backend API`
3. Click "Next"
4. Tại "Capability config":
   - **Client authentication**: ON
   - **Authorization**: OFF
   - **Service accounts roles**: ON
5. Click "Next"
6. Tại "Login settings":
   - **Root URL**: `http://localhost:8082`
   - **Valid redirect URIs**: `http://localhost:8082/*`
7. Click "Save"
8. Vào tab "Credentials" và copy **Client secret** để cập nhật vào `application.yml`

## 6. Tạo Users

1. Vào menu "Users"
2. Click "Create new user"
3. Điền thông tin:
   - **Username**: `testuser`
   - **Email**: `test@example.com`
   - **First name**: `Test`
   - **Last name**: `User`
   - **Email verified**: ON
   - **Enabled**: ON
4. Click "Create"
5. Vào tab "Credentials"
6. Click "Set password"
7. Nhập password: `password123`
8. **Temporary**: OFF
9. Click "Save"

## 7. Tạo Roles (Optional)

1. Vào menu "Realm roles"
2. Click "Create role"
3. Tạo các roles:
   - `admin`
   - `user`
   - `moderator`

## 8. Assign Roles to Users

1. Vào "Users" → chọn user
2. Vào tab "Role mapping"
3. Click "Assign role"
4. Chọn roles và click "Assign"

## 9. Cập nhật cấu hình Backend

Cập nhật `user/src/main/resources/application.yml` với client secret:

```yaml
keycloak:
  realm: sss-realm
  auth-server-url: http://localhost:8080
  resource: sss-backend
  credentials:
    secret: YOUR_CLIENT_SECRET_HERE  # Copy từ Keycloak Admin Console
  bearer-only: true
  cors: true
```

## 10. Test Authentication

1. Khởi động backend: `./gradlew bootRun`
2. Khởi động frontend: `npm start`
3. Truy cập: http://localhost:3000
4. Click "Đăng nhập với Keycloak"
5. Đăng nhập với user đã tạo

## Troubleshooting

### Frontend không redirect được
- Kiểm tra "Valid redirect URIs" trong Keycloak client
- Kiểm tra console logs trong browser

### Backend không xác thực được JWT
- Kiểm tra `issuer-uri` trong application.yml
- Kiểm tra Keycloak realm có đúng không
- Kiểm tra client secret

### CORS errors
- Kiểm tra "Web origins" trong Keycloak client
- Kiểm tra CORS config trong SecurityConfig.java

## URLs quan trọng

- Keycloak Admin: http://localhost:8080/admin
- Keycloak Realm: http://localhost:8080/realms/sss-realm
- Backend API: http://localhost:8082
- Frontend: http://localhost:3000
