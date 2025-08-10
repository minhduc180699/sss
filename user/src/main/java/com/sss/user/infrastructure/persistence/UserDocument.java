package com.sss.user.infrastructure.persistence;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * @author : Ducpm56
 * @date : 06/08/2025
 **/
@Document("users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDocument {
  @Id
  private String id;
  
  @Indexed(unique = true)
  private String username;
  
  private String fullName;
  
  @Indexed(unique = true)
  private String email;
  
  private String password;
  
  private String phoneNumber;
  
  private String address;
  
  private String userType; // REAL_USER, CHARACTER, ADMIN
  
  // Thông tin cho nhân vật truyện tranh/anime
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
