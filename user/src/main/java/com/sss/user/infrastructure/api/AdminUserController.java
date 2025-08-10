package com.sss.user.infrastructure.api;

import com.sss.user.application.UserService;
import com.sss.user.domain.exception.UserNotFoundException;
import com.sss.user.domain.model.User;
import com.sss.user.domain.model.UserId;
import com.sss.user.infrastructure.dto.CreateUserRequest;
import com.sss.user.infrastructure.dto.UpdateUserRequest;
import com.sss.user.infrastructure.dto.UserResponseDto;
import com.sss.user.infrastructure.sync.UserSyncService;
import com.sss.user.infrastructure.sync.KeycloakUserService;
import com.sss.user.infrastructure.sync.KeycloakUserCreateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cho admin user management
 * 
 * @author : AI Assistant  
 * @date : 08/01/2025
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final UserSyncService userSyncService;
    private final KeycloakUserService keycloakUserService;

    /**
     * Lấy danh sách tất cả users với pagination
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {
        
        try {
            log.info("🔍 Admin getting all users - page: {}, size: {}, search: {}", page, size, search);
            
            // Tạo pageable object
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get users (hiện tại chưa có pagination trong UserService, sẽ implement)
            List<User> users = userService.getAllUsers();
            
            // Convert to DTOs
            List<UserResponseDto> userDtos = users.stream()
                .map(UserResponseDto::fromUser)
                .toList();
            
            // Filter by search if provided
            if (search != null && !search.trim().isEmpty()) {
                userDtos = userDtos.stream()
                    .filter(user -> 
                        user.getUsername().toLowerCase().contains(search.toLowerCase()) ||
                        user.getFullName().toLowerCase().contains(search.toLowerCase()) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(search.toLowerCase()))
                    )
                    .toList();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userDtos);
            response.put("total", userDtos.size());
            response.put("page", page);
            response.put("size", size);
            response.put("message", "Users retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error getting users: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get users: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Tạo user mới trong cả MongoDB và Keycloak
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            log.info("🔄 Admin creating new user: {}", request.username());
            
            // Validate password
            if (!request.isPasswordValid()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Password must be at least 6 characters long");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Check if username already exists
            if (userService.existsByUsername(request.username())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Username already exists");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Check if email already exists
            if (request.email() != null && userService.existsByEmail(request.email())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Email already exists");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 1. Tạo user trong MongoDB
            User newUser = createUserInMongoDB(request);
            log.info("✅ Created user in MongoDB: {}", newUser.getUsername());
            
            // 2. Tạo user trong Keycloak
            String keycloakUserId = createUserInKeycloak(request);
            log.info("✅ Created user in Keycloak with ID: {}", keycloakUserId);
            
            // 3. Return response
            UserResponseDto userDto = UserResponseDto.fromUser(newUser);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userDto);
            response.put("keycloak_user_id", keycloakUserId);
            response.put("message", "User created successfully in both MongoDB and Keycloak");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error creating user: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create user: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Cập nhật user trong cả MongoDB và Keycloak
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable String id, 
            @Valid @RequestBody UpdateUserRequest request) {
        try {
            log.info("🔄 Admin updating user: {}", id);
            
            // 1. Update user trong MongoDB
            UserId userId = new UserId(id);
            User updatedUser = userService.updateUserProfile(userId, request);
            log.info("✅ Updated user in MongoDB: {}", updatedUser.getUsername());
            
            // 2. Sync changes to Keycloak
            userSyncService.syncUserToKeycloak(updatedUser);
            log.info("✅ Synced user changes to Keycloak: {}", updatedUser.getUsername());
            
            // 3. Return response
            UserResponseDto userDto = UserResponseDto.fromUser(updatedUser);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userDto);
            response.put("message", "User updated successfully in both MongoDB and Keycloak");
            
            return ResponseEntity.ok(response);
            
        } catch (UserNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "User not found");
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            log.error("❌ Error updating user: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update user: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Xóa user khỏi cả MongoDB và Keycloak
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String id) {
        try {
            log.info("🔄 Admin deleting user: {}", id);
            
            // 1. Get user info trước khi xóa
            UserId userId = new UserId(id);
            User user = userService.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
            
            // 2. Tìm và xóa user trong Keycloak
            var keycloakUser = keycloakUserService.findUserByUsername(user.getUsername());
            if (keycloakUser != null) {
                // Log current roles before deletion for audit
                var currentRoles = keycloakUserService.getUserRoles(keycloakUser.getId());
                log.info("🔍 User {} has roles: {}", user.getUsername(), currentRoles);
                
                keycloakUserService.deleteUser(keycloakUser.getId());
                log.info("✅ Deleted user from Keycloak: {}", user.getUsername());
            } else {
                log.warn("⚠️ User not found in Keycloak: {}", user.getUsername());
            }
            
            // 3. Xóa user khỏi MongoDB
            userService.deleteUser(userId);
            log.info("✅ Deleted user from MongoDB: {}", user.getUsername());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User deleted successfully from both MongoDB and Keycloak");
            
            return ResponseEntity.ok(response);
            
        } catch (UserNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "User not found");
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            log.error("❌ Error deleting user: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get single user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String id) {
        try {
            UserId userId = new UserId(id);
            User user = userService.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
            
            UserResponseDto userDto = UserResponseDto.fromUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userDto);
            response.put("message", "User retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "User not found");
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get user: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Helper methods
    private User createUserInMongoDB(CreateUserRequest request) {
        return userService.createUserFromAdmin(
            request.username(),
            request.email(),
            request.fullName(),
            request.password(),
            request.userType(),
            request.phoneNumber(),
            request.address(),
            request.bio(),
            request.profilePictureUrl(),
            request.dateOfBirth(),
            request.gender(),
            request.location(),
            request.characterName(),
            request.animeMangaSource(),
            request.characterDescription(),
            request.avatarUrl(),
            request.coverImageUrl(),
            request.characterStatus()
        );
    }

    private String createUserInKeycloak(CreateUserRequest request) {
        // Ensure required roles exist in Keycloak
        keycloakUserService.ensureRolesExist();
        
        KeycloakUserCreateDto keycloakDto = KeycloakUserCreateDto.builder()
            .username(request.username())
            .email(request.email())
            .firstName(extractFirstName(request.fullName()))
            .lastName(extractLastName(request.fullName()))
            .build();
        
        // Add custom attributes
        keycloakDto.addAttribute("userType", request.userType().toString());
        keycloakDto.addAttribute("bio", request.bio());
        keycloakDto.addAttribute("location", request.location());
        
        if (request.userType().toString().equals("CHARACTER")) {
            keycloakDto.addAttribute("characterName", request.characterName());
            keycloakDto.addAttribute("animeMangaSource", request.animeMangaSource());
        }
        
        String keycloakUserId = keycloakUserService.createUser(keycloakDto);
        
        // Set password
        keycloakUserService.setUserPassword(keycloakUserId, request.password());
        
        // Assign appropriate roles based on user type
        assignUserRoles(keycloakUserId, request.userType());
        
        return keycloakUserId;
    }

    /**
     * Assign roles to user based on UserType
     */
    private void assignUserRoles(String keycloakUserId, com.sss.user.domain.enumeration.UserType userType) {
        try {
            switch (userType) {
                case ADMIN:
                    // Assign both ADMIN and ROLE_ADMIN for maximum compatibility
                    keycloakUserService.assignRealmRole(keycloakUserId, "ADMIN");
                    keycloakUserService.assignRealmRole(keycloakUserId, "ROLE_ADMIN");
                    log.info("✅ Assigned ADMIN roles to user: {}", keycloakUserId);
                    break;
                    
                case CHARACTER:
                    keycloakUserService.assignRealmRole(keycloakUserId, "CHARACTER");
                    log.info("✅ Assigned CHARACTER role to user: {}", keycloakUserId);
                    break;
                    
                case REAL_USER:
                default:
                    keycloakUserService.assignRealmRole(keycloakUserId, "USER");
                    log.info("✅ Assigned USER role to user: {}", keycloakUserId);
                    break;
            }
        } catch (Exception e) {
            log.error("❌ Failed to assign roles to user {}: {}", keycloakUserId, e.getMessage());
            // Don't throw exception as user is already created, just log the error
        }
    }

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
}
