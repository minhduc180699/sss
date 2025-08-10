# Troubleshooting Keycloak Authentication Issues

## Vấn đề hiện tại
Lỗi authentication khi sử dụng Keycloak với message về token exchange failure.

## Nguyên nhân có thể
1. **Redirect URIs không đúng** trong Keycloak client
2. **Client configuration** không chính xác
3. **PKCE flow** configuration issues

## Giải pháp từng bước

### 1. Kiểm tra và sửa Client Configuration

#### A. Kiểm tra Client `sss-frontend`

1. Vào Keycloak Admin Console: http://localhost:8080/admin
2. Chọn realm `sss-realm`
3. Vào **Clients** → chọn `sss-frontend`
4. Kiểm tra các cài đặt sau:

**Tab Settings:**
```
Client type: OpenID Connect
Client ID: sss-frontend
Name: SSS Frontend Application
Description: Frontend React Application

Capability config:
- Client authentication: OFF
- Authorization: OFF
- Authentication flow:
  ✅ Standard flow
  ✅ Direct access grants
  ✅ Implicit flow (có thể bật nếu cần)
  ❌ Service accounts roles
  ❌ OAuth 2.0 Device Authorization Grant

Login settings:
- Root URL: http://localhost:3000
- Home URL: http://localhost:3000
- Valid redirect URIs: 
  * http://localhost:3000/*
  * http://localhost:3000/auth/callback
- Valid post logout redirect URIs: http://localhost:3000/*
- Web origins: 
  * http://localhost:3000
  * +

Advanced settings:
- Proof Key for Code Exchange Code Challenge Method: S256
- Access Token Lifespan: 5 Minutes (hoặc tùy chỉnh)
- Client Session Idle: 30 Minutes
- Client Session Max: 12 Hours
```

### 2. Test với curl trực tiếp

Thử test authorization flow bằng tay:

```bash
# 1. Lấy authorization code
# Mở browser và truy cập:
http://localhost:8080/realms/sss-realm/protocol/openid-connect/auth?client_id=sss-frontend&redirect_uri=http://localhost:3000/auth/callback&state=random-state&response_type=code&scope=openid

# 2. Sau khi đăng nhập, copy authorization code từ URL redirect
# Sau đó exchange code để lấy token:
curl -X POST http://localhost:8080/realms/sss-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "client_id=sss-frontend" \
  -d "code=YOUR_AUTHORIZATION_CODE_HERE" \
  -d "redirect_uri=http://localhost:3000/auth/callback"
```

### 3. Kiểm tra logs

#### Frontend logs (Browser Console):
```
🔄 Initializing Keycloak auth...
Keycloak initialized, authenticated: false
✅ Auth initialization completed
🚀 Starting Keycloak login process...
```

#### Keycloak logs (Docker):
```bash
docker logs sss-keycloak
```

### 4. Thay thế cấu hình nếu cần

Nếu vẫn gặp lỗi, thử cấu hình đơn giản hơn:

#### Option 1: Tắt PKCE tạm thời
Trong Keycloak client settings:
- **Proof Key for Code Exchange Code Challenge Method**: (empty/none)

#### Option 2: Sử dụng Implicit Flow (không khuyến khích cho production)
```javascript
// Trong keycloak.ts
public async initKeycloak(): Promise<boolean> {
  try {
    const authenticated = await this.keycloakInstance.init({
      onLoad: 'check-sso',
      checkLoginIframe: false,
      flow: 'implicit'  // Thay vì 'standard'
    });
    // ...
  }
}
```

### 5. Debug chi tiết

Thêm debug logging trong keycloak.ts:

```javascript
public login(): Promise<void> {
  console.log('🔐 Login attempt with config:', {
    url: this.keycloakInstance.authServerUrl,
    realm: this.keycloakInstance.realm,
    clientId: this.keycloakInstance.clientId,
    redirectUri: window.location.origin + '/auth/callback'
  });
  
  return this.keycloakInstance.login({
    redirectUri: window.location.origin + '/auth/callback'
  });
}
```

### 6. Kiểm tra Network tab

Trong Browser DevTools → Network tab, kiểm tra:
1. Request đến `/auth` endpoint
2. Redirect về `/auth/callback`
3. Request đến `/token` endpoint
4. Response status và error messages

### 7. Common Issues & Solutions

#### Error: "invalid_client"
- Kiểm tra Client ID đúng chưa
- Kiểm tra Client authentication settings

#### Error: "invalid_redirect_uri"
- Kiểm tra Valid redirect URIs trong Keycloak
- Đảm bảo URL match chính xác

#### Error: "invalid_grant"
- Kiểm tra authorization code chưa expire
- Kiểm tra PKCE configuration

#### Error: "access_denied"
- Kiểm tra user có quyền truy cập realm không
- Kiểm tra user active và enabled

### 8. Kiểm tra cuối cùng

Đảm bảo:
1. ✅ Keycloak server running (http://localhost:8080)
2. ✅ Backend server running (http://localhost:8082) 
3. ✅ Frontend server running (http://localhost:3000)
4. ✅ User đã được tạo và enabled trong Keycloak
5. ✅ Client `sss-frontend` configured correctly
6. ✅ Realm `sss-realm` active

### 9. Reset và test lại

Nếu vẫn không được:
1. Clear browser cache và cookies
2. Restart Keycloak container
3. Recreate client trong Keycloak
4. Test với user mới
