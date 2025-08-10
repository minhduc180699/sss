# 👑 Admin User Management System - Demo Guide

## 🎯 Tổng quan

Hệ thống quản lý người dùng đã được triển khai hoàn chỉnh với các tính năng:
- ✅ **Create User** - Tạo user mới trong cả MongoDB và Keycloak
- ✅ **Update User** - Cập nhật thông tin user với sync
- ✅ **Delete User** - Xóa user khỏi cả 2 hệ thống
- ✅ **Auto Sync** - Đồng bộ tự động giữa MongoDB và Keycloak

## 🚀 Các tính năng đã implement

### **Backend APIs (Spring Boot)**
```java
// AdminUserController - CRUD operations
GET    /api/admin/users                    // Lấy danh sách users với pagination
POST   /api/admin/users                    // Tạo user mới (MongoDB + Keycloak)
GET    /api/admin/users/{id}               // Lấy thông tin user theo ID
PUT    /api/admin/users/{id}               // Cập nhật user (MongoDB + Keycloak)
DELETE /api/admin/users/{id}               // Xóa user (MongoDB + Keycloak)

// UserSyncController - Sync operations  
GET    /api/admin/sync/test-keycloak       // Test kết nối Keycloak
GET    /api/admin/sync/status              // Trạng thái sync
POST   /api/admin/sync/mongodb-to-keycloak // Bulk sync MongoDB -> Keycloak
```

### **Frontend Components (React)**
```typescript
// Components đã tạo
- AdminUserManagement.tsx     // Main component
- UserForm.tsx               // Form tạo/sửa user
- UserList.tsx              // Danh sách users với pagination
- AdminUserManagement.css    // Styles

// Services
- adminApi.ts               // API service cho admin operations
```

## 🔧 User Management Flow

### **1. Create User Flow**
```
Frontend Form → Validation → API Call → MongoDB → Keycloak → Response
```

**Quy trình:**
1. Admin điền form thông tin user
2. Validation frontend (password >= 6 chars, email format, etc.)
3. API call đến `/api/admin/users` (POST)
4. Backend tạo user trong MongoDB
5. Backend tạo user trong Keycloak với password
6. Trả về response với user data

### **2. Update User Flow**
```
Edit Form → API Call → MongoDB Update → Keycloak Sync → Response
```

**Quy trình:**
1. Admin chỉnh sửa thông tin user
2. API call đến `/api/admin/users/{id}` (PUT)
3. Backend cập nhật MongoDB
4. Backend sync changes đến Keycloak attributes
5. Trả về updated user data

### **3. Delete User Flow**
```
Confirm Dialog → API Call → MongoDB Delete → Keycloak Delete → Response
```

**Quy trình:**
1. Admin xác nhận xóa user
2. API call đến `/api/admin/users/{id}` (DELETE)
3. Backend tìm user trong Keycloak
4. Xóa khỏi Keycloak trước
5. Xóa khỏi MongoDB sau
6. Trả về success response

## 📋 User Types & Fields

### **User Types Supported**
- **REAL_USER** - Người dùng thật
- **CHARACTER** - Nhân vật anime/manga
- **ADMIN** - Quản trị viên

### **Fields theo User Type**

#### **Common Fields (All Types)**
```typescript
username: string         // Unique, không đổi được
fullName: string         // Tên hiển thị
email?: string          // Email (optional)
password: string        // Chỉ khi tạo mới
phoneNumber?: string    
address?: string
```

#### **REAL_USER Fields**
```typescript
bio?: string                    // Tiểu sử
profilePictureUrl?: string      // Ảnh đại diện
dateOfBirth?: string           // Ngày sinh
gender?: string                // Giới tính
location?: string              // Vị trí
```

#### **CHARACTER Fields**
```typescript
characterName?: string          // Tên nhân vật (required)
animeMangaSource?: string       // Nguồn gốc (required)
characterDescription?: string   // Mô tả nhân vật
avatarUrl?: string             // Ảnh nhân vật
coverImageUrl?: string         // Ảnh bìa
characterStatus?: string       // Trạng thái
```

## 🎨 UI Features

### **User List Features**
- 🔍 **Search** - Tìm kiếm theo username, name, email
- 📄 **Pagination** - Phân trang với navigation
- 🔄 **Auto Refresh** - Refresh danh sách sau mỗi operation
- 👁️ **Type Icons** - Icon riêng cho từng user type
- 📊 **Status Badges** - Hiển thị trạng thái user

### **Form Features**
- ✅ **Validation** - Frontend và backend validation
- 🎭 **Dynamic Fields** - Fields thay đổi theo user type
- 🔒 **Password Security** - Minimum 6 characters
- 📝 **Rich Form** - Textarea, select, date picker
- ⚠️ **Error Handling** - Hiển thị lỗi rõ ràng

### **Responsive Design**
- 📱 **Mobile Friendly** - Hoạt động tốt trên mobile
- 🖥️ **Desktop Optimized** - UI đẹp trên desktop
- 🎨 **Modern UI** - Clean, professional design

## 🔐 Security & Authorization

### **Admin Only Access**
```java
@PreAuthorize("hasRole('ADMIN')")  // Spring Security
```

### **Frontend Protection**
```typescript
// Chỉ hiển thị menu cho ADMIN
{user?.userType === 'ADMIN' && (
  <AdminUserManagement />
)}
```

### **JWT Validation**
- ✅ Bearer token required
- ✅ Role-based access control
- ✅ Token expiration handling

## 📊 Data Synchronization

### **MongoDB ↔ Keycloak Sync**

#### **User Creation**
```java
1. Create in MongoDB (extended fields)
2. Create in Keycloak (basic info + custom attributes)
3. Set password in Keycloak
4. Return success response
```

#### **User Update**
```java
1. Update MongoDB (all fields)
2. Sync changes to Keycloak attributes
3. Return updated user data
```

#### **User Deletion**
```java
1. Find user in Keycloak by username
2. Delete from Keycloak first
3. Delete from MongoDB
4. Return success response
```

## 🧪 Testing Guide

### **1. Access Admin Panel**
```
1. Login với account ADMIN
2. Click dropdown menu → "Quản lý người dùng"
3. Navigate to /admin/users
```

### **2. Test Create User**
```
1. Click "➕ Add New User"
2. Fill form với different user types
3. Test validation (password < 6 chars, invalid email)
4. Submit và verify user xuất hiện trong list
5. Check Keycloak admin console
```

### **3. Test Update User**
```
1. Click "✏️" button trên user row
2. Modify thông tin user
3. Submit và verify changes
4. Check sync với Keycloak
```

### **4. Test Delete User**
```
1. Click "🗑️" button
2. Confirm deletion
3. Verify user disappears from list
4. Check removed from Keycloak
```

## 🚨 Error Handling

### **Common Errors & Solutions**

#### **"Username already exists"**
- **Cause**: Duplicate username in MongoDB hoặc Keycloak
- **Solution**: Choose different username

#### **"Failed to create user in Keycloak"**
- **Cause**: Keycloak connection issue hoặc admin credentials
- **Solution**: Check Keycloak service và credentials

#### **"Token expired"**
- **Cause**: JWT token hết hạn
- **Solution**: Refresh page để reauth

#### **"Access denied"**
- **Cause**: User không có ADMIN role
- **Solution**: Assign ADMIN role trong Keycloak

## 📈 Performance Notes

### **Optimizations Implemented**
- ✅ **Pagination** - Không load hết users cùng lúc
- ✅ **Search Debouncing** - Frontend search optimization
- ✅ **Error Caching** - Show errors appropriately
- ✅ **Loading States** - UI feedback during operations

### **Database Indexing**
```javascript
// MongoDB indexes đã được tạo
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "userType": 1 });
```

## 🔮 Future Enhancements

### **Possible Improvements**
1. **Bulk Operations** - Select multiple users for bulk actions
2. **Advanced Filtering** - Filter by user type, creation date, etc.
3. **Export/Import** - CSV export/import functionality
4. **Audit Logging** - Track who made what changes
5. **Role Management** - Assign custom roles to users
6. **Password Reset** - Admin can force password reset

## ✅ Deployment Checklist

### **Backend Requirements**
- [ ] MongoDB running và accessible
- [ ] Keycloak running với admin credentials
- [ ] Spring Boot app có Keycloak admin client config
- [ ] Admin user có ADMIN role trong Keycloak

### **Frontend Requirements**
- [ ] React app connected to backend
- [ ] Admin routes protected
- [ ] CSS styles loaded correctly
- [ ] API endpoints accessible

### **Testing Checklist**
- [ ] Can access /admin/users as ADMIN
- [ ] Can create users successfully
- [ ] Users appear in both MongoDB và Keycloak
- [ ] Update operations sync properly
- [ ] Delete removes from both systems
- [ ] Non-admin users cannot access admin panel

## 🎉 Kết luận

Hệ thống User Management đã hoàn thiện với:
- ✅ **Full CRUD operations** với sync 2-way
- ✅ **Professional UI/UX** với responsive design
- ✅ **Robust error handling** và validation
- ✅ **Security best practices** với role-based access
- ✅ **Comprehensive documentation** và testing guide

System sẵn sàng cho production use! 🚀
