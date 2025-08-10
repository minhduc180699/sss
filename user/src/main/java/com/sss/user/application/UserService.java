package com.sss.user.application;

import com.sss.user.domain.enumeration.UserType;
import com.sss.user.domain.exception.UserNotFoundException;
import com.sss.user.domain.model.User;
import com.sss.user.domain.model.UserId;
import com.sss.user.domain.repository.UserRepository;
import com.sss.user.infrastructure.dto.UpdateUserRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author : Ducpm56
 * @date : 06/08/2025
 **/
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User registerUser(String username, String email, String displayName, UserType type) {
    User user = User.builder()
        .id(UserId.generate())
        .username(username)
        .email(email)
        .fullName(displayName)
        .userType(type)
        .isActive(true)
        .isVerified(false)
        .isLoggedIn(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    return userRepository.save(user);
  }

  public User updateProfile(UserId userId, String displayName, String bio, String avatarUrl) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    
    // Cập nhật thông tin profile sử dụng method có sẵn
    user.updateUserInfo(bio, avatarUrl, null, null, null);
    user.updateProfile(displayName, user.getEmail(), user.getPhoneNumber(), user.getAddress());
    
    return userRepository.save(user);
  }

  public User updateUserProfile(UserId userId, UpdateUserRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    
    // Cập nhật thông tin cơ bản
    if (request.fullName() != null) {
      user.setFullName(request.fullName());
    }
    if (request.email() != null) {
      user.setEmail(request.email());
    }
    if (request.phoneNumber() != null) {
      user.setPhoneNumber(request.phoneNumber());
    }
    if (request.address() != null) {
      user.setAddress(request.address());
    }
    
    // Cập nhật thông tin người dùng thật
    if (request.bio() != null) {
      user.setBio(request.bio());
    }
    if (request.profilePictureUrl() != null) {
      user.setProfilePictureUrl(request.profilePictureUrl());
    }
    if (request.dateOfBirth() != null) {
      user.setDateOfBirth(request.dateOfBirth());
    }
    if (request.gender() != null) {
      user.setGender(request.gender());
    }
    if (request.location() != null) {
      user.setLocation(request.location());
    }
    
    // Cập nhật thông tin nhân vật (nếu là character)
    if (user.getUserType() == UserType.CHARACTER) {
      if (request.characterName() != null) {
        user.setCharacterName(request.characterName());
      }
      if (request.animeMangaSource() != null) {
        user.setAnimeMangaSource(request.animeMangaSource());
      }
      if (request.characterDescription() != null) {
        user.setCharacterDescription(request.characterDescription());
      }
      if (request.avatarUrl() != null) {
        user.setAvatarUrl(request.avatarUrl());
      }
      if (request.coverImageUrl() != null) {
        user.setCoverImageUrl(request.coverImageUrl());
      }
    }
    
    // Cập nhật thời gian
    user.setUpdatedAt(LocalDateTime.now());
    
    return userRepository.save(user);
  }

  public Optional<User> getUserById(UserId id) {
    return userRepository.findById(id);
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public User login(String username, String password) {
    var user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    
    // Sử dụng method login có sẵn trong User
    user.login(password);
    
    return userRepository.save(user);
  }

  public User logout(String username) {
    var user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    
    // Sử dụng method logout có sẵn trong User
    user.logout();
    
    return userRepository.save(user);
  }

  public User findByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
  }

  /**
   * Create new user from Keycloak authentication
   */
  public User createUserFromKeycloak(String username, String email, String fullName) {
    User user = User.builder()
        .id(UserId.generate())
        .username(username)
        .email(email != null ? email : "")
        .fullName(fullName != null ? fullName : username)
        .userType(UserType.REAL_USER)
        .isActive(true)
        .isVerified(true) // Keycloak users are considered verified
        .isLoggedIn(true)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    return userRepository.save(user);
  }

  /**
   * Find user by email
   */
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  /**
   * Check if user exists by username
   */
  public boolean existsByUsername(String username) {
    return userRepository.findByUsername(username).isPresent();
  }

  /**
   * Check if user exists by email
   */
  public boolean existsByEmail(String email) {
    return userRepository.findByEmail(email).isPresent();
  }

  /**
   * Create user from admin request với đầy đủ thông tin
   */
  public User createUserFromAdmin(String username, String email, String fullName, String password,
                                 UserType userType, String phoneNumber, String address,
                                 String bio, String profilePictureUrl, String dateOfBirth,
                                 String gender, String location, String characterName,
                                 String animeMangaSource, String characterDescription,
                                 String avatarUrl, String coverImageUrl, String characterStatus) {
    User user = User.builder()
        .id(UserId.generate())
        .username(username)
        .email(email != null ? email : "")
        .fullName(fullName)
        .password(password) // Store hashed password in production
        .userType(userType)
        .phoneNumber(phoneNumber)
        .address(address)
        .bio(bio)
        .profilePictureUrl(profilePictureUrl)
        .dateOfBirth(dateOfBirth)
        .gender(gender)
        .location(location)
        .characterName(characterName)
        .animeMangaSource(animeMangaSource)
        .characterDescription(characterDescription)
        .avatarUrl(avatarUrl)
        .coverImageUrl(coverImageUrl)
        .characterStatus(characterStatus)
        .isActive(true)
        .isVerified(true)
        .isLoggedIn(false)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
    return userRepository.save(user);
  }

  /**
   * Delete user by ID
   */
  public void deleteUser(UserId userId) {
    if (!userRepository.findById(userId).isPresent()) {
      throw new UserNotFoundException("User not found");
    }
    userRepository.delete(userId);
  }
}
