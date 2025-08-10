# 🔄 User Synchronization Guide - SSS Social Network

## Tổng quan vấn đề

Hiện tại hệ thống SSS có **2 hệ thống user độc lập**:
- **Keycloak**: Quản lý authentication (username, email, roles)  
- **MongoDB**: Lưu extended user profiles (bio, character info, etc.)

## 🎯 Giải pháp đồng bộ

### **Kiến trúc Sync Solution**

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│  AuthController  │────▶│  UserSyncService│
│   (React)       │     │                  │     │                 │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                                                           │
                                ┌──────────────────────────┼──────────────────────────┐
                                ▼                          ▼                          ▼
                        ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
                        │   MongoDB       │     │   Keycloak      │     │ KeycloakUserSvc │
                        │   (Extended     │     │   (Auth +       │     │ (Admin API)     │
                        │    Profiles)    │     │    Basic Info)  │     │                 │
                        └─────────────────┘     └─────────────────┘     └─────────────────┘
```

### **Components được tạo**

1. **UserSyncService** - Core sync logic
2. **KeycloakUserService** - Keycloak Admin API integration  
3. **KeycloakUserDto** - Data transfer objects
4. **UserSyncController** - Admin endpoints cho sync operations

## 🔧 Cách hoạt động

### **1. Login Flow với Auto-Sync**

```java
// 1. User đăng nhập qua Keycloak
// 2. JWT token được validate
// 3. AuthController.getProfile() được gọi
// 4. UserSyncService.syncUserFromKeycloak(jwt) được trigger
// 5. System tự động:
//    - Tìm user trong MongoDB
//    - Nếu không có: tạo mới từ JWT claims
//    - Nếu có: cập nhật thông tin nếu thay đổi
//    - Map Keycloak roles thành UserType
```

### **2. Bidirectional Sync**

#### **Keycloak → MongoDB (Auto)**
- Trigger: Mỗi khi user login
- Logic: Extract JWT claims + roles → Update MongoDB
- Mapping: Keycloak roles → UserType enum

#### **MongoDB → Keycloak (Manual/Scheduled)**
- API: `POST /api/admin/sync/mongodb-to-keycloak`
- Logic: Sync user attributes từ MongoDB → Keycloak custom attributes
- Use case: Admin bulk sync, data migration

### **3. Role Mapping**

```java
Keycloak Roles → SSS UserType
├── "admin" / "ADMIN"     → UserType.ADMIN
├── "character"           → UserType.CHARACTER  
└── default               → UserType.REAL_USER
```

## 🚀 API Endpoints

### **Public Endpoints**
```bash
GET /api/auth/profile                    # Auto-sync on profile get
GET /api/admin/sync/test-keycloak       # Test Keycloak connection
GET /api/admin/sync/status              # Get sync status
```

### **Admin Endpoints** (requires ADMIN role)
```bash
POST /api/admin/sync/mongodb-to-keycloak           # Bulk sync to Keycloak
POST /api/admin/sync/user/{username}/to-keycloak   # Sync specific user
```

## ⚙️ Configuration

### **application.yml updates**
```yaml
keycloak:
  # ... existing config ...
  admin:
    username: admin
    password: admin123  
    client-id: admin-cli
```

### **Database updates**
```java
// UserRepository now supports:
Optional<User> findByEmail(String email);

// MongoDB indexes updated for email lookup
```

## 🧪 Testing Sync

### **1. Test Keycloak Connection**
```bash
curl -X GET http://localhost:8082/api/admin/sync/test-keycloak
```

### **2. Check Sync Status**  
```bash
curl -X GET http://localhost:8082/api/admin/sync/status
```

### **3. Manual Bulk Sync**
```bash
curl -X POST http://localhost:8082/api/admin/sync/mongodb-to-keycloak \
  -H "Authorization: Bearer YOUR_ADMIN_JWT"
```

## 📊 Sync Scenarios

### **Scenario 1: New User Registration**
1. User registers in Keycloak
2. First login → JWT contains new user info
3. `UserSyncService` auto-creates MongoDB profile
4. User gets default `REAL_USER` type

### **Scenario 2: Role Change**
1. Admin changes user role in Keycloak  
2. User logs in again
3. `UserSyncService` detects role change
4. MongoDB UserType gets updated

### **Scenario 3: Profile Update**
1. User updates profile in SSS frontend
2. MongoDB gets updated
3. (Optional) Manual sync pushes changes to Keycloak attributes

### **Scenario 4: Bulk Migration**
1. Admin triggers bulk sync
2. All MongoDB users sync to Keycloak
3. Custom attributes get populated

## 🔍 Monitoring & Debugging

### **Log patterns to watch:**
```
🔄 Starting user sync from Keycloak JWT
✅ User synced successfully: username
🔄 User type changed from REAL_USER to ADMIN for user: username
❌ Failed to sync user to Keycloak: error message
```

### **Key metrics:**
- Sync success/failure rates
- Number of users in each system
- Role mapping accuracy

## 🚨 Important Notes

### **Security**
- Keycloak admin credentials in application.yml (consider env vars)
- Sync endpoints require ADMIN role
- JWT validation still required

### **Performance**
- Auto-sync happens on every profile request
- Bulk sync can be resource intensive
- Consider caching for frequent operations

### **Data Consistency**
- Keycloak = source of truth for auth
- MongoDB = source of truth for extended profiles
- Bidirectional sync ensures consistency

## 🔮 Future Enhancements

1. **Real-time Sync**: Keycloak webhooks/events
2. **Conflict Resolution**: Handle data conflicts gracefully
3. **Audit Logging**: Track all sync operations
4. **Performance**: Caching layer for sync operations
5. **UI**: Admin dashboard for sync management

## ✅ Implementation Checklist

- [x] UserSyncService với JWT-based sync
- [x] KeycloakUserService với Admin API
- [x] AuthController integration
- [x] Role mapping logic
- [x] Bidirectional sync support
- [x] Admin endpoints
- [x] Configuration updates
- [x] Database schema updates
- [ ] Testing với real Keycloak instance
- [ ] Error handling improvements
- [ ] Documentation & monitoring

## 🎉 Kết quả

Sau khi implement solution này:
- ✅ **Auto-sync** mỗi khi user login
- ✅ **Consistent data** giữa 2 hệ thống
- ✅ **Role-based access** working properly
- ✅ **Admin tools** để manage sync
- ✅ **Scalable architecture** for future features
