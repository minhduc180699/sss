package com.sss.user.infrastructure.sync;

import com.sss.user.application.UserService;
import com.sss.user.domain.enumeration.UserType;
import com.sss.user.domain.exception.UserNotFoundException;
import com.sss.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service để đồng bộ user data giữa Keycloak và MongoDB
 * 
 * @author : AI Assistant
 * @date : 08/01/2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserService userService;
    private final KeycloakUserService keycloakUserService;

    /**
     * Đồng bộ user từ Keycloak JWT token
     * Tạo mới hoặc cập nhật user trong MongoDB dựa trên JWT claims
     */
    public User syncUserFromKeycloak(Jwt jwt) {
        log.info("🔄 Starting user sync from Keycloak JWT");
        
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String fullName = jwt.getClaimAsString("name");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");
        
        // Extract roles from JWT
        List<String> roles = extractRolesFromJwt(jwt);
        UserType userType = mapRolesToUserType(roles);
        
        log.info("🔍 Syncing user: {} with roles: {}", username, roles);
        
        try {
            // Try to find existing user
            User existingUser = userService.findByUsername(username);
            log.info("✅ Found existing user: {}", username);
            
            // Update user info from Keycloak if changed
            return updateUserFromKeycloak(existingUser, email, fullName, userType);
            
        } catch (UserNotFoundException e) {
            log.info("🔄 User not found, creating new user from Keycloak: {}", username);
            
            // Create new user
            return createUserFromKeycloak(username, email, fullName, userType);
        }
    }

    /**
     * Tạo user mới từ Keycloak data
     */
    private User createUserFromKeycloak(String username, String email, String fullName, UserType userType) {
        User newUser = userService.createUserFromKeycloak(username, email, fullName);
        
        // Set user type based on Keycloak roles
        newUser.setUserType(userType);
        newUser.setUpdatedAt(LocalDateTime.now());
        
        log.info("✅ Created new user: {} with type: {}", username, userType);
        return userService.updateUserProfile(newUser.getId(), 
            createUpdateRequestFromUser(newUser));
    }

    /**
     * Cập nhật user hiện có từ Keycloak data
     */
    private User updateUserFromKeycloak(User existingUser, String email, String fullName, UserType userType) {
        boolean needsUpdate = false;
        
        // Check if basic info changed
        if (email != null && !email.equals(existingUser.getEmail())) {
            existingUser.setEmail(email);
            needsUpdate = true;
        }
        
        if (fullName != null && !fullName.equals(existingUser.getFullName())) {
            existingUser.setFullName(fullName);
            needsUpdate = true;
        }
        
        // Update user type if role changed
        if (userType != existingUser.getUserType()) {
            existingUser.setUserType(userType);
            needsUpdate = true;
            log.info("🔄 User type changed from {} to {} for user: {}", 
                existingUser.getUserType(), userType, existingUser.getUsername());
        }
        
        if (needsUpdate) {
            existingUser.setUpdatedAt(LocalDateTime.now());
            log.info("🔄 Updating user: {} with new info from Keycloak", existingUser.getUsername());
            return userService.updateUserProfile(existingUser.getId(), 
                createUpdateRequestFromUser(existingUser));
        }
        
        return existingUser;
    }

    /**
     * Extract roles từ JWT token
     */
    @SuppressWarnings("unchecked")
    private List<String> extractRolesFromJwt(Jwt jwt) {
        // Keycloak stores roles in realm_access.roles
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof java.util.Map) {
            Object roles = ((java.util.Map<String, Object>) realmAccess).get("roles");
            if (roles instanceof List) {
                return (List<String>) roles;
            }
        }
        
        // Fallback: check resource_access for client-specific roles
        Object resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess instanceof java.util.Map) {
            Object clientAccess = ((java.util.Map<String, Object>) resourceAccess).get("sss-backend");
            if (clientAccess instanceof java.util.Map) {
                Object clientRoles = ((java.util.Map<String, Object>) clientAccess).get("roles");
                if (clientRoles instanceof List) {
                    return (List<String>) clientRoles;
                }
            }
        }
        
        return List.of(); // Return empty list if no roles found
    }

    /**
     * Map Keycloak roles to UserType
     */
    private UserType mapRolesToUserType(List<String> roles) {
        if (roles.contains("admin") || roles.contains("ADMIN")) {
            return UserType.ADMIN;
        } else if (roles.contains("character") || roles.contains("CHARACTER")) {
            return UserType.CHARACTER;
        } else {
            return UserType.REAL_USER; // Default type
        }
    }

    /**
     * Đồng bộ user từ MongoDB sang Keycloak
     * Cập nhật Keycloak user attributes dựa trên MongoDB data
     */
    public void syncUserToKeycloak(User user) {
        log.info("🔄 Syncing user to Keycloak: {}", user.getUsername());
        
        try {
            KeycloakUserDto keycloakUser = keycloakUserService.findUserByUsername(user.getUsername());
            
            if (keycloakUser != null) {
                // Update Keycloak user attributes
                KeycloakUserUpdateDto updateDto = KeycloakUserUpdateDto.builder()
                    .email(user.getEmail())
                    .firstName(extractFirstName(user.getFullName()))
                    .lastName(extractLastName(user.getFullName()))
                    .build();
                
                // Add custom attributes
                updateDto.addAttribute("userType", user.getUserType().toString());
                updateDto.addAttribute("bio", user.getBio());
                updateDto.addAttribute("location", user.getLocation());
                
                if (user.isCharacter()) {
                    updateDto.addAttribute("characterName", user.getCharacterName());
                    updateDto.addAttribute("animeMangaSource", user.getAnimeMangaSource());
                }
                
                keycloakUserService.updateUser(keycloakUser.getId(), updateDto);
                log.info("✅ Updated Keycloak user: {}", user.getUsername());
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to sync user to Keycloak: {}", e.getMessage());
        }
    }

    /**
     * Đồng bộ bulk users từ MongoDB sang Keycloak
     */
    public void syncAllUsersToKeycloak() {
        log.info("🔄 Starting bulk sync from MongoDB to Keycloak");
        
        List<User> allUsers = userService.getAllUsers();
        
        for (User user : allUsers) {
            try {
                syncUserToKeycloak(user);
            } catch (Exception e) {
                log.error("❌ Failed to sync user {}: {}", user.getUsername(), e.getMessage());
            }
        }
        
        log.info("✅ Completed bulk sync. Processed {} users", allUsers.size());
    }

    // Helper methods
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split(" ");
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split(" ");
        if (parts.length > 1) {
            return String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        }
        return "";
    }

    private com.sss.user.infrastructure.dto.UpdateUserRequest createUpdateRequestFromUser(User user) {
        return new com.sss.user.infrastructure.dto.UpdateUserRequest(
            user.getFullName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getAddress(),
            user.getBio(),
            user.getProfilePictureUrl(),
            user.getDateOfBirth(),
            user.getGender(),
            user.getLocation(),
            user.getCharacterName(),
            user.getAnimeMangaSource(),
            user.getCharacterDescription(),
            user.getAvatarUrl(),
            user.getCoverImageUrl()
        );
    }
}
