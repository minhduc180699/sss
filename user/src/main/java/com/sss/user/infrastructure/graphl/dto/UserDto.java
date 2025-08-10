package com.sss.user.infrastructure.graphl.dto;

import com.sss.user.domain.enumeration.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private UserType userType;
    
    // Thông tin cho nhân vật
    private String characterName;
    private String animeMangaSource;
    private String characterDescription;
    private String avatarUrl;
    private String coverImageUrl;
    private String characterStatus;
    
    // Thông tin cho người dùng thật
    private String bio;
    private String profilePictureUrl;
    private String dateOfBirth;
    private String gender;
    private String location;
    
    // Thông tin chung
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isLoggedIn;
    private boolean isVerified;
    private boolean isActive;
}

