package com.sss.user.infrastructure.api;

import com.sss.user.application.UserService;
import com.sss.user.domain.model.User;
import com.sss.user.domain.model.UserId;
import com.sss.user.infrastructure.dto.UpdateUserRequest;

import com.sss.user.infrastructure.sync.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author : Ducpm56
 * @date : 06/08/2025
 **/
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final UserService userService;
  private final UserSyncService userSyncService;

  @GetMapping("/me")
  public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
    try {
      if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
        log.warn("⚠️ No JWT authentication found in /me endpoint");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Authentication required");
        return ResponseEntity.status(401).body(errorResponse);
      }

      Jwt jwt = (Jwt) authentication.getPrincipal();
      String username = jwt.getClaimAsString("preferred_username");
      log.info("🔍 Getting current user profile for: {}", username);
      
      // Sync user from Keycloak and get from MongoDB
      User user = userSyncService.syncUserFromKeycloak(jwt);
      
      Map<String, Object> userData = convertUserToMap(user);
      
      Map<String, Object> apiResponse = new HashMap<>();
      apiResponse.put("success", true);
      apiResponse.put("data", userData);
      apiResponse.put("message", "User retrieved successfully");
      
      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      log.error("❌ Error getting current user: {}", e.getMessage());
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String id) {
    try {
      UserId userId = new UserId(id);
      Optional<User> userOpt = userService.getUserById(userId);
      
      if (userOpt.isEmpty()) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "User not found");
        return ResponseEntity.status(404).body(errorResponse);
      }
      
      User user = userOpt.get();
      Map<String, Object> userData = convertUserToMap(user);
      
      Map<String, Object> apiResponse = new HashMap<>();
      apiResponse.put("success", true);
      apiResponse.put("data", userData);
      apiResponse.put("message", "User retrieved successfully");
      
      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      log.error("❌ Error getting user by ID {}: {}", id, e.getMessage());
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<Map<String, Object>> updateUser(@PathVariable String id, 
                                                       @RequestBody UpdateUserRequest userData,
                                                       Authentication authentication) {
    try {
      // Verify authentication
      if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Authentication required");
        return ResponseEntity.status(401).body(errorResponse);
      }

      Jwt jwt = (Jwt) authentication.getPrincipal();
      User currentUser = userSyncService.syncUserFromKeycloak(jwt);
      
      // Check permissions: user can only update their own profile, unless they're admin
      UserId targetUserId = new UserId(id);
      boolean isAdmin = authentication.getAuthorities().stream()
          .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
      boolean isOwnProfile = currentUser.getId().equals(targetUserId);
      
      if (!isAdmin && !isOwnProfile) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Access denied: You can only update your own profile");
        return ResponseEntity.status(403).body(errorResponse);
      }
      
      log.info("🔍 User {} updating profile for user {}", currentUser.getUsername(), id);
      User updatedUser = userService.updateUserProfile(targetUserId, userData);
      
      Map<String, Object> userResponse = convertUserToMap(updatedUser);
      
      Map<String, Object> apiResponse = new HashMap<>();
      apiResponse.put("success", true);
      apiResponse.put("data", userResponse);
      apiResponse.put("message", "User updated successfully");
      
      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      log.error("❌ Error updating user {}: {}", id, e.getMessage());
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("success", false);
      errorResponse.put("message", e.getMessage());
      return ResponseEntity.status(500).body(errorResponse);
    }
  }

  /**
   * Helper method to convert User domain object to Map response
   */
  private Map<String, Object> convertUserToMap(User user) {
    Map<String, Object> userData = new HashMap<>();
    userData.put("id", user.getId().value());
    userData.put("username", user.getUsername());
    userData.put("email", user.getEmail());
    userData.put("fullName", user.getFullName());
    userData.put("userType", user.getUserType().toString());
    userData.put("phoneNumber", user.getPhoneNumber());
    userData.put("address", user.getAddress());
    userData.put("bio", user.getBio());
    userData.put("profilePictureUrl", user.getProfilePictureUrl());
    userData.put("dateOfBirth", user.getDateOfBirth());
    userData.put("gender", user.getGender());
    userData.put("location", user.getLocation());
    userData.put("createdAt", user.getCreatedAt());
    userData.put("updatedAt", user.getUpdatedAt());
    userData.put("isLoggedIn", user.isLoggedIn());
    userData.put("isVerified", user.isVerified());
    userData.put("isActive", user.isActive());
    
    // Thông tin nhân vật (nếu có)
    if (user.getUserType().toString().equals("CHARACTER")) {
      userData.put("characterName", user.getCharacterName());
      userData.put("animeMangaSource", user.getAnimeMangaSource());
      userData.put("characterDescription", user.getCharacterDescription());
      userData.put("avatarUrl", user.getAvatarUrl());
      userData.put("coverImageUrl", user.getCoverImageUrl());
      userData.put("characterStatus", user.getCharacterStatus());
    }
    
    return userData;
  }
}
