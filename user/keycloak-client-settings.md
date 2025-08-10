# Cấu hình Keycloak Client để khắc phục lỗi authentication

## Vấn đề
Lỗi authentication sau khi user đăng nhập thành công và cập nhật thông tin.

## Giải pháp: Cập nhật Client Settings

### 1. Kiểm tra Client `sss-frontend`

Vào Keycloak Admin Console → Realm `sss-realm` → Clients → `sss-frontend`

### 2. Tab Settings - Cấu hình cơ bản

```
Client type: OpenID Connect
Client ID: sss-frontend
Name: SSS Frontend Application
Always display in console: OFF
Client authentication: OFF
Authorization: OFF
```

### 3. Authentication flow

```
Standard flow: ON
Implicit flow: OFF (khuyến nghị)
Direct access grants: ON
Service accounts roles: OFF
OAuth 2.0 Device Authorization Grant: OFF
OIDC CIBA Grant: OFF
```

### 4. Login settings

```
Root URL: http://localhost:3000
Home URL: http://localhost:3000
Valid redirect URIs:
  - http://localhost:3000/*
  - http://localhost:3000/auth/callback
  - http://localhost:3000/auth/callback/*
Valid post logout redirect URIs:
  - http://localhost:3000/*
Web origins:
  - http://localhost:3000
  - +
Admin URL: (leave empty)
```

### 5. Tab Advanced Settings

```
Proof Key for Code Exchange Code Challenge Method: S256
Access Token Lifespan: 5 Minutes
Client Session Idle: 30 Minutes
Client Session Max: 12 Hours
Client Offline Session Idle: Offline Session Max
Client Offline Session Max: Offline Session Idle
```

### 6. Tab Capability config (nếu có tab riêng)

```
Client authentication: OFF
Authorization: OFF
Authentication flow:
  ✅ Standard flow
  ✅ Direct access grants
  ❌ Implicit flow
  ❌ Service accounts roles
```

## Các bước khắc phục

### Bước 1: Clear browser data
1. Mở DevTools → Application → Storage
2. Clear tất cả cookies cho localhost:8080 và localhost:3000
3. Clear localStorage và sessionStorage

### Bước 2: Restart Keycloak
```bash
docker-compose restart keycloak
```

### Bước 3: Test lại
1. Truy cập http://localhost:3000
2. Click "Đăng nhập với Keycloak"
3. Nhập user/password
4. Kiểm tra browser console logs

### Bước 4: Debug logs

Trong browser console, bạn sẽ thấy:
```
🔄 Initializing Keycloak auth...
Keycloak initialized, authenticated: false
🚀 Starting Keycloak login process...
🔐 Login attempt with config: {...}
```

Sau khi đăng nhập thành công:
```
Keycloak initialized, authenticated: true
User authenticated, token: eyJ...
🔄 Loading user profile from backend...
🔑 Token set in localStorage: eyJ...
```

### Bước 5: Kiểm tra Network tab

Trong Network tab, kiểm tra:
1. Request GET `/auth` → Status 302 (redirect)
2. Request POST `/token` → Status 200 (success)
3. Request GET `/api/auth/profile` → Status 200 hoặc 401

Nếu thấy 401 ở step 3, check token format và backend logs.

## Troubleshooting bổ sung

### Nếu vẫn lỗi PKCE:
Tạm thời tắt PKCE:
- Proof Key for Code Exchange Code Challenge Method: (empty)

### Nếu vẫn lỗi redirect:
Kiểm tra exact match:
- Valid redirect URIs phải chính xác là: `http://localhost:3000/auth/callback`

### Nếu token bị reject:
1. Kiểm tra backend JWT decoder configuration
2. Kiểm tra realm issuer URL
3. Kiểm tra client audience

## Test manual

Thử test flow bằng tay:

1. **Authorization Code URL:**
```
http://localhost:8080/realms/sss-realm/protocol/openid-connect/auth?client_id=sss-frontend&redirect_uri=http://localhost:3000/auth/callback&state=random&response_type=code&scope=openid
```

2. **Token Exchange (thay CODE_HERE bằng code thực):**
```bash
curl -X POST "http://localhost:8080/realms/sss-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "client_id=sss-frontend" \
  -d "code=CODE_HERE" \
  -d "redirect_uri=http://localhost:3000/auth/callback"
```

Nếu thành công, bạn sẽ nhận được access_token.
