package com.sss.user.domain.model;

import com.sss.user.domain.enumeration.UserType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Ducpm56
 * @date : 06/08/2025
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  private UserId id;
  private String username;
  private String fullName;
  private String email;
  private String password;
  private String phoneNumber;
  private String address;
  private UserType userType;
  
  // Thông tin cho nhân vật truyện tranh/anime
  private String characterName;        // Tên nhân vật
  private String animeMangaSource;     // Nguồn gốc (anime/manga nào)
  private String characterDescription; // Mô tả nhân vật
  private String avatarUrl;            // Ảnh đại diện
  private String coverImageUrl;        // Ảnh bìa
  private String characterStatus;      // Trạng thái nhân vật (active, inactive, etc.)
  
  // Thông tin cho người dùng thật
  private String bio;                  // Tiểu sử người dùng
  private String profilePictureUrl;    // Ảnh đại diện người dùng
  private String dateOfBirth;          // Ngày sinh
  private String gender;               // Giới tính
  private String location;             // Vị trí địa lý
  
  // Thông tin chung
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean isLoggedIn;
  private boolean isVerified;          // Xác thực tài khoản
  private boolean isActive;            // Trạng thái hoạt động

  public void updateProfile(String fullName, String email, String phoneNumber, String address) {
    this.fullName = fullName;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.address = address;
    this.updatedAt = LocalDateTime.now();
  }

  public void updateCharacterInfo(String characterName, String animeMangaSource, 
                                 String characterDescription, String avatarUrl) {
    this.characterName = characterName;
    this.animeMangaSource = animeMangaSource;
    this.characterDescription = characterDescription;
    this.avatarUrl = avatarUrl;
    this.updatedAt = LocalDateTime.now();
  }

  public void updateUserInfo(String bio, String profilePictureUrl, 
                           String dateOfBirth, String gender, String location) {
    this.bio = bio;
    this.profilePictureUrl = profilePictureUrl;
    this.dateOfBirth = dateOfBirth;
    this.gender = gender;
    this.location = location;
    this.updatedAt = LocalDateTime.now();
  }

  public boolean isAdmin() {
    return this.userType == UserType.ADMIN;
  }

  public boolean isRealUser() {
    return this.userType == UserType.REAL_USER;
  }

  public boolean isCharacter() {
    return this.userType == UserType.CHARACTER;
  }

  public void login(String inputPassword) {
    if (!this.password.equals(inputPassword)) {
      throw new IllegalArgumentException("Invalid password");
    }
    this.isLoggedIn = true;
  }

  public void logout() {
    this.isLoggedIn = false;
  }

  public String getDisplayName() {
    if (isCharacter() && characterName != null && !characterName.isEmpty()) {
      return characterName;
    }
    return fullName != null ? fullName : username;
  }

  public String getDisplayAvatar() {
    if (isCharacter() && avatarUrl != null && !avatarUrl.isEmpty()) {
      return avatarUrl;
    }
    return profilePictureUrl != null ? profilePictureUrl : avatarUrl;
  }
}
