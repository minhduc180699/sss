package com.sss.user.infrastructure.dto;

import com.sss.user.domain.enumeration.UserType;
import com.sss.user.domain.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO cho response của user data
 * 
 * @author : AI Assistant
 * @date : 08/01/2025
 */
@Data
@Builder
public class UserResponseDto {
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
    
    // Display methods
    public String getDisplayName() {
        if (userType == UserType.CHARACTER && characterName != null && !characterName.isEmpty()) {
            return characterName;
        }
        return fullName != null ? fullName : username;
    }
    
    public String getDisplayAvatar() {
        if (userType == UserType.CHARACTER && avatarUrl != null && !avatarUrl.isEmpty()) {
            return avatarUrl;
        }
        return profilePictureUrl != null ? profilePictureUrl : avatarUrl;
    }
    
    /**
     * Convert từ User domain object
     */
    public static UserResponseDto fromUser(User user) {
        return UserResponseDto.builder()
            .id(user.getId().value())
            .username(user.getUsername())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .address(user.getAddress())
            .userType(user.getUserType())
            .characterName(user.getCharacterName())
            .animeMangaSource(user.getAnimeMangaSource())
            .characterDescription(user.getCharacterDescription())
            .avatarUrl(user.getAvatarUrl())
            .coverImageUrl(user.getCoverImageUrl())
            .characterStatus(user.getCharacterStatus())
            .bio(user.getBio())
            .profilePictureUrl(user.getProfilePictureUrl())
            .dateOfBirth(user.getDateOfBirth())
            .gender(user.getGender())
            .location(user.getLocation())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .isLoggedIn(user.isLoggedIn())
            .isVerified(user.isVerified())
            .isActive(user.isActive())
            .build();
    }
}
