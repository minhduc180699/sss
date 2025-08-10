package com.sss.user.infrastructure.dto;

import com.sss.user.domain.enumeration.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho việc tạo user mới
 * 
 * @author : AI Assistant
 * @date : 08/01/2025
 */
public record CreateUserRequest(
    @NotBlank(message = "Username is required")
    String username,
    
    @NotBlank(message = "Full name is required")
    String fullName,
    
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Password is required")
    String password,
    
    String phoneNumber,
    String address,
    
    @NotNull(message = "User type is required")
    UserType userType,
    
    // Thông tin cho người dùng thật
    String bio,
    String profilePictureUrl,
    String dateOfBirth,
    String gender,
    String location,
    
    // Thông tin cho nhân vật
    String characterName,
    String animeMangaSource,
    String characterDescription,
    String avatarUrl,
    String coverImageUrl,
    String characterStatus
) {
    
    /**
     * Validate password strength
     */
    public boolean isPasswordValid() {
        return password != null && password.length() >= 6;
    }
    
    /**
     * Get display name based on user type
     */
    public String getDisplayName() {
        if (userType == UserType.CHARACTER && characterName != null && !characterName.trim().isEmpty()) {
            return characterName;
        }
        return fullName;
    }
}
