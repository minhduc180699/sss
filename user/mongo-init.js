// Khởi tạo database và collections
db = db.getSiblingDB('sss_user_db');

// Tạo collection users nếu chưa có
db.createCollection('users');

// Tạo indexes cho collection users
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "userType": 1 }); // Index cho loại user
db.users.createIndex({ "characterName": 1 }); // Index cho tên nhân vật
db.users.createIndex({ "animeMangaSource": 1 }); // Index cho nguồn anime/manga
db.users.createIndex({ "isActive": 1 }); // Index cho trạng thái hoạt động
db.users.createIndex({ "isVerified": 1 }); // Index cho trạng thái xác thực

print('✅ Database sss_user_db initialized successfully!');
print('✅ Collection users created with indexes!');
print('✅ Indexes created for: username, email, userType, characterName, animeMangaSource, isActive, isVerified');
