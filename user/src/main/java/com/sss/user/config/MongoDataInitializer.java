package com.sss.user.config;

import com.sss.user.domain.enumeration.UserType;
import com.sss.user.domain.model.User;
import com.sss.user.domain.model.UserId;
import com.sss.user.infrastructure.persistence.UserDocument;
import com.sss.user.infrastructure.persistence.UserMapper;
import com.sss.user.infrastructure.persistence.SpringDataUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class MongoDataInitializer implements CommandLineRunner {

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(String... args) throws Exception {
        // Tạo users mặc định
        createDefaultUsers();
    }

    private void createDefaultUsers() {
        // Tạo admin user
        createAdminUser();
        
        // Tạo real user mẫu
        createSampleRealUser();
        
        // Tạo character users mẫu
        createSampleCharacters();
    }

    private void createAdminUser() {
        Optional<UserDocument> existingAdmin = userRepository.findByUsername("admin");
        
        if (existingAdmin.isEmpty()) {
            User adminUser = User.builder()
                    .id(UserId.generate())
                    .username("admin")
                    .fullName("System Administrator")
                    .email("admin@sss.com")
                    .password("admin123")
                    .phoneNumber("0123456789")
                    .address("Hanoi, Vietnam")
                    .userType(UserType.ADMIN)
                    .bio("Quản trị viên hệ thống SSS")
                    .isVerified(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isLoggedIn(false)
                    .build();

            UserDocument adminDocument = userMapper.toDocument(adminUser);
            userRepository.save(adminDocument);
            
            System.out.println("✅ Admin user created successfully!");
            System.out.println("Username: admin | Password: admin123");
        }
    }

    private void createSampleRealUser() {
        Optional<UserDocument> existingUser = userRepository.findByUsername("user");
        
        if (existingUser.isEmpty()) {
            User realUser = User.builder()
                    .id(UserId.generate())
                    .username("user")
                    .fullName("Nguyễn Văn A")
                    .email("user@sss.com")
                    .password("user123")
                    .phoneNumber("0987654321")
                    .address("Ho Chi Minh City, Vietnam")
                    .userType(UserType.REAL_USER)
                    .bio("Fan cuồng anime và manga! 🎌")
                    .profilePictureUrl("https://example.com/avatar1.jpg")
                    .dateOfBirth("1995-05-15")
                    .gender("Male")
                    .location("Ho Chi Minh City")
                    .isVerified(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isLoggedIn(false)
                    .build();

            UserDocument userDocument = userMapper.toDocument(realUser);
            userRepository.save(userDocument);
            
            System.out.println("✅ Sample real user created successfully!");
            System.out.println("Username: user | Password: user123");
        }
    }

    private void createSampleCharacters() {
        // Tạo nhân vật Naruto
        createCharacter("naruto", "Naruto Uzumaki", "Naruto", 
                       "Nhân vật chính trong series Naruto", "https://example.com/naruto.jpg");
        
        // Tạo nhân vật Goku
        createCharacter("goku", "Son Goku", "Dragon Ball", 
                       "Nhân vật chính trong series Dragon Ball", "https://example.com/goku.jpg");
        
        // Tạo nhân vật Luffy
        createCharacter("luffy", "Monkey D. Luffy", "One Piece", 
                       "Thuyền trưởng băng hải tặc Mũ Rơm", "https://example.com/luffy.jpg");
    }

    private void createCharacter(String username, String characterName, String source, 
                               String description, String avatarUrl) {
        Optional<UserDocument> existingChar = userRepository.findByUsername(username);
        
        if (existingChar.isEmpty()) {
            User character = User.builder()
                    .id(UserId.generate())
                    .username(username)
                    .fullName(characterName)
                    .email(username + "@character.sss.com")
                    .password("character123")
                    .userType(UserType.CHARACTER)
                    .characterName(characterName)
                    .animeMangaSource(source)
                    .characterDescription(description)
                    .avatarUrl(avatarUrl)
                    .coverImageUrl(avatarUrl)
                    .characterStatus("active")
                    .isVerified(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isLoggedIn(false)
                    .build();

            UserDocument charDocument = userMapper.toDocument(character);
            userRepository.save(charDocument);
            
            System.out.println("✅ Character '" + characterName + "' created successfully!");
            System.out.println("Username: " + username + " | Password: character123");
        }
    }
}
